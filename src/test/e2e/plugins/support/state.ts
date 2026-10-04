import { LiveGuiHandle, type ItemWrapper, type PlayerWrapper, type ServerWrapper } from '@plugwright/runner';
import { GUI, ITEM } from './expected.js';
import { numberIn, sleep, stripColors } from './text.js';

/**
 * Reading Guilds' state back out of the server.
 *
 * Everything here goes through a surface a player can actually see — a chat line or an open
 * inventory — rather than through plugin internals. That is what keeps the assertions honest: if
 * a GUI stops rendering the balance, `guildBank` stops finding it and the test fails, instead of
 * quietly reading a stale internal value and passing.
 */

/**
 * How many chat messages a bot may send at once, and how many per second it may keep sending.
 *
 * Paper's chat throttle is a budget rather than a rate limit, so the pacing here is modelled the
 * same way instead of being a fixed gap. `ServerGamePacketListenerImpl` builds
 * `chatSpamThrottler = new TickThrottler(20, 200)` and per chat message calls
 *
 *     count.addAndGet(20) < 200      // isIncrementAndUnderThreshold()
 *
 * while `TickThrottler.tick()` drains exactly one per tick, so twenty per second. Every message
 * costs twenty and the drain returns twenty per second, which means the budget is ten messages of
 * burst followed by a sustained one per second. The threshold is not configurable: `spam-limiter`
 * in paper-global.yml exposes only the recipe and tab limiters.
 *
 * A flat one-second gap would respect that but waste the burst allowance, stretching the suite from
 * six minutes to twenty for no safety gained. The leaky bucket below spends the allowance instead:
 * a bot that has been quiet may send `BURST` messages back to back, and every second of waiting
 * earns one more.
 *
 * At 200ms, which is five messages a second, this suite added a hundred per second against a drain
 * of twenty and was kicked thirteen times in a single run.
 */
/** Messages a bot may send back to back after being quiet, matching Paper's burst allowance. */
const BURST = 10;

/** Messages per second a bot may keep sending once the burst is spent. */
const PER_SECOND = 1;

const lastCommandAt = new WeakMap<PlayerWrapper, number>();

/** Unspent burst messages per bot. See {@link BURST}. */
const allowance = new WeakMap<PlayerWrapper, number>();

/** Why a bot's connection ended, once it has. Keyed per bot. */
const disconnectedBecause = new WeakMap<PlayerWrapper, string>();
const watched = new WeakSet<PlayerWrapper>();

/**
 * Records when a bot's connection ends, so a later command can say so instead of silently vanishing.
 *
 * mineflayer's `bot.end` is a *method*, not a flag, so there is no property to read — the events are
 * the only documented signal. Listening once per bot keeps the reason available for the message:
 * Paper's "Kicked for spamming" is worth surfacing verbatim, because it explains a whole cascade of
 * timeouts that otherwise look like unrelated assertion failures.
 */
function watchConnection(player: PlayerWrapper): void {
    if (watched.has(player)) return;
    watched.add(player);

    const bot = player.bot as unknown as {
        on(event: string, handler: (...args: any[]) => void): void;
    };
    bot.on('end', (reason: unknown) => disconnectedBecause.set(player, reasonText(reason)));
    bot.on('kicked', (reason: unknown) => disconnectedBecause.set(player, reasonText(reason)));
    bot.on('error', (reason: unknown) => disconnectedBecause.set(player, reasonText(reason)));
}

function reasonText(reason: unknown): string {
    if (reason == null) return 'the connection closed';
    if (reason instanceof Error) return reason.message;
    if (typeof reason === 'string') return reason;
    return JSON.stringify(reason);
}

/**
 * Sends one command, holding off just long enough that a burst of them cannot trip Paper's chat
 * throttle. Every helper in the fixture and every spec goes through here rather than
 * `player.chat`.
 *
 * A bot whose connection has already ended throws instead of being sent another command. mineflayer
 * accepts a `chat` on a closed socket without complaint and drops it, so without this guard a kicked
 * bot turns every subsequent wait into a timeout against a message that was never going to arrive —
 * which reads as a broken assertion rather than a broken connection.
 */
export async function run(player: PlayerWrapper, command: string): Promise<void> {
    watchConnection(player);

    const reason = disconnectedBecause.get(player);
    if (reason !== undefined) {
        throw new Error(
            `${player.username}'s connection had already ended (${reason}); ` +
                `"${command}" would never have been sent`,
        );
    }

    const now = Date.now();
    const previous = lastCommandAt.get(player);
    if (previous !== undefined) {
        // One token back for every second elapsed since the last send, capped at the burst.
        const earned = Math.floor(((now - previous) / 1000) * PER_SECOND);
        allowance.set(player, Math.min(BURST, (allowance.get(player) ?? BURST) + earned));
    } else {
        allowance.set(player, BURST);
    }

    while ((allowance.get(player) ?? 0) < 1) {
        await sleep(1000 / PER_SECOND);
        allowance.set(player, (allowance.get(player) ?? 0) + PER_SECOND);
    }

    allowance.set(player, (allowance.get(player) ?? 0) - 1);
    lastCommandAt.set(player, Date.now());
    player.chat(command);
}

/** Polls `read` until it returns something other than null or undefined. */
export async function waitFor<T>(
    read: () => T | Promise<T | undefined | null> | undefined | null,
    what: string,
    { timeout = 10_000, interval = 200 }: { timeout?: number; interval?: number } = {},
): Promise<T> {
    const deadline = Date.now() + timeout;
    let last: unknown;
    for (;;) {
        try {
            const value = await read();
            if (value !== undefined && value !== null) return value as T;
        } catch (error) {
            last = error;
        }
        if (Date.now() >= deadline) {
            throw new Error(
                `Timed out after ${timeout}ms waiting for ${what}` +
                (last instanceof Error ? `: ${last.message}` : ''),
            );
        }
        await new Promise(resolve => setTimeout(resolve, interval));
    }
}

/**
 * Waits for a window titled `title` without sending anything.
 *
 * For windows the plugin opens as a *consequence* of another window — clicking a vault entry in the
 * picker opens the vault — where there is no command left to run.
 */
export async function awaitGui(player: PlayerWrapper, title: string, timeout = 10_000): Promise<LiveGuiHandle> {
    await waitFor(
        () => {
            const current = player.bot.currentWindow;
            return current && windowTitle(current).includes(title) ? true : undefined;
        },
        `${player.username}'s "${title}" window to open`,
        { timeout },
    );
    return new LiveGuiHandle(player.bot as never, () => true);
}

/**
 * Re-opens a GUI until one of its items satisfies `predicate`.
 *
 * `openGui` only waits for the window; this waits for its *contents*, which matters when the contents
 * are what a permission change is supposed to alter. Re-running the command is the honest way to ask
 * the server again: the window it replaces is a fresh snapshot.
 */
export async function awaitGuiItem(
    player: PlayerWrapper,
    command: string,
    title: string,
    predicate: (item: ItemWrapper) => boolean,
    timeout = 15_000,
): Promise<LiveGuiHandle> {
    let handle: LiveGuiHandle | undefined;
    await waitFor(
        async () => {
            handle = await openGui(player, command, title);
            return openItems(player).some(predicate) ? true : undefined;
        },
        `${player.username}'s "${title}" window to hold a matching item`,
        { timeout },
    );
    return handle!;
}

/**
 * Closes whatever inventory the bot has open, if anything.
 */
export async function closeGui(player: PlayerWrapper): Promise<void> {
    const window = player.bot.currentWindow;
    if (!window) return;
    await player.bot.closeWindow(window as never);
}

/**
 * The text of a chat component, however the server encoded it, with colour codes removed.
 *
 * Written here rather than reusing Plugwright's `ItemWrapper.parseChat` because that one cannot
 * read a 1.21.8 inventory title. Every Guilds GUI title is built from a legacy string with two or
 * more colour codes, which Paper encodes as an NBT component whose `extra` list holds plain string
 * tags; `parseChat` re-wraps each of those in a compound before reading it and so recovers only the
 * leading fragment — `&8» &r{name}'s Info` parses as `» ` and no more. Titles are matched on
 * fragments rather than whole strings precisely because of this, and the matching itself lives
 * here so the built-in `player.gui({ title })` cannot silently fail to see a menu at all.
 */
function titleText(raw: unknown): string {
    if (raw == null) return '';
    if (typeof raw === 'string') {
        try {
            return titleText(JSON.parse(raw));
        } catch {
            return raw;
        }
    }
    if (Array.isArray(raw)) return raw.map(titleText).join('');
    if (typeof raw !== 'object') return '';

    const node = raw as Record<string, unknown>;

    // NBT shapes: { type: 'string', value } and { type: 'list', value: { type, value: [...] } }
    if (node.type === 'string') return String(node.value ?? '');
    if (node.type === 'compound') return titleParts(node.value);
    if (node.type === 'list') {
        const list = node.value as Record<string, unknown> | undefined;
        return list && 'value' in list ? titleParts(list.value) : titleText(list);
    }

    // A list element prismarine-nbt could not reduce arrives under an empty key
    // ({ "": { type: 'string', value: '…' } }). Without this, every part of a Guilds title after
    // the leading colour run is dropped and all four menus parse as the same bare '» '.
    if (Object.keys(node).length === 1 && '' in node) return titleText(node['']);

    return titleParts(node);
}

/** The `text` / `extra` / `with` children of one chat component, in reading order. */
function titleParts(parts: unknown): string {
    if (Array.isArray(parts)) return parts.map(titleText).join('');
    if (parts === null || typeof parts !== 'object') return '';

    const node = parts as Record<string, unknown>;
    let text = '';
    if (node.translate !== undefined) text += String(node.translate);
    if (node.text !== undefined) text += titleText(node.text);
    if (node.extra !== undefined) text += titleText(node.extra);
    if (Array.isArray(node.with)) text += node.with.map(titleText).join(' ');
    return text;
}

/** The open window's title as plain, colour-free text. */
function windowTitle(window: unknown): string {
    return stripColors(titleText((window as { title?: unknown } | null)?.title));
}

/**
 * Opens a Guilds GUI by running `command` and waiting for a window titled `title` to appear.
 *
 * The previous window is closed first, so re-opening the same menu — which a `describe.serial`
 * block does routinely — cannot be satisfied by the window that is already up. Closing is cheap and
 * immediate on the client, and the wait afterwards is for a genuinely new window, so the two never
 * race.
 */
export async function openGui(
    player: PlayerWrapper,
    command: string,
    title: string,
    timeout = 20_000,
): Promise<LiveGuiHandle> {
    await closeGui(player);
    // Paced like every other send. `openGui` is the most-called function in the GUI tests, and
    // `guildList` opens it once per `createGuild`, so sending here raw left a large fraction of
    // the suite's chat traffic outside the throttle guard.
    await run(player, command);
    await waitFor(
        () => {
            const current = player.bot.currentWindow;
            return current && windowTitle(current).includes(title) ? true : undefined;
        },
        `${player.username}'s "${title}" window to open for "${command}"`,
        { timeout },
    );

    /*
     * The handle's own title matcher is deliberately permissive. A `LiveGuiHandle` re-reads the
     * title through `ItemWrapper.parseChat` — the parser that cannot read a 1.21.8 Guilds title —
     * every time it takes a snapshot, so a matcher built on that reading would reject every menu
     * this module just accepted. `openGui` has already checked the title with the reader above;
     * from here the handle is a live view of whatever window the bot has open, which is what its
     * locators need.
     *
     * The wait defaults to 20s rather than 10s. This is a wait on a positive signal, so a
     * generous budget costs nothing when the server is healthy. The 10s it replaced was
     * tight enough that a loaded CI runner, still generating three worlds, missed the
     * window and failed the test.
     */
    return new LiveGuiHandle(player.bot as never, () => true);
}

/**
 * Every item currently in the bot's open inventory.
 *
 * `LiveGuiHandle.locator` resolves a *single* item — the right tool for "click this" or "assert
 * this exists" — but there is no way to ask a live handle to enumerate, and the list and members
 * GUIs only mean something read as a whole. `player.getCurrentGui()` is the snapshot the runner
 * itself builds its locators from, so the parsed names and lore are exactly what a locator would
 * have returned; it is marked internal, so this is the one place in the suite that reaches for it.
 */
export function openItems(player: PlayerWrapper): ItemWrapper[] {
    const gui = player.getCurrentGui();
    if (!gui) throw new Error(`${player.username} has no inventory open`);
    return gui.items;
}

/**
 * Items in the paginated content area of a Guilds GUI (slots 0-44).
 *
 * Row 6 carries the previous/next buttons at slots 46 and 50 plus the filler the plugin paints
 * across the bottom row, so anything at slot 45 or above is navigation rather than content.
 */
export function contentItems(player: PlayerWrapper): ItemWrapper[] {
    return openItems(player).filter(item => item.slot < 45);
}

/**
 * Splits a `Label: value` lore block into a map.
 *
 * Guilds' lore is a fixed set of `Label&8: &aValue` lines — `&8• &7Name: &a{name}` in the members
 * GUI, `&cName&8: &a{guild-name}` in the guild list — so the labels are the stable part and only
 * the values are ever asserted on. The decorative bullet the members GUI puts in front of its
 * labels is dropped so both GUIs key the same way.
 */
function fieldsOf(item: ItemWrapper): Map<string, string> {
    const fields = new Map<string, string>();
    for (const line of item.lore) {
        const text = stripColors(line);
        const separator = text.indexOf(':');
        if (separator <= 0) continue;
        const label = text.slice(0, separator).trim().replace(/^[-•·*>]\s*/, '');
        if (label) fields.set(label, text.slice(separator + 1).trim());
    }
    return fields;
}

/** One guild's row in the guild list GUI. */
export interface ListEntry {
    name: string;
    prefix: string;
    master: string;
    status: string;
    tier: number;
    balance: number;
    memberCount: number;
}

/**
 * Every guild on the server, as the guild list GUI renders it.
 *
 * `guis.guild-list.head-lore` supplies the labels; the guild names, tier and balance come from
 * Guilds' own state, which is what makes this usable as a read-back in other specs.
 */
export async function guildList(player: PlayerWrapper): Promise<ListEntry[]> {
    await openGui(player, '/guild list', GUI.list);

    try {
        return readListEntries(player);
    } finally {
        // A read leaves nothing to look at, and closing here keeps a stale menu from outliving the
        // bot that opened it. Callers that want to interact with the GUI use `openGui` directly.
        await closeGui(player);
    }
}

/** The guild list GUI's rows, read from the window the bot currently has open. */
function readListEntries(player: PlayerWrapper): ListEntry[] {
    const entries: ListEntry[] = [];
    for (const item of contentItems(player)) {
        const fields = fieldsOf(item);
        const name = fields.get('Name');
        if (name === undefined) continue;
        entries.push({
            name,
            prefix: fields.get('Prefix') ?? '',
            master: fields.get('Master') ?? '',
            status: fields.get('Status') ?? '',
            tier: numberIn(fields.get('Tier') ?? '') ?? 0,
            balance: numberIn(fields.get('Balance') ?? '') ?? 0,
            memberCount: numberIn(fields.get('Member Count') ?? '') ?? 0,
        });
    }
    return entries;
}

/** One member's row in the members GUI. */
export interface MemberEntry {
    name: string;
    role: string;
    status: string;
}

/** The guild's roster, as its members GUI renders it. Must be run by a member of that guild. */
export async function guildMembers(player: PlayerWrapper): Promise<MemberEntry[]> {
    await openGui(player, '/guild members', GUI.members);

    try {
        const members: MemberEntry[] = [];
        for (const item of contentItems(player)) {
            const fields = fieldsOf(item);
            const name = fields.get('Name');
            if (name === undefined) continue;
            members.push({ name, role: fields.get('Role') ?? '', status: fields.get('Status') ?? '' });
        }
        return members;
    } finally {
        await closeGui(player);
    }
}

/** A guild's bank balance, read back out of the admin report. */
export async function guildBank(server: ServerWrapper, guild: string): Promise<number> {
    const output = await server.execute(`guild admin bank balance ${guild}`);
    // The sentence ends in a full stop, so the amount is matched as digits with an optional
    // fraction rather than a run of "digits, dots and commas" that would swallow it.
    const match = stripColors(output).match(/bank balance of ([\d,]+(?:\.\d+)?)/);
    if (!match) {
        throw new Error(`Could not read the bank balance of "${guild}" from: ${stripColors(output).trim()}`);
    }
    return Number(match[1].replace(/,/g, ''));
}

/**
 * A player's own balance, read through EssentialsX's economy — the provider Vault resolves to.
 *
 * EssentialsX's `/eco` has no read subcommand (give/take/set/reset only), so this asks the
 * `/balance` command instead, which takes an optional offline player when the sender may look at
 * other people's money. The console is op, so it always can.
 */
export async function playerBalance(server: ServerWrapper, player: PlayerWrapper): Promise<number> {
    const output = await server.execute(`balance ${player.username}`);
    const match = stripColors(output).match(/\$([\d,.]+)/);
    if (!match) {
        throw new Error(`Could not read the balance of ${player.username} from: ${stripColors(output).trim()}`);
    }
    return Number(match[1].replace(/,/g, ''));
}

/**
 * The numbers on the lore line carrying `label`, in order.
 *
 * Guilds renders a home's position as one interpolated string, and how a fractional coordinate is
 * rendered depends on the location's own formatting. Comparing the numbers instead of the text means
 * the assertion survives a bot settling at 320.5 rather than exactly 320.
 */
export function loreNumbers(player: PlayerWrapper, label: string): number[] {
    for (const item of openItems(player)) {
        for (const line of item.lore) {
            const text = stripColors(line);
            if (!text.includes(label)) continue;
            return Array.from(text.matchAll(/-?[\d,]+(?:\.\d+)?/g), match =>
                Number(match[0].replace(/,/g, '')),
            );
        }
    }
    throw new Error(`no lore line containing "${label}" is on screen`);
}

/** True when an item whose display name contains `fragment` is on screen right now. */
export function hasItem(player: PlayerWrapper, fragment: string): boolean {
    return openItems(player).some(item => stripColors(item.displayName).includes(fragment));
}

/** True when some item on screen carries `fragment` on one of its lore lines. */
export function hasLoreFragment(player: PlayerWrapper, fragment: string): boolean {
    return openItems(player).some(item => item.lore.some(line => stripColors(line).includes(fragment)));
}

/**
 * The first window slot that belongs to the bot rather than to the GUI.
 *
 * prismarine-windows builds a container window as `[container slots][the player's own slots]` and
 * reports the boundary as `inventoryStart`. Guilds' vault is a fixed 54-slot chest, so the boundary
 * is 54 and the bot's own 36 slots occupy 54-89.
 */
export function playerInventoryStart(player: PlayerWrapper): number {
    const window = player.bot.currentWindow as { inventoryStart?: number } | null;
    if (!window || typeof window.inventoryStart !== 'number') {
        throw new Error(`${player.username} has no container window open`);
    }
    return window.inventoryStart;
}

/**
 * The window slot showing the named item in the bot's own inventory, for clicking it.
 *
 * The bot's inventory is *not* indexed the way a container window is laid out, so adding the
 * container's size to an inventory index gets it wrong: `bot.inventory.slots` follows the player
 * inventory window's own ordering — main inventory at 9-35, hotbar at 36-44, which is why a stack
 * handed out by `giveItem` lands at index 36 — while the container window puts its own slots first
 * and only says where its section begins. mineflayer reconciles the two with
 * `window.inventoryStart - bot.inventory.inventoryStart`, which is 45 for a 54-slot chest, and this
 * goes through the same offset rather than assuming one.
 */
export function ownItemWindowSlot(player: PlayerWrapper, itemName: string): number {
    const window = player.bot.currentWindow as { inventoryStart?: number; inventoryEnd?: number } | null;
    const own = player.bot.inventory as { inventoryStart?: number; slots: ({ name: string } | null)[] };
    if (!window || typeof window.inventoryStart !== 'number' || typeof window.inventoryEnd !== 'number') {
        throw new Error(`${player.username} has no container window open`);
    }

    const index = own.slots.findIndex(item => item?.name === itemName);
    if (index < 0) throw new Error(`${player.username} is not carrying any ${itemName}`);

    const slot = index + (window.inventoryStart - (own.inventoryStart ?? 0));
    if (slot < window.inventoryStart || slot >= window.inventoryEnd) {
        throw new Error(
            `${player.username}'s ${itemName} is at inventory slot ${index}, which this window does not show`,
        );
    }
    return slot;
}

/**
 * Teleports a bot and waits for it to land, returning where it came to rest.
 *
 * Guild homes are recorded wherever the player is standing when `/guild sethome` runs, so a test
 * that teleports into the air and immediately sets a home is really asserting about how quickly the
 * bot fell rather than about the teleport. The returned position is the honest one to assert
 * against: the home the plugin stored is the home the player was standing on.
 */
export async function teleportAndSettle(
    player: PlayerWrapper,
    x: number,
    y: number,
    z: number,
): Promise<{ x: number; y: number; z: number }> {
    await player.teleport(x, y, z);

    /*
     * Wait for the height to hold still for a full second, not for two equal readings.
     *
     * Two readings a quarter of a second apart agree while the bot is still at the height it was
     * teleported to, before gravity has had a chance to move it, so that check reported a position
     * the bot was about to leave. A second of stillness is longer than a tick and than the pause
     * between a teleport and the first fall, so it means the bot has actually landed.
     */
    // A tenth of a block over most of a second. Tight enough to mean the bot has stopped falling,
    // loose enough that standing in a current or on a slope still counts as settled: what the caller
    // needs is a position it can rely on for the home, and the assertions already allow a block.
    const HOLD_MS = 800;
    const EPSILON = 0.1;
    let previous = Number.NaN;
    let stillSince = Date.now();
    await waitFor(
        () => {
            const current = player.bot.entity.position.y;
            if (Math.abs(current - previous) >= EPSILON) {
                stillSince = Date.now();
            }
            previous = current;
            return Date.now() - stillSince >= HOLD_MS ? true : undefined;
        },
        `${player.username} to settle after being teleported to ${x}, ${y}, ${z}`,
        { timeout: 30_000, interval: 100 },
    );

    const position = player.bot.entity.position;
    return { x: position.x, y: position.y, z: position.z };
}


/** Items stored in the container the bot is looking at, not the ones it is carrying. */
export function containerItems(player: PlayerWrapper): ItemWrapper[] {
    const start = playerInventoryStart(player);
    return openItems(player).filter(item => item.slot < start);
}

/**
 * The item sitting on the bot's cursor, as `name` or `namexcount`.
 *
 * `heldItem` is the *hotbar* selection and is a different thing entirely; the cursor is the stack
 * a player is carrying between two slots, which is the middle step of every vault deposit.
 * prismarine-windows applies the click locally before the server answers, so this reflects the
 * click as soon as it is sent.
 */
export function cursorItem(player: PlayerWrapper): string | null {
    const item = (player.bot.currentWindow as { selectedItem?: { name: string; count: number } | null } | null)
        ?.selectedItem;
    return item ? `${item.name}x${item.count}` : null;
}

/** Clicks a window slot. `button` 0 is a left click, which is all Guilds' GUIs respond to. */
export async function clickSlot(player: PlayerWrapper, slot: number, button = 0): Promise<void> {
    await player.bot.clickWindow(slot, button, 0);
}

export { GUI, ITEM };

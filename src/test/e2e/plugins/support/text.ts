/**
 * Text helpers shared by the whole suite.
 *
 * Guilds writes its messages and GUI strings with `&` legacy colour codes. The client receives
 * them as `§x`, so a chat line never matches the literal from `languages/en-US.yml` verbatim.
 * Everything here exists to bridge that without hardcoding a single escape sequence into a test.
 */

/**
 * A legacy colour/formatting sequence as the client sees it.
 *
 * The valid codes are 0-9, a-f and k-o plus r-x, so `e` and `f` are colours like any other and the
 * class has to span the whole range. Leaving them out leaves the code itself sitting in the middle
 * of the text, which is how ACF's highlighted `@guilds` came out with a stray code in front of it
 * and failed to match its own message.
 *
 * Exported because three modules need to strip the same codes and they have to agree: a title
 * read that keeps a `\u00a7e` while a chat line from the same command drops it will not compare.
 */
export const SECTION_COLOUR = /\u00a7[0-9A-FK-ORX]/gi;

/** Drops every `§x` sequence, leaving the readable text. */
export function stripColors(text: string): string {
    return text.replace(SECTION_COLOUR, '');
}

/** Resolves after `ms`. */
export function sleep(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * Polls `predicate` until it returns something other than undefined, or `timeout` elapses.
 *
 * `waitUntil` from the runner throws on timeout; this returns undefined instead, which is what a
 * retry loop needs — a not-quite-there-yet is not a failure.
 */
export async function waitUntil(
    predicate: () => boolean | Promise<boolean | undefined> | undefined,
    { timeout = 5_000, interval = 100 }: { timeout?: number; interval?: number } = {},
): Promise<boolean | undefined> {
    const deadline = Date.now() + timeout;
    for (;;) {
        if (await predicate()) return true;
        if (Date.now() >= deadline) return undefined;
        await sleep(interval);
    }
}

/**
 * A pattern that matches `text` inside a chat line even where Guilds interleaved colour codes.
 *
 * `says("created successfully!")` matches `§a…created §fsuccessfully!§a`. Each character is escaped
 * on its own, so a literal `.` stays a literal rather than becoming a wildcard.
 */
export function says(text: string): RegExp {
    const body = Array.from(text)
        .map(character => character.replace(/[.*+?^${}()|[\]\\/]/g, '\\$&'))
        .join('(?:' + SECTION_COLOUR.source + ')*');
    return new RegExp(body);
}

/**
 * Any line a player received since `since`, as a plain array.
 *
 * The runner's `MessageBuffer` is not an array, so `slice` is the way to get one that `join`,
 * spread and the rest of the string API will accept.
 */
export function linesSince(buffer: { slice(start?: number, end?: number): string[] }, since = 0): string[] {
    return buffer.slice(since);
}

/** True when any line received since `since` matches `pattern`. */
export function received(buffer: { slice(start?: number, end?: number): string[] }, pattern: RegExp, since = 0): boolean {
    return linesSince(buffer, since).some(line => pattern.test(stripColors(line)));
}

/**
 * Formats like `me.glaremasters.guilds.utils.EconomyUtils`, whose DecimalFormat is
 * `###,###.##` with US symbols: thousands separated, at most two decimals, none when whole.
 *
 * This is the form Guilds puts in its *messages*. It is not a form any command accepts: ACF parses
 * `/guild bank deposit 9,000` as a non-numeric amount, so arguments use {@link amount}.
 */
export function money(value: number): string {
    const [whole, fraction = ''] = Math.abs(value).toFixed(2).split('.');
    const grouped = whole.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    const trimmed = fraction.replace(/0+$/, '');
    return `${value < 0 ? '-' : ''}${grouped}${trimmed ? `.${trimmed}` : ''}`;
}

/**
 * Reads a number out of a line that mentions one, e.g. a money amount or a member count.
 * Returns null when the line has none, which is the signal to keep polling.
 */
export function numberIn(line: string): number | null {
    const match = stripColors(line).match(/(-?[\d,]+(?:\.\d+)?)/);
    if (!match) return null;
    const value = Number(match[1].replace(/,/g, ''));
    return Number.isFinite(value) ? value : null;
}

/**
 * The form Guilds' commands accept for an amount: no thousands separator.
 *
 * ACF parses the argument as a `Double` before the handler sees it, and a comma in it is a syntax
 * error rather than a digit.
 */
export function amount(value: number): string {
    return String(Math.round(value * 100) / 100);
}

/**
 * Reads the remaining seconds out of a Guilds cooldown refusal.
 *
 * `home.cooldown`, `sethome.cooldown`, `accept.cooldown` and `request.cooldown` all report how
 * long is left as a number of seconds, so a test can wait exactly as long as the plugin says
 * instead of guessing. The three wordings differ, so all three are matched:
 *
 * - "You must wait at least 15 seconds before doing this again" (home, sethome)
 * - "You are currently on cooldown from joining a guild. Try again in 15 seconds." (accept)
 * - "You can't send another request for 15 seconds." (request)
 */
export function secondsRemaining(text: string): number | undefined {
    const match = stripColors(text).match(
        /must wait at least ([\d.]+) seconds|currently on cooldown[^.]*?([\d.]+)|another request for ([\d.]+) seconds/i,
    );
    if (!match) return undefined;
    const value = Number(match[1] ?? match[2] ?? match[3]);
    return Number.isFinite(value) ? value : undefined;
}

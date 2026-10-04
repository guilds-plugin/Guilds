import { expect, type PlayerWrapper, type TestContext } from '@plugwright/runner';
import { MSG } from './expected.js';
import { guildBank, guildList, playerBalance, run, waitFor } from './state.js';
import { amount, money, received, says, stripColors, waitUntil } from './text.js';

// The pacing helper lives in state.ts so openGui can use it without a cycle. Re-exported here
// because that is where every spec reaches for it.
export { run };



/**
 * The `guilds` fixture: every setup step more than one spec needs.
 *
 * Two rules shape the design.
 *
 * **Deterministic starting state.** Guilds stores everything by UUID and the runner supplies the
 * bots' identities, so nothing here can assume a fresh server. Guild names and additional bot
 * names come from a per-process counter, and every guild a test creates is destroyed afterwards,
 * so no test can inherit another one's guild, balance or tier.
 *
 * **Player-visible outcomes.** Each helper drives real commands and waits for the message or GUI
 * entry that proves the change landed, instead of firing a command and hoping.
 */

/** Guild creation is charged to the player's own balance (cost.creation in the staged config). */
export const CREATION_COST = 100;

/** The balance every funded player in the suite starts from, so "before" values are known. */
export const STARTING_BALANCE = 5000;

/** Guilds charges this out of the bank for `/guild sethome` (cost.sethome in the staged config). */
export const SETHOME_COST = 25;

/** tiers.yml tier 2 costs this out of the bank to upgrade into. */
export const TIER_TWO_COST = 200;

/** Roles, by name, from the shipped roles.yml. */
export const ROLE = {
    master: 'GuildMaster',
    officer: 'Officer',
    veteran: 'Veteran',
    member: 'Member',
} as const;

let sequence = 0;

/** A short, alphanumeric token unique for the lifetime of this test run. */
function unique(): string {
    sequence += 1;
    return sequence.toString(36).padStart(4, '0');
}

/** A guild name no other test in this run can have claimed. Fits the 20-character prefix limit. */
export function guildName(label = 'guild'): string {
    return `E2E${unique()}${label}`.slice(0, 20);
}

/** A bot name that cannot collide with the runner's own, or with another test's. */
export function playerName(): string {
    return `gw${unique()}`.slice(0, 16);
}

export interface CreateGuildOptions {
    name?: string;
    prefix?: string;
    /** Balance to give the player before creating; defaults to STARTING_BALANCE. */
    balance?: number;
    /** Skip funding entirely, for the "cannot afford a guild" case. */
    unfunded?: boolean;
}

export interface GuildsFixtures {
    /** A guild name unique to this run. */
    guildName(label?: string): string;
    /** A bot name unique to this run, for `ctx.createPlayer({ username })`. */
    playerName(): string;
    /** Sends a command without tripping Paper's chat throttle. */
    run(player: PlayerWrapper, command: string): Promise<void>;

    /** Gives a player an exact balance through the EssentialsX economy Vault wraps. */
    fund(player: PlayerWrapper, amount: number): Promise<void>;
    /** Reads a player's balance back. */
    balanceOf(player: PlayerWrapper): Promise<number>;
    /**
     * Sets a guild's bank to an exact balance, using the owning player's own bank commands.
     */
    setBank(guild: string, amount: number, actor?: PlayerWrapper): Promise<void>;
    /** Reads a guild's bank balance back. */
    bankOf(guild: string): Promise<number>;

    /** Answers the pending `/guild` prompt with `/guild confirm`. */
    confirm(player: PlayerWrapper): Promise<void>;
    /** Answers the pending `/guild` prompt with `/guild cancel`. */
    cancel(player: PlayerWrapper): Promise<void>;

    /** Creates a guild, answering its confirmation prompt, and returns the guild's name. */
    createGuild(player: PlayerWrapper, options?: CreateGuildOptions): Promise<string>;

    /** Invites `target`, without asserting the outcome, for tests that go their own way. */
    invite(master: PlayerWrapper, target: PlayerWrapper, guild: string): Promise<void>;
    /** `/guild accept <guild>` from the invited player. */
    accept(player: PlayerWrapper, guild: string): Promise<void>;
    /** `/guild decline <guild>` from the invited player. */
    decline(player: PlayerWrapper, guild: string): Promise<void>;
    /** Invites `target` and has them accept, asserting both sides. */
    addMember(master: PlayerWrapper, target: PlayerWrapper, guild: string): Promise<void>;

    /** Promotes or demotes `target` one role step, and asserts the guild master saw it. */
    shiftRole(master: PlayerWrapper, target: PlayerWrapper, guild: string, direction: 'promote' | 'demote'): Promise<void>;

    /** Removes a guild outright, so the next test cannot inherit it. */
    destroyGuild(player: PlayerWrapper, guild: string): Promise<void>;
    /** Registers an already-created guild for teardown when the test ends. */
    track(guild: string): void;
    /**
     * Suppresses this test's automatic teardown, for a `describe.serial` block whose later steps
     * depend on the guild its first step created. The block is then responsible for deleting it.
     */
    keep(): void;

    /** Grants or denies a Bukkit permission node for a player through LuckPerms. */
    grant(player: PlayerWrapper, node: string): Promise<void>;
    revoke(player: PlayerWrapper, node: string): Promise<void>;
    /** Runs a console command and returns its colour-stripped output. */
    console(command: string): Promise<string>;

    /**
     * Re-issues `probe` until `effect` arrives.
     *
     * How the fixtures wait for a per-user LuckPerms change to reach the client. LuckPerms applies
     * node updates to an online player off the main thread, so the acknowledgement of the console
     * command that made the change is not the moment the player sees it — and LuckPerms' own reply
     * is unreadable here, because it answers on the sender's channel and the only sender available
     * is an RCON console nobody can read.
     *
     * `probe` must be harmless in whichever direction is *not* being waited for, since it is sent
     * repeatedly. `/guild bank withdraw 0` is the usual choice: it moves no money, but it is gated
     * by the same node as any real withdrawal.
     */
    awaitEffect(player: PlayerWrapper, probe: string, effect: string | RegExp): Promise<void>;
}

/**
 * Builds the fixture for one test.
 *
 * Guilds created through `createGuild`/`track` are torn down by a finalizer the runner runs after
 * the test body — including on failure — so a test that dies halfway leaves nothing behind.
 */
export function guildsFixtures(ctx: TestContext): GuildsFixtures {
    const tracked: string[] = [];
    let cleanedUp = false;
    let keep = false;

    /** `admin remove` is player-only and confirmation-gated, so deletion goes through a bot. */
    const remove = async (player: PlayerWrapper, guild: string): Promise<void> => {
        await fixtures.grant(player, 'guilds.command.admin');
        // `admin motd <guild>` reads and changes nothing, and is gated by the node just granted, so
        // it is a safe place to wait for LuckPerms to reach the client.
        await fixtures.awaitEffect(player, `/guild admin motd ${guild}`, MSG.motdNotSet);

        await run(player, `/guild admin remove ${guild}`);
        await expect(player).toHaveReceivedMessage(
            `You are about to delete the guild ${guild}. Type /guilds confirm to remove this guild`,
            { timeout: 10_000 },
        );
        await run(player, '/guild confirm');
        await expect(player).toHaveReceivedMessage(`You have successfully removed ${guild}.`, { timeout: 10_000 });
        await fixtures.revoke(player, 'guilds.command.admin');
    };

    const fixtures: GuildsFixtures = {
        guildName,
        playerName,
        run,

        async fund(player, wanted) {
            await ctx.server.execute(`eco set ${player.username} ${amount(wanted)}`);
            // EssentialsX applies the write asynchronously relative to the console reply, so the
            // balance is read back rather than assumed.
            await waitFor(
                async () => ((await playerBalance(ctx.server, player)) === wanted ? true : undefined),
                `${player.username}'s balance to be ${wanted}`,
            );
        },

        async balanceOf(player) {
            return playerBalance(ctx.server, player);
        },

        /**
         * Sets a guild's bank to an exact balance.
         *
         * Deliberately routed through the owning player's own `/guild bank deposit` and
         * `/guild bank withdraw` rather than the console's `/guild admin bank …`: every admin
         * command that takes a guild *and* a second argument is currently refused by ACF's
         * `@Values` check, so the admin route cannot reach the handler at all. See
         * docs/e2e-testing.md. A deposit is funded first, since it comes out of the player's own
         * money; a withdrawal pays into it, which does not matter to the assertions.
         */
        async setBank(guild, target, actor: PlayerWrapper = ctx.player) {
            for (let attempt = 0; attempt < 6; attempt++) {
                const current = await guildBank(ctx.server, guild);
                const delta = Math.round((target - current) * 100) / 100;
                if (delta === 0) return;

                if (delta > 0) {
                    // A deposit comes out of the actor's own money, so top the balance up rather
                    // than resetting it — a later assertion may care what they had.
                    const held = await fixtures.balanceOf(actor);
                    if (held < delta) await fixtures.fund(actor, held + Math.ceil(delta));
                    const since = actor.getMessageBufferIndex();
                    await run(actor, `/guild bank deposit ${amount(delta)}`);
                    await expect(actor).toHaveReceivedMessage(MSG.depositSuccess(actor.username, money(delta)), {
                        since,
                        timeout: 10_000,
                    });
                } else {
                    const since = actor.getMessageBufferIndex();
                    await run(actor, `/guild bank withdraw ${amount(-delta)}`);
                    await expect(actor).toHaveReceivedMessage(
                        MSG.withdrawSuccess(actor.username, money(-delta)),
                        { since, timeout: 10_000 },
                    );
                }
            }

            const settled = await guildBank(ctx.server, guild);
            if (Math.abs(settled - target) > 0.005) {
                throw new Error(`Could not set "${guild}"'s bank to ${target}; it is ${settled}`);
            }
        },

        async bankOf(guild) {
            return guildBank(ctx.server, guild);
        },

        async confirm(player) {
            await run(player, '/guild confirm');
            await expect(player).toHaveReceivedMessage(MSG.confirmSuccess);
        },

        async cancel(player) {
            await run(player, '/guild cancel');
            await expect(player).toHaveReceivedMessage(MSG.cancelSuccess);
        },

        async createGuild(player, options = {}) {
            const name = options.name ?? guildName();
            const prefix = options.prefix ?? name.slice(0, 8);

            if (!options.unfunded) await fixtures.fund(player, options.balance ?? STARTING_BALANCE);

            /*
             * A bot can arrive already in a guild. The runner gives each bot a deterministic
             * offline UUID, so a name reused by a later run maps onto the same account — and the
             * plugin's own storage, in the server directory, survives between runs. Rather than
             * require a wiped server, leave whatever guild is there first.
             *
             * `/guild leave` answers one of two ways, and waiting for whichever comes first keeps
             * this free in the common case: a bot that is already guildless gets `error.no-guild`
             * straight back, and only a bot that really is in a guild has to wait out the
             * confirmation prompt.
             */
            const leaving = player.getMessageBufferIndex();
            await run(player, '/guild leave');
            await waitUntil(
                () =>
                    received(player.messageBuffer, says(MSG.leaveWarning), leaving) ||
                    received(player.messageBuffer, says(MSG.noGuild), leaving)
                        ? true
                        : undefined,
                { timeout: 10_000, interval: 25 },
            );
            if (received(player.messageBuffer, says(MSG.leaveWarning), leaving)) {
                await fixtures.confirm(player);
                await expect(player).toHaveReceivedMessage(MSG.leaveSuccess, { timeout: 10_000 });
            }

            await run(player, prefix ? `/guild create ${name} ${prefix}` : `/guild create ${name}`);
            await expect(player).toHaveReceivedMessage(MSG.createWarning);
            await fixtures.confirm(player);
            await expect(player).toHaveReceivedMessage(MSG.created(name));

            await waitFor(
                async () => ((await guildList(player)).some(entry => entry.name === name) ? true : undefined),
                `"${name}" to appear in the guild list`,
            );

            tracked.push(name);
            return name;
        },

        async invite(master, target, guild) {
            await run(master, `/guild invite ${target.username}`);
            await expect(target).toHaveReceivedMessage(`'${guild}'`);
        },

        async accept(player, guild) {
            await run(player, `/guild accept ${guild}`);
            await expect(player).toHaveReceivedMessage(MSG.acceptSuccess(guild));
        },

        async decline(player, guild) {
            await run(player, `/guild decline ${guild}`);
            await expect(player).toHaveReceivedMessage(MSG.declineSuccess);
        },

        async addMember(master, target, guild) {
            await fixtures.invite(master, target, guild);
            await fixtures.accept(target, guild);
        },

        async shiftRole(master, target, guild, direction) {
            await run(master, `/guild ${direction} ${target.username}`);
            const fragment = direction === 'promote' ? MSG.promoteSuccess : MSG.demoteSuccess;
            await expect(master).toHaveReceivedMessage(fragment(target.username));
            // The affected player is told too, and is offline-tolerant in Guilds; both bots are
            // online here, so the notification is asserted rather than skipped.
            await expect(target).toHaveReceivedMessage(
                direction === 'promote' ? MSG.promotedNotice(master.username) : MSG.demotedNotice(),
            );
            void guild;
        },

        async destroyGuild(player, guild) {
            await remove(player, guild);
            const at = tracked.indexOf(guild);
            if (at >= 0) tracked.splice(at, 1);
        },

        track(guild) {
            tracked.push(guild);
        },

        keep() {
            keep = true;
        },

        async grant(player, node) {
            await setNode(player, node, true);
        },

        async revoke(player, node) {
            await setNode(player, node, false);
        },

        async console(command) {
            return ctx.server.execute(command);
        },

        async awaitEffect(player, probe, effect) {
            const pattern = typeof effect === 'string' ? says(effect) : effect;
            // The outer interval is the gap between whole probe-and-wait cycles. Leaving it at zero
            // would chain them back to back, and this is the one place the suite re-sends a command
            // in a loop — the burst most likely to trip Paper's chat throttle.
            const PROBE_GAP_MS = 400;
            await waitFor(
                async () => {
                    const since = player.getMessageBufferIndex();
                    await run(player, probe);
                    return waitUntil(
                        async () => {
                            const lines = player.messageBuffer.slice(since).map(stripColors);
                            return lines.some(line => pattern.test(line)) ? true : undefined;
                        },
                        { timeout: 900, interval: 50 },
                    );
                },
                `"${probe}" to answer with ${String(effect)}`,
                { timeout: 20_000, interval: PROBE_GAP_MS },
            );
        },
    };

    /**
     * Sets a per-user LuckPerms override and waits for it to resolve.
     *
     * LuckPerms recalculates an online player's permissions on its own schedule, so the command's
     * acknowledgement is not the moment the node takes effect. A command sent straight afterwards
     * would still see the old value, and the test would fail for the wrong reason.
     */
    const setNode = async (player: PlayerWrapper, node: string, value: boolean): Promise<void> => {
        await luckPerms(`user ${player.username} permission set ${node} ${value}`);
    };

    /**
     * Runs a LuckPerms command, waiting out its own command queue.
     *
     * LuckPerms processes one command at a time and answers a second one with "Another command is
     * being executed, waiting for it to finish…" instead of doing anything. Two permission commands
     * in quick succession therefore silently drop the second, which would leave a grant looking
     * like it never happened. Re-issuing until the queue answers is the fix, and it costs nothing
     * in the common case where the first attempt goes through.
     */
    const luckPerms = async (command: string, attempts = 8): Promise<string> => {
        let output = '';
        for (let attempt = 0; attempt < attempts; attempt++) {
            output = await ctx.server.execute(`lp ${command}`);
            if (!stripColors(output).includes('Another command is being executed')) return output;
            await new Promise(resolve => setTimeout(resolve, 150));
        }
        return output;
    };

    ctx.cleanup(async () => {
        if (cleanedUp) return;
        cleanedUp = true;
        if (keep || tracked.length === 0) return;

        // A test may already have deleted its own guilds. Asking the guild list which of ours
        // still exist keeps teardown from waiting out a confirmation timeout per missing guild.
        let surviving: Set<string>;
        try {
            surviving = new Set((await guildList(ctx.player)).map(guild => guild.name));
        } catch {
            surviving = new Set(tracked);
        }

        // The test's bot is still connected while finalizers run, so the player-only delete works.
        const problems: string[] = [];
        for (const guild of tracked.splice(0)) {
            if (!surviving.has(guild)) continue;
            try {
                await remove(ctx.player, guild);
            } catch (error) {
                problems.push(`${guild}: ${error instanceof Error ? error.message : String(error)}`);
            }
        }

        /*
         * A teardown failure fails the test.
         *
         * This used to be swallowed, and that turned out to matter more than it looks. A bot
         * disconnected by Paper mid-teardown made `remove` throw, the throw was caught, the guild
         * leaked, and the run still reported every test as passed: thirteen kicks in one run, all
         * invisible. A leaked guild then changes what later tests see, so the suite quietly loses
         * the isolation it is built on. If teardown cannot finish, the test did not really pass.
         */
        if (problems.length > 0) {
            throw new Error(
                `${problems.length} guild(s) could not be removed in teardown, so this test's state ` +
                    `leaked into later tests:\n  ${problems.join('\n  ')}`,
            );
        }
    });

    return fixtures;
}


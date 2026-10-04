import { expect, test } from '@plugwright/runner';
import { commandWithout, expectEither, expectReceived } from '../plugins/support/assertions.js';
import { SETHOME_COST } from '../plugins/support/fixtures.js';
import { MSG } from '../plugins/support/expected.js';
import { secondsRemaining, sleep, stripColors } from '../plugins/support/text.js';
import { teleportAndSettle } from '../plugins/support/state.js';

/**
 * Cooldowns and argument validation.
 *
 * Guilds keeps three cooldowns in play that a player can observe, and the staged
 * `src/test/e2e/server/plugins/Guilds/config.yml` shortens all of them well below the shipped
 * defaults so a suite can watch one start, block the next attempt, and then expire. Every wait
 * here is for the number the plugin prints rather than a guessed constant, because a fixed sleep
 * would either be flaky or needlessly slow.
 *
 * The validation half of the file covers what ACF and Guilds do with bad input: a missing argument,
 * a value of the wrong shape, a target the server does not know, and a command whose subcommand is
 * incomplete.
 */

/** Somewhere flat and clear of spawn, for the sethome steps. */
const HOME = { x: 260, y: 90, z: -200 };

/**
 * Waits for a cooldown to run out, using the seconds Guilds reported in its refusal.
 *
 * The refusal prints whole seconds while the real expiry is fractional, so the printed number can
 * be up to a second short. The margin covers that, and re-issuing the command while the cooldown is
 * live would only start it again.
 */
async function waitOutCooldown(player: any, since: number, what: string): Promise<void> {
    const seen = player.messageBuffer.slice(since).map(stripColors).join(' | ');
    const wait = secondsRemaining(seen);
    // A refusal that does not say how long is left would turn the wait below into a guess, so the
    // number has to be there before it is used.
    if (wait === undefined || !(wait > 0)) {
        throw new Error(`the ${what} refusal should say how long is left, but the bot received: ${seen}`);
    }
    await sleep((wait + 1.5) * 1000);
}

test('a second home cannot be set until the cooldown expires', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);
    await guilds.setBank(guild, SETHOME_COST * 2);

    await player.teleport(HOME.x, HOME.y, HOME.z);
    await guilds.run(player, '/guild sethome');
    await expect(player).toHaveReceivedMessage(MSG.sethomeSuccess);

    // timers.cooldowns.sethome is short in the staged config, and has to outlast command latency.
    const blocked = player.getMessageBufferIndex();
    await guilds.run(player, '/guild sethome');
    // Whichever of the two answers arrives first, and a refusal is the one that proves the cooldown
    // did its job. Waiting for a specific reply inside a short fixed window fails on a loaded runner
    // for no reason other than the server being slow.
    const outcome = await expectEither(player, blocked, MSG.sethomeCooldown, MSG.sethomeSuccess);
    expect(outcome).toBe('expected');

    await waitOutCooldown(player, blocked, 'sethome');

    const retried = player.getMessageBufferIndex();
    await guilds.run(player, '/guild sethome');
    await expectReceived(player, MSG.sethomeSuccess, retried);
    // The second one really was charged, which is what proves it was not the first that answered.
    expect(await guilds.bankOf(guild)).toBe(0);
});

test('a second teleport home cannot happen until the cooldown expires', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);
    await guilds.setBank(guild, SETHOME_COST);

    const home = await teleportAndSettle(player, HOME.x, HOME.y, HOME.z);
    await guilds.run(player, '/guild sethome');
    await expect(player).toHaveReceivedMessage(MSG.sethomeSuccess);

    await guilds.run(player, '/guild home');
    await expect(player).toHaveReceivedMessage(MSG.homeTeleported);
    await expect(player).toBeNear(home.x, home.y, home.z, { tolerance: 1.5 });

    const elsewhere = { x: -260, y: 90, z: 200 };
    await player.teleport(elsewhere.x, elsewhere.y, elsewhere.z);

    const blocked = player.getMessageBufferIndex();
    await guilds.run(player, '/guild home');
    const outcome = await expectEither(player, blocked, MSG.homeCooldown, MSG.homeTeleported);
    expect(outcome).toBe('expected');

    // A refused teleport must not have moved anyone.
    expect(Math.abs(player.bot.entity.position.x - elsewhere.x)).toBeLessThan(5);

    await waitOutCooldown(player, blocked, 'home');

    const retried = player.getMessageBufferIndex();
    await guilds.run(player, '/guild home');
    await expectReceived(player, MSG.homeTeleported, retried);
    await expect(player).toBeNear(home.x, home.y, home.z, { tolerance: 1.5 });
});

test('a second join request cannot be sent until the cooldown expires', async ({ createPlayer, guilds }) => {
    const master = await createPlayer({ username: guilds.playerName() });
    const guild = await guilds.createGuild(master);

    // `/guild request <guild>` asks a guild to invite the sender, and Guilds' own `NoGuild`
    // condition requires the sender to be guildless — so this bot never creates one.
    const asker = await createPlayer({ username: guilds.playerName() });

    const first = asker.getMessageBufferIndex();
    await guilds.run(asker, `/guild request ${guild}`);
    await expectReceived(asker, MSG.requestSuccess(guild), first);

    // timers.cooldowns.request is short in the staged config, and has to outlast command latency.
    const blocked = asker.getMessageBufferIndex();
    await guilds.run(asker, `/guild request ${guild}`);
    const outcome = await expectEither(asker, blocked, MSG.requestCooldown, MSG.requestSuccess(guild));
    expect(outcome).toBe('expected');

    await waitOutCooldown(asker, blocked, 'request');

    const retried = asker.getMessageBufferIndex();
    await guilds.run(asker, `/guild request ${guild}`);
    await expectReceived(asker, MSG.requestSuccess(guild), retried);

    // The request reached the guild's master, which is the point of it.
    await expect(master).toHaveReceivedMessage(MSG.requestIncoming(asker.username));
});

test('a cooldown is keyed by player, not by guild', async ({ player, createPlayer, guilds }) => {
    const mine = await guilds.createGuild(player);
    await guilds.setBank(mine, SETHOME_COST);

    const home = await teleportAndSettle(player, HOME.x, HOME.y, HOME.z);
    await guilds.run(player, '/guild sethome');
    await expect(player).toHaveReceivedMessage(MSG.sethomeSuccess);

    const blocked = player.getMessageBufferIndex();
    await guilds.run(player, '/guild sethome');
    expect(await expectEither(player, blocked, MSG.sethomeCooldown, MSG.sethomeSuccess)).toBe('expected');

    // The guild's own home is untouched by the refusal: going there still works, and lands where it
    // was put. (The `home` cooldown is separate and is not running, since no teleport has happened.)
    await player.teleport(-260, 90, 200);
    const going = player.getMessageBufferIndex();
    await guilds.run(player, '/guild home');
    await expectReceived(player, MSG.homeTeleported, going);
    await expect(player).toBeNear(home.x, home.y, home.z, { tolerance: 1.5 });

    // A different bot's guild is entirely unaffected by the first one's cooldown.
    const other = await createPlayer({ username: guilds.playerName() });
    const theirs = await guilds.createGuild(other);
    await guilds.setBank(theirs, SETHOME_COST, other);

    await other.teleport(-260, 90, -200);
    const otherSetting = other.getMessageBufferIndex();
    await guilds.run(other, '/guild sethome');
    await expectReceived(other, MSG.sethomeSuccess, otherSetting);
    await expect(other).not.toHaveReceivedMessage(MSG.sethomeCooldown);
});

test('a missing argument is reported rather than acted on', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);

    // `/guild invite` names a player, `/guild bank deposit` an amount. Both answer with the usage
    // line ACF builds from the command's `@Syntax`, and neither acts.
    await commandWithout(player, guilds, '/guild invite', MSG.invalidSyntax, MSG.inviteSent('someone'));
    await commandWithout(
        player,
        guilds,
        '/guild bank deposit',
        MSG.invalidSyntax,
        MSG.depositSuccess(player.username, '10'),
    );

    expect(await guilds.bankOf(guild)).toBe(0);
});

test('a player the server does not know cannot be named as a target', async ({ player, guilds }) => {
    await guilds.createGuild(player);

    // ACF's completion runs before the handler: the `@players` list only holds names the server
    // has actually seen, so an invented one is rejected as not being one of the options and the
    // handler never runs. That is why this reports the completion rather than the plugin's own
    // `error.player-no-exist`.
    await commandWithout(
        player,
        guilds,
        '/guild invite NoSuchPlayer',
        MSG.valueRejected('players'),
        MSG.inviteSent('NoSuchPlayer'),
    );
    await commandWithout(
        player,
        guilds,
        '/guild kick NoSuchPlayer',
        MSG.valueRejected('members'),
        MSG.kickSuccess('NoSuchPlayer'),
    );
});

test('a guild that does not exist is refused by name', async ({ guilds }) => {
    // Guilds has no command whose *guild* argument can name a guild that is not the sender's own:
    // the member commands take the sender's own guild, and the admin commands are
    // `@Values("@guilds")`, so an invented name is rejected by ACF's completion rather than by the
    // plugin's own `error.guild-no-exist`. That is why `MSG.guildNoExist` has no test that reaches
    // its handler — the completion is always the thing that answers first.
    const output = await guilds.console('guild admin bank balance NoSuchGuild');
    expect(stripColors(output)).toContain(MSG.valueRejected('guilds'));
    expect(stripColors(output)).not.toContain('bank balance of');
});

test('a command with only a parent subcommand lists its children', async ({ player, guilds }) => {
    await guilds.createGuild(player);

    // `/guild bank` on its own is not a command: ACF answers with the subcommands it does have,
    // which is a more useful answer than a usage line would be.
    const since = player.getMessageBufferIndex();
    await guilds.run(player, '/guild bank');
    await expectReceived(player, 'Search results for /guild bank', since);
    await expect(player).not.toHaveReceivedMessage(MSG.bankBalance, { since });
});

test('confirm and cancel with nothing pending are refused', async ({ player, guilds }) => {
    await guilds.createGuild(player);

    // The guild is already created, so there is no prompt left to answer.
    await commandWithout(player, guilds, '/guild confirm', MSG.confirmError, MSG.confirmSuccess);
    await commandWithout(player, guilds, '/guild cancel', MSG.cancelError, MSG.cancelSuccess);
});

test('a guild-only command refuses a player in no guild', async ({ createPlayer, guilds }) => {
    const stranger = await createPlayer({ username: guilds.playerName() });

    await commandWithout(stranger, guilds, '/guild info', MSG.noGuild, 'Info');
    await commandWithout(stranger, guilds, '/guild members', MSG.noGuild, 'Members of');
    await commandWithout(stranger, guilds, '/guild bank balance', MSG.noGuild, MSG.bankBalance);
    await commandWithout(stranger, guilds, '/guild bank deposit 10', MSG.noGuild, MSG.depositSuccess(stranger.username, '10'));
});

test('a negative amount is refused', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player, { balance: 1000 });

    // A negative amount is a number, so it is the handler rather than ACF that has to turn it
    // away. Which message it uses is not pinned here — the point is that no money moves.
    const before = await guilds.bankOf(guild);
    const held = await guilds.balanceOf(player);

    const since = player.getMessageBufferIndex();
    await guilds.run(player, '/guild bank withdraw -50');
    await expect.poll(() => player.messageBuffer.slice(since).some(line => stripColors(line).length > 0), {
        timeout: 10_000,
        message: 'the command should be answered somehow',
    }).toBe(true);

    expect(await guilds.bankOf(guild)).toBe(before);
    expect(await guilds.balanceOf(player)).toBe(held);
});
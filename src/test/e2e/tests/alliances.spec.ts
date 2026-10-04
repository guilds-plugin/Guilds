import { expect, test } from '@plugwright/runner';
import { commandWithout, expectReceived } from '../plugins/support/assertions.js';
import { MSG } from '../plugins/support/expected.js';
import { guildList } from '../plugins/support/state.js';

/**
 * Alliances between two independent guilds: requesting, accepting, declining, listing, removing,
 * and the refusals around each.
 *
 * Every test builds its own pair of guilds with its own bots, so none of them depends on another's
 * alliances.
 *
 * Cross-player assertions carry no cursor: each bot's message buffer is its own, the guild names
 * are unique to the run, and a fresh bot starts with an empty buffer — so anything that matches can
 * only have come from the commands this test sent.
 */

/** Two guilds, each with its own master. */
async function pair(ctx: { player: any; createPlayer: any; guilds: any }) {
    const otherMaster = await ctx.createPlayer({ username: ctx.guilds.playerName() });
    const mine = await ctx.guilds.createGuild(ctx.player);
    const theirs = await ctx.guilds.createGuild(otherMaster);
    return { otherMaster, mine, theirs };
}

/**
 * Sends `ally add` and waits for both sides to acknowledge it.
 *
 * Both waits are scoped to a cursor taken before the send. Several tests build an alliance, tear it
 * down and build it again between the *same two guilds*, and these three messages repeat verbatim
 * each time round, so an unscoped wait matched the previous round's reply and returned before the
 * server had done anything. That is not a flake: it let the next command run against state the
 * plugin had not reached yet.
 */
async function requestAlly(guilds: any, asker: any, mine: string, otherMaster: any, theirs: string) {
    // One cursor per buffer. `getMessageBufferIndex` counts lines in a single player's own buffer, so
    // a cursor taken from the asker means nothing to the bot being asked, and using it there skipped
    // past the very line the wait was looking for.
    const asked = asker.getMessageBufferIndex();
    const heard = otherMaster.getMessageBufferIndex();
    await guilds.run(asker, `/guild ally add ${theirs}`);
    await expectReceived(asker, MSG.allyInviteSent(theirs), asked);
    await expectReceived(otherMaster, MSG.allyIncoming(mine), heard);
}

/** Sends `ally accept` and waits for both sides to acknowledge it. */
async function acceptAlly(guilds: any, otherMaster: any, mine: string, asker: any, theirs: string) {
    // Per buffer again, for the same reason as in `requestAlly`.
    const accepted = otherMaster.getMessageBufferIndex();
    const told = asker.getMessageBufferIndex();
    await guilds.run(otherMaster, `/guild ally accept ${mine}`);
    await expectReceived(otherMaster, MSG.allyAccepted(mine), accepted);
    // Guilds bug: the message names the guild that *accepted*, not the one the request went to.
    // See docs/e2e-testing.md.
    await expectReceived(asker, MSG.allyTargetAccepted(theirs), told);
}

test('an alliance request is sent, received and accepted', async ({ player, createPlayer, guilds }) => {
    const { otherMaster, mine, theirs } = await pair({ player, createPlayer, guilds });

    await requestAlly(guilds, player, mine, otherMaster, theirs);
    await acceptAlly(guilds, otherMaster, mine, player, theirs);

    const listed = player.getMessageBufferIndex();
    await guilds.run(player, '/guild ally list');
    await expectReceived(player, MSG.allyList, listed);
    await expectReceived(player, theirs, listed);
});

test('an alliance request can be declined', async ({ player, createPlayer, guilds }) => {
    const { otherMaster, mine, theirs } = await pair({ player, createPlayer, guilds });

    await requestAlly(guilds, player, mine, otherMaster, theirs);

    await guilds.run(otherMaster, `/guild ally decline ${mine}`);
    await expectReceived(otherMaster, MSG.allyDeclined(mine));
    // Same inverted guild name as the accept message.
    await expectReceived(player, MSG.allyTargetDeclined(theirs));

    await commandWithout(player, guilds, '/guild ally list', MSG.allyNone, MSG.allyList);
});

test('an alliance can be listed and then removed from either side', async ({ player, createPlayer, guilds }) => {
    const { otherMaster, mine, theirs } = await pair({ player, createPlayer, guilds });

    await requestAlly(guilds, player, mine, otherMaster, theirs);
    await acceptAlly(guilds, otherMaster, mine, player, theirs);

    await guilds.run(player, `/guild ally remove ${theirs}`);
    await expectReceived(player, MSG.allyRemoved(theirs));
    // Same inverted guild name: the message names the guild that did the removing.
    await expectReceived(otherMaster, MSG.allyTargetRemoved(mine));

    await commandWithout(player, guilds, '/guild ally list', MSG.allyNone, MSG.allyList);

    // And it can be rebuilt and then removed from the other side instead.
    await requestAlly(guilds, player, mine, otherMaster, theirs);
    await acceptAlly(guilds, otherMaster, mine, player, theirs);

    await guilds.run(otherMaster, `/guild ally remove ${mine}`);
    await expectReceived(otherMaster, MSG.allyRemoved(mine));
    await expectReceived(player, MSG.allyTargetRemoved(theirs));

    await commandWithout(otherMaster, guilds, '/guild ally list', MSG.allyNone, MSG.allyList);
});

test('a duplicate alliance request is refused', async ({ player, createPlayer, guilds }) => {
    const { otherMaster, mine, theirs } = await pair({ player, createPlayer, guilds });

    await requestAlly(guilds, player, mine, otherMaster, theirs);

    await commandWithout(
        player,
        guilds,
        `/guild ally add ${theirs}`,
        MSG.allyAlreadyRequested,
        MSG.allyInviteSent(theirs),
    );
});

test('an alliance that already exists cannot be requested again', async ({ player, createPlayer, guilds }) => {
    const { otherMaster, mine, theirs } = await pair({ player, createPlayer, guilds });

    await requestAlly(guilds, player, mine, otherMaster, theirs);
    await acceptAlly(guilds, otherMaster, mine, player, theirs);

    // The alliance is established …
    const listed = player.getMessageBufferIndex();
    await guilds.run(player, '/guild ally list');
    await expectReceived(player, MSG.allyList, listed);
    await expectReceived(player, theirs, listed);

    // … and the pending request was cleared when it was accepted, so a second attempt is refused
    // as an existing alliance rather than as a duplicate request.
    await commandWithout(player, guilds, `/guild ally add ${theirs}`, MSG.allyAlready, MSG.allyInviteSent(theirs));
});

test('a guild cannot ally with itself', async ({ player, guilds }) => {
    const mine = await guilds.createGuild(player);

    await commandWithout(player, guilds, `/guild ally add ${mine}`, MSG.allySameGuild, MSG.allyInviteSent(mine));
});

test('removing a guild that is not an ally is refused', async ({ player, createPlayer, guilds }) => {
    const { theirs } = await pair({ player, createPlayer, guilds });

    await commandWithout(
        player,
        guilds,
        `/guild ally remove ${theirs}`,
        MSG.allyNotAllied,
        MSG.allyRemoved(theirs),
    );
});

test('declining an alliance that was never requested changes nothing', async ({ player, createPlayer, guilds }) => {
    const { otherMaster, mine } = await pair({ player, createPlayer, guilds });

    // `ally decline` returns quietly when there is no pending request, so the only thing to
    // observe is that the guild is still without allies.
    await guilds.run(otherMaster, `/guild ally decline ${mine}`);
    await commandWithout(otherMaster, guilds, '/guild ally list', MSG.allyNone, MSG.allyList);
});

test('a member without the alliance role cannot manage alliances', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);
    const other = await createPlayer({ username: guilds.playerName() });
    const otherGuild = await guilds.createGuild(other);

    await commandWithout(
        member,
        guilds,
        `/guild ally add ${otherGuild}`,
        MSG.roleNoPermission,
        MSG.allyInviteSent(otherGuild),
    );
    await commandWithout(
        member,
        guilds,
        `/guild ally remove ${otherGuild}`,
        MSG.roleNoPermission,
        MSG.allyRemoved(otherGuild),
    );
});

test('a deleted guild drops out of its allies', async ({ player, createPlayer, guilds }) => {
    const { otherMaster, mine, theirs } = await pair({ player, createPlayer, guilds });

    await requestAlly(guilds, player, mine, otherMaster, theirs);
    await acceptAlly(guilds, otherMaster, mine, player, theirs);

    await guilds.run(otherMaster, '/guild delete');
    await expect(otherMaster).toHaveReceivedMessage(MSG.deleteWarning);
    await guilds.confirm(otherMaster);
    await expect(otherMaster).toHaveReceivedMessage(MSG.deleteSuccess(theirs));

    // The surviving ally is told, and its list is empty again.
    await expect(player).toHaveReceivedMessage(MSG.deleteNotifyAlly(theirs));
    await commandWithout(player, guilds, '/guild ally list', MSG.allyNone, MSG.allyList);
    expect((await guildList(player)).some(entry => entry.name === theirs)).toBe(false);
});

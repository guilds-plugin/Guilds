import { describe, expect, test } from '@plugwright/runner';
import { commandWithout, expectReceived } from '../plugins/support/assertions.js';
import { MSG } from '../plugins/support/expected.js';
import { guildList } from '../plugins/support/state.js';
import { stripColors } from '../plugins/support/text.js';

/**
 * Invite codes: creating them, listing them, redeeming them, deleting them, running out of uses,
 * and the refusals around each.
 *
 * The lifecycle test is a `describe.serial` block on purpose: creating a code, redeeming it and
 * deleting it are three steps of one code's life, so they run in order on one bot and one guild
 * rather than each rebuilding the state the next one needs.
 */

/** Pulls the generated code out of a `codes.created` line. */
function parseCode(text: string): string | undefined {
    const match = stripColors(text).match(/new invite code:\s*(\S+)\s+with/);
    return match?.[1];
}

test('a code can be created, listed and deleted', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);

    const since = player.getMessageBufferIndex();
    await guilds.run(player, '/guild code create');
    await expectReceived(player, MSG.codeCreated, since);
    await expectReceived(player, '1 uses', since);

    const code = parseCode(player.messageBuffer.slice(since).join('\n'));
    expect(code).toBeTruthy();

    // codes.length is seven in the staged config.
    expect(code!.length).toBe(7);

    const listed = player.getMessageBufferIndex();
    await guilds.run(player, '/guild code list');
    await expectReceived(player, MSG.codeListHeader, listed);
    await expectReceived(player, code!, listed);
    await expectReceived(player, 'Uses Remaining', listed);

    const deleted = player.getMessageBufferIndex();
    await guilds.run(player, `/guild code delete ${code}`);
    await expectReceived(player, MSG.codeDeleted, deleted);

    // A guild with no codes says so rather than listing nothing.
    await commandWithout(player, guilds, '/guild code list', MSG.codeEmpty, code!);
});

test('a code can be redeemed and puts the player in the guild', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const created = player.getMessageBufferIndex();
    await guilds.run(player, '/guild code create 2');
    await expectReceived(player, MSG.codeCreated, created);
    const code = parseCode(player.messageBuffer.slice(created).join('\n'))!;

    const joiner = await createPlayer({ username: guilds.playerName() });
    const since = joiner.getMessageBufferIndex();
    await guilds.run(joiner, `/guild code redeem ${code}`);
    await expectReceived(joiner, MSG.codeJoined(guild), since);
    // A code has its own guild-wide announcement, distinct from the invite one.
    await expectReceived(joiner, `${joiner.username} has joined the guild using an invite code`);

    const entry = (await guildList(joiner)).find(item => item.name === guild)!;
    expect(entry.memberCount).toBe(2);
});

test('an invalid code is refused', async ({ player, createPlayer, guilds }) => {
    await guilds.createGuild(player);
    const stranger = await createPlayer({ username: guilds.playerName() });

    await commandWithout(
        stranger,
        guilds,
        '/guild code redeem NOSUCHC',
        MSG.codeInvalid,
        MSG.codeJoined('any guild'),
    );
});

test('a code runs out of uses and then says so', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const created = player.getMessageBufferIndex();
    await guilds.run(player, '/guild code create 1');
    await expectReceived(player, MSG.codeCreated, created);
    const code = parseCode(player.messageBuffer.slice(created).join('\n'))!;

    const first = await createPlayer({ username: guilds.playerName() });
    const joining = first.getMessageBufferIndex();
    await guilds.run(first, `/guild code redeem ${code}`);
    await expectReceived(first, MSG.codeJoined(guild), joining);

    // Tier 1 allows two members, so this one is refused on capacity first …
    const second = await createPlayer({ username: guilds.playerName() });
    await commandWithout(second, guilds, `/guild code redeem ${code}`, MSG.acceptGuildFull, MSG.codeJoined(guild));

    // … and once a slot is free, by the code having no uses left.
    await guilds.run(player, `/guild kick ${first.username}`);
    await expect(player).toHaveReceivedMessage(MSG.kickSuccess(first.username));

    await commandWithout(second, guilds, `/guild code redeem ${code}`, MSG.codeOut, MSG.codeJoined(guild));
});

test('a guild cannot hold more active codes than the configured limit', async ({ player, guilds }) => {
    await guilds.createGuild(player);

    // codes.amount is two in the staged config.
    for (let index = 0; index < 2; index++) {
        const since = player.getMessageBufferIndex();
        await guilds.run(player, '/guild code create');
        await expectReceived(player, MSG.codeCreated, since);
    }

    await commandWithout(player, guilds, '/guild code create', MSG.codeMax, MSG.codeCreated);
});

test('code information is restricted by role', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const created = player.getMessageBufferIndex();
    await guilds.run(player, '/guild code create');
    await expectReceived(player, MSG.codeCreated, created);
    const code = parseCode(player.messageBuffer.slice(created).join('\n'))!;

    const seen = player.getMessageBufferIndex();
    await guilds.run(player, `/guild code info ${code}`);
    await expectReceived(player, 'Invite Code Information', seen);
    await expectReceived(player, 'Redeemers', seen);

    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    await commandWithout(member, guilds, `/guild code info ${code}`, MSG.roleNoPermission, 'Invite Code Information');
    await commandWithout(member, guilds, `/guild code delete ${code}`, MSG.roleNoPermission, MSG.codeDeleted);
});

test('redeeming a code is refused for a player who already has a guild', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const created = player.getMessageBufferIndex();
    await guilds.run(player, '/guild code create 5');
    await expectReceived(player, MSG.codeCreated, created);
    const code = parseCode(player.messageBuffer.slice(created).join('\n'))!;

    const joiner = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, joiner, guild);

    await commandWithout(joiner, guilds, `/guild code redeem ${code}`, MSG.alreadyInGuild, MSG.codeJoined(guild));
});

describe.serial('one code through its whole life', () => {
    // The steps share a guild and a code, so they belong to one bot in order rather than each
    // rebuilding what the previous one left behind. The runner keeps the same bot connected across
    // the block, which is what lets the code carry over from one step to the next.
    let code = '';
    let guild = '';

    test('create', async ({ player, guilds }) => {
        // The block's later steps need this guild, so the per-test teardown steps aside.
        guilds.keep();
        guild = await guilds.createGuild(player);

        const since = player.getMessageBufferIndex();
        await guilds.run(player, '/guild code create 3');
        await expectReceived(player, MSG.codeCreated, since);

        code = parseCode(player.messageBuffer.slice(since).join('\n')) ?? '';
        expect(code.length).toBe(7);
    });

    test('it is listed for the guild master', async ({ player, guilds }) => {
        const listed = player.getMessageBufferIndex();
        await guilds.run(player, '/guild code list');
        await expectReceived(player, MSG.codeListHeader, listed);
        await expectReceived(player, 'Uses Remaining', listed);
        await expectReceived(player, '3', listed);

        // The master can also inspect it in detail.
        const info = player.getMessageBufferIndex();
        await guilds.run(player, `/guild code info ${code}`);
        await expectReceived(player, 'Invite Code Information', info);
        await expectReceived(player, 'Redeemers', info);
    });

    test('a member can be added and cannot manage codes', async ({ player, createPlayer, guilds }) => {
        const member = await createPlayer({ username: guilds.playerName() });
        await guilds.addMember(player, member, guild);

        await commandWithout(member, guilds, `/guild code info ${code}`, MSG.roleNoPermission, 'Invite Code Information');
        await commandWithout(member, guilds, `/guild code delete ${code}`, MSG.roleNoPermission, MSG.codeDeleted);
    });

    test('it is deleted', async ({ player, guilds }) => {
        await guilds.run(player, `/guild code delete ${code}`);
        await expect(player).toHaveReceivedMessage(MSG.codeDeleted);

        await commandWithout(player, guilds, '/guild code list', MSG.codeEmpty, code);
    });

    test('and the guild the block built goes with it', async ({ player, guilds }) => {
        // `keep()` handed teardown over to the block, so the block deletes what it built.
        await guilds.run(player, '/guild delete');
        await expect(player).toHaveReceivedMessage(MSG.deleteWarning);
        await guilds.confirm(player);
        await expect(player).toHaveReceivedMessage(MSG.deleteSuccess(guild));
    });
});

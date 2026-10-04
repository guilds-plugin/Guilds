import { expect, test } from '@plugwright/runner';
import { commandWithout, expectNeverSeen, expectReceived } from '../plugins/support/assertions.js';
import { MSG } from '../plugins/support/expected.js';
import { guildList } from '../plugins/support/state.js';
import { stripColors } from '../plugins/support/text.js';

/**
 * Guild and ally chat: who receives a line, who must not, and what the toggle does.
 *
 * Every negative assertion goes through `commandWithout`, which first waits for a reply that proves
 * the server processed the command and only then checks that the unwanted line never arrived — an
 * `expect(...).not.toHaveReceivedMessage` on its own returns while the buffer is still clean, before
 * the server has answered anything.
 */

test('guild chat reaches the other member', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    const since = member.getMessageBufferIndex();
    await guilds.run(player, '/guild gc hello guild');
    await expectReceived(member, MSG.guildChatLabel, since);
    await expectReceived(member, 'hello guild', since);
    await expectReceived(member, player.username, since);
});

test('guild chat does not reach a player outside the guild', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);
    const outsider = await createPlayer({ username: guilds.playerName() });

    // The outsider answers /guild check, which proves the server processed the command that
    // follows; only then is their silence meaningful.
    await commandWithout(outsider, guilds, '/guild check', MSG.noPendingInvites, MSG.guildChatLabel);

    await guilds.run(player, '/guild gc secret to the guild');
    await expectReceived(member, 'secret to the guild');

    // The outsider is asked something harmless afterwards, so the server has caught up.
    await commandWithout(outsider, guilds, '/guild check', MSG.noPendingInvites, 'secret to the guild');
});

test('a player who left the guild no longer receives its chat', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    await guilds.run(member, '/guild leave');
    await expect(member).toHaveReceivedMessage(MSG.leaveWarning);
    await guilds.confirm(member);
    await expect(member).toHaveReceivedMessage(MSG.leaveSuccess);

    const afterLeaving = member.getMessageBufferIndex();
    await guilds.run(player, '/guild gc after you left');
    await commandWithout(
        member,
        guilds,
        '/guild check',
        MSG.noPendingInvites,
        'after you left',
    );

    expect(member.getMessageBufferIndex()).toBeGreaterThan(afterLeaving);
});

test('a member can speak for the guild, and the message is formatted for their role', async ({
    player,
    createPlayer,
    guilds,
}) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    // roles.yml gives every role `chat`, so a plain Member can open the channel.
    const heard = player.getMessageBufferIndex();
    await guilds.run(member, '/guild gc spoken by a member');
    await expectReceived(player, MSG.guildChatLabel, heard);
    await expectReceived(player, 'spoken by a member', heard);
    await expectReceived(player, member.username, heard);
});

test('toggling the guild channel on routes plain chat into the guild', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    const on = player.getMessageBufferIndex();
    await guilds.run(player, '/guild gc');
    await expectReceived(player, MSG.chatToggledOn('Guild'), on);

    const heard = member.getMessageBufferIndex();
    // Routed through the pacing helper like every other send: a plain chat line is still chat
    // as far as Paper's throttle is concerned, and going around the helper both breaks the
    // ordering against the toggle above and adds an unpaced send to the connection.
    await guilds.run(player, 'routed into the guild');
    await expectReceived(member, MSG.guildChatLabel, heard);
    await expectReceived(member, 'routed into the guild', heard);

    const off = player.getMessageBufferIndex();
    await guilds.run(player, '/guild gc');
    await expectReceived(player, MSG.chatToggledOff('Guild'), off);

    // With the channel off, a plain line is ordinary server chat again: the other member sees the
    // raw message without the guild chat label.
    const plain = member.getMessageBufferIndex();
    await guilds.run(player, 'ordinary chat');
    await expectReceived(member, 'ordinary chat', plain);
    await expect(member).not.toHaveReceivedMessage(MSG.guildChatLabel, { since: plain });

    // …and /guild gc still works explicitly.
    const explicit = member.getMessageBufferIndex();
    await guilds.run(player, '/guild gc explicitly guild chat');
    await expectReceived(member, MSG.guildChatLabel, explicit);
});

test('ally chat reaches an allied guild and not the third party', async ({ player, createPlayer, guilds }) => {
    const allyMaster = await createPlayer({ username: guilds.playerName() });
    const ally = await createPlayer({ username: guilds.playerName() });
    const strangerMaster = await createPlayer({ username: guilds.playerName() });

    const home = await guilds.createGuild(player);
    const allyGuild = await guilds.createGuild(allyMaster);
    const strangerGuild = await guilds.createGuild(strangerMaster);
    await guilds.addMember(allyMaster, ally, allyGuild);

    await guilds.run(player, `/guild ally add ${allyGuild}`);
    await expect(player).toHaveReceivedMessage(MSG.allyInviteSent(allyGuild));
    await guilds.run(allyMaster, `/guild ally accept ${home}`);
    await expect(allyMaster).toHaveReceivedMessage(MSG.allyAccepted(home));

    const heard = ally.getMessageBufferIndex();
    await guilds.run(player, '/guild ac across the alliance');
    await expectReceived(ally, MSG.allyChatLabel, heard);
    await expectReceived(ally, 'across the alliance', heard);
    await expectReceived(ally, home, heard);

    // The allied guild's master can answer.
    const reply = player.getMessageBufferIndex();
    await guilds.run(allyMaster, '/guild ac reply from the ally');
    await expectReceived(player, MSG.allyChatLabel, reply);
    await expectReceived(player, 'reply from the ally', reply);

    // A guild that is not in the alliance hears nothing. The stranger is a guild master itself, so
    // its "the server has caught up" probe is a command it can actually answer.
    expect((await guildList(strangerMaster)).some(entry => entry.name === strangerGuild)).toBe(true);
    await commandWithout(
        strangerMaster,
        guilds,
        '/guild bank balance',
        MSG.bankBalance,
        'across the alliance',
    );
});

test('the chat format carries the configured guild label', async ({ player, guilds }) => {
    // guild.format.chat is "&7&l[Guild Chat]&r &b[{role}&b]&r &b {player}: {message}", so the label
    // and the sender both come from the staged config rather than from the test.
    await guilds.createGuild(player);

    const since = player.getMessageBufferIndex();
    await guilds.run(player, '/guild gc format check');
    await expectReceived(player, '[Guild Chat]', since);
    await expectReceived(player, 'format check', since);
    await expect(player).toHaveReceivedMessage(
        stripColors(MSG.guildChatLabel).replace('[', '').replace(']', ''),
    );
});

test('guild chat is refused for a player in no guild', async ({ createPlayer, guilds }) => {
    const stranger = await createPlayer({ username: guilds.playerName() });

    await commandWithout(stranger, guilds, '/guild gc hello', MSG.noGuild, MSG.guildChatLabel);
    await commandWithout(stranger, guilds, '/guild ac hello', MSG.noGuild, MSG.allyChatLabel);
    await expectNeverSeen(stranger, 'hello');
});

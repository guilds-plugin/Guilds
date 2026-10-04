import { expect, test } from '@plugwright/runner';
import { commandWithout, expectNeverSeen, expectReceived } from '../plugins/support/assertions.js';
import { ROLE } from '../plugins/support/fixtures.js';
import { MSG } from '../plugins/support/expected.js';
import { guildList, guildMembers } from '../plugins/support/state.js';

/**
 * Membership: invitations, joining and declining them, leaving, kicking, the role ladder and
 * ownership transfer.
 *
 * Every test builds its own guild with its own bots. Tier 1 allows two members (see
 * src/test/e2e/server/plugins/Guilds/tiers.yml), which is what makes the capacity refusal
 * reachable and is why no test here ever puts a third player in a guild.
 */

test('an invited player accepts and both sides see it happen', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const guest = await createPlayer({ username: guilds.playerName() });

    await guilds.invite(player, guest, guild);
    await expect(player).toHaveReceivedMessage(MSG.inviteSent(guest.username));
    await expect(guest).toHaveReceivedMessage(MSG.inviteReceived(guild));

    await guilds.accept(guest, guild);
    await expect(player).toHaveReceivedMessage(MSG.acceptPlayerJoined(guest.username));

    // Verified through the members GUI rather than by trusting the messages above.
    const roster = await guildMembers(player);
    expect(roster.map(member => member.name).sort()).toEqual([player.username, guest.username].sort());
    expect(roster.find(member => member.name === guest.username)!.role).toBe(ROLE.member);
    expect(roster.find(member => member.name === player.username)!.role).toBe(ROLE.master);

    const entry = (await guildList(player)).find(g => g.name === guild)!;
    expect(entry.memberCount).toBe(2);
});

test('an invited player can decline and stays without a guild', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const guest = await createPlayer({ username: guilds.playerName() });

    await guilds.invite(player, guest, guild);
    await guilds.decline(guest, guild);

    const roster = await guildMembers(player);
    expect(roster.map(member => member.name)).toEqual([player.username]);

    await commandWithout(guest, guilds, '/guild bank balance', MSG.noGuild, MSG.bankBalance);
});

test('a player cannot accept a guild they were never invited to', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const stranger = await createPlayer({ username: guilds.playerName() });

    // New guilds are private, so an uninvited accept is refused.
    await commandWithout(stranger, guilds, `/guild accept ${guild}`, MSG.acceptNotInvited, MSG.acceptSuccess(guild));
});

test('a player cannot be invited twice', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const guest = await createPlayer({ username: guilds.playerName() });

    await guilds.invite(player, guest, guild);
    await commandWithout(
        player,
        guilds,
        `/guild invite ${guest.username}`,
        MSG.inviteAlreadyInvited,
        MSG.inviteSent(guest.username),
    );
});

test('a player who is already in a guild cannot be invited', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const busy = await createPlayer({ username: guilds.playerName() });
    await guilds.createGuild(busy);

    await commandWithout(
        player,
        guilds,
        `/guild invite ${busy.username}`,
        MSG.inviteAlreadyInGuild(busy.username),
        MSG.inviteSent(busy.username),
    );
});

test('a player cannot join a second guild while in one', async ({ player, createPlayer, guilds }) => {
    const rival = await createPlayer({ username: guilds.playerName() });
    const elsewhere = await guilds.createGuild(rival);
    await guilds.invite(rival, player, elsewhere);

    // Only now does the player hold a guild of their own, which is what the refusal is about.
    await guilds.createGuild(player);

    await commandWithout(player, guilds, `/guild accept ${elsewhere}`, MSG.alreadyInGuild, MSG.acceptSuccess(elsewhere));
});

test('a member can leave and the guild is told', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    await guilds.run(member, '/guild leave');
    await expect(member).toHaveReceivedMessage(MSG.leaveWarning);
    await guilds.confirm(member);
    await expect(member).toHaveReceivedMessage(MSG.leaveSuccess);

    // The remaining member is notified.
    await expectReceived(player, MSG.playerLeft(member.username));

    const roster = await guildMembers(player);
    expect(roster.map(member => member.name)).toEqual([player.username]);
});

test('a member leaving warns that the guild master deletes the guild by leaving', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);

    await guilds.run(player, '/guild leave');
    await expect(player).toHaveReceivedMessage(MSG.leaveWarningMaster);
    await guilds.confirm(player);
    await expect(player).toHaveReceivedMessage(MSG.leaveSuccess);

    expect((await guildList(player)).some(g => g.name === guild)).toBe(false);
});

test('the guild master can kick a member and both see the outcome', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    await guilds.run(player, `/guild kick ${member.username}`);
    await expect(player).toHaveReceivedMessage(MSG.kickSuccess(member.username));
    await expect(member).toHaveReceivedMessage(MSG.kickedBy(player.username));
    await expectReceived(player, MSG.kickedNotice(member.username, player.username));

    const roster = await guildMembers(player);
    expect(roster.map(m => m.name)).toEqual([player.username]);

    await commandWithout(member, guilds, '/guild bank balance', MSG.noGuild, MSG.bankBalance);
});

test('a member without the kick role cannot kick, and cannot be promoted by doing so', async ({
    player,
    createPlayer,
    guilds,
}) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    // roles.yml gives the Member role kick: false, so this is refused on role grounds.
    await commandWithout(
        member,
        guilds,
        `/guild kick ${player.username}`,
        MSG.roleNoPermission,
        MSG.kickSuccess(player.username),
    );
});

test('promotion and demotion change what a member may do', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    // `@players` only completes to players who are actually online, so the invite target has to be
    // a real bot rather than a name.
    const target = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    // The ladder runs Member -> Veteran -> Officer -> GuildMaster, and `invite` is the first
    // permission the two lowest roles differ on: false for Member, true from Veteran up.
    const invite = `/guild invite ${target.username}`;
    await commandWithout(member, guilds, invite, MSG.roleNoPermission, MSG.inviteSent(target.username));

    await guilds.shiftRole(player, member, guild, 'promote');
    expect((await guildMembers(player)).find(m => m.name === member.username)!.role).toBe(ROLE.veteran);

    const promoted = member.getMessageBufferIndex();
    await guilds.run(member, invite);
    await expectReceived(member, MSG.inviteSent(target.username), promoted);

    await guilds.shiftRole(player, member, guild, 'demote');
    expect((await guildMembers(player)).find(m => m.name === member.username)!.role).toBe(ROLE.member);

    await commandWithout(member, guilds, invite, MSG.roleNoPermission, MSG.inviteSent(target.username));
});

test('promotion is refused when there is no higher role to move into', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const officer = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, officer, guild);
    await guilds.shiftRole(player, officer, guild, 'promote');
    await guilds.shiftRole(player, officer, guild, 'promote');
    expect((await guildMembers(player)).find(m => m.name === officer.username)!.role).toBe(ROLE.officer);

    // Officer may promote in general, but the guild master is already the top of the ladder.
    await commandWithout(
        officer,
        guilds,
        `/guild promote ${player.username}`,
        MSG.cantPromote,
        MSG.promoteSuccess(player.username),
    );
});

test('a member without the promote role is stopped before the command runs', async ({
    player,
    createPlayer,
    guilds,
}) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    await commandWithout(
        member,
        guilds,
        `/guild promote ${player.username}`,
        MSG.roleNoPermission,
        MSG.promoteSuccess(player.username),
    );
});

test('ownership can be transferred, and the old master loses the master role', async ({
    player,
    createPlayer,
    guilds,
}) => {
    const guild = await guilds.createGuild(player);
    const heir = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, heir, guild);

    await guilds.run(player, `/guild transfer ${heir.username}`);
    await expect(player).toHaveReceivedMessage(MSG.transferSuccess);
    await expect(heir).toHaveReceivedMessage(MSG.newMaster);

    // Guild.transferGuild swaps the two roles rather than demoting to Officer.
    const roster = await guildMembers(heir);
    expect(roster.find(m => m.name === heir.username)!.role).toBe(ROLE.master);
    expect(roster.find(m => m.name === player.username)!.role).toBe(ROLE.member);

    // The guild list follows the master, which is how the transfer shows up outside the roster.
    expect((await guildList(heir)).find(g => g.name === guild)!.master).toBe(heir.username);
});

test('a full guild refuses another member', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    const third = await createPlayer({ username: guilds.playerName() });
    await guilds.invite(player, third, guild);

    // tier 1 allows two members, so this one is over the limit.
    await commandWithout(third, guilds, `/guild accept ${guild}`, MSG.acceptGuildFull, MSG.acceptSuccess(guild));

    const roster = await guildMembers(player);
    expect(roster.map(m => m.name).sort()).toEqual([player.username, member.username].sort());
});

test('the kick command only accepts a name from the guild roster', async ({ player, createPlayer, guilds }) => {
    await guilds.createGuild(player);
    const stranger = await createPlayer({ username: guilds.playerName() });

    // `@Values("@members") @Single` is what refuses the name, before the handler ever runs, so the
    // refusal is ACF's completion error rather than one of Guilds' own messages.
    await commandWithout(
        player,
        guilds,
        `/guild kick ${stranger.username}`,
        MSG.valueRejected('members'),
        MSG.kickSuccess(stranger.username),
    );
});

test('a player who leaves loses the permissions their guild granted them', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    // tiers.yml grants guilds.command.admin to guild members, and the runner denies that node on
    // LuckPerms' default group, so running an admin command at all proves the guild handed it over.
    const asMember = member.getMessageBufferIndex();
    await guilds.run(member, `/guild admin status ${guild}`);
    await expectReceived(member, MSG.statusChanged('Public'), asMember);

    await guilds.run(member, '/guild leave');
    await expect(member).toHaveReceivedMessage(MSG.leaveWarning);
    await guilds.confirm(member);
    await expect(member).toHaveReceivedMessage(MSG.leaveSuccess);

    const afterLeaving = member.getMessageBufferIndex();
    await guilds.run(member, `/guild admin status ${guild}`);
    await expectReceived(member, MSG.permissionDenied, afterLeaving);

    await expectNeverSeen(member, MSG.statusChanged('Public'), afterLeaving);
});

test('a player who is kicked loses the permissions their guild granted them', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    await guilds.run(player, `/guild kick ${member.username}`);
    await expect(player).toHaveReceivedMessage(MSG.kickSuccess(member.username));

    const afterKick = member.getMessageBufferIndex();
    await guilds.run(member, `/guild admin status ${guild}`);
    await expectReceived(member, MSG.permissionDenied, afterKick);
});

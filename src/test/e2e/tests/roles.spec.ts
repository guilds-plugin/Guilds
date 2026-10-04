import { expect, test, type PlayerWrapper } from '@plugwright/runner';
import { commandWithout, expectReceived } from '../plugins/support/assertions.js';
import { ROLE, STARTING_BALANCE } from '../plugins/support/fixtures.js';
import { MSG } from '../plugins/support/expected.js';
import { guildList, guildMembers } from '../plugins/support/state.js';

/**
 * Who is allowed to do what.
 *
 * Guilds gates a command twice, and this file keeps the two apart: `roles.yml` decides whether the
 * player's *guild role* carries the permission, and a Bukkit node decides whether the account may
 * run the command at all. The runner hands every bot `guilds.command.*` and the tier grants
 * `guilds.command.admin`, so what is left to cover is role refusals and deliberate per-player node
 * denials.
 */

/** One player's role, read out of the guild's members GUI. */
async function roleOf(player: PlayerWrapper, of: string): Promise<string> {
    const roster = await guildMembers(player);
    return roster.find(member => member.name === of)!.role;
}

test('a plain member is refused the commands their role does not carry', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);
    await guilds.setBank(guild, 500);

    expect(await roleOf(player, member.username)).toBe(ROLE.member);

    const refused: Array<[string, string]> = [
        ['/guild rename SomethingElse', MSG.renamed('SomethingElse')],
        ['/guild prefix Other', MSG.prefixChanged('Other')],
        ['/guild delete', MSG.deleteSuccess(guild)],
        ['/guild upgrade', MSG.upgradeSuccess],
        ['/guild status', MSG.statusChanged('Public')],
        [`/guild transfer ${player.username}`, MSG.transferSuccess],
        ['/guild bank withdraw 1', MSG.withdrawSuccess(member.username, '1')],
        ['/guild sethome', MSG.sethomeSuccess],
        ['/guild code create 1', MSG.codeCreated],
        ['/guild motd set Nope', MSG.motdSet('Nope')],
    ];

    for (const [command, mustNotSee] of refused) {
        await commandWithout(member, guilds, command, MSG.roleNoPermission, mustNotSee);
    }
});

test('a member may still do the things their role does carry', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);
    await guilds.fund(member, STARTING_BALANCE);

    // Member carries chat, deposit-money and open-vault.
    const deposit = member.getMessageBufferIndex();
    await guilds.run(member, '/guild bank deposit 40');
    await expectReceived(member, MSG.depositSuccess(member.username, '40'), deposit);
    expect(await guilds.bankOf(guild)).toBe(40);

    const on = member.getMessageBufferIndex();
    await guilds.run(member, '/guild gc');
    await expectReceived(member, MSG.chatToggledOn('Guild'), on);

    const off = member.getMessageBufferIndex();
    await guilds.run(member, '/guild gc');
    await expectReceived(member, MSG.chatToggledOff('Guild'), off);
});

test('a Bukkit permission node can close a command the role allows', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);
    await guilds.setBank(guild, 500);

    // roles.yml gives Officer and above withdraw-money; a plain Member does not have it, so the
    // only thing that can refuse this withdrawal is the Bukkit node.
    await guilds.shiftRole(player, member, guild, 'promote');
    await guilds.shiftRole(player, member, guild, 'promote');
    expect(await roleOf(player, member.username)).toBe(ROLE.officer);

    // Withdrawing zero moves no money, and is gated by the same node — so it can stand in for a
    // real withdrawal while waiting for LuckPerms' change to reach the client.
    const probe = '/guild bank withdraw 0';

    await guilds.revoke(member, 'guilds.command.bank.withdraw');
    await guilds.awaitEffect(member, probe, MSG.permissionDenied);

    await commandWithout(
        member,
        guilds,
        '/guild bank withdraw 10',
        MSG.permissionDenied,
        MSG.withdrawSuccess(member.username, '10'),
    );
    expect(await guilds.bankOf(guild)).toBe(500);

    await guilds.grant(member, 'guilds.command.bank.withdraw');
    await guilds.awaitEffect(member, probe, MSG.withdrawSuccess(member.username, '0'));

    const granted = member.getMessageBufferIndex();
    await guilds.run(member, '/guild bank withdraw 10');
    await expectReceived(member, MSG.withdrawSuccess(member.username, '10'), granted);
    expect(await guilds.bankOf(guild)).toBe(490);
});

test('upgrading charges the tier price and moves the guild onto the new tier', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);
    await guilds.setBank(guild, 200);

    const upgrading = player.getMessageBufferIndex();
    await guilds.run(player, '/guild upgrade');
    await expectReceived(player, MSG.upgradeMoneyWarning, upgrading);
    await guilds.confirm(player);
    await expectReceived(player, MSG.upgradeSuccess, player.getMessageBufferIndex() - 1);

    // 200 is the whole tier 2 price in tiers.yml.
    expect(await guilds.bankOf(guild)).toBe(0);
    expect((await guildList(player)).find(g => g.name === guild)!.tier).toBe(2);
});

/*
 * Tier permissions survive an upgrade.
 *
 * `CommandUpgrade.accept` used to refresh a guild's Bukkit permissions with
 * `removeGuildPermsFromAll` → `upgradeTier` → `addGuildPermsToAll`, and both permission calls
 * dispatch onto the async executor (`settings.run-vault-async`). Nothing ordered them relative to
 * each other, so the removal could land after the addition. Tiers 1 and 2 in the staged tiers.yml
 * grant the same node, which makes the outcome unambiguous: after an upgrade the guild master had
 * lost `guilds.command.admin` and was refused the very command they could run a moment earlier.
 *
 * `GuildHandler.applyTierPerms` now runs both halves in one chain, revoke before grant.
 */
test('an upgrade leaves the master with the permissions the new tier grants', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);
    await guilds.setBank(guild, 200);

    // Before: the tier's permission is in force.
    const before = player.getMessageBufferIndex();
    await guilds.run(player, `/guild admin status ${guild}`);
    await expectReceived(player, MSG.statusChanged('Public'), before);

    await guilds.run(player, '/guild upgrade');
    await expect(player).toHaveReceivedMessage(MSG.upgradeMoneyWarning);
    await guilds.confirm(player);
    await expect(player).toHaveReceivedMessage(MSG.upgradeSuccess);

    // After: still in force. LuckPerms reaches the client on its own schedule, so wait for the
    // command to answer rather than for a fixed delay.
    await guilds.awaitEffect(player, `/guild admin status ${guild}`, MSG.statusChanged('Public'));
});

test('a member can run the admin commands their tier grants, but not the role-gated ones', async ({
    player,
    createPlayer,
    guilds,
}) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    // tiers.yml grants guilds.command.admin to every member, and the runner denies it on
    // LuckPerms' default group, so this only works because the guild handed it over.
    const status = member.getMessageBufferIndex();
    await guilds.run(member, `/guild admin status ${guild}`);
    await expectReceived(member, MSG.statusChanged('Public'), status);

    // …but the role table still gates what the admin surface may actually do.
    await commandWithout(member, guilds, '/guild upgrade', MSG.roleNoPermission, MSG.upgradeSuccess);
    await commandWithout(member, guilds, '/guild delete', MSG.roleNoPermission, MSG.deleteSuccess(guild));
});

test('a player in no guild is told so rather than refused by role', async ({ createPlayer, guilds }) => {
    const stranger = await createPlayer({ username: guilds.playerName() });

    await commandWithout(stranger, guilds, '/guild bank balance', MSG.noGuild, MSG.bankBalance);
    await commandWithout(stranger, guilds, '/guild members', MSG.noGuild, MSG.permissionDenied);
    await expect(stranger).not.toHaveReceivedMessage(MSG.roleNoPermission);
});

/*
 * Guilds bug, kept as a reproducer rather than a fix — see docs/e2e-testing.md.
 *
 * Every admin command whose first argument is a guild resolved with `@Flags("other") @Values("@guilds")`
 * is refused by ACF as soon as the command takes a *second* argument: `admin bank deposit <guild>
 * <amount>`, `admin rename <guild> <name>`, `admin prefix <guild> <prefix>` and `admin motd set
 * <guild> <motd>` all answer "Please specify one of (@guilds)", from the console and from a player
 * alike. The single-argument forms (`admin bank balance <guild>`, `admin status <guild>`) work,
 * which is what makes the failure easy to miss. ACF validates the `@Values` parameter against the
 * argument *after* the one the `@Flags` context consumed.
 *
 * These two tests pin the behaviour. When it is fixed they are the expectations to correct — and
 * `setBank` in the fixtures can go back to the admin route it no longer uses.
 */
test('an admin bank deposit of a named guild is refused by the argument check', async ({ player, server, guilds }) => {
    const guild = await guilds.createGuild(player);
    await guilds.setBank(guild, 100);

    const response = await server.execute(`guild admin bank deposit ${guild} 500`);
    expect(response).toContain('Please specify one of');

    // Nothing changed, and the same command with only the guild does work.
    expect(await guilds.bankOf(guild)).toBe(100);
    expect(await server.execute(`guild admin bank balance ${guild}`)).toContain('bank balance of 100');
});

test('the same admin command works through the player-facing bank commands', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);
    await guilds.setBank(guild, 100);

    const depositing = player.getMessageBufferIndex();
    await guilds.run(player, '/guild bank deposit 25');
    await expectReceived(player, MSG.depositSuccess(player.username, '25'), depositing);
    expect(await guilds.bankOf(guild)).toBe(125);
});

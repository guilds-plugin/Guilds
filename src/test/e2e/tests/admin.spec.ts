import { expect, test } from '@plugwright/runner';
import { expectReceived } from '../plugins/support/assertions.js';
import { ROLE, guildName } from '../plugins/support/fixtures.js';
import { ITEM, MSG } from '../plugins/support/expected.js';
import { guildList, guildMembers, teleportAndSettle } from '../plugins/support/state.js';
import { stripColors } from '../plugins/support/text.js';

/**
 * The admin commands.
 *
 * Most of them take their guild as an argument rather than as "the sender's own guild", and most
 * of them take a second argument after it, an amount, a name or a piece of text. The arena
 * commands take an arena and a position the same way.
 *
 * ACF validates a `@Values` argument against the *last* argument of the command while the
 * parameter it belongs to is the *first* one, so the guild name was compared against a completion
 * that had been filtered by the second argument. The filtered list came back empty, ACF fell back
 * to the literal completion name, and every one of these commands was refused with "Please specify
 * one of (@guilds)" before its handler ran. The single-argument forms kept working, which is what
 * made the failure easy to miss.
 */

/** Somewhere flat and clear of spawn, for the arena spawn point. */
const SPOT = { x: 260, y: 90, z: -200 };

test('an admin command that names a guild and a second argument reaches its handler', async ({
    player,
    guilds,
}) => {
    const guild = await guilds.createGuild(player);

    const deposit = stripColors(await guilds.console(`guild admin bank deposit ${guild} 500`));
    expect(deposit).toContain('put 500.0 into');
    expect(await guilds.bankOf(guild)).toBe(500);

    const withdraw = stripColors(await guilds.console(`guild admin bank withdraw ${guild} 200`));
    expect(withdraw).toContain('taken 200.0 from');
    expect(await guilds.bankOf(guild)).toBe(300);
});

test('an admin command that names a guild which does not exist says so', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);

    const response = stripColors(await guilds.console('guild admin bank deposit NoSuchGuild 500'));
    expect(response).toContain(MSG.guildNoExist);
    expect(response).not.toContain('bank balance of');
    expect(await guilds.bankOf(guild)).toBe(0);
});

test('an admin command can put a player into a guild', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const guest = await createPlayer({ username: guilds.playerName() });

    const response = stripColors(await guilds.console(`guild admin addplayer ${guild} ${guest.username}`));
    expect(response).toContain(`added ${guest.username}`);

    const roster = await guildMembers(player);
    expect(roster.map(member => member.name).sort()).toEqual([player.username, guest.username].sort());
});

test('an admin command that names a guild and a second argument is refused for an unknown player', async ({
    player,
    guilds,
}) => {
    const guild = await guilds.createGuild(player);

    const response = stripColors(await guilds.console(`guild admin addplayer ${guild} NoSuchPlayer`));
    expect(response).toContain(MSG.playerNoExist('NoSuchPlayer'));
    expect(response).not.toContain('Please specify one of');
});

test('an admin command can hand a guild to one of its members', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const heir = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, heir, guild);

    const response = stripColors(await guilds.console(`guild admin transfer ${guild} ${heir.username}`));
    expect(response).toContain(MSG.transferSuccess);

    // The guild list follows the master, which is how the transfer shows up outside the roster.
    expect((await guildList(player)).find(g => g.name === guild)!.master).toBe(heir.username);

    // The admin route demotes the old master one step down the ladder, where the player-facing
    // `/guild transfer` swaps the two roles instead.
    const roster = await guildMembers(heir);
    expect(roster.find(member => member.name === heir.username)!.role).toBe(ROLE.master);
    expect(roster.find(member => member.name === player.username)!.role).toBe(ROLE.officer);
});

test('an admin command can hand a player upgrade tickets', async ({ player, createPlayer, guilds }) => {
    await guilds.createGuild(player);
    const target = await createPlayer({ username: guilds.playerName() });

    await guilds.console(`guild admin give ${target.username} 2`);

    // The ticket is a paper named "Guild Upgrade Ticket", with the amount as the stack size.
    await expect(target).toContainItem(ITEM.ticketMaterial, { count: 2 });
    const ticket = target.bot.inventory.items().find(item => item.name === ITEM.ticketMaterial);
    expect(ticket?.count).toBe(2);
    expect(JSON.stringify(ticket?.customName)).toContain(ITEM.ticket);
});

test('an admin command can teleport to an arena spawn point', async ({ player, guilds }) => {
    const arena = guildName('arena');
    const guild = await guilds.createGuild(player);

    // The tier grants the admin node to the guild's members, and LuckPerms applies it on its own
    // schedule, so the first admin command is issued until it answers.
    await guilds.awaitEffect(player, `/guild admin motd ${guild}`, MSG.motdNotSet);

    const creating = player.getMessageBufferIndex();
    await guilds.run(player, `/guild arena create ${arena}`);
    await expectReceived(player, MSG.arenaCreated(arena), creating);

    const spot = await teleportAndSettle(player, SPOT.x, SPOT.y, SPOT.z);
    const setting = player.getMessageBufferIndex();
    await guilds.run(player, `/guild arena set challenger ${arena}`);
    await expectReceived(player, MSG.arenaChallengerSet(arena), setting);

    const teleporting = player.getMessageBufferIndex();
    await guilds.run(player, `/guild arena tp ${arena} challenger`);
    await expectReceived(player, MSG.arenaTeleported('challenger', arena), teleporting);
    await expect(player).toBeNear(spot.x, spot.y, spot.z, { tolerance: 1.5 });

    // An arena outlives the test, so it is removed again.
    const deleting = player.getMessageBufferIndex();
    await guilds.run(player, `/guild arena delete ${arena}`);
    await expectReceived(player, MSG.arenaDeleted(arena), deleting);
});

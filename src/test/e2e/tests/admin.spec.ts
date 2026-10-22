import { expect, test } from '@plugwright/runner';
import { MSG } from '../plugins/support/expected.js';
import { guildMembers } from '../plugins/support/state.js';
import { stripColors } from '../plugins/support/text.js';

/**
 * The admin commands that name a guild.
 *
 * Every admin command takes its guild as an argument rather than as "the sender's own guild", and
 * most of them take a second argument after it — an amount, a name, a piece of text.
 *
 * ACF validates a `@Values` argument against the *last* argument of the command while the
 * parameter it belongs to is the *first* one, so the guild name was compared against a completion
 * that had been filtered by the second argument. The filtered list came back empty, ACF fell back
 * to the literal completion name, and every one of these commands was refused with "Please specify
 * one of (@guilds)" before its handler ran. The single-argument forms kept working, which is what
 * made the failure easy to miss.
 */

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

import { expect, test, type PlayerWrapper } from '@plugwright/runner';
import { commandWithout } from '../plugins/support/assertions.js';
import { CREATION_COST, STARTING_BALANCE, type GuildsFixtures } from '../plugins/support/fixtures.js';
import { MSG } from '../plugins/support/expected.js';
import { guildList } from '../plugins/support/state.js';
import { received, says, waitUntil } from '../plugins/support/text.js';

/**
 * Makes sure the bot is in no guild, for a command that refuses a player who is in one.
 *
 * The plugin's storage lives in the server directory and outlives a run, and the runner reuses a
 * bot name for the same offline account, so a bot can turn up already in a guild. The create
 * fixture leaves any guild for exactly that reason; this is the same step, for a bot that is not
 * about to create a guild.
 */
async function ensureNoGuild(player: PlayerWrapper, guilds: GuildsFixtures): Promise<void> {
    const leaving = player.getMessageBufferIndex();
    await guilds.run(player, '/guild leave');
    // A master is told they are the master rather than that they are leaving, but the prompt is
    // registered either way, so both wordings mean there is something to confirm.
    const prompted = () =>
        received(player.messageBuffer, says(MSG.leaveWarning), leaving) ||
        received(player.messageBuffer, says(MSG.leaveWarningMaster), leaving);
    await waitUntil(
        () => (prompted() || received(player.messageBuffer, says(MSG.noGuild), leaving) ? true : undefined),
        { timeout: 10_000, interval: 25 },
    );
    if (prompted()) {
        await guilds.confirm(player);
        await expect(player).toHaveReceivedMessage(MSG.leaveSuccess, { timeout: 10_000 });
    }
}

/**
 * Guild lifecycle: creating a guild, the confirmation prompt that gates it, every input rule that
 * refuses one, renaming, deleting, and the commands that report on a guild afterwards.
 *
 * Each test starts from an unprompted bot with a fresh balance, so nothing here depends on the
 * order the file's tests run in.
 */

test('creates a guild, charges for it, and shows it in the guild list', async ({ player, guilds }) => {
    const name = guilds.guildName('made');

    await guilds.fund(player, STARTING_BALANCE);
    await guilds.run(player, `/guild create ${name} Made`);
    await expect(player).toHaveReceivedMessage(MSG.createWarning);

    // Nothing exists until the prompt is answered.
    await expect(player).not.toHaveReceivedMessage(MSG.created(name));

    await guilds.confirm(player);
    await expect(player).toHaveReceivedMessage(MSG.created(name));

    expect(await guilds.balanceOf(player)).toBe(STARTING_BALANCE - CREATION_COST);

    const listed = await guildList(player);
    const entry = listed.find(guild => guild.name === name);
    expect(entry).toBeDefined();
    expect(entry!.master).toBe(player.username);
    expect(entry!.memberCount).toBe(1);
    expect(entry!.tier).toBe(1);
    expect(entry!.status).toBe('Private');
});

test('cancelling the prompt leaves no guild and no charge', async ({ player, guilds }) => {
    const name = guilds.guildName('cancel');

    await guilds.fund(player, STARTING_BALANCE);
    await guilds.run(player, `/guild create ${name} Cancel`);
    await expect(player).toHaveReceivedMessage(MSG.createWarning);

    await guilds.cancel(player);
    await expect(player).toHaveReceivedMessage(MSG.createCancelled);

    expect(await guilds.balanceOf(player)).toBe(STARTING_BALANCE);
    expect((await guildList(player)).some(guild => guild.name === name)).toBe(false);
});

test('rejects a guild name that breaks the configured requirements', async ({ player, guilds }) => {
    await guilds.fund(player, STARTING_BALANCE);

    // guild.requirements.name is "[a-zA-Z0-9&]{1,64}", so the hyphen is the offending character.
    await commandWithout(
        player,
        guilds,
        '/guild create not-a-valid-name Bad',
        MSG.nameRequirements,
        MSG.created('not-a-valid-name'),
    );
});

test('rejects a name too long to serve as its own prefix', async ({ player, guilds }) => {
    await guilds.fund(player, STARTING_BALANCE);

    // guild.requirements.prefix allows 20 characters; with no prefix given, the name is measured
    // against the prefix rule too, which produces its own message.
    const tooLong = 'E2E' + 'x'.repeat(25);
    await commandWithout(player, guilds, `/guild create ${tooLong}`, MSG.nameTooLong, MSG.created(tooLong));
});

test('rejects an explicit prefix that breaks the prefix requirements', async ({ player, guilds }) => {
    await guilds.fund(player, STARTING_BALANCE);

    const name = guilds.guildName('pfx');
    const badPrefix = 'p'.repeat(21);
    await commandWithout(player, guilds, `/guild create ${name} ${badPrefix}`, MSG.prefixTooLong, MSG.created(name));
});

/*
 * Guilds bug, kept as a reproducer rather than a fix — see docs/e2e-testing.md.
 *
 * `GuildHandler.blacklistCheck` compares the arguments the wrong way round. With the shipped
 * `guild.blacklist.case-sensitive: true` it asks whether a blacklisted *word* contains the
 * proposed *name*, so a name that is a substring of "crap" is refused while a name that merely
 * contains "crap" sails through — the opposite of what the config comment asks for. Both halves
 * are pinned below so the behaviour cannot change unnoticed; these are the expectations to correct
 * when the comparison is fixed.
 */
test('rejects a guild name that is a substring of a blacklisted word', async ({ player, guilds }) => {
    await guilds.fund(player, STARTING_BALANCE);

    // "crap" is one of the shipped blacklisted words, and it contains "cr".
    await commandWithout(player, guilds, '/guild create cr', MSG.blacklist, MSG.created('cr'));
});

test('accepts a guild name that contains a blacklisted word', async ({ player, guilds }) => {
    const name = 'crapguild';

    await guilds.fund(player, STARTING_BALANCE);
    await guilds.run(player, `/guild create ${name} Crappy`);
    await expect(player).toHaveReceivedMessage(MSG.createWarning);
    await guilds.confirm(player);
    await expect(player).toHaveReceivedMessage(MSG.created(name));

    expect((await guildList(player)).some(guild => guild.name === name)).toBe(true);
});

test('rejects a guild name another guild already holds', async ({ player, createPlayer, guilds }) => {
    const name = await guilds.createGuild(player);
    const other = await createPlayer({ username: guilds.playerName() });
    await guilds.fund(other, STARTING_BALANCE);

    await commandWithout(other, guilds, `/guild create ${name} Other`, MSG.nameTaken, MSG.created(name));
});

test('rejects creation when the player cannot afford it', async ({ player, guilds }) => {
    const name = guilds.guildName('poor');

    await guilds.fund(player, CREATION_COST - 1);
    await commandWithout(player, guilds, `/guild create ${name} Poor`, MSG.notEnoughMoney, MSG.created(name));

    expect(await guilds.balanceOf(player)).toBe(CREATION_COST - 1);
});

test('rejects creation while the player is already in a guild', async ({ player, guilds }) => {
    await guilds.createGuild(player);
    const second = guilds.guildName('second');

    await commandWithout(player, guilds, `/guild create ${second} Sec`, MSG.alreadyInGuild, MSG.created(second));
});

/*
 * The confirmation is answered some time after the command, and the world moves on in between.
 * The checks the command ran are therefore run again when the pending action is confirmed: the
 * player can join another guild, another guild can take the name, the owner can reload the config.
 * Without that, the confirmation creates a guild the command would have refused to create.
 */
test('refuses the confirmation when the player joined a guild in the meantime', async ({
    player,
    createPlayer,
    guilds,
}) => {
    const name = guilds.guildName('joined');

    await ensureNoGuild(player, guilds);
    await guilds.fund(player, STARTING_BALANCE);
    await guilds.run(player, `/guild create ${name} Joined`);
    await expect(player).toHaveReceivedMessage(MSG.createWarning);

    // The player joins somebody else's guild before the prompt is answered.
    const other = await createPlayer({ username: guilds.playerName() });
    const elsewhere = await guilds.createGuild(other);
    await guilds.addMember(other, player, elsewhere);

    await commandWithout(player, guilds, '/guild confirm', MSG.alreadyInGuild, MSG.created(name));

    // No second guild, and the player is only in the guild they joined.
    const listed = await guildList(player);
    expect(listed.some(guild => guild.name === name)).toBe(false);
    expect(listed.filter(guild => guild.master === player.username)).toHaveLength(0);
    expect(listed.find(guild => guild.name === elsewhere)!.memberCount).toBe(2);

    // The refused offer is dropped, so it cannot be confirmed later by accident.
    await commandWithout(player, guilds, '/guild confirm', MSG.confirmError, MSG.created(name));

    // Nothing was charged for a guild that was never created.
    expect(await guilds.balanceOf(player)).toBe(STARTING_BALANCE);
});

test('refuses the confirmation when another guild took the name in the meantime', async ({
    player,
    createPlayer,
    guilds,
}) => {
    const name = guilds.guildName('taken');

    await ensureNoGuild(player, guilds);
    await guilds.fund(player, STARTING_BALANCE);
    await guilds.run(player, `/guild create ${name} Taken`);
    await expect(player).toHaveReceivedMessage(MSG.createWarning);

    // Another player claims the name before the prompt is answered.
    const other = await createPlayer({ username: guilds.playerName() });
    await guilds.createGuild(other, { name });

    await commandWithout(player, guilds, '/guild confirm', MSG.nameTaken, MSG.created(name));

    // One guild with that name, and it is the other player's.
    const listed = await guildList(player);
    expect(listed.filter(guild => guild.name === name)).toHaveLength(1);
    expect(listed.find(guild => guild.name === name)!.master).toBe(other.username);

    // The player is in no guild at all, and was charged nothing.
    await commandWithout(player, guilds, '/guild bank balance', MSG.noGuild, MSG.bankBalance);
    expect(await guilds.balanceOf(player)).toBe(STARTING_BALANCE);
});

test('renames a guild', async ({ player, guilds }) => {
    const original = await guilds.createGuild(player);
    const renamed = guilds.guildName('new');

    await guilds.run(player, `/guild rename ${renamed}`);
    await expect(player).toHaveReceivedMessage(MSG.renamed(renamed));

    const listed = await guildList(player);
    expect(listed.some(guild => guild.name === renamed)).toBe(true);
    expect(listed.some(guild => guild.name === original)).toBe(false);
});

test('rejects a rename to a name another guild already holds', async ({ player, createPlayer, guilds }) => {
    const own = await guilds.createGuild(player);
    const other = await createPlayer({ username: guilds.playerName() });
    const taken = await guilds.createGuild(other);

    await commandWithout(player, guilds, `/guild rename ${taken}`, MSG.nameTaken, MSG.renamed(taken));

    const listed = await guildList(player);
    expect(listed.some(guild => guild.name === own)).toBe(true);
    expect(listed.some(guild => guild.name === taken)).toBe(true);
});

test('allows a guild to keep the name it already has', async ({ player, guilds }) => {
    // GuildInputValidator deliberately excludes the guild's own id from the taken-name check, so
    // a rename to the current name is a no-op rather than a conflict with itself.
    const name = await guilds.createGuild(player);

    await guilds.run(player, `/guild rename ${name}`);
    await expect(player).toHaveReceivedMessage(MSG.renamed(name));

    const entry = (await guildList(player)).find(guild => guild.name === name)!;
    expect(entry.master).toBe(player.username);
});

test('deletes a guild once the prompt is confirmed', async ({ player, guilds }) => {
    const name = await guilds.createGuild(player);

    await guilds.run(player, '/guild delete');
    await expect(player).toHaveReceivedMessage(MSG.deleteWarning);
    await guilds.confirm(player);
    await expect(player).toHaveReceivedMessage(MSG.deleteSuccess(name));

    expect((await guildList(player)).some(guild => guild.name === name)).toBe(false);

    // The guild is gone from the player's point of view too, not just from the list.
    await commandWithout(player, guilds, '/guild bank balance', MSG.noGuild, MSG.bankBalance);
});

test('cancelling the delete prompt keeps the guild', async ({ player, guilds }) => {
    const name = await guilds.createGuild(player);

    await guilds.run(player, '/guild delete');
    await expect(player).toHaveReceivedMessage(MSG.deleteWarning);
    await guilds.cancel(player);
    await expect(player).toHaveReceivedMessage(MSG.deleteCancelled);

    expect((await guildList(player)).some(guild => guild.name === name)).toBe(true);
});

test('toggles the guild between public and private', async ({ player, guilds }) => {
    const name = await guilds.createGuild(player);
    const entry = async () => (await guildList(player)).find(guild => guild.name === name)!;

    expect((await entry()).status).toBe('Private');

    await guilds.run(player, '/guild status');
    await expect(player).toHaveReceivedMessage(MSG.statusChanged('Public'));
    expect((await entry()).status).toBe('Public');

    await guilds.run(player, '/guild status');
    await expect(player).toHaveReceivedMessage(MSG.statusChanged('Private'));
    expect((await entry()).status).toBe('Private');
});

test('changes the guild prefix', async ({ player, guilds }) => {
    const name = await guilds.createGuild(player);

    await guilds.run(player, '/guild prefix Renamed');
    await expect(player).toHaveReceivedMessage(MSG.prefixChanged('Renamed'));

    const entry = (await guildList(player)).find(guild => guild.name === name)!;
    expect(entry.prefix).toBe('Renamed');
});

test('sets, shows and clears the guild MOTD', async ({ player, guilds }) => {
    const name = await guilds.createGuild(player);

    await commandWithout(player, guilds, '/guild motd', MSG.motdNotSet, MSG.motdView());

    await guilds.run(player, '/guild motd set E2E-MOTD');
    await expect(player).toHaveReceivedMessage(MSG.motdSet('E2E-MOTD'));

    await guilds.run(player, '/guild motd');
    await expect(player).toHaveReceivedMessage(MSG.motdView());
    await expect(player).toHaveReceivedMessage('E2E-MOTD');

    await guilds.run(player, '/guild motd remove');
    await expect(player).toHaveReceivedMessage(MSG.motdRemove);
    await commandWithout(player, guilds, '/guild motd', MSG.motdNotSet, 'E2E-MOTD');
});

test('reports guild membership through the guild list and rejects guild-scoped commands elsewhere', async ({
    player,
    createPlayer,
    guilds,
}) => {
    const name = await guilds.createGuild(player);

    // The list GUI is what a player can read: the guild exists, and this bot is its master.
    const entry = (await guildList(player)).find(guild => guild.name === name)!;
    expect(entry.master).toBe(player.username);
    expect(entry.memberCount).toBe(1);

    // A bot outside any guild has no pending invitations ...
    const outsider = await createPlayer({ username: guilds.playerName() });
    await commandWithout(outsider, guilds, '/guild check', MSG.noPendingInvites, name);

    // ... and cannot run a command that needs a guild.
    await commandWithout(outsider, guilds, '/guild bank balance', MSG.noGuild, MSG.bankBalance);
});

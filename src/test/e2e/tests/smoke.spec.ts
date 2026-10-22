import { expect, test } from '@plugwright/runner';
import { MSG } from '../plugins/support/expected.js';
import { guildList } from '../plugins/support/state.js';
import { stripColors } from '../plugins/support/text.js';

/**
 * The runtime smoke test.
 *
 * The rest of the suite is written against a modern server and cannot run on the oldest versions
 * the plugin supports. This file can: it only asserts that the plugin came up, that the things it
 * refuses to start without came up with it, and that it answers.
 *
 * That is deliberately less than the full suite and deliberately more than "the server started". A
 * plugin that loads but whose economy provider never registered, or whose command layer is dead,
 * is broken on that version, and only a check that drives it finds that out.
 *
 * It runs on every environment, the modern ones included, so a smoke failure on 1.21.8 is as loud
 * as one on 1.8.8.
 */

/** The plugins Guilds refuses to start without, as they appear in the server log. */
const REQUIRED_PLUGINS = ['Vault', 'Essentials', 'LuckPerms'] as const;

/**
 * The environments the recovery fixtures are staged into.
 *
 * The build stages them into the smoke environments only: a guild file in the data folder would
 * change what the full suite on the local environment sees. A new smoke environment has to be
 * added here as well, or the two checks below are skipped on it.
 */
const SMOKE_ENVIRONMENTS = [
    'smoke-1.8.8',
    'smoke-1.12.2',
    'smoke-1.16.5',
    'smoke-1.21.8',
    'smoke-26.1.2',
] as const;

/** The guild in the backup directory. The plugin must never load it. */
const BACKUP_GUILD = 'BackupSentinel';

/** The guild in the data folder. The plugin is expected to load it. */
const STORED_GUILD = 'StoredSentinel';

test('Guilds and its dependencies are up', async ({ server }) => {
    const log = server.session.consoleLog.slice().join('\n');

    for (const plugin of REQUIRED_PLUGINS) {
        expect(log).toContain(`Enabling ${plugin}`);
        expect(log).not.toContain(`Could not load '${plugin}'`);
    }

    // Guilds disables itself rather than starting in a broken state when no economy has registered.
    expect(log).toContain('Enabling Guilds');
    expect(log).not.toContain('Disabling Guilds');
});

test('Vault has both an economy and a permission provider', async ({ server }) => {
    const vaults = stripColors(await server.execute('vault-info'));

    expect(vaults).toContain('Economy:');
    expect(vaults).toContain('Permission:');
    expect(vaults).not.toContain('Economy: null');
});

test(
    'a guild file in the backup directory is not loaded as a record',
    { environments: [...SMOKE_ENVIRONMENTS] },
    async ({ player }) => {
        const names = (await guildList(player)).map((guild) => guild.name);

        // The file is a complete, valid guild record, so a plugin that read the backup directory
        // as a data folder would have it in the list under this name.
        expect(names).not.toContain(BACKUP_GUILD);
    },
);

test(
    'a guild file in the data folder is loaded',
    { environments: [...SMOKE_ENVIRONMENTS] },
    async ({ player }) => {
        const names = (await guildList(player)).map((guild) => guild.name);

        // The counterpart of the check above: the ordinary path still works, so the recovery work
        // did not stop the plugin from reading the folder it is supposed to read.
        expect(names).toContain(STORED_GUILD);
    },
);

test('the economy moves money', async ({ player, guilds }) => {
    await guilds.fund(player, 2500);

    expect(await guilds.balanceOf(player)).toBe(2500);
});

test('a guild command answers', async ({ player, guilds }) => {
    await guilds.run(player, '/guild check');
    await expect(player).toHaveReceivedMessage(MSG.noPendingInvites, { timeout: 10_000 });
});

test('a guild can be created', async ({ player, guilds }) => {
    const name = guilds.guildName('smoke');

    await guilds.fund(player, 5000);
    await guilds.run(player, `/guild create ${name} ${name.slice(0, 8)}`);
    await expect(player).toHaveReceivedMessage(MSG.createWarning);

    await guilds.confirm(player);
    await expect(player).toHaveReceivedMessage(MSG.created(name));

    // The fixture removes the guild again once the test is over.
    guilds.track(name);
});

import { expect, test } from '@plugwright/runner';
import { MSG } from './support/expected.js';
import { stripColors } from './support/text.js';

/**
 * Runs before every other spec in the session, and aborts the whole run on failure.
 *
 * Guilds disables itself rather than starting in a broken state when Vault is missing or no
 * economy has registered — which on Paper means nothing in the suite fails for the right reason
 * later, everything just fails. These checks turn that into one clear failure at the top.
 */

/** Names as they appear in the server log, not in the plugin's own wording. */
const REQUIRED_PLUGINS = ['Vault', 'Essentials', 'LuckPerms'] as const;

test('Guilds and its hard dependencies are enabled', async ({ server }) => {
    const log = server.session.consoleLog.slice().join('\n');

    for (const plugin of REQUIRED_PLUGINS) {
        expect(log).toContain(`Enabling ${plugin}`);
        expect(log).not.toContain(`Could not load '${plugin}'`);
    }

    // Guilds hard-depends on a Vault economy at enable time; without one it disables itself.
    expect(log).toContain('Enabling Guilds');
    expect(log).not.toContain('Disabling Guilds');
});

test('the economy and permission providers Vault needs are registered', async ({ server }) => {
    const vaults = stripColors(await server.execute('vault-info'));

    // Guilds calls Economy#getBalance and Permission#playerHas during enable; Vault only names a
    // provider here once it has found one, and Guilds disables itself if either is missing.
    expect(vaults).toContain('Economy:');
    expect(vaults).toContain('Permission:');
    expect(vaults).not.toContain('Economy: null');

    // …and LuckPerms, not Vault's own SuperPerms shim, is what the permission tests drive.
    expect(vaults.toLowerCase()).toContain('luckperms');
});

test('Guilds commands are reachable and its language is English', async ({ player, guilds }) => {
    await guilds.fund(player, 1000);

    // `/guild check` has no side effects, so this cannot perturb a later spec, and it only
    // answers for a player Guilds recognises — which is what "the command layer is alive" means.
    await guilds.run(player, '/guild check');
    await expect(player).toHaveReceivedMessage(MSG.noPendingInvites, { timeout: 10_000 });

    // `settings.messagesLanguage` in the staged config. A mismatch here would silently make
    // every message assertion in the suite meaningless.
    await guilds.run(player, '/guild language en-US');
    await expect(player).toHaveReceivedMessage(MSG.languageSet('en-US'));
});

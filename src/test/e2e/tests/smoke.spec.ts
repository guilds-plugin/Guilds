import { expect, test } from '@plugwright/runner';
import { MSG } from '../plugins/support/expected.js';
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

test('Guilds and its hard dependencies are enabled', async ({ server }) => {
    const log = server.session.consoleLog.slice().join('\n');

    for (const plugin of REQUIRED_PLUGINS) {
        expect(log).toContain(`Enabling ${plugin}`);
        expect(log).not.toContain(`Could not load '${plugin}'`);
    }

    // Guilds disables itself rather than starting in a broken state when no economy has registered.
    expect(log).toContain('Enabling Guilds');
    expect(log).not.toContain('Disabling Guilds');
});

test('the economy and permission providers Vault needs are registered', async ({ server }) => {
    const vaults = stripColors(await server.execute('vault-info'));

    expect(vaults).toContain('Economy:');
    expect(vaults).toContain('Permission:');
    expect(vaults).not.toContain('Economy: null');
});

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

import { expect, test } from '@plugwright/runner';
import { existsSync, readdirSync, readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { guildList } from '../plugins/support/state.js';

/**
 * What the plugin does when one of its guild files cannot be read.
 *
 * The server starts with two guild files in the plugin's data folder: one that reads and one that
 * does not parse. The second one is the case this file is about.
 *
 * A guild file that does not parse is the only copy of that guild, so the plugin may not write
 * over it. One unreadable file is a problem with one guild rather than with the whole database,
 * so the plugin may not refuse to start either, and it may not cost the server the guilds that
 * did read.
 */

/** The id of the staged guild file that reads. */
const READABLE_GUILD_ID = '11111111-1111-1111-1111-111111111111';

/** The id of the staged guild file that does not parse. */
const UNREADABLE_GUILD_ID = '22222222-2222-2222-2222-222222222222';

/** The name of the guild in the file that reads. */
const READABLE_GUILD_NAME = 'E2EStaged';

/** The name the unreadable file claims for the guild it holds. */
const UNREADABLE_GUILD_NAME = 'E2EBroken';

/**
 * The plugin's data folder inside the throwaway server directory.
 *
 * The folder is looked up rather than assumed: the runner's working directory depends on how it
 * was started, and a spec that hard-codes a path fails for a reason that has nothing to do with
 * what it is testing.
 *
 * @returns the path of the plugin's data folder
 */
function pluginFolder(): string {
    let dir = process.cwd();

    for (let depth = 0; depth < 8; depth++) {
        const candidates = [
            join(dir, 'generated', 'local', 'run', 'plugins', 'Guilds'),
            join(dir, 'src', 'test', 'e2e', 'generated', 'local', 'run', 'plugins', 'Guilds'),
        ];

        for (const candidate of candidates) {
            if (existsSync(candidate)) return candidate;
        }

        dir = dirname(dir);
    }

    throw new Error('could not find the plugin data folder');
}

/**
 * @returns the folder the plugin reads its guild files from
 */
function dataFolder(): string {
    return join(pluginFolder(), 'data');
}

/**
 * @param name the file's name
 * @returns every copy of that file in the backup folder
 */
function backupsOf(name: string): string[] {
    const root = join(pluginFolder(), 'backups', 'corrupt');
    if (!existsSync(root)) return [];

    const copies: string[] = [];
    for (const stamp of readdirSync(root)) {
        const folder = join(root, stamp, 'guilds');
        if (!existsSync(folder)) continue;
        for (const file of readdirSync(folder)) {
            copies.push(join(folder, file));
        }
    }

    return copies.filter(copy => copy.endsWith(name));
}

test('the plugin starts with a guild file it cannot read, and says which file', async ({ server }) => {
    const log = server.session.consoleLog.slice().join('\n');

    // The plugin disables itself when it cannot start, so the log is where an operator finds out
    // whether it did.
    expect(log).toContain('Enabling Guilds');
    expect(log).not.toContain('Disabling Guilds');

    // The operator is told which file is the problem, rather than only that something was wrong.
    expect(log).toContain(`${UNREADABLE_GUILD_ID}.json`);
});

test('the guilds that could be read are available', async ({ player }) => {
    const listed = await guildList(player);

    expect(listed.some(entry => entry.name === READABLE_GUILD_NAME)).toBe(true);
    // The guild whose file does not parse is not there, and pretending otherwise would be worse.
    expect(listed.some(entry => entry.name === UNREADABLE_GUILD_NAME)).toBe(false);
});

test('the guild file that could not be read is left on disk, unchanged', async () => {
    const file = join(dataFolder(), `${UNREADABLE_GUILD_ID}.json`);
    const copies = backupsOf(`${UNREADABLE_GUILD_ID}.json`);

    expect(existsSync(file)).toBe(true);
    expect(copies.length).toBe(1);
    // The backup was taken when the plugin read the file, so the file on disk and the backup are
    // the same bytes: nothing has written over it since.
    expect(readFileSync(file, 'utf8')).toBe(readFileSync(copies[0], 'utf8'));
});

test('the guild file that could be read is still readable', async () => {
    const file = join(dataFolder(), `${READABLE_GUILD_ID}.json`);

    expect(existsSync(file)).toBe(true);
    expect(readFileSync(file, 'utf8')).toContain(READABLE_GUILD_NAME);
});

test('the guild file that could not be read was copied to the backup folder', async () => {
    const original = join(dataFolder(), `${UNREADABLE_GUILD_ID}.json`);
    const copies = backupsOf(`${UNREADABLE_GUILD_ID}.json`);

    expect(copies.length).toBe(1);
    expect(readFileSync(copies[0], 'utf8')).toBe(readFileSync(original, 'utf8'));
});

test('the backup of the guild file is outside the data folder', async () => {
    for (const copy of backupsOf(`${UNREADABLE_GUILD_ID}.json`)) {
        expect(copy.startsWith(dataFolder())).toBe(false);
    }
});

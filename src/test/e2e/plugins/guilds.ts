import { definePlugin } from '@plugwright/runner';
import { guildsFixtures } from './support/fixtures.js';

/**
 * The Guilds E2E fixture set.
 *
 * Loaded from build.gradle.kts with `plugins { local("guilds") }`, and responsible for three
 * things:
 *
 * 1. `setup` — one-time server state, before any spec file is imported.
 * 2. `extendContext` — the `guilds` fixture every spec destructures.
 * 3. `tests` — a preflight spec, which runs ahead of the suite and aborts the entire session if
 *    Guilds or one of its hard dependencies did not come up.
 */
export default definePlugin({
    name: 'guilds',

    async setup({ session }) {
        const console = session.console;
        if (!console) throw new Error('The Guilds E2E environment needs a server console.');

        /*
         * Guilds declares no permissions in its plugin.yml, so a non-op player has none of
         * `guilds.command.*` and every `/guild …` from a bot would be refused. LuckPerms puts
         * every joining player into its `default` group; granting there rather than per bot keeps
         * this to one command and leaves the per-user API free for the negative permission tests.
         *
         * `guilds.command.admin` is denied again straight away. The staged tiers.yml grants that
         * node to guild members, and denying it at group level means a member can only run admin
         * commands because their guild gave them the permission — which is what the leave/kick
         * suites assert. The fixtures grant it per player when they need it for teardown.
         */
        await console.execute('lp group default permission set guilds.command.* true');
        await console.execute('lp group default permission set guilds.command.admin false');

        // Guilds is the plugin under test, so its absence from the log is a provisioning failure.
        const log = session.consoleLog.slice().join('\n');
        if (!log.includes('Guilds')) {
            throw new Error('Guilds never appeared in the server log — it did not enable.');
        }
    },

    extendContext(ctx) {
        return { guilds: guildsFixtures(ctx) };
    },

    tests: [
        {
            mode: 'preflight',
            // Resolved against this module's own compiled location rather than process.cwd(), so
            // it does not depend on how the run was launched.
            file: new URL('./guilds-preflight.spec.js', import.meta.url).pathname,
        },
    ],
});

declare module '@plugwright/runner' {
    interface TestContext {
        guilds: ReturnType<typeof guildsFixtures>;
    }
}

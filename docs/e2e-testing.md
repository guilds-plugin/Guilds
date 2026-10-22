# Guilds end-to-end tests

End-to-end tests for Guilds, driven through real Minecraft players on a real Paper server by
[Plugwright](https://github.com/Drownek/plugwright) 3.0.0. Every test signs a bot in, sends chat or
clicks an inventory, and asserts on what that bot then sees — a chat line, a GUI, its own position,
its own money. Nothing reaches into plugin internals, and no test is allowed to pass by asserting
that nothing happened.

## What is covered, and what is not

`COVERAGE.md` maps every feature area to the file that tests it, and lists the areas that are
deliberately deferred along with why. Read that first if you want to know whether a change is
covered.

## Running the tests

The Gradle plugin provisions everything: it downloads Paper, stages the `shadowJar` this project
just built, fetches Vault, EssentialsX and LuckPerms, and writes a deterministic Guilds config.

```bash
./gradlew plugwrightTest          # provision, run everything, report
```

The first run takes a few minutes because it downloads a server. After that, the usual loop is:

```bash
./gradlew plugwrightTest          # re-run the whole suite
```

### The smoke suite on the other versions

`plugwrightTest` runs the whole suite on 1.21.8. The versions the plugin supports but the full
suite cannot run on are covered by a smaller smoke suite instead: the plugin and its hard
dependencies come up, Vault finds an economy and a permission provider, the economy moves money, a
command answers, and a guild can be created.

```bash
./gradlew plugwrightSmokeTest     # every supported version, one after the other
```

One environment at a time, because every environment binds the same port. The run takes about
three minutes, most of it server start-up.

| Environment | Minecraft | JDK | What it runs |
| --- | --- | --- | --- |
| `smoke-1.8.8` | 1.8.8 | 11 | the smoke spec |
| `smoke-1.12.2` | 1.12.2 | 11 | the smoke spec |
| `smoke-1.16.5` | 1.16.5 | 16 | the smoke spec |
| `smoke-1.21.8` | 1.21.8 | 21 | the smoke spec |
| `smoke-26.1.2` | 26.1.2 | 25 | the smoke spec |
| `smoke-26.3` | 26.3 | 25 | a server-level check, see below |

A single environment can be run on its own:

```bash
./gradlew plugwrightTestSmoke-1.8.8
```

#### What the older environments need, and why

Three things about the old servers are not obvious, and each of them is handled in
`build.gradle.kts` rather than in the test:

**A JDK of their own.** Plugwright's local mode starts the server with the JVM of the project's
Java toolchain, and that toolchain is one setting for the whole build. A Paper 1.8.8 server does
not run on Java 21, and a Paper 26.3 server does not run on it either. Every smoke environment
therefore gets a launcher script that runs the server on the JDK its version needs.

**No `--nogui` flag.** The runner always passes it, and the old CraftBukkit builds reject it as an
unknown option, which stops the server before it loads a single plugin. The launcher script drops
it for the versions that refuse it.

**No native transport.** The old CraftBukkit builds ship a Netty that predates Java 9 and decides
whether it can use direct buffers by reading the address of a heap buffer, which no longer reads
as zero on a modern JVM. Without that support the native transport cannot work, and every
connection the server accepts dies with "Unable to access address of buffer" — the bot never gets
a reply, and the test fails on a socket error rather than on anything to do with Guilds. The
legacy environments set `use-native-transport=false` in `server.properties`, which puts the server
on the same NIO transport every Windows server uses.

**An older EssentialsX.** EssentialsX 2.21 and newer need an Adventure API that Paper only
started shipping with 1.18, so on 1.16.5 the plugin fails to enable with a
`NoClassDefFoundError` for MiniMessage's `TagResolver`. The legacy environments run 2.20.1, the
newest build that does not need it.

**The newest Paper build, not the oldest.** Plugwright's provision task downloads the oldest
build the API lists for a version, and for 1.8.8 and 1.12.2 that build is a paperclip jar whose
patch data points at a Mojang URL that no longer serves the vanilla server jar, so the server
exits before it starts. The smoke environments download the newest build instead.

#### 26.3 has no bot yet

`minecraft-data`, the protocol table mineflayer reads, has no entry for 26.3. The server is fine
— it comes up, Guilds enables and it finds its providers — but the bot is refused as an outdated
client, so no test that needs a player can run there. That environment gets a server-level check
instead:

```bash
./gradlew smokeServerCheckSmoke-26.3
```

It starts the server and asserts that Guilds and its hard dependencies enabled, that Guilds did
not disable itself, and that it found the economy and the permission provider. It cannot prove
that the command layer works, because that needs a player.

26.1.2 is the same protocol as 26.1, which is the newest one minecraft-data knows, so the bot
connects to that environment as 26.1.

The legacy environments start the server through a shell script, and so does the 26.3 server
check, so they do not run on Windows.

### Running one file, or one test

Plugwright filters by substring, through the generated config rather than a Gradle flag. The config
the plugin writes is `build/tmp/plugwright/local.json`; copy it, narrow `tests.include` to the spec
file you want, and point the runner at the copy:

```bash
# One spec file.
python3 - <<'EOF'
import json
c = json.load(open('build/tmp/plugwright/local.json'))
c['tests']['include'] = ['gui']
json.dump(c, open('build/tmp/plugwright/one.json', 'w'), indent=2)
EOF

cd src/test/e2e
./node_modules/.bin/plugwright --config ../../build/tmp/plugwright/one.json
```

`tests.include` matches spec *file* names, and `tests.names` matches individual test names, so
`"include": ["gui"], "names": ["vault"]` runs only the vault tests. Set either to `null` to run
everything in that dimension.

Do **not** run the runner without provisioning first — `./gradlew plugwrightProvisionLocal`. The
plugin's `writeFiles` step is what puts the staged `config.yml` into the server directory, and a
plain runner invocation skips it, leaving the plugin's shipped defaults in place. Several tests read
values out of that config and will fail confusingly against the defaults.

### A server you can poke at by hand

```bash
./gradlew plugwrightRunServer      # start Paper with the same plugins and config, and leave it up
```

Useful when a test fails and you want to walk into the situation yourself with a real client.

The smoke environments have their own, and the server they start is the one the test drives:

```bash
./gradlew plugwrightRunServerSmoke-1.8.8
```

### Wiping state

```bash
./gradlew plugwrightClean          # delete the server directory: worlds, plugin data, player records
```

Everything the suite writes lives under `src/test/e2e/generated/`, which is git-ignored along with
`node_modules/` and `dist/`. Nothing it produces can reach version control, and no test reads or
writes anything outside that directory.

## Layout

```
src/test/e2e/
  plugins/            runner plugin: fixtures, assertions, read-back helpers, expected strings
  tests/              the specs, one file per feature area, plus smoke.spec.ts
  server/plugins/     the Guilds config the tests are written against
  generated/          the server, worlds and reports (git-ignored)
```

`plugins/support/expected.ts` holds every player-visible string the suite asserts on, derived from
`src/main/resources/languages/en-US.yml` and the staged config. If Guilds rewords a message, that
file is the only thing that needs to change.

## Runtime

A full run is roughly ten minutes: about 1,500 commands paced at 200ms each, plus the handful of
tests that wait on a real three-second cooldown expiring.

The smoke suite is about three minutes for every version together, almost all of it server
start-up: each version downloads its own Paper build and starts its own server.

The runner's per-test timeout defaults to 30s, which the cooldown tests sit uncomfortably close to.
CI sets `TEST_TIMEOUT=60000` for headroom. To match locally:

```bash
TEST_TIMEOUT=60000 ./gradlew plugwrightTest
```

The pacing is not incidental. Paper counts chat per connection and disconnects a bot with "Kicked
for spamming" once its excess-message counter passes a threshold, so every send in the suite goes
through `guilds.run`, which spaces them out. Two consequences worth knowing before you add a test:

- **Do not call `player.chat` or `bot.chat` directly.** Use `guilds.run`, which paces and refuses to
  send to a bot whose connection has already ended. A kicked bot silently accepts further `chat`
  calls, so without that guard a kick turns into a timeout against a message that was never coming.
- **Do not poll for "any message at all".** An earlier version of the buff cooldown test probed for
  an empty pattern before opening its GUI; since `/guild buff` opens an inventory and says nothing in
  chat, that probe sat out its full fifteen-second timeout on every single run. Wait for the
  specific thing you need instead — `awaitGuiItem` already re-runs a command until its contents
  match, which is the honest way to wait for a permission to reach the client.

## Writing a test

Two rules, both of which exist because the alternative is a test that cannot fail.

**Prove the change landed before asserting it.** Firing a command and immediately checking the
result is a race. Every helper in `plugins/support/fixtures.ts` sends a command and waits for the
message that proves Guilds processed it. Use `expect(player).toHaveReceivedMessage(...)` rather
than reading `messageBuffer` directly, and pass `since: player.getMessageBufferIndex()` when a test
has several steps so an earlier step's message cannot satisfy a later assertion.

**Do not assert an absence without first waiting for a presence.** `expect(player).not
.toHaveReceivedMessage(...)` returns instantly, so on its own it only proves nothing had arrived
*yet*. `commandWithout` in `plugins/support/assertions.ts` runs the command, waits for the reply
that proves it was processed, and only then asserts the other message never came.

Never sleep a fixed length to "let something happen". Poll for the thing (`expect.poll`, or
`waitFor`/`waitUntil` in the support layer), so the test takes as long as the plugin actually needs
and no longer. The one exception is a cooldown, and there the suite waits for the number Guilds
itself printed rather than a constant.

## Bugs this suite found

### An upgrade used to leave the guild without the tier's permissions

The tier change revoked the old tier's Bukkit nodes and granted the new tier's as two independent
calls, each of which dispatches onto the async executor, and nothing ordered the two, so the removal
could land after the addition. A master who had just upgraded was refused the commands the new tier
was meant to open, and had to be handed the node through LuckPerms before they worked again.
`roles.spec.ts` now carries `an upgrade leaves the master with the permissions the new tier grants`,
and `GuildHandler.applyTierPerms` runs both halves in one chain, revoke before grant. Fixed in #799.

### The vault blacklist, which was never broken

Worth writing down because it cost real time and reads exactly like a plugin bug.
`guis.vault.blacklist.materials` is read through ConfigMe's `ListProperty`, which asks the YAML
reader for a `List` at that path:

```java
protected <T> T getTypedObject(String path, Class<T> type) {
    Object value = getObject(path);
    return type.isInstance(value) ? type.cast(value) : null;   // null, no exception, no log
}
```

The staged config gave it `materials: "BEDROCK"`, a scalar. `String` is not a `List`, so the property
fell back to its default of empty and matched nothing. No error and no log line, and because the
server rewrites `config.yml` on save, the file on disk still read `- BEDROCK` afterwards. An empty
list is the default, so "nothing is refused" is indistinguishable from "the listener never runs",
which is what I concluded after a long detour through listener-ordering theories.
`src/test/e2e/server/plugins/Guilds/config.yml` now writes the value as a YAML list, which is also
how the shipped default is written, and `a blacklisted item is refused by the vault` asserts the
refusal.

### Every admin command that named a guild was refused

ACF validates a `@Values` argument against the *last* argument of the command, while the parameter
it belongs to was the *first* one — the guild name the command had already consumed. The completion
came back filtered by the second argument, so the guild name was not in it, and the command was
refused with "Please specify one of (@guilds)" before its handler ran. `/guild admin bank deposit
<guild> <amount>`, `/guild admin addplayer <guild> <player>` and every other two-argument admin
command were unreachable; the single-argument forms kept working, which is what made it easy to
miss.

The `@Values` annotation is only meaningful on a command's last argument, so it is gone from the
parameters ACF resolves by name, and Guilds' own context resolver answers with the guild it found
or with `error.guild-no-exist`. `admin.spec.ts` covers the surface.

## Deliberate limitations

**A player's own inventory index is not the window slot.** `bot.inventory.slots` follows the player
inventory window's ordering — main inventory at 9-35, hotbar at 36-44 — while a container window
puts its own slots first. Adding the container size to an inventory index is off by the hotbar
length. `ownItemWindowSlot` in `plugins/support/state.ts` reconciles the two through the offset
mineflayer itself uses; use it rather than doing the arithmetic.

**A guild name is only ever resolved by Guilds itself.** The admin commands take a guild by name
and the context resolver looks it up, so an invented name is answered with the plugin's own
`error.guild-no-exist` rather than with ACF's completion error. `admin.spec.ts` and
`cooldowns.spec.ts` assert that.

**Waiting for a LuckPerms change to reach a client is indirect.** LuckPerms recalculates an online
player's permissions on its own schedule, and the acknowledgement of the console command that made
the change is not the moment the player sees it. `guilds.awaitEffect` re-issues a harmless gated
probe (`/guild bank withdraw 0`, which moves no money) until the effect shows up.

**Timing assumptions that remain.** A cooldown has to actually elapse and a confirmation has to
actually be typed, so the cooldown tests are the slowest in the suite and sit in the mid-20s. That is
why CI raises the per-test timeout rather than treating it as a defect.
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
  tests/              the specs, one file per feature area
  server/plugins/     the Guilds config the tests are written against
  generated/          the server, worlds and reports (git-ignored)
```

`plugins/support/expected.ts` holds every player-visible string the suite asserts on, derived from
`src/main/resources/languages/en-US.yml` and the staged config. If Guilds rewords a message, that
file is the only thing that needs to change.

## Runtime

A full run is roughly ten minutes: about 1,500 commands paced at 200ms each, plus the handful of
tests that wait on a real three-second cooldown expiring.

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

## Bugs this suite found, and the plugin has since fixed

### An upgrade used to leave the guild without the tier's permissions

The tier change revoked the old tier's Bukkit nodes and granted the new tier's as two independent
calls, each of which dispatches onto the async executor, and nothing ordered the two — so the removal
could land after the addition. `roles.spec.ts` now carries `an upgrade leaves the master with the
permissions the new tier grants` in its place, and `GuildHandler.applyTierPerms` runs both halves in
one chain.

## Deliberate limitations

**A list setting has to be written as a list in the staged config.** ConfigMe reads `guis.vault.
blacklist.materials` by asking SnakeYAML for a `List` at that path, and a scalar node fails the type
check and falls back to the property's default. `materials: "BEDROCK"` therefore loads as `''` and
the vault blacklist matches nothing — no error, no log line, just a setting that silently is not
the one in the file. `server/plugins/Guilds/config.yml` writes the vault blacklist as a YAML list for
this reason; keep it that way. This cost an afternoon once already: the vault blacklist looked
broken in the plugin when the config file was the thing at fault.

**A player's own inventory index is not the window slot.** `bot.inventory.slots` follows the player
inventory window's ordering — main inventory at 9-35, hotbar at 36-44 — while a container window
puts its own slots first. Adding the container size to an inventory index is off by the hotbar
length. `ownItemWindowSlot` in `plugins/support/state.ts` reconciles the two through the offset
mineflayer itself uses; use it rather than doing the arithmetic.

**Guilds' admin commands that take a guild and a second argument are unreachable.** ACF's `@Values`
check refuses them, so `/guild admin bank deposit <guild> <amount>` never reaches its handler. The
suite drives the bank through the owning player's own `/guild bank deposit` instead, and
`roles.spec.ts` keeps a test that documents the admin route's refusal.

**`error.guild-no-exist` has no test that reaches its handler.** Member commands take the sender's
own guild as their `guild` argument, and the admin commands are `@Values("@guilds")`, so an invented
guild name is always rejected by the completion first. `cooldowns.spec.ts` asserts that instead.

**Waiting for a LuckPerms change to reach a client is indirect.** LuckPerms recalculates an online
player's permissions on its own schedule, and the acknowledgement of the console command that made
the change is not the moment the player sees it. `guilds.awaitEffect` re-issues a harmless gated
probe (`/guild bank withdraw 0`, which moves no money) until the effect shows up.

**Timing assumptions that remain.** A cooldown has to actually elapse and a confirmation has to
actually be typed, so the cooldown tests are the slowest in the suite and sit in the mid-20s. That is
why CI raises the per-test timeout rather than treating it as a defect.
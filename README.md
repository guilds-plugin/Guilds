# Guilds

[![Discord](https://discordapp.com/api/guilds/164280494874165248/widget.png?style=banner2)](https://helpch.at/discord)
[![Crowdin](https://badges.crowdin.net/guilds/localized.svg)](https://crowdin.com/project/guilds)

Guilds adds RPG-style player communities to Minecraft servers. Players can create and join guilds, build shared identities, compete with other groups, manage claims and roles, and connect with common server plugins such as Vault, LuckPerms, EssentialsX, PlaceholderAPI, and WorldGuard.

For user documentation, see the [Guilds wiki](https://wiki.helpch.at/).  
For API documentation, see the [Javadocs](https://guilds-plugin.github.io/javadocs/).

---

## Requirements

Guilds now requires **Java 11 or newer** at runtime.

This applies even when running older Minecraft versions such as 1.8.8. The plugin may support legacy Minecraft server versions, but the JVM running the server must be Java 11+.

Recommended Java versions for local testing:

| Minecraft / Paper version | Recommended Java |
| --- | ---: |
| 1.8.8 | 11 |
| 1.16.5 | 16 |
| 1.18.2 | 17 |
| 1.19.4 | 17 |
| 1.20.6 | 21 |
| 1.21.1 | 21 |
| 1.21.4 | 21 |
| 1.21.8 | 21 |
| 26.1.2 | 25 |

---

## Runtime dependencies

Guilds uses [Quark](https://github.com/BX-Team/Quark) to load the Kotlin runtime at plugin startup instead of shading Kotlin directly into the plugin jar. This keeps the distributed jar smaller while preserving a normal Bukkit/Spigot/Paper plugin workflow.

The Quark loader is configured for the **Bukkit** platform so the same artifact can run on Spigot-compatible and Paper-compatible servers.

Server admins should allow the server to download runtime dependencies from Maven Central on first startup. If outbound network access is blocked, preload or mirror the required runtime dependencies before deploying.

---

## Supported integrations

Guilds is designed to work with common server plugins and APIs:

| Integration | Purpose |
| --- | --- |
| Vault | Economy and permission bridge |
| LuckPerms | Permission provider through Vault |
| EssentialsX | Economy and chat-related compatibility |
| PlaceholderAPI | Placeholder expansion support |
| WorldGuard | Region and claim-related hooks |
| bStats | Anonymous plugin metrics |

Vault is required for normal operation. Economy and permission providers must be available through Vault.

---

## Building from source

Use the Gradle wrapper:

```bash
./gradlew clean shadowJar --no-configuration-cache
```

The shaded plugin jar is written to:

```text
build/libs/Guilds-<version>.jar
```

Configuration cache is intentionally disabled for Quark-enabled builds because Quark's generated-file task is not currently configuration-cache compatible.

To check dependency updates:

```bash
./gradlew dependencyUpdates --no-configuration-cache --no-parallel
```

---

## Local test servers

The build includes local Paper test-server tasks powered by `run-paper`.

Examples:

```bash
./gradlew runServer1_8_8 --no-configuration-cache
./gradlew runServer1_16_5 --no-configuration-cache
./gradlew runServer1_21_8 --no-configuration-cache
./gradlew runServer26_1_2 --no-configuration-cache
```

These tasks:

- build and attach the current Guilds jar
- create a local `eula.txt`
- run the target server with the configured Java toolchain
- preload common test plugins such as Vault, LuckPerms, and EssentialsX where compatible

Run-server tasks are intended for local smoke testing. They are interactive and should not be used as normal CI checks.

---

## Development notes

Current modernization decisions:

- Java runtime baseline: **Java 11+**
- Java bytecode target: **Java 11**
- Kotlin runtime: loaded at startup through Quark
- Kotlin stdlib: not shaded into the jar
- Shadow: still used for the remaining implementation dependencies
- ConfigMe: pinned to `1.3.0` for legacy Paper compatibility
- Triumph GUI: pinned to `3.1.11` until GUI constructors are migrated
- HikariCP: pinned to `4.0.3` for conservative compatibility

Before opening a pull request, run:

```bash
./gradlew clean shadowJar --no-configuration-cache
```

Optional formatting/license checks:

```bash
./gradlew spotlessCheck --no-configuration-cache
```

---

## Contributing

Contributions are welcome.

Good places to help:

- bug reports and reproduction cases
- compatibility testing across Minecraft versions
- translations on [Crowdin](https://crowdin.com/project/guilds)
- documentation improvements
- pull requests for open [GitHub issues](https://github.com/guilds-plugin/Guilds/issues)

For support or project discussion, join the Discord linked at the top of this README.

---

## Community add-ons

- [GuildClaimsAddon](https://github.com/Nerumir/GuildClaimsAddon) — alternative addon for the claiming system

---

## Special thanks

![YourKit](https://www.yourkit.com/images/yklogo.png)

YourKit supports open source projects with tools for monitoring and profiling Java and .NET applications.

- [YourKit Java Profiler](https://www.yourkit.com/java/profiler/)
- [YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/)
- [YourKit YouMonitor](https://www.yourkit.com/youmonitor/)

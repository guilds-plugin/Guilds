# Guilds

[![Discord](https://discordapp.com/api/guilds/164280494874165248/widget.png?style=banner2)](https://helpch.at/discord)
[![Crowdin](https://badges.crowdin.net/guilds/localized.svg)](https://crowdin.com/project/guilds)
[![Javadocs](https://img.shields.io/badge/API-docs-blue)](https://guilds-plugin.github.io/javadocs/)

Guilds turns your server into a place where players gather around something bigger than a single account. They start a guild, bring friends in, earn ranks, claim land, save up money together, and go to war with a rival group. All of it lives in one command, and players never have to learn a second plugin to take part.

If you are a player, the [wiki](https://wiki.helpch.at/) is the better read. This page is for server owners deciding whether to install it.

## What your players get

**A group with an identity.** A name, a prefix that shows up in chat, a colour, and a message of the day. Guild chat has its own channel, separate from alliance chat, so the two never bleed into each other.

**A reason to keep playing together.** Ranks give officers something to climb toward, a shared bank lets the group pool money for upgrades, and land claims give the guild somewhere safe to build.

**Things to do together.** Guild wars pit two groups against each other in an arena you set up. Alliance status lets two guilds help each other without merging. Invite codes let someone join without an officer online to approve it.

**Progress that persists.** Upgrades, ranks, land, and the bank all survive restarts, and everything is editable from one place.

## Requirements

You need a few things in place before Guilds will start.

| Requirement | Notes |
| --- | --- |
| Java 11 or newer | Required at runtime, even on older Minecraft |
| Vault | Required. Guilds will not enable without it |
| An economy plugin | Required, reached through Vault (EssentialsX works) |
| A permissions plugin | Required, reached through Vault (LuckPerms works) |

Guilds checks for all three at startup and disables itself with a clear message if any are missing, so a failed install tells you why rather than misbehaving later.

### Java version by Minecraft version

This trips people up. The Minecraft version and the Java version are separate choices, and Java 11 is the floor no matter how old your Minecraft is.

| Minecraft / Paper | Java |
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

### Optional integrations

| Plugin | What it adds | Without it |
| --- | --- | --- |
| WorldGuard | Land claims | Claim commands do nothing |
| PlaceholderAPI | `%guilds_*%` placeholders | Placeholders return empty |
| Essentials | Chat and economy compatibility | Everything else still works |

## Installation

1. Install Vault, plus an economy plugin and a permissions plugin.
2. Drop the Guilds jar in `plugins/`.
3. Start the server. Guilds writes its config files and prints its logo.
4. Grant your staff the `guilds.admin` permission.

On the very first start, Guilds pulls its Kotlin runtime down from Maven Central rather than bundling it, which keeps the download small. If your server has no outbound network access, mirror those dependencies ahead of time.

## Configuration

Four files land in `plugins/Guilds/`, and you can edit any of them.

| File | What it controls |
| --- | --- |
| `config.yml` | Everything else: claims, cooldowns, costs, chat, storage, the command alias |
| `tiers.yml` | The upgrade ladder, what each rank costs, and the limits attached to it |
| `roles.yml` | Ranks and the permissions each one grants |
| `buffs.yml` | Purchasable guild buffs |

A `languages/` folder sits alongside them with 29 translations. Players pick their own with `/guild language`, and `/guild reload` picks up edits to `config.yml` and `buffs.yml`.

Tiers do not have to be numbered without gaps. If you delete one from the middle of `tiers.yml`, guilds below it skip to the next tier that exists.

### Storage

Guilds saves to JSON by default and can write to MySQL, MariaDB or SQLite instead. A mistyped `storage-type` gets you a loud warning at startup rather than a silent second database.

## Commands

Everything runs under one command. Out of the box it answers to `/guild`, `/guilds` and `/g`, and you can change that with `settings.plugin-aliases`.

The commands players use most:

| Command | What it does |
| --- | --- |
| `/guild create <name>` | Start a guild |
| `/guild invite <player>` | Invite someone |
| `/guild accept` | Take an invite |
| `/guild list` | See every guild |
| `/guild info` | Open the guild info window |
| `/guild members` | See who's in the guild |
| `/guild promote` / `demote` | Move a member up or down the ranks |
| `/guild bank deposit` / `withdraw` | Pool money |
| `/guild claim` | Claim land |
| `/guild upgrade` | Move up a tier |
| `/guild vault` | Open the shared vault |
| `/guild buff` | Open the buff shop |
| `/guild home` / `sethome` | Manage the guild home |
| `/guild chat` / `ac` | Toggle guild or alliance chat |
| `/guild war challenge` | Start a war with a rival |
| `/guild code create` | Make a join code |
| `/guild code redeem <code>` | Join with a code |

Two commands cover most of the rest: `/guild help` lists what your server has enabled, and `/guild status` shows your rank, balance and tier.

<details>
<summary>Every admin command</summary>

| Command | What it does |
| --- | --- |
| `/guild admin addplayer` / `removeplayer` | Add or remove a member by hand |
| `/guild admin transfer <guild> <player>` | Hand a guild to someone else |
| `/guild admin rename <guild> <name>` | Rename a guild |
| `/guild admin remove <guild>` | Delete a guild |
| `/guild admin prefix <guild> <prefix>` | Change the chat prefix |
| `/guild admin motd set` / `remove` | Manage the message of the day |
| `/guild admin status <guild> <status>` | Set whether a guild is recruiting |
| `/guild admin upgrade <guild>` | Upgrade without charging the bank |
| `/guild admin bank deposit` / `withdraw` / `balance` | Move money in or out by hand |
| `/guild admin sethome` / `home` / `delhome` | Manage homes from outside the guild |
| `/guild admin claim` / `unclaim` | Claim and release land |
| `/guild admin vault [number]` | Open a guild vault |
| `/guild admin give <player> <amount>` | Hand out upgrade tickets |
| `/guild admin spy` | Watch guild chat |
| `/guild admin score setwins` / `setloses` / `resetall` | Adjust war records |
| `/guild arena create` / `delete` / `list` | Manage war arenas |
| `/guild arena set challenger` / `defender` | Set the two spawn points |
| `/guild arena tp` | Jump to an arena spot |
| `/guild console backup` | Back up all guild data |
| `/guild console migrate <backend>` | Move data between storage backends |
| `/guild console unclaimall` | Release every claim on the server |

</details>

## Permissions

Permissions follow a predictable shape, so you can grant them by rank or hand them out one at a time.

| Node | Grants |
| --- | --- |
| `guilds.group.member` | Everything a normal member needs |
| `guilds.command.<name>` | A single command |
| `guilds.admin` | The whole `admin` section |

Ranks can carry permissions too, which is the usual way to do this: set a rank's nodes in `roles.yml` and Guilds applies them as players move up and down.

## Placeholders

With PlaceholderAPI installed you get `%guilds_*%` placeholders for scoreboards, tab lists, chat formats and anything else that reads placeholders.

| Placeholder | Shows |
| --- | --- |
| `%guilds_name%` | Guild name |
| `%guilds_prefix%` | Chat prefix |
| `%guilds_master%` | Guild master's name |
| `%guilds_member_count%` | Members on the roster |
| `%guilds_members_online%` | Members online now |
| `%guilds_tier%` / `%guilds_tier_name%` | Current tier |
| `%guilds_balance%` | Bank balance |
| `%guilds_role%` | Your rank |
| `%guilds_formatted%` | The configured chat format |

## Translations

Messages ship in 29 languages. Players choose their own with `/guild language`, so a server can run in one language while individuals read theirs in another.

Adding or correcting a translation happens on [Crowdin](https://crowdin.com/project/guilds), and you do not need to touch the plugin to do it.

## Telemetry

Guilds reports anonymous usage stats through [bStats](https://bstats.org/). Nothing about your players is collected, and the plugin works exactly the same either way.

## Getting help

Something not working, or a question about setup? The [Discord](https://helpch.at/discord) is the fastest way to get an answer, and the [wiki](https://wiki.helpch.at/) has the detailed guides.

If you want to build on top of Guilds, the [Javadocs](https://guilds-plugin.github.io/javadocs/) cover the API, including the events other plugins can listen for and cancel.

## Contributing

Bug reports with reproduction steps are worth more than anything else. Compatibility testing across Minecraft versions is a real gap and always welcome, as are translation fixes, documentation improvements, and PRs against open [issues](https://github.com/guilds-plugin/Guilds/issues).

To build a jar:

```bash
./gradlew clean shadowJar
```

It lands in `build/libs/`. Run `./gradlew check` before opening a pull request.

## Add-ons

- [GuildClaimsAddon](https://github.com/Nerumir/GuildClaimsAddon) by Nerumir, an alternative implementation of the claiming system

## License

Released under the MIT License. See [LICENSE](LICENSE).
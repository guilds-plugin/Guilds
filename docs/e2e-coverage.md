# E2E coverage inventory

Which Guilds feature is tested where, and what is deliberately left out. See `e2e-testing.md` for
how to run any of it.

133 tests across 11 spec files, plus 3 preflight tests that run before every suite and fail the run
if Guilds or any of its hard dependencies did not load.

## Preflight — `plugins/guilds-preflight.spec.ts`

Runs first, every time. A failure here means the environment is broken and nothing after it is
meaningful.

| Check | Asserts |
| --- | --- |
| Guilds and its hard dependencies are enabled | Vault, EssentialsX, LuckPerms and Guilds are all enabled in the Bukkit plugin list |
| The economy and permission providers Vault needs are registered | Vault's `Economy` and `Permission` services resolve |
| Guilds commands are reachable and its language is English | `/guild` answers, and does so in English |

## A. Guild lifecycle — `tests/lifecycle.spec.ts` (19)

| Requirement | Test |
| --- | --- |
| Successful guild creation | creates a guild, charges for it, and shows it in the guild list |
| Creation confirmation and cancellation | cancelling the prompt leaves no guild and no charge |
| Invalid guild names and prefixes | rejects a guild name that breaks the configured requirements / rejects a name too long to serve as its own prefix / rejects an explicit prefix that breaks the prefix requirements |
| Duplicate guild names | rejects a guild name another guild already holds |
| Insufficient funds | rejects creation when the player cannot afford it |
| Creating while already in a guild | rejects creation while the player is already in a guild |
| Guild renaming | renames a guild / rejects a rename to a name another guild already holds / allows a guild to keep the name it already has |
| Guild deletion and its confirmation | deletes a guild once the prompt is confirmed / cancelling the delete prompt keeps the guild |
| Status and information commands | toggles the guild between public and private / changes the guild prefix / sets, shows and clears the guild MOTD / reports guild membership through the guild list and rejects guild-scoped commands elsewhere |
| Name blacklist | rejects a guild name that is a substring of a blacklisted word / accepts a guild name that contains a blacklisted word |

## B. Membership management — `tests/membership.spec.ts` (18)

| Requirement | Test |
| --- | --- |
| Inviting another player | an invited player accepts and both sides see it happen |
| Receiving and accepting | an invited player accepts and both sides see it happen |
| Declining an invitation | an invited player can decline and stays without a guild |
| Preventing duplicate invitations | a player cannot be invited twice / a player who is already in a guild cannot be invited |
| Preventing joining when already in another guild | a player cannot join a second guild while in one / a player cannot accept a guild they were never invited to |
| Leaving a guild | a member can leave and the guild is told |
| Master leaving deletes the guild | a member leaving warns that the guild master deletes the guild by leaving |
| Kicking a member | the guild master can kick a member and both see the outcome |
| Preventing unauthorized kicks | a member without the kick role cannot kick, and cannot be promoted by doing so / the kick command only accepts a name from the guild roster |
| Promoting and demoting | promotion and demotion change what a member may do / promotion is refused when there is no higher role to move into / a member without the promote role is stopped before the command runs |
| Transferring ownership | ownership can be transferred, and the old master loses the master role |
| Guild capacity | a full guild refuses another member |
| Permissions after leaving or being removed | a player who leaves loses the permissions their guild granted them / a player who is kicked loses the permissions their guild granted them |

## C. Roles and permissions — `tests/roles.spec.ts` (9)

| Requirement | Test |
| --- | --- |
| Default member permissions | a plain member is refused the commands their role does not carry / a member may still do the things their role does carry |
| Guild master permissions | a member can run the admin commands their tier grants, but not the role-gated ones |
| Promotion and demotion effects | covered in `membership.spec.ts`, where the promoted player actually attempts the newly opened command |
| Commands restricted by guild role | a plain member is refused the commands their role does not carry |
| Commands restricted by Bukkit permission nodes | a Bukkit permission node can close a command the role allows (LuckPerms) |
| Unauthorized members cannot perform admin actions | a member can run the admin commands their tier grants, but not the role-gated ones |
| Tier permissions after upgrade | upgrading charges the tier price and moves the guild onto the new tier / **an upgrade leaves the master with the permissions the new tier grants** |
| A player in no guild | a player in no guild is told so rather than refused by role |
| Unreachable admin route | an admin bank deposit of a named guild is refused by the argument check / the same admin command works through the player-facing bank commands |

## D. Guild bank and upgrades — `tests/bank.spec.ts` (15)

| Requirement | Test |
| --- | --- |
| Bank balance reporting | a new guild starts with an empty bank |
| Successful deposits | a deposit moves money from the player into the bank |
| Successful withdrawals | a withdrawal moves money back out of the bank |
| Insufficient player funds | a deposit the player cannot afford is refused and moves nothing |
| Insufficient guild funds | a withdrawal larger than the bank is refused and moves nothing |
| Withdrawal permission enforcement | via roles: a Bukkit permission node can close a command the role allows |
| Bank limits | a deposit over the tier maximum is refused / a deposit that lands exactly on the tier maximum is allowed |
| Successful upgrades | an upgrade costs the bank, not the player, and is refundable by cancelling |
| Upgrade costs and deductions | an upgrade costs the bank, not the player, and is refundable by cancelling (bank before and after) |
| Insufficient funds for upgrades | an upgrade the bank cannot pay for is refused |
| Maximum-tier handling | a guild at the top tier cannot upgrade again |
| Rounding | a fractional amount is rounded the way Guilds rounds it |
| Invalid amounts | a negative amount is refused / an amount that is not a number is refused as a syntax error |
| Notification scope | the whole guild is told about a transaction, not just the player who made it |

## E. Guild homes — `tests/homes.spec.ts` (8)

| Requirement | Test |
| --- | --- |
| Setting a home | a home can be set, teleported to and deleted |
| Teleporting to the home | a home can be set, teleported to and deleted (asserted with `toBeNear` on the bot's position) |
| Deleting the home | a home can be set, teleported to and deleted |
| No home set | a home can be set, teleported to and deleted (the tail asserts `/guild home` is then refused) |
| Permission restrictions | a member without the change-home role cannot set or delete a home |
| Teleporting without the role | a member without the change-home role cannot set or delete a home (going *is* allowed) |
| Teleport from the GUI | the info GUI shows the home and teleports to it |
| No guild | a player in no guild cannot use the home commands |
| Cooldowns | a second home cannot be set until the cooldown expires / a second teleport home cannot happen until the cooldown expires / the home cooldown is per player, not per guild |

## F. Guild chat — `tests/chat.spec.ts` (8)

| Requirement | Test |
| --- | --- |
| Guild-only chat delivery | guild chat reaches the other member |
| Alliance chat delivery | ally chat reaches an allied guild and not the third party |
| Outsiders do not receive private messages | guild chat does not reach a player outside the guild |
| Chat channel toggling | toggling the guild channel on routes plain chat into the guild |
| Messages reach the intended players | guild chat reaches the other member / ally chat reaches an allied guild and not the third party |
| Behaviour after leaving | a player who left the guild no longer receives its chat |
| Formatting | a member can speak for the guild, and the message is formatted for their role / the chat format carries the configured guild label |
| No guild | guild chat is refused for a player in no guild |

## G. Alliances — `tests/alliances.spec.ts` (10)

| Requirement | Test |
| --- | --- |
| Sending alliance requests | an alliance request is sent, received and accepted |
| Accepting requests | an alliance request is sent, received and accepted |
| Declining requests | an alliance request can be declined / declining an alliance that was never requested changes nothing |
| Listing alliances | an alliance can be listed and then removed from either side |
| Removing alliances | an alliance can be listed and then removed from either side |
| Invalid or duplicate requests | a duplicate alliance request is refused / an alliance that already exists cannot be requested again / a guild cannot ally with itself / removing a guild that is not an ally is refused |
| Alliance permissions | a member without the alliance role cannot manage alliances |
| Alliance chat between guilds | `chat.spec.ts`, ally chat reaches an allied guild and not the third party |
| Cleanup on deletion | a deleted guild drops out of its allies |

## H. Invite codes — `tests/codes.spec.ts` (12, of which 5 are one `describe.serial` chain)

| Requirement | Test |
| --- | --- |
| Creating invite codes | a code can be created, listed and deleted |
| Viewing and listing codes | a code can be created, listed and deleted / *and it is listed for the guild master* |
| Redeeming a code | a code can be redeemed and puts the player in the guild |
| Deleting codes | a code can be created, listed and deleted |
| Invalid codes | an invalid code is refused |
| Reusing codes where restricted | a code runs out of uses and then says so |
| Permission restrictions | code information is restricted by role / *a member can be added and cannot manage codes* |
| Full lifecycle | *one code through its whole life* — a `describe.serial` chain: create → listed → redeem → deleted, asserting membership at each step rather than the command's reply |

## I. Inventory and GUI functionality — `tests/gui.spec.ts` (18)

Every Guilds GUI, driven through Plugwright's live GUI handles and locators. Expected names and lore
are read from the staged `config.yml`/`buffs.yml` via `plugins/support/expected.ts`, not hardcoded.

| GUI | Tests |
| --- | --- |
| Guild information | the info GUI renders the guild it was opened for / the info GUI follows the guild it was showing / the info GUI navigates to the members GUI and back / the info GUI is reachable and shows a home once one is set |
| Guild list | the guild list shows every guild with its master and tier / clicking a guild in the list opens its members |
| Member list | the members GUI renders the roster and its navigation |
| Guild vault | the vault picker lists the vaults the tier unlocks / a locked vault is marked as locked and cannot be opened / a vault holds items and gives them back |
| Vault blacklist | a blacklisted item is refused by the vault |
| Guild buffs | the buff GUI shows a locked buff and refuses to sell it / a buff can be bought once the permission is granted / a buff the guild bank cannot pay for is refused / a guild that has just bought a buff is on cooldown for the next one / a member without the buff role cannot open the buff GUI |
| Navigation | the info GUI navigates to the members GUI and back / clicking a guild in the list opens its members |
| Permission-dependent visibility | the buff GUI shows a locked buff and refuses to sell it / a member without the buff role cannot open the buff GUI |
| Read-back agreement | the guild list read agrees with what the GUI shows / the members read agrees with what the GUI shows |

## J. Cooldowns and validation — `tests/cooldowns.spec.ts` (11)

| Requirement | Test |
| --- | --- |
| Commands blocked during a cooldown | a second home cannot be set until the cooldown expires / a second teleport home cannot happen until the cooldown expires / a second join request cannot be sent until the cooldown expires |
| Successful actions after expiry | the same three, each re-issuing the command after the plugin's own reported time is up |
| Cooldown scope | a cooldown is keyed by player, not by guild |
| Invalid command arguments | a negative amount is refused / a guild-only command refuses a player in no guild |
| Missing required arguments | a missing argument is reported rather than acted on |
| Nonexistent players or guilds | a player the server does not know cannot be named as a target / a guild that does not exist is refused by name |
| Attempts to perform restricted actions | a guild-only command refuses a player in no guild / confirm and cancel with nothing pending are refused |
| Subcommand dispatch | a command with only a parent subcommand lists its children |

Other cooldowns are tested where the feature lives: `homes.spec.ts` for `sethome` and `home`,
`gui.spec.ts` for the per-guild buff cooldown.

## K. Language selection — `tests/language.spec.ts` (5)

| Requirement | Test |
| --- | --- |
| The language selection command | a player can switch language and gets the translated reply / a player in a guild is told so in the language they chose |
| A translated response afterwards | a player can switch language and gets the translated reply (a later `/guild info` answers in French) |
| Isolation from unrelated assertions | one player switching language leaves everyone else in English / the language actually reverts, not just the acknowledgement |
| Invalid input | an unknown language is refused by the command completion |

Every test switches only its own bot and restores `en-US` in a `finally`, so a failure cannot leave
the server answering in another language for the rest of the run. ACF's `usePerIssuerLocale(true,
false)` is what makes that safe, and one player switching language leaves everyone else in English
asserts it rather than assuming it.

## Fixture self-check — `tests/bank.spec.ts`

`STARTING_BALANCE is what the fixtures actually fund` is not a Guilds test. It checks that the
fixture's stated constants match what the server reports, so that every other test's arithmetic
about balances rests on a fact rather than on an assumption.

## Deferred, and why

Out of scope for this phase, as agreed:

| Area | Why it is not here |
| --- | --- |
| WorldGuard land claims and region protection | Needs a WorldGuard server and region fixtures; nothing in Guilds' claim commands is reachable without one |
| Guild wars and the arena | Arena setup is a separate orchestration with its own timing and no command-level entry point that does not first require a challenge |
| Database backend compatibility | One environment runs JSON storage. Exercising SQL/MariaDB needs a second provisioned server per backend, which is a matrix, not a suite |
| Server restart and migration | The suite assumes a freshly provisioned server and never restarts one; migration testing needs a stop/start harness the runner does not currently expose |
| Internal API and event inspection | Deliberately excluded: the suite asserts on player-visible outcomes only. Reaching into the API would make tests pass on internals that a player would never see |
| PlaceholderAPI | Needs a second plugin and a way to read resolved placeholders as a bot; there is no command surface for it |
| Historical Minecraft versions | One environment (1.21.8 / Java 21). The build pins `e2eMinecraftVersion` as a property, so a matrix is a matter of adding environments once one is proven |
| Spy mode | `GuildHandler#isSpy` has no command or GUI surface a bot can exercise |
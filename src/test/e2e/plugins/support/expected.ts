/**
 * Every player-visible string the suite asserts on, in one place.
 *
 * Each value is derived from Guilds' own sources — `src/main/resources/languages/en-US.yml` for
 * messages, and `src/test/e2e/server/plugins/Guilds/*.yml` for GUI titles — with the `&`
 * colour codes dropped, because that is how the strings arrive in a bot's chat buffer.
 *
 * Nothing here hardcodes an escape sequence or a § sign. When Guilds reworded a message or the
 * staged config renamed a GUI, this file is the only thing that has to change.
 */

/**
 * GUI titles. Each is a distinctive *fragment* of the configured title rather than the whole
 * thing, so the `{name}`/`{guild}` placeholders the GUIs interpolate never have to be
 * reconstructed by the test.
 */
export const GUI = {
    /** guis.guild-info.name — "&8» &r{name}'s Info" */
    info: "'s Info",
    /** guis.guild-info-members.name — "&8» &rMembers of {name}" */
    members: 'Members of',
    /** guis.guild-list.gui-name — "Guild List" */
    list: 'Guild List',
    /** guis.vault-picker.name — "&8» &r{name}'s Vaults" */
    vaultPicker: "'s Vaults",
    /** guis.vault.name — "&8» &rGuild Vault" */
    vault: 'Guild Vault',
    /** buffs.yml guild-buffs.gui-name — "Guild Buffs" */
    buffs: 'Guild Buffs',
} as const;

/** Item names/lore fragments the GUIs render, from the same config files. */
export const ITEM = {
    /** guis.guild-info.members-name */
    infoMembers: 'Guild Members',
    /** guis.guild-info.bank-name */
    infoBank: 'Guild Bank',
    /** guis.guild-info.tier-name */
    infoTier: 'Guild Tier',
    /** guis.guild-info.status-name-item */
    infoStatus: 'Guild Status',
    /** guis.guild-info.vault-name */
    infoVaults: 'Guild Vaults',
    /** guis.guild-info.home-empty, shown when no home is set */
    homeEmpty: 'Not Set',
    /** guis.vault-picker.unlocked / .locked */
    vaultUnlocked: 'Unlocked',
    vaultLocked: 'Locked',
    /** guis.guild-list.next-page-item-name */
    nextPage: 'Next Page',
    /** guis.guild-list.previous-page-item-name */
    previousPage: 'Previous Page',
} as const;

/**
 * Message fragments from `languages/en-US.yml`, with `&` codes removed.
 *
 * Values taking arguments are functions, because Guilds substitutes `{player}`/`{guild}`/
 * `{amount}` before the message is sent and the test has to expect the substituted form.
 */
export const MSG = {
    // create
    createWarning: 'The creation of a Guild cost',
    created: (guild: string) => `Guild '${guild}' created successfully!`,
    createCancelled: 'Guild creation cancelled!',
    nameTaken: 'This name is already taken!',
    nameRequirements: "Your guild's name does not match the requirements",
    nameTooLong: "You didn't provide a prefix",
    prefixTooLong: 'The guild prefix provided did not match the requirements',

    // error
    notEnoughMoney: 'you do not have enough money to do that',
    alreadyInGuild: "You're already in a guild!",
    noGuild: "You're not in a guild!",
    roleNoPermission: 'Your role is not high enough to do that!',
    tierNoPermission: 'Your guild tier is not high enough to do that!',
    guildNoExist: 'That Guild does not exist!',
    playerNotFound: (player: string) => `Player '${player}' is not online!`,
    playerNotInGuild: (player: string) => `${player} is not in your Guild!`,
    /**
     * error.player-no-exist. No test asserts this arrives, for the same reason
     * `guildNoExist` has none: the command completions only ever offer names the server knows, so
     * ACF refuses the argument before the handler can. Kept because it documents the branch that
     * is currently unreachable, and because a test that ever does reach it will want it here.
     */
    playerNoExist: (player: string) => `it doesn't look like that player has ever been on the server`,
    blacklist: 'is on the blacklist!',

    // confirm / cancel
    confirmError: 'You have no actions to confirm!',
    confirmSuccess: 'Action confirmed!',
    cancelError: 'You have no actions to cancel!',
    cancelSuccess: 'Action cancelled!',

    // invite / accept / decline
    inviteSent: (player: string) => `You've successfully invited ${player} to your guild!`,
    inviteReceived: (guild: string) => `has invited you to his/her guild, '${guild}'`,
    inviteAlreadyInvited: 'User is already invited to your guild',
    inviteAlreadyInGuild: (player: string) => `${player} is already in another guild!`,
    acceptSuccess: (guild: string) => `You joined guild '${guild}' successfully`,
    acceptNotInvited: "This guild is private / you haven't received an invite from the guild.",
    acceptGuildFull: 'This guild is full!',
    acceptPlayerJoined: (player: string) => `${player} has joined your guild!`,
    acceptCooldown: 'You are currently on cooldown from joining a guild',
    declineSuccess: 'Guild Invite Declined!',
    noPendingInvites: 'you currently do not have any pending guild invites',
    pendingInvites: 'pending invite(s) from the guild(s)',
    requestSuccess: (guild: string) => `You've successfully requested an invite from ${guild}`,
    requestIncoming: (player: string) => `${player} is requesting to join the guild.`,
    /** request.cooldown — "{time}" is the seconds Guilds has left. */
    requestCooldown: "You can't send another request for",

    // leave / kick / roles
    leaveWarning: 'Type /guilds confirm to leave your guild',
    leaveWarningMaster: "You're the Guild Master of this guild",
    leaveSuccess: "You've successfully left your guild!",
    playerLeft: (player: string) => `Player '${player}' left your guild!`,
    guildMasterLeft: 'The Guild Master,',
    kickSuccess: (player: string) => `Successfully kicked ${player} from your guild!`,
    kickedBy: (kicker: string) => `You have been kicked from your guild by ${kicker}!`,
    kickedNotice: (player: string, kicker: string) =>
        `Player '${player}' has been kicked from the guild by ${kicker}!`,
    promoteSuccess: (player: string) => `You've successfully promoted ${player} from`,
    demoteSuccess: (player: string) => `You've successfully demoted ${player} from`,
    promotedNotice: (by: string) => `You've been promoted from`,
    demotedNotice: () => `You've been demoted from`,
    cantPromote: 'That player can not be promoted!',
    cantDemote: 'That player can not be demoted!',
    transferSuccess: 'Guild has been transferred!',
    newMaster: 'You are the new Guild Master of your Guild!',

    // bank
    bankBalance: 'Your guild\'s bank has a balance of',
    depositSuccess: (player: string, amount: string) =>
        `${player} has just deposited ${amount} into the Guild Bank.`,
    withdrawSuccess: (player: string, amount: string) =>
        `${player} has just withdrawn ${amount} from the Guild Bank.`,
    notEnoughBank: "There isn't enough in the Guild Bank to do that",
    bankOverMax: 'you would go over your max bank balance',

    // upgrade
    upgradeMoneyWarning: 'You are about to spend',
    upgradeSuccess: "You've successfully upgraded your guild!",
    upgradeTierMax: "Cannot upgrade again! You're already maxed!",
    upgradeNotEnoughMoney: "don't have enough money to upgrade your Guild",

    // rename / prefix / status / delete
    renamed: (name: string) => `Guild name changed to ${name}!`,
    prefixChanged: (prefix: string) => `Guild's prefix changed successfully to ${prefix}!`,
    statusChanged: (status: string) => `Guild status set to ${status}!`,
    deleteWarning: 'Type /guilds confirm to delete your guild',
    deleteSuccess: (guild: string) => `Deleted '${guild}' successfully!`,
    deleteCancelled: 'Guild deletion cancelled!',

    // homes
    sethomeSuccess: "You've set your guild home!",
    sethomeDeleted: "You've successfully deleted your guild home!",
    sethomeCooldown: 'You must wait at least',
    homeNoHomeSet: 'No home set!',
    homeTeleported: "You've teleported to your guild home!",
    homeCooldown: 'You must wait at least',

    // chat channels
    chatToggledOn: (type: string) => `${type} chat toggled on.`,
    chatToggledOff: (type: string) => `${type} chat toggled off.`,
    guildChatLabel: '[Guild Chat]',
    allyChatLabel: '[Ally Chat]',

    // alliances
    allyInviteSent: (guild: string) => `You've successfully sent an ally invite to ${guild}!`,
    allyIncoming: (guild: string) => `You have an incoming ally request from ${guild}!`,
    allyAccepted: (guild: string) => `You've accepted an ally request from ${guild}`,
    allyTargetAccepted: (guild: string) => `Your ally request to ${guild} has been accepted!`,
    allyDeclined: (guild: string) => `${guild}'s ally request has been denied!`,
    allyTargetDeclined: (guild: string) => `Your ally request to ${guild} has been denied!`,
    allyList: 'Your Guild has the following allies:',
    allyNone: 'You have no allies!',
    allyAlready: 'That guild is already your ally!',
    allySameGuild: "You can't send an ally invite to yourself!",
    allyNotAllied: "You can't remove a guild that isn't on your ally list!",
    allyAlreadyRequested: 'You currently already have a pending ally request to this guild',
    allyRemoved: (guild: string) => `You have removed ${guild} from your ally list!`,
    /** delete.notify-allies — what a partner is told when a guild is removed. */
    deleteNotifyAlly: (guild: string) => `${guild} is no longer your ally because it has been deleted.`,
    allyTargetRemoved: (guild: string) => `${guild} has removed you from their ally list!`,
    allyMax: 'you already have the max allies allowed for your tier',

    // invite codes
    codeCreated: "You've created a new invite code:",
    codeListHeader: 'Here are the following active invite codes for your guild:',
    codeEmpty: 'I have no codes, try making one first',
    codeDeleted: 'That invite code has been removed from your guild!',
    codeInvalid: 'That invite code does not seem to exist.',
    codeJoined: (guild: string) => `You've successfully joined ${guild} using an invite code!`,
    codeOut: 'That invite code is all out of uses!',
    codeMax: 'You already have too many active invite codes in your guild!',

    // motd
    motdNotSet: 'Your guild currently does not have a MOTD set',
    /** motd.motd — "&7[&aGuild MOTD&7] {motd}", so only the badge is a fixed fragment. */
    motdView: () => '[Guild MOTD]',
    motdSet: (motd: string) => `You've successfully set your guild's MOTD to ${motd}`,
    motdRemove: "You've successfully removed your guild's MOTD.",

    // buffs
    buffNoPermission: 'you do not have permission to buy this buff',
    buffCooldown: 'your guild has recently bought a buff',

    // vault
    vaultMaxed: 'you already have the max amount of vaults for your Guild Tier',
    vaultBlacklisted: 'is blacklisted',

    // language
    languageSet: (language: string) => `You've successfully set your language to ${language}`,

    /**
     * ACF's `@Values` rejection: "Error: Please specify one of (<completion>)."
     *
     * The completion name is part of the sentence and differs per command, so it is a parameter.
     * This is what stops an unknown player from being named as a target — the `@players` list only
     * contains names the server has seen, so a player who has never joined is rejected here rather
     * than by the handler's own "player not found" branch.
     */
    valueRejected: (completion: string) => `Please specify one of (@${completion})`,

    // ACF's own messages, overridden by the acf-core section of en-US.yml
    /** invalid-syntax — the "Usage:" line ACF builds from a command's `@Syntax`. */
    invalidSyntax: 'Usage:',
    permissionDenied: 'you do not have permission to perform this command',
} as const;

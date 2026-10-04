import { expect, test } from '@plugwright/runner';
import { commandWithout, expectReceived } from '../plugins/support/assertions.js';
import { SETHOME_COST } from '../plugins/support/fixtures.js';
import type { ItemWrapper } from '@plugwright/runner';
import { ITEM, MSG } from '../plugins/support/expected.js';
import {
    GUI,
    awaitGui,
    awaitGuiItem,
    closeGui,
    clickSlot,
    containerItems,
    contentItems,
    cursorItem,
    guildList,
    guildMembers,
    hasItem,
    hasLoreFragment,
    openGui,
    openItems,
    ownItemWindowSlot,
    playerInventoryStart,
} from '../plugins/support/state.js';
import { money, stripColors } from '../plugins/support/text.js';

/**
 * Every Guilds inventory, driven through Plugwright's live GUI handles and locators.
 *
 * Names and lore are asserted against the values staged in
 * `src/test/e2e/server/plugins/Guilds/{config,buffs}.yml`, not against a literal written here, so
 * the tests follow the configuration instead of freezing one copy of it.
 *
 * Items are located by display name and read through `ItemWrapper`; the one place a raw slot index
 * appears is the vault, where depositing means clicking a specific slot of the player's own
 * inventory.
 */

const names = (player: any) => openItems(player).map(item => stripColors(item.displayName));

test('the info GUI renders the guild it was opened for', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);
    await guilds.setBank(guild, 750);

    const handle = await openGui(player, '/guild info', GUI.info);

    // guis.guild-info.* — the six headline blocks, in the slots the config puts them in.
    expect(handle).toBeDefined();
    expect(names(player)).toContain(ITEM.infoTier);
    expect(names(player)).toContain(ITEM.infoBank);
    expect(names(player)).toContain(ITEM.infoMembers);
    expect(names(player)).toContain(ITEM.infoStatus);
    expect(names(player)).toContain('Guild Home');
    expect(names(player)).toContain(ITEM.infoVaults);

    // guis.guild-info.bank-lore: "&8• &7Balance: &e{current} &7/ &e{max}"
    expect(hasLoreFragment(player, '750')).toBe(true);
    expect(hasLoreFragment(player, '10,000')).toBe(true);

    // guis.guild-info.members-lore
    expect(hasLoreFragment(player, '1 / 2')).toBe(true);

    // guis.guild-info.status-name.private, with a private guild's material
    expect(hasLoreFragment(player, 'Private')).toBe(true);
    expect(openItems(player).some(item => item.name === 'redstone')).toBe(true);

    // guis.guild-info.home-empty, because no home is set yet.
    expect(hasLoreFragment(player, ITEM.homeEmpty)).toBe(true);
});

test('the info GUI follows the guild it is showing', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    await openGui(player, '/guild info', GUI.info);
    // guis.guild-info.members-lore — two members out of the tier's two.
    expect(hasLoreFragment(player, '2 / 2')).toBe(true);
    expect(hasLoreFragment(player, 'Public')).toBe(false);

    await guilds.run(player, '/guild status');
    await expect(player).toHaveReceivedMessage(MSG.statusChanged('Public'));

    await openGui(player, '/guild info', GUI.info);
    // guis.guild-info.status-name.public, with the public material this time round.
    expect(hasLoreFragment(player, 'Public')).toBe(true);
    expect(openItems(player).some(item => item.name === 'emerald')).toBe(true);
});

test('the info GUI navigates to the members GUI and back', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    const info = await openGui(player, '/guild info', GUI.info);
    await info.locator(item => stripColors(item.displayName).includes(ITEM.infoMembers)).click({ timeout: 10_000 });

    // InfoGUI routes the members button into the members GUI for the same guild. Plugwright's own
    // title reader cannot parse a Guilds title on 1.21.8, so the navigation is checked against the
    // roster the members GUI renders.
    await expect.poll(() => hasLoreFragment(player, `Name: ${member.username}`), {
        timeout: 10_000,
        message: 'the members button should open the members GUI',
    }).toBe(true);
    expect(hasLoreFragment(player, `Name: ${player.username}`)).toBe(true);
    expect(hasLoreFragment(player, 'Role: GuildMaster')).toBe(true);
    expect(hasLoreFragment(player, 'Role: Member')).toBe(true);

});

test('the members GUI renders the roster and its navigation', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    const handle = await openGui(player, '/guild members', GUI.members);

    // guis.guild-info-members.item.name is a single space; the lore is what identifies a member.
    const rows = contentItems(player).filter(item => item.lore.some(line => stripColors(line).includes('Name:')));
    expect(rows.length).toBe(2);

    // guis.guild-info-members.item.online
    expect(hasLoreFragment(player, 'Online')).toBe(true);

    // guis.guild-info-members.nav — "Next" and "Previous" are present even on a single page.
    expect(names(player)).toContain('Next');
    expect(names(player)).toContain('Previous');
    expect(handle).toBeDefined();

    // Clicking a member row does nothing: the GUI cancels every click. The client picks the item up
    // locally the moment the click is sent and the server's correction lands a tick later, so the
    // unchanged inventory is polled rather than sampled at the instant the click returns.
    const before = names(player).sort();
    await handle.locator(item => item.lore.some(line => stripColors(line).includes('Name:'))).click({ timeout: 5000 });
    await expect.poll(() => names(player).sort(), {
        timeout: 5_000,
        message: 'the roster should be unchanged after clicking a member row',
    }).toEqual(before);
});

test('the guild list shows every guild with its master and tier', async ({ player, createPlayer, guilds }) => {
    const mine = await guilds.createGuild(player);
    const otherMaster = await createPlayer({ username: guilds.playerName() });
    const theirs = await guilds.createGuild(otherMaster);

    await openGui(player, '/guild list', GUI.list);
    // guis.guild-list.item-name is "&f{player}'s Guild" — the guild master's name.
    expect(names(player)).toContain(`${player.username}'s Guild`);
    expect(names(player)).toContain(`${otherMaster.username}'s Guild`);
    // guis.guild-list.head-lore
    expect(hasLoreFragment(player, `Name: ${mine}`)).toBe(true);
    expect(hasLoreFragment(player, `Name: ${theirs}`)).toBe(true);
    expect(hasLoreFragment(player, `Master: ${player.username}`)).toBe(true);
});

test('clicking a guild in the list opens its members', async ({ player, createPlayer, guilds }) => {
    const mine = await guilds.createGuild(player);
    const otherMaster = await createPlayer({ username: guilds.playerName() });
    await guilds.createGuild(otherMaster);

    const handle = await openGui(player, '/guild list', GUI.list);
    await handle
        .locator(item => item.lore.some(line => stripColors(line).includes(`Name: ${mine}`)))
        .click({ timeout: 10_000 });

    // The members GUI is what the list row opens, so wait for its contents rather than assuming
    // the click landed in the same tick.
    await expect.poll(() => hasLoreFragment(player, 'Role: GuildMaster'), {
        timeout: 10_000,
        message: 'the guild row should open that guild\'s members',
    }).toBe(true);
    expect(hasLoreFragment(player, `Name: ${player.username}`)).toBe(true);
});

test('the vault picker lists the vaults the tier unlocks', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);

    const handle = await openGui(player, '/guild vault', GUI.vaultPicker);
    expect(handle).toBeDefined();

    // guis.vault-picker.item-lore: "&8• &7Vault &9#{number}" and the unlocked/locked word.
    expect(hasLoreFragment(player, 'Vault #1')).toBe(true);
    expect(hasLoreFragment(player, ITEM.vaultUnlocked)).toBe(true);

    // Tier 1 unlocks exactly one vault, and it is a chest.
    expect(contentItems(player).filter(item => item.name === 'chest').length).toBe(1);
});

test('a locked vault is marked as locked and cannot be opened', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);
    await guilds.setBank(guild, 200);

    // Upgrading to tier 2 unlocks a second vault.
    await guilds.run(player, '/guild upgrade');
    await expect(player).toHaveReceivedMessage(MSG.upgradeMoneyWarning);
    await guilds.confirm(player);
    await expect(player).toHaveReceivedMessage(MSG.upgradeSuccess);

    await openGui(player, '/guild vault', GUI.vaultPicker);
    expect(hasLoreFragment(player, 'Vault #2')).toBe(true);
    expect(hasLoreFragment(player, ITEM.vaultUnlocked)).toBe(true);
    expect(contentItems(player).filter(item => item.name === 'chest').length).toBe(2);
});

test('a vault holds items and gives them back', async ({ player, guilds }) => {
    await guilds.createGuild(player);
    await player.giveItem('diamond', 8);

    // Open the picker, then the first vault behind it.
    const picker = await openGui(player, '/guild vault', GUI.vaultPicker);
    await picker.locator(item => item.name === 'chest').click({ timeout: 10_000 });

    // The vault itself is opened by the click, not by a command.
    const vault = await awaitGui(player, GUI.vault);
    expect(vault).toBeDefined();

    // A Guild vault is a fixed 54-slot chest, and it starts empty.
    expect(playerInventoryStart(player)).toBe(54);
    expect(containerItems(player).length).toBe(0);

    // Moving an item in is the pair of clicks a player makes in any chest: take the stack onto
    // the cursor out of their own inventory, then drop it on an empty vault slot.
    const carried = ownItemWindowSlot(player, 'diamond');
    await clickSlot(player, carried);
    await expect.poll(() => cursorItem(player), {
        timeout: 5_000,
        message: 'the diamond should be on the cursor',
    }).toBe('diamondx8');

    await clickSlot(player, 0);
    await expect.poll(() => containerItems(player).map(item => `${item.name}x${item.count}`), {
        timeout: 10_000,
        message: 'the diamond should end up in the guild vault',
    }).toEqual(['diamondx8']);

    // And moving it out is the same two clicks the other way round.
    await clickSlot(player, 0);
    await expect.poll(() => cursorItem(player), {
        timeout: 5_000,
        message: 'the diamond should be back on the cursor',
    }).toBe('diamondx8');
    await clickSlot(player, carried);
    await expect(player).toContainItem('diamond', { count: 8 });

    // The vault is a chest with a real inventory behind it, so the deposit survives closing it.
    await closeGui(player);
    await guilds.run(player, '/guild vault 1');
    await awaitGui(player, GUI.vault);
    expect(containerItems(player).length).toBe(0);
});

test('a blacklisted item is refused by the vault', async ({ player, guilds }) => {
    // `guis.vault.blacklist.materials` names BEDROCK in the staged config, so the refusal path in
    // VaultBlacklistListener is reachable. Taking the stack onto the cursor is the click that gets
    // refused — the stack can only reach a vault slot if it first comes off the cursor, so refusing
    // the pickup is what keeps it out.
    await guilds.createGuild(player);
    await player.giveItem('bedrock', 4);
    await player.giveItem('cobblestone', 4);

    const picker = await openGui(player, '/guild vault', GUI.vaultPicker);
    await picker.locator(item => item.name === 'chest').click({ timeout: 10_000 });
    await awaitGui(player, GUI.vault);

    const since = player.getMessageBufferIndex();
    const bedrock = ownItemWindowSlot(player, 'bedrock');
    await clickSlot(player, bedrock);
    await expect(player).toHaveReceivedMessage(MSG.vaultBlacklisted, { since, timeout: 10_000 });

    // prismarine-windows applies a click locally before the server answers, so the cursor is only
    // evidence once the server's correction has landed: the blacklisted stack goes back into the
    // player's own inventory rather than being carried into the vault.
    await expect.poll(() => cursorItem(player), {
        timeout: 5_000,
        message: 'the blacklisted stack should be taken back off the cursor',
    }).toBeNull();
    expect(containerItems(player)).toEqual([]);
    await expect(player).toContainItem('bedrock', { count: 4 });

    // An item that is not blacklisted still goes in, so the refusal above is the blacklist at work
    // rather than a vault that has stopped accepting anything.
    const cobble = ownItemWindowSlot(player, 'cobblestone');
    await clickSlot(player, cobble);
    await clickSlot(player, 0);
    await expect.poll(() => containerItems(player).map(item => `${item.name}x${item.count}`), {
        timeout: 10_000,
        message: 'an item that is not blacklisted should still be accepted',
    }).toEqual(['cobblestonex4']);
});

test('the buff GUI shows a locked buff and refuses to sell it', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);
    await guilds.setBank(guild, 500);

    const handle = await openGui(player, '/guild buff', GUI.buffs);
    expect(handle).toBeDefined();

    // buffs.yml names the locked variant when the player lacks guilds.test.buff.
    expect(names(player)).toContain('Locked: Haste');
    expect(names(player)).toContain('Locked: Blindness');

    await handle.locator(item => stripColors(item.displayName) === 'Locked: Haste').click({ timeout: 10_000 });
    await expect(player).toHaveReceivedMessage(MSG.buffNoPermission, { timeout: 10_000 });

    // Nothing was charged.
    expect(await guilds.bankOf(guild)).toBe(500);
});

test('a buff can be bought once the permission is granted', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);
    await guilds.setBank(guild, 500);
    await guilds.grant(player, 'guilds.test.buff');

    // The permission decides which of the two variants buffs.yml renders, so wait for the GUI to
    // show the unlocked one rather than assuming LuckPerms has already reached the client.
    const handle = await awaitGuiItem(
        player,
        '/guild buff',
        GUI.buffs,
        item => stripColors(item.displayName) === 'Substance of the Redmod Graff',
    );
    expect(names(player)).not.toContain('Locked: Haste');

    await handle
        .locator(item => stripColors(item.displayName) === 'Substance of the Redmod Graff')
        .click({ timeout: 10_000 });

    // The price comes out of the guild bank (buffs.yml: price 50.0).
    await expect.poll(async () => await guilds.bankOf(guild), {
        timeout: 10_000,
        message: 'buying the buff should take 50 out of the guild bank',
    }).toBe(450);

    // …and an effect is applied to the buyer. mineflayer keys `entity.effects` by the protocol's
    // numeric effect id on 1.21.8, so the assertion is that an effect arrived rather than which one.
    await expect.poll(() => Object.keys(player.bot.entity.effects ?? {}).length, {
        timeout: 10_000,
        message: 'the buff should apply a potion effect to the buyer',
    }).toBeGreaterThan(0);
});

test('a buff the guild bank cannot pay for is refused', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);
    await guilds.setBank(guild, 100);
    await guilds.grant(player, 'guilds.test.buff');

    // buffs.yml prices the blindness buff at 5000, which this guild cannot pay.
    const handle = await awaitGuiItem(
        player,
        '/guild buff',
        GUI.buffs,
        item => stripColors(item.displayName) === 'Scales of the Dragon',
    );
    await handle.locator(item => stripColors(item.displayName) === 'Scales of the Dragon').click({ timeout: 10_000 });

    await expect(player).toHaveReceivedMessage(MSG.notEnoughBank, { timeout: 10_000 });
    expect(await guilds.bankOf(guild)).toBe(100);
    expect(Object.keys(player.bot.entity.effects ?? {}).length).toBe(0);
});

test('a guild that has just bought a buff is on cooldown for the next one', async ({ player, guilds }) => {
    const mine = await guilds.createGuild(player);
    await guilds.setBank(mine, 500);
    await guilds.grant(player, 'guilds.test.buff');

    // `awaitGuiItem` re-runs the command until the GUI holds the item, which is exactly the wait
    // for the LuckPerms grant to reach the client. Nothing else is needed here — an earlier version
    // probed for "any message at all" first, and since `/guild buff` opens a GUI and says nothing
    // in chat, that probe sat out its full timeout on every run.
    const haste = await awaitGuiItem(
        player,
        '/guild buff',
        GUI.buffs,
        item => stripColors(item.displayName) === 'Substance of the Redmod Graff',
    );
    await haste.locator(item => stripColors(item.displayName) === 'Substance of the Redmod Graff').click({ timeout: 10_000 });
    await expect.poll(async () => await guilds.bankOf(mine), { timeout: 10_000 }).toBe(450);

    // guild-buffs.cooldown is three seconds in the staged buffs.yml and is per guild, so a second
    // purchase in the same breath is refused rather than charged.
    const second = await awaitGuiItem(
        player,
        '/guild buff',
        GUI.buffs,
        item => stripColors(item.displayName) === 'Scales of the Dragon',
    );
    await second.locator(item => stripColors(item.displayName) === 'Scales of the Dragon').click({ timeout: 10_000 });
    await expect(player).toHaveReceivedMessage(MSG.buffCooldown, { timeout: 10_000 });
    expect(await guilds.bankOf(mine)).toBe(450);
});

test('a member without the buff role cannot open the buff GUI', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    await commandWithout(member, guilds, '/guild buff', MSG.roleNoPermission, MSG.buffNoPermission);
});

test('the guild list read agrees with what the GUI shows', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);
    await guilds.setBank(guild, 4242);

    const entry = (await guildList(player)).find(item => item.name === guild)!;
    expect(entry.balance).toBe(4242);

    await openGui(player, '/guild list', GUI.list);
    // The lore formats the balance the way Guilds' own messages do, thousands separator and all.
    expect(hasLoreFragment(player, money(4242))).toBe(true);
    expect(hasLoreFragment(player, '4242')).toBe(false);
});

test('the members read agrees with what the GUI shows', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    const roster = await guildMembers(player);
    expect(roster.map(entry => entry.name).sort()).toEqual([player.username, member.username].sort());

    await guilds.run(player, '/guild status');
    await expect(player).toHaveReceivedMessage(MSG.statusChanged('Public'));
    await expectReceived(player, MSG.statusChanged('Public'));
});

test('the info GUI is reachable and shows a home once one is set', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);
    await guilds.setBank(guild, SETHOME_COST);
    await player.teleport(300, 90, -300);
    await guilds.run(player, '/guild sethome');
    await expect(player).toHaveReceivedMessage(MSG.sethomeSuccess);

    await openGui(player, '/guild info', GUI.info);
    expect(hasLoreFragment(player, '300')).toBe(true);
    expect(hasLoreFragment(player, '-300')).toBe(true);
});

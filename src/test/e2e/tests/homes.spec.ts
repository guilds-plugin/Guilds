import { expect, test } from '@plugwright/runner';
import { commandWithout, expectNeverSeen, expectReceived } from '../plugins/support/assertions.js';
import { SETHOME_COST } from '../plugins/support/fixtures.js';
import { MSG } from '../plugins/support/expected.js';
import { GUI, ITEM, contentItems, hasLoreFragment, openGui } from '../plugins/support/state.js';
import { secondsRemaining, sleep, stripColors } from '../plugins/support/text.js';

/**
 * Guild homes: setting one costs the bank, teleporting to it moves the player, deleting it works,
 * and each is gated by a cooldown and a role.
 *
 * Positions are asserted with `toBeNear` on the bot's own coordinates rather than by trusting the
 * "You've teleported" line: a plugin that says it teleported and does not would otherwise pass.
 */

/** Somewhere flat, high above the terrain, well clear of spawn. */
const HOME = { x: 320, y: 90, z: -240 };
const ELSEWHERE = { x: -320, y: 90, z: 240 };

test('a home can be set, teleported to and deleted', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);
    // cost.sethome is charged from the bank, not from the player.
    await guilds.setBank(guild, SETHOME_COST);

    await player.teleport(HOME.x, HOME.y, HOME.z);
    const setting = player.getMessageBufferIndex();
    await guilds.run(player, '/guild sethome');
    await expectReceived(player, MSG.sethomeSuccess, setting);
    expect(await guilds.bankOf(guild)).toBe(0);

    await player.teleport(ELSEWHERE.x, ELSEWHERE.y, ELSEWHERE.z);

    const going = player.getMessageBufferIndex();
    await guilds.run(player, '/guild home');
    await expectReceived(player, MSG.homeTeleported, going);
    await expect(player).toBeNear(HOME.x, HOME.y, HOME.z, { tolerance: 1.5 });

    const deleting = player.getMessageBufferIndex();
    await guilds.run(player, '/guild delhome');
    await expectReceived(player, MSG.sethomeDeleted, deleting);

    await commandWithout(player, guilds, '/guild home', MSG.homeNoHomeSet, MSG.homeTeleported);
});

test('setting a home the bank cannot pay for is refused', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);
    await player.teleport(HOME.x, HOME.y, HOME.z);

    // The bank is empty and cost.sethome is 25.
    await commandWithout(player, guilds, '/guild sethome', MSG.notEnoughBank, MSG.sethomeSuccess);

    // Nothing was set, so there is still nowhere to teleport to.
    await commandWithout(player, guilds, '/guild home', MSG.homeNoHomeSet, MSG.homeTeleported);
});

test('a second home cannot be set until the cooldown expires', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);
    await guilds.setBank(guild, SETHOME_COST * 2);

    await player.teleport(HOME.x, HOME.y, HOME.z);
    await guilds.run(player, '/guild sethome');
    await expect(player).toHaveReceivedMessage(MSG.sethomeSuccess);

    await player.teleport(ELSEWHERE.x, ELSEWHERE.y, ELSEWHERE.z);

    // timers.cooldowns.sethome is three seconds in the staged config.
    const refused = player.getMessageBufferIndex();
    await guilds.run(player, '/guild sethome');
    await expectReceived(player, MSG.sethomeCooldown, refused, 2_000);
    await expect(player).not.toHaveReceivedMessage(MSG.sethomeSuccess, { since: refused });

    // The refusal says how long is left, so the wait is the plugin's own number rather than a
    // guess. Re-issuing sethome in the meantime would only start the cooldown again.
    // The refusal says how long is left, so the wait is the plugin's own number rather than a
    // guess. `getRemaining` reports whole seconds, so a margin is added on top: the real expiry
    // can be up to a second later than the number printed. Re-issuing sethome in the meantime
    // would only start the cooldown again.
    const wait = secondsRemaining(player.messageBuffer.slice(refused).join('\n'));
    expect(wait ?? 0).toBeGreaterThan(0);
    await sleep(((wait ?? 0) + 1.5) * 1000);

    const retried = player.getMessageBufferIndex();
    await guilds.run(player, '/guild sethome');
    await expectReceived(player, MSG.sethomeSuccess, retried);
    expect(await guilds.bankOf(guild)).toBe(0);
});

test('a second teleport home cannot happen until the cooldown expires', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);
    await guilds.setBank(guild, SETHOME_COST);

    await player.teleport(HOME.x, HOME.y, HOME.z);
    await guilds.run(player, '/guild sethome');
    await expect(player).toHaveReceivedMessage(MSG.sethomeSuccess);

    await guilds.run(player, '/guild home');
    await expect(player).toHaveReceivedMessage(MSG.homeTeleported);

    await player.teleport(ELSEWHERE.x, ELSEWHERE.y, ELSEWHERE.z);

    const refused = player.getMessageBufferIndex();
    await guilds.run(player, '/guild home');
    await expectReceived(player, MSG.homeCooldown, refused, 2_000);
    await expect(player).not.toHaveReceivedMessage(MSG.homeTeleported, { since: refused });
    // Still standing where they were: a refused teleport must not move anyone.
    expect(Math.abs(player.bot.entity.position.x - ELSEWHERE.x)).toBeLessThan(5);

    const wait = secondsRemaining(player.messageBuffer.slice(refused).join('\n'));
    expect(wait ?? 0).toBeGreaterThan(0);
    await sleep(((wait ?? 0) + 1.5) * 1000);

    const retried = player.getMessageBufferIndex();
    await guilds.run(player, '/guild home');
    await expectReceived(player, MSG.homeTeleported, retried);
    await expect(player).toBeNear(HOME.x, HOME.y, HOME.z, { tolerance: 1.5 });
});

test('a member without the change-home role cannot set or delete a home', async ({ player, createPlayer, guilds }) => {
    const guild = await guilds.createGuild(player);
    await guilds.setBank(guild, SETHOME_COST);
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    await member.teleport(HOME.x, HOME.y, HOME.z);
    await commandWithout(member, guilds, '/guild sethome', MSG.roleNoPermission, MSG.sethomeSuccess);

    // The guild master sets one, and the member still cannot remove it.
    await player.teleport(HOME.x, HOME.y, HOME.z);
    await guilds.run(player, '/guild sethome');
    await expect(player).toHaveReceivedMessage(MSG.sethomeSuccess);

    await commandWithout(member, guilds, '/guild delhome', MSG.roleNoPermission, MSG.sethomeDeleted);

    // Going to the home is not role-gated at all — only setting and deleting one are — so the
    // member can follow the guild there even though they may not move the guild's home.
    await commandWithout(member, guilds, '/guild home', MSG.homeTeleported, MSG.roleNoPermission);
    await expect(member).toBeNear(HOME.x, HOME.y, HOME.z, { tolerance: 1.5 });
});

test('the info GUI shows the home and teleports to it', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);
    await guilds.setBank(guild, SETHOME_COST);

    await openGui(player, '/guild info', GUI.info);
    // The home button is there, and guis.guild-info.home-empty is what an unset home reads as.
    expect(contentItems(player).some(item => stripColors(item.displayName).includes('Guild Home'))).toBe(true);
    expect(hasLoreFragment(player, ITEM.homeEmpty)).toBe(true);

    await player.teleport(HOME.x, HOME.y, HOME.z);
    await guilds.run(player, '/guild sethome');
    await expect(player).toHaveReceivedMessage(MSG.sethomeSuccess);

    await openGui(player, '/guild info', GUI.info);
    expect(hasLoreFragment(player, ITEM.homeEmpty)).toBe(false);
    // The lore carries the coordinates, not just "somewhere".
    expect(hasLoreFragment(player, String(HOME.x))).toBe(true);
    expect(hasLoreFragment(player, String(HOME.z))).toBe(true);

    await player.teleport(ELSEWHERE.x, ELSEWHERE.y, ELSEWHERE.z);

    // guis.guild-info.home-teleport is enabled in the staged config, so clicking the home button
    // goes through the same code path as /guild home.
    const handle = await openGui(player, '/guild info', GUI.info);
    await handle.locator(item => stripColors(item.displayName).includes('Guild Home')).click({ timeout: 10_000 });

    await expect(player).toBeNear(HOME.x, HOME.y, HOME.z, { tolerance: 1.5 });
});

test('a player in no guild cannot use the home commands', async ({ createPlayer, guilds }) => {
    const stranger = await createPlayer({ username: guilds.playerName() });

    await commandWithout(stranger, guilds, '/guild sethome', MSG.noGuild, MSG.sethomeSuccess);
    await commandWithout(stranger, guilds, '/guild home', MSG.noGuild, MSG.homeTeleported);
    await commandWithout(stranger, guilds, '/guild delhome', MSG.noGuild, MSG.sethomeDeleted);
});

test('the home cooldown is per player, not per guild', async ({ player, createPlayer, guilds }) => {
    const mine = await guilds.createGuild(player);
    await guilds.setBank(mine, SETHOME_COST);

    await player.teleport(HOME.x, HOME.y, HOME.z);
    await guilds.run(player, '/guild sethome');
    await expect(player).toHaveReceivedMessage(MSG.sethomeSuccess);

    // A second sethome is refused on cooldown …
    const refused = player.getMessageBufferIndex();
    await guilds.run(player, '/guild sethome');
    await expectReceived(player, MSG.sethomeCooldown, refused, 2_000);

    // … but the cooldown is keyed by player, so another bot's guild is unaffected.
    const other = await createPlayer({ username: guilds.playerName() });
    const theirs = await guilds.createGuild(other);
    await guilds.setBank(theirs, SETHOME_COST, other);

    await other.teleport(ELSEWHERE.x, ELSEWHERE.y, ELSEWHERE.z);
    const setting = other.getMessageBufferIndex();
    await guilds.run(other, '/guild sethome');
    await expectReceived(other, MSG.sethomeSuccess, setting);

    await expectNeverSeen(other, MSG.sethomeCooldown);
});

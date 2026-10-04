import { expect, test } from '@plugwright/runner';
import { commandWithout, expectReceived } from '../plugins/support/assertions.js';
import { CREATION_COST, STARTING_BALANCE } from '../plugins/support/fixtures.js';
import { MSG } from '../plugins/support/expected.js';
import { guildList } from '../plugins/support/state.js';

/**
 * The guild bank: what it reports, what moves into it, what comes back out, what it refuses, and
 * the tier upgrade it pays for.
 *
 * Every test funds a known balance and then checks the balance that results, rather than trusting
 * the confirmation message on its own — a plugin that reports a withdrawal it never performed would
 * otherwise pass.
 */

test('a new guild starts with an empty bank', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);

    const since = player.getMessageBufferIndex();
    await guilds.run(player, '/guild bank balance');
    await expectReceived(player, MSG.bankBalance, since);
    await expectReceived(player, '0', since);

    expect(await guilds.bankOf(guild)).toBe(0);
});

test('a deposit moves money from the player into the bank', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player, { balance: 1000 });
    // Creating the guild cost CREATION_COST out of that balance.
    expect(await guilds.balanceOf(player)).toBe(1000 - CREATION_COST);

    const since = player.getMessageBufferIndex();
    await guilds.run(player, '/guild bank deposit 250');
    await expectReceived(player, MSG.depositSuccess(player.username, '250'), since);

    expect(await guilds.balanceOf(player)).toBe(1000 - CREATION_COST - 250);
    expect(await guilds.bankOf(guild)).toBe(250);
});

test('a withdrawal moves money back out of the bank', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player, { balance: 1000 });
    await guilds.setBank(guild, 400);
    // 1,000 funded, 100 spent creating the guild, 400 moved into the bank.
    expect(await guilds.balanceOf(player)).toBe(500);

    const since = player.getMessageBufferIndex();
    await guilds.run(player, '/guild bank withdraw 150');
    await expectReceived(player, MSG.withdrawSuccess(player.username, '150'), since);

    expect(await guilds.bankOf(guild)).toBe(250);
    expect(await guilds.balanceOf(player)).toBe(500 + 150);
});

test('a deposit the player cannot afford is refused and moves nothing', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player, { balance: 100 });

    await commandWithout(
        player,
        guilds,
        '/guild bank deposit 500',
        MSG.notEnoughMoney,
        MSG.depositSuccess(player.username, '500'),
    );

    expect(await guilds.balanceOf(player)).toBe(100 - CREATION_COST);
    expect(await guilds.bankOf(guild)).toBe(0);
});

test('a withdrawal larger than the bank is refused and moves nothing', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player, { balance: 1000 });
    await guilds.setBank(guild, 100);

    await commandWithout(
        player,
        guilds,
        '/guild bank withdraw 500',
        MSG.notEnoughBank,
        MSG.withdrawSuccess(player.username, '500'),
    );

    expect(await guilds.bankOf(guild)).toBe(100);
    // 1,000 funded, 100 for the guild, 100 into the bank.
    expect(await guilds.balanceOf(player)).toBe(800);
});

test('a deposit over the tier maximum is refused', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player, { balance: 50_000 });
    await guilds.setBank(guild, 9_000);

    // tiers.yml gives tier 1 a max-bank-balance of 10,000, and only 1,000 more would fit.
    await commandWithout(
        player,
        guilds,
        '/guild bank deposit 2000',
        MSG.bankOverMax,
        MSG.depositSuccess(player.username, '2,000'),
    );

    expect(await guilds.bankOf(guild)).toBe(9_000);
    expect(await guilds.balanceOf(player)).toBe(50_000 - CREATION_COST - 9_000);
});

test('a deposit that lands exactly on the tier maximum is allowed', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player, { balance: 50_000 });
    await guilds.setBank(guild, 9_000);

    const since = player.getMessageBufferIndex();
    await guilds.run(player, '/guild bank deposit 1000');
    await expectReceived(player, MSG.depositSuccess(player.username, '1,000'), since);
    expect(await guilds.bankOf(guild)).toBe(10_000);
});

test('an upgrade costs the bank, not the player, and is refundable by cancelling', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player, { balance: 5000 });
    await guilds.setBank(guild, 200);

    const warning = player.getMessageBufferIndex();
    await guilds.run(player, '/guild upgrade');
    await expectReceived(player, MSG.upgradeMoneyWarning, warning);
    await expectReceived(player, '200', warning);

    // Nothing has moved yet: the price is only taken on confirmation.
    expect(await guilds.bankOf(guild)).toBe(200);

    await guilds.cancel(player);
    await expect(player).toHaveReceivedMessage(MSG.cancelSuccess);
    expect(await guilds.bankOf(guild)).toBe(200);

    await guilds.run(player, '/guild upgrade');
    await expect(player).toHaveReceivedMessage(MSG.upgradeMoneyWarning);
    await guilds.confirm(player);
    await expect(player).toHaveReceivedMessage(MSG.upgradeSuccess);

    expect(await guilds.bankOf(guild)).toBe(0);
    // 5,000 funded, 100 for the guild, 200 into the bank to pay for the tier.
    expect(await guilds.balanceOf(player)).toBe(5_000 - CREATION_COST - 200);
});

test('an upgrade the bank cannot pay for is refused', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player, { balance: 5000 });

    // The tier 2 price is 200 and the bank is empty.
    await commandWithout(player, guilds, '/guild upgrade', MSG.upgradeNotEnoughMoney, MSG.upgradeMoneyWarning);

    expect(await guilds.bankOf(guild)).toBe(0);
    expect((await guildList(player)).find(g => g.name === guild)!.tier).toBe(1);
});

test('a guild at the top tier cannot upgrade again', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player, { balance: 50_000 });

    for (let tier = 2; tier <= 3; tier++) {
        await guilds.setBank(guild, tier === 2 ? 200 : 300);
        await guilds.run(player, '/guild upgrade');
        await expect(player).toHaveReceivedMessage(MSG.upgradeMoneyWarning);
        await guilds.confirm(player);
        await expect(player).toHaveReceivedMessage(MSG.upgradeSuccess);
    }

    await commandWithout(player, guilds, '/guild upgrade', MSG.upgradeTierMax, MSG.upgradeMoneyWarning);

    expect((await guildList(player)).find(g => g.name === guild)!.tier).toBe(3);
});

test('a fractional amount is rounded the way Guilds rounds it', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player, { balance: 1000 });

    // `Double.rounded()` in Guilds formats to two decimals, so this is not integer rounding.
    const since = player.getMessageBufferIndex();
    await guilds.run(player, '/guild bank deposit 10.567');
    await expectReceived(player, MSG.depositSuccess(player.username, '10.57'), since);

    expect(await guilds.bankOf(guild)).toBe(10.57);
    expect(await guilds.balanceOf(player)).toBeCloseTo(1_000 - CREATION_COST - 10.57, 2);
});

test('a negative amount is refused', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player, { balance: 1000 });
    await guilds.setBank(guild, 100);

    await commandWithout(
        player,
        guilds,
        '/guild bank deposit -50',
        MSG.notEnoughMoney,
        MSG.depositSuccess(player.username, '-50'),
    );
    expect(await guilds.bankOf(guild)).toBe(100);
    expect(await guilds.balanceOf(player)).toBe(1_000 - CREATION_COST - 100);
});

test('an amount that is not a number is refused as a syntax error', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player, { balance: 1000 });

    await commandWithout(
        player,
        guilds,
        '/guild bank deposit lots',
        MSG.invalidSyntax,
        MSG.depositSuccess(player.username, 'lots'),
    );

    expect(await guilds.bankOf(guild)).toBe(0);
    expect(await guilds.balanceOf(player)).toBe(1_000 - CREATION_COST);
});

test('the whole guild is told about a transaction, not just the player who made it', async ({
    player,
    createPlayer,
    guilds,
}) => {
    const guild = await guilds.createGuild(player, { balance: 1000 });
    const member = await createPlayer({ username: guilds.playerName() });
    await guilds.addMember(player, member, guild);

    const heard = member.getMessageBufferIndex();
    await guilds.run(player, '/guild bank deposit 100');
    await expectReceived(member, MSG.depositSuccess(player.username, '100'), heard);
    expect(await guilds.balanceOf(player)).toBe(1_000 - CREATION_COST - 100);
});

test('STARTING_BALANCE is what the fixtures actually fund', async ({ player, guilds }) => {
    // Guards the number the rest of the suite reasons about.
    await guilds.fund(player, STARTING_BALANCE);
    expect(await guilds.balanceOf(player)).toBe(STARTING_BALANCE);
});

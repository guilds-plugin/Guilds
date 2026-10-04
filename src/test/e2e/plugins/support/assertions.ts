import { expect, type PlayerWrapper } from '@plugwright/runner';
import type { GuildsFixtures } from './fixtures.js';
import { sleep } from './text.js';

/**
 * Assertion shapes that the runner's matchers do not cover on their own.
 *
 * `expect(player).not.toHaveReceivedMessage(…)` returns as soon as the buffer is clean — which is
 * immediately, before the server has even processed the command. On its own it proves nothing
 * about a message that has not arrived yet. Everything here either waits for a positive signal
 * first, or waits out the window before asserting the absence.
 */

/** Asserts a player received `pattern`, optionally only counting lines after `since`. */
export async function expectReceived(
    player: PlayerWrapper,
    pattern: string | RegExp,
    since?: number,
    timeout = 10_000,
): Promise<void> {
    await expect(player).toHaveReceivedMessage(pattern, since === undefined ? { timeout } : { since, timeout });
}

/**
 * Asserts `pattern` never reached `player`, after giving the server `window` ms to answer.
 *
 * Prefer {@link commandWithout} where a command is involved: this only proves that nothing arrived
 * within the window, which is exactly as strong as that window is long.
 */
export async function expectNeverSeen(
    player: PlayerWrapper,
    pattern: string | RegExp,
    since = 0,
    window = 2_000,
): Promise<void> {
    await sleep(window);
    await expect(player).not.toHaveReceivedMessage(pattern, { since });
}

/**
 * Runs `command`, waits for the reply that proves Guilds processed it, and only then asserts that
 * `absent` was never sent.
 *
 * The positive wait is what makes the negative assertion mean something: by the time it resolves,
 * the server has already answered this exact command, so a message the command would have sent
 * alongside it would have been in the buffer too.
 */
export async function commandWithout(
    player: PlayerWrapper,
    guilds: GuildsFixtures,
    command: string,
    reply: string | RegExp,
    absent: string | RegExp,
    timeout = 10_000,
): Promise<void> {
    const since = player.getMessageBufferIndex();
    await guilds.run(player, command);
    await expectReceived(player, reply, since, timeout);
    await expect(player).not.toHaveReceivedMessage(absent, { since });
}

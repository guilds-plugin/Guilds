import { expect, waitForAssertion, type PlayerWrapper } from '@plugwright/runner';
import type { GuildsFixtures } from './fixtures.js';
import { received, says, sleep } from './text.js';

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
    await settleBuffer(player);
    await expect(player).not.toHaveReceivedMessage(absent, { since });
}

/**
 * Waits until the player's buffer has been quiet for a moment.
 *
 * `not.toHaveReceivedMessage` is only as strong as the window it looks at, and one reply is not the
 * same as the server having finished talking. A command that notifies a second bot can leave that
 * bot's buffer with a message still in flight, and a later absence assertion scoped from just before
 * its own command will then match that straggler and fail for the wrong reason. Waiting for the
 * buffer to stop growing removes the race rather than widening the window.
 */
export async function settleBuffer(player: PlayerWrapper, quietMs = 500, timeout = 5_000): Promise<void> {
    const deadline = Date.now() + timeout;
    let lastLength = player.messageBuffer.length;
    let lastChange = Date.now();

    for (;;) {
        await sleep(50);
        const length = player.messageBuffer.length;
        if (length !== lastLength) {
            lastLength = length;
            lastChange = Date.now();
        }
        if (Date.now() - lastChange >= quietMs) return;
        if (Date.now() >= deadline) return;
    }
}

/**
 * Waits for whichever of two outcomes the server produces, and reports which one it was.
 *
 * Used where the point of the assertion is *which* of two answers comes back, and a short fixed
 * window is the wrong tool: on a loaded runner the reply simply took longer than the window and the
 * test failed against behaviour that was correct. Waiting for the first of the two and then asserting
 * which one it was keeps the assertion strong, because "neither arrived" still fails, and it does not
 * care how long the server took to answer.
 *
 * @returns the label of whichever pattern matched
 */
export async function expectEither(
    player: PlayerWrapper,
    since: number,
    expected: string | RegExp,
    other: string | RegExp,
    timeout = 15_000,
): Promise<'expected' | 'other'> {
    const matches = (pattern: string | RegExp) =>
        received(player.messageBuffer, typeof pattern === 'string' ? says(pattern) : pattern, since);

    try {
        await waitForAssertion(async () => {
            if (matches(expected) || matches(other)) return;
            throw new Error('neither answer has arrived yet');
        }, { timeout, interval: 100 });
    } catch {
        throw new Error(
            `neither "${String(expected)}" nor "${String(other)}" reached ${player.username} within ${timeout}ms`,
        );
    }
    return matches(expected) ? 'expected' : 'other';
}

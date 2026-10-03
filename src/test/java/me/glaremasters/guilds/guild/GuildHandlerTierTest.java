package me.glaremasters.guilds.guild;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for the tier ladder lookup that {@link GuildHandler#upgradeTier(Guild)} and
 * {@link GuildHandler#isMaxTier(Guild)} both depend on.
 *
 * <p>The bug these cover was a disagreement between two numbers. "How many tiers are configured"
 * and "what is the highest level configured" are only the same value while the levels happen to
 * be numbered 1..N. An owner who deleted or renumbered a tier in {@code tiers.yml} made
 * {@code isMaxTier} disagree with {@code upgradeTier}, which then stored a null tier on the
 * guild.
 *
 * <p>Only the static lookup is exercised here. It is the whole of the decision, and testing it
 * directly keeps the test free of a {@link GuildHandler} instance, which would need a plugin, a
 * data folder and a database behind it.
 */
class GuildHandlerTierTest {

    private static GuildTier tier(int level) {
        return GuildTier.builder().level(level).name("Tier " + level).build();
    }

    private static List<GuildTier> ladder(int... levels) {
        GuildTier[] built = new GuildTier[levels.length];
        for (int i = 0; i < levels.length; i++) {
            built[i] = tier(levels[i]);
        }
        return Arrays.asList(built);
    }

    private static int levelOf(GuildTier tier) {
        assertNotNull(tier, "expected a tier but the lookup returned null");
        return tier.getLevel();
    }

    @Test
    @DisplayName("a contiguous ladder advances one level at a time")
    void contiguousLadder() {
        List<GuildTier> tiers = ladder(1, 2, 3, 4, 5);

        assertEquals(2, levelOf(GuildHandler.nextTierAbove(tiers, 1)));
        assertEquals(3, levelOf(GuildHandler.nextTierAbove(tiers, 2)));
        assertEquals(5, levelOf(GuildHandler.nextTierAbove(tiers, 4)));
    }

    @Test
    @DisplayName("the top of the ladder has nothing above it")
    void topOfLadderHasNoNextTier() {
        assertNull(GuildHandler.nextTierAbove(ladder(1, 2, 3, 4, 5), 5));
    }

    @Test
    @DisplayName("a ladder with a gap skips the missing level")
    void gapIsSkippedRatherThanFailing() {
        // The shape that used to store a null tier: levels 1, 2, 4, 5 after deleting 3.
        List<GuildTier> tiers = ladder(1, 2, 4, 5);

        assertEquals(4, levelOf(GuildHandler.nextTierAbove(tiers, 2)),
                "a guild on 2 must reach 4, not a tier 3 that does not exist");
        assertEquals(5, levelOf(GuildHandler.nextTierAbove(tiers, 4)));
        assertNull(GuildHandler.nextTierAbove(tiers, 5));
    }

    @Test
    @DisplayName("a ladder that does not start at one still climbs from the bottom")
    void ladderBelowTheCurrentLevel() {
        List<GuildTier> tiers = ladder(7, 8, 9);

        assertEquals(8, levelOf(GuildHandler.nextTierAbove(tiers, 7)));
        assertEquals(7, levelOf(GuildHandler.nextTierAbove(tiers, 0)),
                "a guild below every configured tier should reach the lowest one");
    }

    @Test
    @DisplayName("an empty ladder yields nothing rather than throwing")
    void emptyLadder() {
        assertNull(GuildHandler.nextTierAbove(Collections.emptyList(), 1));
    }

    @Test
    @DisplayName("a single-tier ladder is already at the top")
    void singleTierLadder() {
        assertNull(GuildHandler.nextTierAbove(ladder(1), 1));
        assertEquals(1, levelOf(GuildHandler.nextTierAbove(ladder(1), 0)));
    }

    @Test
    @DisplayName("duplicate levels still resolve to a tier above the guild")
    void duplicateLevelsResolveDeterministically() {
        // Warned about at startup, but the ladder must keep working rather than break.
        List<GuildTier> tiers = ladder(1, 2, 2, 3);

        assertEquals(2, levelOf(GuildHandler.nextTierAbove(tiers, 1)));
        assertEquals(3, levelOf(GuildHandler.nextTierAbove(tiers, 2)));
    }

    @Test
    @DisplayName("the lookup ignores the order tiers were loaded in")
    void orderOfTiersDoesNotMatter() {
        List<GuildTier> descending = ladder(5, 4, 3, 2, 1);

        assertEquals(2, levelOf(GuildHandler.nextTierAbove(descending, 1)));
        assertEquals(5, levelOf(GuildHandler.nextTierAbove(descending, 4)));
        assertNull(GuildHandler.nextTierAbove(descending, 5));
    }
}

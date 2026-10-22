package me.glaremasters.guilds.guild;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Covers what one use of an invite code means.
 *
 * <p>A code is a counter, a list of the players who spent it, and the player who made it. Those
 * three things are the whole of its stored shape, and the code redeem path is not allowed to
 * change any of them: a redeem spends exactly one use, records exactly one redeemer, and the
 * creator and the remaining count are what the next reader sees.
 */
class GuildCodeTest {

    private static final String ID = "ABC1234";

    private GuildCode code;
    private UUID creatorId;

    @BeforeEach
    void setUp() {
        creatorId = UUID.randomUUID();
        code = new GuildCode(ID, 3, creatorId, new ArrayList<>());
    }

    @Test
    @DisplayName("a redeem spends one use and records one redeemer")
    void aRedeemSpendsOneUse() {
        final UUID redeemer = UUID.randomUUID();

        code.addRedeemer(player(redeemer));

        assertEquals(2, code.getUses());
        assertEquals(Arrays.asList(redeemer), code.getRedeemers());
    }

    @Test
    @DisplayName("a redeem leaves the creator and the id alone")
    void aRedeemKeepsTheCreator() {
        code.addRedeemer(player(UUID.randomUUID()));

        assertEquals(ID, code.getId());
        assertSame(creatorId, code.getCreator());
    }

    @Test
    @DisplayName("the same player can spend the code twice, and it costs two uses")
    void theSamePlayerCanRedeemTwice() {
        // Documented, not endorsed: a player who leaves the guild they joined with a code can
        // come back and spend the code again, once the join cooldown has run out. The code
        // charges them for it, and the redeemers list names them twice.
        final UUID redeemer = UUID.randomUUID();
        final Player player = player(redeemer);

        code.addRedeemer(player);
        code.addRedeemer(player);

        assertEquals(1, code.getUses());
        assertEquals(Arrays.asList(redeemer, redeemer), code.getRedeemers());
    }

    /**
     * A player with the given uuid.
     */
    private static Player player(final UUID uuid) {
        final Player player = Mockito.mock(Player.class);
        Mockito.when(player.getUniqueId()).thenReturn(uuid);
        return player;
    }
}

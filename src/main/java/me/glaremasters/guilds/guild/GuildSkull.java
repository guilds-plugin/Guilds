package me.glaremasters.guilds.guild;

import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.ProfileInputType;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;
import java.util.Objects;

/**
 * Represents a guild skull, which is a player head in Minecraft that has a custom texture.
 */
public class GuildSkull {
    private final String serialized;
    private transient ItemStack itemStack;

    /**
     * Creates a guild skull from a player.
     *
     * @param player the player whose head will be used for the guild skull
     */
    public GuildSkull(Player player) {
        final Profileable playerProfile = Profileable.of(player);

        itemStack = XSkull.createItem().profile(playerProfile).apply();
        serialized = XSkull.of(itemStack.getItemMeta()).getProfileValue();
    }

    /**
     * Creates a guild skull from a texture string.
     *
     * @param texture the texture string, which should be a Minecraft resource location string
     */
    public GuildSkull(String texture) {
        final ProfileInputType type = ProfileInputType.typeOf(texture);

        if (type == null) {
            this.serialized = Base64.getEncoder().encodeToString(Objects.requireNonNull(texture).getBytes());
        } else {
            this.serialized = texture;
        }

        this.itemStack = createSkull();
    }

    /**
     * Creates a guild skull from a serialized string.
     *
     * @return the guild skull
     */
    public ItemStack createSkull() {
        final ProfileInputType type = ProfileInputType.typeOf(serialized);

        if (type == null) {
            final ProfileInputType backupType = ProfileInputType.typeOf("c10591e6909e6a281b371836e462d67a2c78fa0952e910f32b41a26c48c1757c");
            final Profileable backupProfile = Profileable.of(backupType, serialized);

            return XSkull.createItem().profile(backupProfile).apply();
        }

        final Profileable playerProfile = Profileable.of(type, serialized);

        return XSkull.createItem().profile(playerProfile).apply();
    }

    /**
     * Gets the guild skull as an item stack.
     *
     * @return the guild skull as an item stack
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Get the serialized string
     *
     * @return serialized string
     */
    public String getSerialized() {
        return serialized;
    }
}

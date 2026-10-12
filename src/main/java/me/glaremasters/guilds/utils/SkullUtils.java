package me.glaremasters.guilds.utils;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
import org.bukkit.inventory.ItemStack;

/** Cross-version player skull creation with a fallback for XSeries profile reflection failures. */
public final class SkullUtils {

    private SkullUtils() {
    }

    public static ItemStack create(Profileable profile) {
        try {
            return XSkull.createItem().profile(profile).apply();
        } catch (RuntimeException | LinkageError failure) {
            if (!hasLinkageFailure(failure)) {
                throw failure;
            }

            ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
            if (head == null) {
                throw failure;
            }
            return head;
        }
    }

    public static String profileValue(ItemStack item, String fallback) {
        try {
            return XSkull.of(item.getItemMeta()).getProfileValue();
        } catch (RuntimeException | LinkageError failure) {
            if (!hasLinkageFailure(failure)) {
                throw failure;
            }
            return fallback;
        }
    }

    private static boolean hasLinkageFailure(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof LinkageError) {
                return true;
            }
        }
        return false;
    }
}

package onl.tesseract.srp.common.adapter.utils;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

/**
 * Utility methods for working with Bukkit Players.
 */
public final class PlayerUtils {
    private PlayerUtils() {
        // Utility class
    }

    /**
     * Converts an actor (Player or Projectile shooter) to a Player.
     */
    public static Player asPlayer(Object actor) {
        if (actor instanceof Player player) {
            return player;
        }
        if (actor instanceof Projectile projectile) {
            Object shooter = projectile.getShooter();
            if (shooter instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    /**
     * Attempts to free the chestplate slot by moving it to an inventory slot.
     * @return true if the slot is now free or was already empty, false if no room found.
     */
    public static boolean tryFreeChestplateSlot(Player player) {
        org.bukkit.inventory.ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate == null) {
            return true;
        }
        java.util.Map<Integer, org.bukkit.inventory.ItemStack> leftovers = player.getInventory().addItem(chestplate);
        return leftovers.isEmpty();
    }
}


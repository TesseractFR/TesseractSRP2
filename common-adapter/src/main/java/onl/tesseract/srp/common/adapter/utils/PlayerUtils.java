package onl.tesseract.srp.common.adapter.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility methods for working with Bukkit Players.
 */
public final class PlayerUtils {
    private static final Map<UUID, PlayerProfile> PROFILES = new ConcurrentHashMap<>();
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

    public static ItemStack getPlayerHead(UUID uuid) {
        PlayerProfile profile = PROFILES.computeIfAbsent(uuid, id -> {
            PlayerProfile p = Bukkit.createProfile(id);
            p.complete();
            return p;
        });
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        item.editMeta(meta -> ((SkullMeta) meta).setPlayerProfile(profile));
        return item;
    }
}


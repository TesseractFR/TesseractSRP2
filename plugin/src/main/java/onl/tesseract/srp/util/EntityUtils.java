package onl.tesseract.srp.util;

import org.bukkit.entity.*;

/**
 * Utility methods for working with Bukkit Entities.
 */
public final class EntityUtils {
    private EntityUtils() {
        // Utility class
    }

    /**
     * Checks if the entity can be saddled.
     */
    public static boolean isSaddlable(Entity entity) {
        return entity instanceof Pig || entity instanceof Strider
            || entity instanceof Horse || entity instanceof Donkey
            || entity instanceof Mule || entity instanceof Camel;
    }

    /**
     * Checks if the entity can be used as a mount.
     */
    public static boolean isLivingMount(Entity entity) {
        return entity instanceof Horse || entity instanceof Donkey
            || entity instanceof Mule || entity instanceof Camel
            || entity instanceof Pig || entity instanceof Strider;
    }
}


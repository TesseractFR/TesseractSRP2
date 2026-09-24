package onl.tesseract.srp.exception;

/**
 * Thrown when a world is referenced but does not exist in Bukkit.
 */
public class WorldMissingException extends RuntimeException {
    public WorldMissingException(String message) {
        super(message);
    }
}


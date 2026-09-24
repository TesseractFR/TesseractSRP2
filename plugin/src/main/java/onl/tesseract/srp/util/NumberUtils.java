package onl.tesseract.srp.util;

/**
 * Extension utility for comparing Int with UInt.
 */
public final class NumberUtils {
    private NumberUtils() {
        // Utility class
    }

    /**
     * Compares this Int with a UInt by converting the UInt to Int.
     */
    public static int compareTo(int value, long uintValue) {
        return Integer.compare(value, (int) uintValue);
    }
}


package onl.tesseract.srp.exception;

/**
 * Thrown when a player is expected to be connected but is not.
 */
public class PlayerNotConnectedException extends RuntimeException {
    public PlayerNotConnectedException() {
        super();
    }

    public PlayerNotConnectedException(String message) {
        super(message);
    }
}


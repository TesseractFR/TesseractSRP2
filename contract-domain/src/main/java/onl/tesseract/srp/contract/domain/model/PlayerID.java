package onl.tesseract.srp.contract.domain.model;

import java.util.UUID;

public record PlayerID(
        UUID value
) {
    public PlayerID {
        if (value == null) {
            throw new IllegalArgumentException("PlayerID value cannot be null");
        }
    }
}

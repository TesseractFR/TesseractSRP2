package onl.tesseract.srp.common.domain.model;

import java.util.UUID;

public record PlayerId(UUID value) {
    public PlayerId {
        if (value == null) {
            throw new IllegalArgumentException("PlayerId value cannot be null");
        }
    }
}

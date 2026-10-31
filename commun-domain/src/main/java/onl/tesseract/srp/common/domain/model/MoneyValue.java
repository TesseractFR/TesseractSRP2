package onl.tesseract.srp.common.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record MoneyValue(BigDecimal value) {
    public MoneyValue {
        Objects.requireNonNull(value, "value");
        if (value.signum() <= 0) {
            throw new IllegalArgumentException("value must be positive");
        }
    }
}

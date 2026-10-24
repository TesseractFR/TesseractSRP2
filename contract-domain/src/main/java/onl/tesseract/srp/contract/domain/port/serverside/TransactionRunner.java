package onl.tesseract.srp.contract.domain.port.serverside;

import java.util.function.Supplier;

public interface TransactionRunner {
    <T> T inTransaction(Supplier<T> action);
}

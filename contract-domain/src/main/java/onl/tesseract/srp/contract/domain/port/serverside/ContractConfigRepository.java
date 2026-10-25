package onl.tesseract.srp.contract.domain.port.serverside;

import java.time.Duration;

public interface ContractConfigRepository {
    Duration getScrollTtl();
}
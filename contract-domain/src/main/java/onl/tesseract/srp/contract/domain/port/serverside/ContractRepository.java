package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.Contract;

import java.time.Instant;
import java.util.Optional;

public interface ContractRepository {
    Contract save(Contract contract);
    Optional<Contract> findById(long id);
    void delete(long id);
    int deleteExpired(Instant now);

    boolean settle(long contractId, PlayerId deliverer, int payout);

}
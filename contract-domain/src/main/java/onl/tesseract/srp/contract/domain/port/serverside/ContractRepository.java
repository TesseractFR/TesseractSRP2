package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.contract.domain.model.contract.Contract;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ContractRepository {
    Contract save(Contract contract);
    Optional<Contract> findById(long id);
    boolean delete(long id);
    List<Long> deleteExpired(Instant now);
}

package onl.tesseract.srp.contract.adapter.serverside.jpa;

import onl.tesseract.srp.contract.adapter.serverside.jpa.repository.ContractJpaRepository;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractDraft;
import onl.tesseract.srp.contract.domain.model.contract.ContractId;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/** Persiste les contrats dans la table {@code t_contracts}. */
@Component
public class JpaContractRepository implements ContractRepository {

    private final ContractJpaRepository contractJpaRepository;
    private final ContractMapper contractMapper;

    public JpaContractRepository(ContractJpaRepository contractJpaRepository, ContractMapper contractMapper) {
        this.contractJpaRepository = contractJpaRepository;
        this.contractMapper = contractMapper;
    }

    @Override
    @Transactional
    public Contract create(ContractDraft draft) {
        return contractMapper.toDomain(contractJpaRepository.save(contractMapper.toEntity(draft)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Contract> findById(ContractId id) {
        return contractJpaRepository.findById(id.value()).map(contractMapper::toDomain);
    }

    @Override
    @Transactional
    public void delete(ContractId id) {
        contractJpaRepository.deleteById(id.value());
    }

    @Override
    @Transactional
    public int deleteExpired(Instant now) {
        return contractJpaRepository.deleteExpired(now);
    }
}

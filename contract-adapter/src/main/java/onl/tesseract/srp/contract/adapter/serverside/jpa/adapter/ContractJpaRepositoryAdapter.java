package onl.tesseract.srp.contract.adapter.serverside.jpa.adapter;

import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;
import onl.tesseract.srp.contract.adapter.serverside.jpa.entity.ContractEntity;
import onl.tesseract.srp.contract.adapter.serverside.jpa.repository.ContractJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/** Adapte les opérations de persistance JPA au port {@link ContractRepository}. */
@Component
public class ContractJpaRepositoryAdapter implements ContractRepository {

    private final ContractJpaRepository contractJpaRepository;
    public ContractJpaRepositoryAdapter(ContractJpaRepository contractJpaRepository) {
        this.contractJpaRepository = contractJpaRepository;
    }

    @Override
    @Transactional
    public Contract save(Contract contract) {
        return contractJpaRepository.save(ContractEntity.fromDomain(contract)).toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Contract> findById(long id) {
        return contractJpaRepository.findById(id).map(ContractEntity::toDomain);
    }

    @Override
    @Transactional
    public void delete(long id) {
        contractJpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public int deleteExpired(Instant now) {
        return contractJpaRepository.deleteExpired(now);
    }

}

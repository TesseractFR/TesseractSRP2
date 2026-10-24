package onl.tesseract.srp.contract.adapter.serverside.jpa.adapter;

import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;
import onl.tesseract.srp.contract.adapter.serverside.jpa.entity.ContractEntity;
import onl.tesseract.srp.contract.adapter.serverside.jpa.mapper.ContractMapper;
import onl.tesseract.srp.contract.adapter.serverside.jpa.repository.ContractJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class ContractRepositoryJpaAdapter implements ContractRepository {

    private final ContractJpaRepository jpaRepo;
    private final ContractMapper mapper;

    public ContractRepositoryJpaAdapter(ContractJpaRepository jpaRepo, ContractMapper mapper) {
        this.jpaRepo = jpaRepo;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Contract save(Contract contract) {
        ContractEntity saved = jpaRepo.save(mapper.toEntity(contract));
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Contract> findById(long id) {
        return jpaRepo.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public boolean delete(long id) {
        return jpaRepo.deleteByIdCounting(id) > 0;
    }

    @Override
    @Transactional
    public List<Long> deleteExpired(Instant now) {
        List<Long> ids = jpaRepo.findExpiredIds(now);
        if (!ids.isEmpty()) {
            jpaRepo.deleteAllByIdInBatch(ids);
        }
        return ids;
    }
}

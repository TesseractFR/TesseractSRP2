package onl.tesseract.srp.contract.adapter.serverside.jpa.adapter;

import onl.tesseract.srp.common.domain.port.userside.PlayerService;
import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.exception.PaymentFailedException;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;
import onl.tesseract.srp.contract.adapter.serverside.jpa.entity.ContractEntity;
import onl.tesseract.srp.contract.adapter.serverside.jpa.repository.ContractJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Component
public class ContractJpaRepositoryAdapter implements ContractRepository {

    private final ContractJpaRepository jpaRepo;
    private final PlayerService playerService;

    public ContractJpaRepositoryAdapter(ContractJpaRepository jpaRepo, PlayerService playerService) {
        this.jpaRepo = jpaRepo;
        this.playerService = playerService;
    }

    @Override
    @Transactional
    public Contract save(Contract contract) {
        return jpaRepo.save(ContractEntity.fromDomain(contract)).toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Contract> findById(long id) {
        return jpaRepo.findById(id).map(ContractEntity::toDomain);
    }

    @Override
    @Transactional
    public void delete(long id) {
        jpaRepo.deleteByIdCounting(id);
    }

    @Override
    @Transactional
    public int deleteExpired(Instant now) {
        return jpaRepo.deleteExpired(now);
    }

    @Override
    @Transactional
    public boolean settle(long contractId, PlayerID deliverer, int payout) {
        if (jpaRepo.deleteByIdCounting(contractId) == 0) {
            return false;
        }
        if (!playerService.adjustMoney(deliverer.value(), payout)) {
            throw new PaymentFailedException();
        }
        return true;
    }
}
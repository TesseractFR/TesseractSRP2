package onl.tesseract.srp.contract.adapter.serverside.jpa.adapter;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.ticket.ContractPlayerState;
import onl.tesseract.srp.contract.domain.port.serverside.ContractPlayerStateRepository;
import onl.tesseract.srp.contract.adapter.serverside.jpa.mapper.ContractPlayerStateMapper;
import onl.tesseract.srp.contract.adapter.serverside.jpa.repository.ContractPlayerStateJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ContractPlayerStateRepositoryJpaAdapter implements ContractPlayerStateRepository {

    private final ContractPlayerStateJpaRepository jpaRepo;
    private final ContractPlayerStateMapper mapper;

    public ContractPlayerStateRepositoryJpaAdapter(ContractPlayerStateJpaRepository jpaRepo,
                                                   ContractPlayerStateMapper mapper) {
        this.jpaRepo = jpaRepo;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public ContractPlayerState find(PlayerID player) {
        return jpaRepo.findById(player.value())
                .map(mapper::toDomain)
                .orElseGet(() -> ContractPlayerState.fresh(player));
    }

    @Override
    @Transactional
    public void save(ContractPlayerState state) {
        jpaRepo.save(mapper.toEntity(state));
    }
}

package onl.tesseract.srp.common.adapter.serverside.jpa.repository;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.SrpPlayer;
import onl.tesseract.srp.common.domain.port.serverside.PlayerRepository;
import onl.tesseract.srp.common.adapter.serverside.jpa.entity.SrpPlayerEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA adapter for SrpPlayerRepository.
 */
@Component
public class SrpPlayerJpaRepositoryAdapter implements PlayerRepository {
    private final SrpPlayerJpaRepository jpaRepository;

    public SrpPlayerJpaRepositoryAdapter(SrpPlayerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SrpPlayer getById(PlayerId id) {
        Optional<SrpPlayerEntity> entity = jpaRepository.findById(id.value());
        return entity.map(SrpPlayerEntity::toDomain).orElse(null);
    }

    @Override
    public SrpPlayer save(SrpPlayer entity) {
        SrpPlayerEntity saved = jpaRepository.save(SrpPlayerEntity.fromDomain(entity));
        return saved.toDomain();
    }

}


package onl.tesseract.srp.repository.hibernate.player;

import onl.tesseract.srp.common.domain.model.SrpPlayer;
import onl.tesseract.srp.repository.generic.player.SrpPlayerRepository;
import onl.tesseract.srp.repository.hibernate.SrpPlayerEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter for SrpPlayerRepository.
 */
@Component
public class SrpPlayerJpaRepositoryAdapter implements SrpPlayerRepository {
    private final SrpPlayerJpaRepository jpaRepository;

    public SrpPlayerJpaRepositoryAdapter(SrpPlayerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SrpPlayer getById(UUID id) {
        Optional<SrpPlayerEntity> entity = jpaRepository.findById(id);
        return entity.map(SrpPlayerEntity::toDomain).orElse(null);
    }

    @Override
    public SrpPlayer save(SrpPlayer entity) {
        SrpPlayerEntity saved = jpaRepository.save(SrpPlayerEntity.fromDomain(entity));
        return saved.toDomain();
    }

}


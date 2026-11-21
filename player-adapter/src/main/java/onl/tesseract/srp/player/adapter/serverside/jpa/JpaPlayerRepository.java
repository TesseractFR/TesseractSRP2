package onl.tesseract.srp.player.adapter.serverside.jpa;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.player.domain.model.SrpPlayer;
import onl.tesseract.srp.player.domain.port.serverside.PlayerRepository;
import onl.tesseract.srp.player.adapter.serverside.jpa.entity.SrpPlayerEntity;
import onl.tesseract.srp.player.adapter.serverside.jpa.repository.SrpPlayerJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Persistance JPA du profil SRP des joueurs.
 */
@Component
public class JpaPlayerRepository implements PlayerRepository {
    private final SrpPlayerJpaRepository jpaRepository;

    public JpaPlayerRepository(SrpPlayerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<SrpPlayer> findById(PlayerId id) {
        return jpaRepository.findById(id.value()).map(SrpPlayerEntity::toDomain);
    }

    @Override
    public SrpPlayer save(SrpPlayer entity) {
        SrpPlayerEntity saved = jpaRepository.save(SrpPlayerEntity.fromDomain(entity));
        return saved.toDomain();
    }

}


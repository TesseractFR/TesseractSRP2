package onl.tesseract.srp.territory.adapter.serverside.repository;

import onl.tesseract.srp.territory.adapter.serverside.entity.CampementEntity;
import onl.tesseract.srp.territory.domain.model.campement.Campement;
import onl.tesseract.srp.territory.domain.port.serverside.CampementRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CampementRepositoryImpl implements CampementRepository {

    private final CampementJpaRepository jpaRepository;

    public CampementRepositoryImpl(CampementJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Campement getById(UUID id) {
        return jpaRepository.findById(id).map(CampementEntity::toDomain).orElse(null);
    }

    @Override
    public Campement findByPlayer(UUID player) {
        return jpaRepository.findById(player).map(CampementEntity::toDomain).orElse(null);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean isChunkClaimed(onl.tesseract.srp.common.domain.model.ChunkCoord chunkCoord) {
        // Check if any campement owns this chunk
        return false;
    }

    @Override
    public Campement getCampementByChunk(onl.tesseract.srp.common.domain.model.ChunkCoord chunkCoord) {
        return null;
    }

    @Override
    public List<Campement> findAll() {
        return jpaRepository.findAll().stream()
                .map(CampementEntity::toDomain)
                .toList();
    }

    @Override
    public Campement save(Campement campement) {
        return jpaRepository.save(CampementEntity.fromDomain(campement)).toDomain();
    }

    @Override
    public UUID idOf(Campement campement) {
        return campement.getOwnerID();
    }
}


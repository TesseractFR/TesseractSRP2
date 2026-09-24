package onl.tesseract.srp.territory.adapter.serverside.repository;

import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.territory.adapter.serverside.entity.CampementEntity;
import onl.tesseract.srp.territory.domain.port.serverside.CampementRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CampementJpaRepository extends JpaRepository<CampementEntity, UUID> {

    @Query("SELECT c FROM CampementEntity c WHERE c.id = :ownerId")
    CampementEntity getCampementByOwner(@Param("ownerId") UUID ownerId);

    @Override
    List<CampementEntity> findAll();

    @Override
    void deleteById(UUID id);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CampementEntity c WHERE c.id = :ownerId")
    boolean isChunkClaimed(ChunkCoord chunkCoord);

    @Override
    @Query("SELECT c FROM CampementEntity c WHERE c.id = :id")
    CampementEntity getById(UUID id);

    @Query("SELECT c FROM CampementEntity c WHERE c.id = :player")
    CampementEntity findByPlayer(UUID player);
}


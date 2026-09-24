package onl.tesseract.srp.territory.adapter.serverside.repository;

import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.territory.adapter.serverside.entity.chunk.TerritoryChunkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface TerritoryChunkJpaRepository extends JpaRepository<TerritoryChunkEntity, ChunkCoord> {

    @Query("SELECT t FROM TerritoryChunkEntity t WHERE t.chunkEntityId.world = :world AND t.chunkEntityId.x BETWEEN :minX AND :maxX AND t.chunkEntityId.z BETWEEN :minZ AND :maxZ")
    Collection<TerritoryChunkEntity> findAllByRange(@Param("world") String world, @Param("minX") int minX, @Param("maxX") int maxX, @Param("minZ") int minZ, @Param("maxZ") int maxZ);
}


package onl.tesseract.srp.territory.adapter.serverside.entity;

import jakarta.persistence.*;
import lombok.*;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.territory.domain.model.campement.Campement;
import onl.tesseract.srp.territory.adapter.serverside.entity.chunk.CampementChunkEntity;
import onl.tesseract.srp.territory.adapter.serverside.entity.chunk.ChunkEntityId;
import onl.tesseract.srp.territory.domain.model.campement.CampementChunk;

import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Entity
@Table(name = "t_campements")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampementEntity {

    @Id
    private UUID id; // owner ID

    @Embedded
    private CoordinateEntity spawnLocation;

    @Column(name = "camp_level")
    private int campLevel;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "t_campement_trusted_players", joinColumns = @JoinColumn(name = "campement_id"))
    private Set<UUID> trustedPlayers = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "campement", fetch = FetchType.EAGER)
    private Set<CampementChunkEntity> chunks = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (spawnLocation == null) {
            spawnLocation = new CoordinateEntity();
        }
    }

    public static CampementEntity fromDomain(Campement campement) {
        CampementEntity entity = CampementEntity
                .builder()
                .id(campement.getId())
                .spawnLocation(new CoordinateEntity(campement.getSpawnpoint()))
                .campLevel(campement.getCampLevel())
                .build();
        entity.trustedPlayers = new HashSet<>(campement.getTrusted());
        entity.chunks = campement.getChunks().stream()
                .map(chunk -> new CampementChunkEntity(ChunkEntityId.fromDomain(chunk.getChunkCoord()), entity))
                .collect(Collectors.toSet());
        return entity;
    }

    public Campement toDomain() {
        Coordinate spawn = spawnLocation .toDomain();
        Campement campement = new Campement(id, campLevel, spawn,
                trustedPlayers == null ? new HashSet<>() : new HashSet<>(trustedPlayers));
        if (chunks != null) {
            campement.addChunks(chunks.stream()
                    .map(entity -> new CampementChunk(entity.getChunkEntityId().toDomain(), campement))
                    .collect(Collectors.toSet()));
        }
        return campement;
    }
}


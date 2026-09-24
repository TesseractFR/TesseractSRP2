package onl.tesseract.srp.territory.adapter.serverside.entity;

import jakarta.persistence.*;
import lombok.*;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.territory.domain.model.campement.Campement;

import java.util.UUID;

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

    @PrePersist
    public void prePersist() {
        if (spawnLocation == null) {
            spawnLocation = new CoordinateEntity();
        }
    }

    public static CampementEntity fromDomain(Campement campement) {
        return CampementEntity
                .builder()
                .id(campement.getId())
                .spawnLocation(new CoordinateEntity(campement.getSpawnpoint()))
                .campLevel(campement.getCampLevel())
                .build();
    }

    public Campement toDomain() {
        Coordinate spawn = spawnLocation .toDomain();
        return new Campement(id, campLevel, spawn);
    }
}


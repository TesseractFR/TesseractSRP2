package onl.tesseract.srp.skill.adapter.serverside.jpa.entity.station;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "crafting_stations")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StationEntity {
    @EmbeddedId
    private StationEntityKey key;

    @Column(name = "tier_level", nullable = false)
    private int tierLevel;

    @Column(name = "quality_bonus_level", nullable = false)
    private int qualityBonusLevel;

    @Column(name = "success_bonus_level", nullable = false)
    private int successBonusLevel;

    @Column(name = "time_reduc_level", nullable = false)
    private int timeReducLevel;

}

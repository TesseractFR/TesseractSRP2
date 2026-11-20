package onl.tesseract.srp.commission.adapter.serverside.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import onl.tesseract.srp.common.domain.model.enums.Quality;

/** Entité JPA représentant un tableau persisté dans la table {@code t_commission_boards}. */
@Entity
@Data
@NoArgsConstructor
@Table(name = "t_commission_boards")
public class CommissionBoardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nul pour le tableau du spawn. */
    @Column(name = "guild_id", length = 36, unique = true)
    private String guildId;

    @Column(name = "max_craft_tier", nullable = false)
    private int maxCraftTier;

    @Column(name = "max_harvest_tier", nullable = false)
    private int maxHarvestTier;

    @Column(name = "slots_per_type", nullable = false)
    private int slotsPerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "max_required_quality", nullable = false)
    private Quality maxRequiredQuality;

    @Column(name = "money_bonus_level", nullable = false)
    private int moneyBonusLevel;

    @Column(name = "usage_tax_rate", nullable = false)
    private double usageTaxRate;

    @Column(name = "completed_commission_count", nullable = false)
    private int completedCommissionCount;

    /** Les quatre colonnes de position sont nulles tant que le tableau n'est pas posé. */
    @Column(name = "position_world")
    private String positionWorld;

    @Column(name = "position_x")
    private Integer positionX;

    @Column(name = "position_y")
    private Integer positionY;

    @Column(name = "position_z")
    private Integer positionZ;
}

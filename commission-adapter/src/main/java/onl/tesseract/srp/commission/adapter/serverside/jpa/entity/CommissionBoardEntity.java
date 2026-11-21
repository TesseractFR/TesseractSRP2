package onl.tesseract.srp.commission.adapter.serverside.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "board_tier", nullable = false)
    private int boardTier;

    @Column(name = "max_harvest_tier", nullable = false)
    private int maxHarvestTier;

    @Column(name = "max_craft_tier", nullable = false)
    private int maxCraftTier;

    @Column(name = "harvest_slots", nullable = false)
    private int harvestSlots;

    @Column(name = "craft_slots", nullable = false)
    private int craftSlots;

    @Column(name = "money_bonus_level", nullable = false)
    private int moneyBonusLevel;

    @Column(name = "usage_tax_rate", nullable = false)
    private double usageTaxRate;

    @Column(name = "completed_commission_count", nullable = false)
    private int completedCommissionCount;
}

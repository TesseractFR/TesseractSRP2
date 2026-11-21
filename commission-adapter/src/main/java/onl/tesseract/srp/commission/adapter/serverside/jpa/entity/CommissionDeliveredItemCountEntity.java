package onl.tesseract.srp.commission.adapter.serverside.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.common.domain.model.enums.Quality;

/**
 * Nombre d'items livrés sur un tableau, par type de commission et par qualité demandée, dans la table
 * {@code t_commission_delivered_items}. Il sert aux conditions du tier du tableau.
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "t_commission_delivered_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"board_id", "type", "minimal_quality"}))
public class CommissionDeliveredItemCountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommissionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "minimal_quality", nullable = false)
    private Quality minimalQuality;

    @Column(name = "item_count", nullable = false)
    private int itemCount;
}

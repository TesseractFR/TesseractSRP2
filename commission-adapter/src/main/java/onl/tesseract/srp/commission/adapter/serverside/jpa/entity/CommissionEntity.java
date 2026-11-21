package onl.tesseract.srp.commission.adapter.serverside.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardPart;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.common.domain.model.enums.Quality;

/** Entité JPA représentant une commission persistée dans la table {@code t_commissions}. */
@Entity
@Data
@NoArgsConstructor
@Table(name = "t_commissions", indexes = @Index(name = "idx_commissions_board_id", columnList = "board_id"))
public class CommissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommissionBoardPart part;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommissionType type;

    @Column(nullable = false)
    private String material;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "delivered_quantity", nullable = false)
    private int deliveredQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "minimal_quality", nullable = false)
    private Quality minimalQuality;

    @Column(nullable = false)
    private int reward;
}

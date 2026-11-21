package onl.tesseract.srp.commission.domain.port.serverside;

import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;
import onl.tesseract.srp.commission.domain.model.commission.Commission;
import onl.tesseract.srp.commission.domain.model.commission.CommissionDraft;
import onl.tesseract.srp.commission.domain.model.commission.CommissionId;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.commission.domain.model.commission.CommissionDelivery;
import onl.tesseract.srp.common.domain.model.enums.Quality;

import java.util.List;
import java.util.Optional;

/** Port de persistance des commissions et d'enregistrement des livraisons. */
public interface CommissionRepository {
    /** @return la commission, ou {@link Optional#empty()} si elle n'existe plus */
    Optional<Commission> findById(CommissionId commissionId);

    /** @return les commissions du tableau, de la plus ancienne à la plus récente, liste vide si aucune */
    List<Commission> findAllByBoard(CommissionBoardId boardId);

    /**
     * Applique en une seule transaction la progression de la commission, le gain du livreur, la taxe
     * et l'XP de la guilde propriétaire du tableau, le compteur de commissions terminées et celui des
     * items livrés, compté à la qualité minimale de la commission.
     */
    void recordDelivery(CommissionDelivery delivery);

    /** @return items livrés sur ce tableau, sur des commissions de ce type qui demandaient exactement cette qualité */
    int countDeliveredItems(CommissionBoardId boardId, CommissionType type, Quality minimalQuality);

    /** Supprime {@code removedCommissions} et enregistre {@code newCommissions} en une seule transaction. */
    void replaceCommissions(List<CommissionId> removedCommissions, List<CommissionDraft> newCommissions);
}

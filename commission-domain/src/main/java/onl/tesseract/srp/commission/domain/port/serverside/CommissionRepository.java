package onl.tesseract.srp.commission.domain.port.serverside;

import onl.tesseract.srp.commission.domain.model.board.BoardId;
import onl.tesseract.srp.commission.domain.model.commission.Commission;
import onl.tesseract.srp.commission.domain.model.commission.CommissionDraft;
import onl.tesseract.srp.commission.domain.model.commission.CommissionId;
import onl.tesseract.srp.commission.domain.model.commission.Delivery;

import java.util.List;
import java.util.Optional;

/** Port de persistance des commissions et d'enregistrement des livraisons. */
public interface CommissionRepository {
    /** @return la commission, ou {@link Optional#empty()} si elle n'existe plus */
    Optional<Commission> findById(CommissionId commissionId);

    /** @return les commissions du tableau, de la plus ancienne à la plus récente, liste vide si aucune */
    List<Commission> findAllByBoard(BoardId boardId);

    /**
     * Applique en une seule transaction la progression de la commission, le gain du livreur, la taxe
     * et l'XP de la guilde propriétaire du tableau, et le compteur de commissions terminées.
     */
    void recordDelivery(Delivery delivery);

    /** Supprime {@code removedCommissions} et enregistre {@code newCommissions} en une seule transaction. */
    void replaceCommissions(List<CommissionId> removedCommissions, List<CommissionDraft> newCommissions);
}

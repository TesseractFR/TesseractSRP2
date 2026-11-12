package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.Contract;

/** Port d'envoi des messages liés aux contrats et aux tickets. */
public interface ContractMessagingRepository {
    /** Annonce la réussite de l'achat et le solde restant. */
    void sendPurchaseSucceeded(PlayerId player, Contract contract, int ticketsLeft);
    /** Informe le joueur qu'il ne possède pas de ticket. */
    void sendPurchaseNoTicket(PlayerId player);
    /** Informe le joueur que son inventaire ne peut pas recevoir le parchemin. */
    void sendPurchaseInventoryFull(PlayerId player);
    /** Informe le joueur qu'aucun contrat n'est disponible à l'achat. */
    void sendPurchaseNoContractAvailable(PlayerId player);

    // livraison : c'est le PNJ (métier ou compétence) qui parle, d'où le paramètre npcJob
    /** Annonce au joueur la réussite de la livraison et le montant versé. */
    void sendDeliverySucceeded(PlayerId player, String npcJob, int reward);
    /** Informe le joueur que le contrat vise un autre métier. */
    void sendDeliveryWrongClient(PlayerId player, String npcJob);
    /** Informe le joueur que le parchemin est invalide. */
    void sendDeliveryInvalidScroll(PlayerId player, String npcJob);
    /** Informe le joueur que le contrat a expiré. */
    void sendDeliveryExpired(PlayerId player, String npcJob);
    /** Informe le joueur du nombre d'objets manquants. */
    void sendDeliveryMissingItems(PlayerId player, String npcJob, int missing);
    /** Informe le joueur du nombre d'objets de qualité requise manquants. */
    void sendDeliveryNotEnoughQuality(PlayerId player, String npcJob, int missing);
    /** Informe le joueur que le paiement a échoué. */
    void sendDeliveryPaymentFailed(PlayerId player, String npcJob);
    /** Informe le joueur qu'il doit tenir un parchemin pour interagir avec le PNJ. */
    void sendNoScrollInHand(PlayerId player, String npcJob);

    // parchemin et tickets
    /** Informe le joueur qu'un parchemin invalide ou expiré a été détruit. */
    void sendScrollDestroyed(PlayerId player);
    /** Informe le joueur du nombre de parchemins expirés. */
    void sendScrollsExpired(PlayerId player, int count);
    /** Présente au joueur son solde de tickets et son quota quotidien. */
    void sendTicketsShown(PlayerId player, int tickets, int dailyQuota);

    // staff
    /** Signale au membre du staff que la quantité fournie est invalide. */
    void sendStaffInvalidAmount(PlayerId staff);
    /** Confirme au membre du staff l'attribution de tickets à un joueur. */
    void sendStaffTicketsGiven(PlayerId staff, PlayerId target, int amount);
    /** Présente au membre du staff le solde de tickets et le quota d'un joueur. */
    void sendStaffTicketsShown(PlayerId staff, PlayerId target, int tickets, int dailyQuota);
}

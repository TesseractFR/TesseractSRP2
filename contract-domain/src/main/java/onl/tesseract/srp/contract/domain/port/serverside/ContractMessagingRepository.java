package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.DeliveryRefusal;

public interface ContractMessagingRepository {
    // achat
    void sendPurchaseSucceeded(PlayerId player, Contract contract, int ticketsLeft);
    void sendPurchaseNoTicket(PlayerId player);
    void sendPurchaseInventoryFull(PlayerId player);
    void sendPurchaseNoContractAvailable(PlayerId player);

    // livraison : c'est le PNJ (métier ou compétence) qui parle, d'où le paramètre npcJob
    void sendDeliverySucceeded(PlayerId player, String npcJob, int reward);
    void sendDeliveryRefused(PlayerId player, String npcJob, DeliveryRefusal reason, int missing);
    void sendNoScrollInHand(PlayerId player, String npcJob);

    // parchemin et tickets
    void sendScrollDestroyed(PlayerId player);
    void sendScrollsExpired(PlayerId player, int count);
    void sendTicketsShown(PlayerId player, int tickets, int dailyQuota);

    // staff
    void sendStaffInvalidAmount(PlayerId staff);
    void sendStaffTicketsGiven(PlayerId staff, PlayerId target, int amount);
    void sendStaffTicketsShown(PlayerId staff, PlayerId target, int tickets, int dailyQuota);
}
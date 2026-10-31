package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.DeliveryRefusal;

public interface ContractMessagingRepository {
    // achat
    void sendPurchaseSucceeded(PlayerID player, Contract contract, int ticketsLeft);
    void sendPurchaseNoTicket(PlayerID player);
    void sendPurchaseInventoryFull(PlayerID player);
    void sendPurchaseNoContractAvailable(PlayerID player);

    // livraison : c'est le PNJ (métier ou compétence) qui parle, d'où le paramètre npcJob
    void sendDeliverySucceeded(PlayerID player, String npcJob, int reward);
    void sendDeliveryRefused(PlayerID player, String npcJob, DeliveryRefusal reason, int missing);
    void sendNoScrollInHand(PlayerID player, String npcJob);

    // parchemin et tickets
    void sendScrollDestroyed(PlayerID player);
    void sendScrollsExpired(PlayerID player, int count);
    void sendTicketsShown(PlayerID player, int tickets, int dailyQuota);

    // staff
    void sendStaffInvalidAmount(PlayerID staff);
    void sendStaffTicketsGiven(PlayerID staff, PlayerID target, int amount);
    void sendStaffTicketsShown(PlayerID staff, PlayerID target, int tickets, int dailyQuota);
}
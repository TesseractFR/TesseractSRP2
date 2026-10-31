package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.contract.domain.model.PlayerID;

import java.util.OptionalInt;

public interface PlayerInfoRepository {
    OptionalInt findTickets(PlayerID player);
    void saveTickets(PlayerID player, int tickets);
    int getDailyQuota(PlayerID player);
    double getMoneyBonus(PlayerID player, String job, String material);
}
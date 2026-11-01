package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;

import java.util.OptionalInt;

public interface PlayerInfoRepository {
    OptionalInt findTickets(PlayerId player);
    void saveTickets(PlayerId player, int tickets);
    int getDailyQuota(PlayerId player);
    double getMoneyBonus(PlayerId player, String job, String material);
}
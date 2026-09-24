package onl.tesseract.srp.territory.domain.port.userside.guild;

import java.util.UUID;

public interface MoneyService {
    void transfertToGuild(UUID playerID, int amount);

    int getPlayerMoney(UUID playerID);

    void transfertFromGuild(UUID playerID, int amount);

    void payCreation(UUID playerID, int guildCost);

    void transfertToGuildStaff(int amount);
}

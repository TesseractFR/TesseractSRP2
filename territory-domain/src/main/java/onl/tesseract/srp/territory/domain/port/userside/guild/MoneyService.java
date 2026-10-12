package onl.tesseract.srp.territory.domain.port.userside.guild;

import java.util.UUID;

public interface MoneyService {
    boolean transfertToGuild(UUID playerID, int amount);

    int getPlayerMoney(UUID playerID);

    boolean transfertFromGuild(UUID playerID, int amount);

    boolean payCreation(UUID playerID, int guildCost);

}

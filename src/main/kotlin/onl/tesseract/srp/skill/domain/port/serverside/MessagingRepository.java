package onl.tesseract.srp.skill.domain.port.serverside;

import onl.tesseract.srp.skill.domain.model.PlayerID;
import onl.tesseract.srp.skill.domain.model.recipe.Tier;

public interface MessagingRepository {
    void sendInsuffisantTableTier(PlayerID player, Tier tier);

    void sendInsuffisantComponent(PlayerID player);

    void sendCraftStarted(PlayerID player);

    void sendCraftQueued(PlayerID player);
}

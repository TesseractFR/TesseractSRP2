package onl.tesseract.srp.skill.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.skill.domain.model.recipe.Tier;

public interface MessagingRepository {
    void sendInsuffisantTableTier(PlayerId player, Tier tier);

    void sendInsuffisantComponent(PlayerId player);

    void sendCraftStarted(PlayerId player);

    void sendCraftQueued(PlayerId player);
}

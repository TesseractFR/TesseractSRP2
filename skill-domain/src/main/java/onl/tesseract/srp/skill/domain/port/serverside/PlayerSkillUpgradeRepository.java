package onl.tesseract.srp.skill.domain.port.serverside;

import onl.tesseract.srp.skill.domain.model.PlayerID;
import onl.tesseract.srp.skill.domain.model.Quality;
import onl.tesseract.srp.skill.domain.model.skill.SkillName;

public interface PlayerSkillUpgradeRepository {
    Quality getMaximumIngredientQuality(PlayerID player, SkillName skill);

    void saveMaximumIngredientQuality(PlayerID player, SkillName skill, Quality quality);
}

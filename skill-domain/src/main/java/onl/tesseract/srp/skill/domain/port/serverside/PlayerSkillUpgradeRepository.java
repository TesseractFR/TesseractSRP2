package onl.tesseract.srp.skill.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.skill.domain.model.skill.SkillName;

public interface PlayerSkillUpgradeRepository {
    Quality getMaximumIngredientQuality(PlayerId player, SkillName skill);

    void saveMaximumIngredientQuality(PlayerId player, SkillName skill, Quality quality);
}

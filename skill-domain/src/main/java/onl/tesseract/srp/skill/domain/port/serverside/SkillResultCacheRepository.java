package onl.tesseract.srp.skill.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.skill.domain.model.crafting.LootCache;
import onl.tesseract.srp.skill.domain.model.crafting.PlayerLootCache;
import onl.tesseract.srp.skill.domain.model.skill.SkillName;
import lombok.NonNull;

import java.util.Map;

public interface SkillResultCacheRepository {

    Map<PlayerId, PlayerLootCache> findAll();

    void delete(@NonNull PlayerId playerID, @NonNull SkillName skillName);

    void save(PlayerId player, SkillName skill, LootCache lootCache);
}

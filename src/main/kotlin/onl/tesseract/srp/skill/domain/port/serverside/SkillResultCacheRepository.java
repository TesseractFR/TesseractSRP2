package onl.tesseract.srp.skill.domain.port.serverside;

import onl.tesseract.srp.skill.domain.model.PlayerID;
import onl.tesseract.srp.skill.domain.model.crafting.LootCache;
import onl.tesseract.srp.skill.domain.model.crafting.PlayerLootCache;
import onl.tesseract.srp.skill.domain.model.skill.SkillName;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface SkillResultCacheRepository {

    Map<PlayerID, PlayerLootCache> findAll();

    void delete(@NotNull PlayerID playerID, @NotNull SkillName skillName);

    void save(PlayerID player, SkillName skill, LootCache lootCache);
}

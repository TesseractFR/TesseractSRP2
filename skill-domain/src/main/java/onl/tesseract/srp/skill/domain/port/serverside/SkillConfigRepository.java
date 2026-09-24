package onl.tesseract.srp.skill.domain.port.serverside;

import onl.tesseract.srp.skill.domain.model.skill.Skill;

import java.util.Map;

public interface SkillConfigRepository {
    Map<String, Skill> getSkills();
}

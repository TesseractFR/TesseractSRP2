package onl.tesseract.srp.skill.domain.port.userside;

import onl.tesseract.srp.skill.domain.model.skill.Skill;
import onl.tesseract.srp.skill.domain.port.serverside.SkillConfigRepository;

public class SkillService {

    private final SkillConfigRepository skillConfigRepository;

    public SkillService(SkillConfigRepository skillConfigRepository) {
        this.skillConfigRepository = skillConfigRepository;
    }


    public Skill getSkillFromStructureID(String structureID) {

        return skillConfigRepository.getSkills().values()
                .stream()
                .filter(skill -> skill.structure().value().equals(structureID))
                .findFirst().orElse(null);
    }
}

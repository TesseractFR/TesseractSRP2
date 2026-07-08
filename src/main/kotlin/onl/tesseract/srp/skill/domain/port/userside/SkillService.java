package onl.tesseract.srp.skill.domain.port.userside;

import onl.tesseract.srp.repository.yaml.skill.SkillConfigRepository;
import onl.tesseract.srp.skill.domain.model.skill.Skill;

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

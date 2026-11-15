package onl.tesseract.srp.contract.adapter.serverside.skill;

import onl.tesseract.srp.skill.domain.model.skill.Skill;
import onl.tesseract.srp.skill.domain.port.serverside.SkillConfigRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Lit les compétences dans le repository de skill-domain.
 * <p>
 * Viole volontairement la règle « pas de repository d'un autre domaine » (dette A-1) : à remplacer
 * par {@code SkillService} dès qu'il exposera la liste des compétences.
 */
@Component
public class SkillConfigSkillCatalogPort implements SkillCatalogPort {
    private final SkillConfigRepository skillConfigRepository;

    public SkillConfigSkillCatalogPort(SkillConfigRepository skillConfigRepository) {
        this.skillConfigRepository = skillConfigRepository;
    }

    @Override
    public List<Skill> findAllSkills() {
        return List.copyOf(skillConfigRepository.getSkills().values());
    }
}

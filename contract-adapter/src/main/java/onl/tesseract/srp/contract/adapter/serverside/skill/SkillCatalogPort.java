package onl.tesseract.srp.contract.adapter.serverside.skill;

import onl.tesseract.srp.skill.domain.model.skill.Skill;

import java.util.List;

/**
 * Accès à la liste des compétences pour le module contrat.
 * <p>
 * Dette connue : skill-domain n'expose pas encore cette liste par un service. Ce port isole la
 * seule dépendance au repository de skill-domain dans {@link SkillConfigSkillCatalogPort}, en
 * attendant {@code SkillService.findAllSkills()}.
 */
public interface SkillCatalogPort {
    /** @return toutes les compétences configurées, liste vide si aucune */
    List<Skill> findAllSkills();
}

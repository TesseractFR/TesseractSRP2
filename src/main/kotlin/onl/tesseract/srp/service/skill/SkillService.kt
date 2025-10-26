package onl.tesseract.srp.service.skill

import onl.tesseract.srp.domain.skill.Skill
import onl.tesseract.srp.repository.yaml.skill.SkillConfigRepository
import org.springframework.stereotype.Component

@Component
class SkillService(val skillConfigRepository: SkillConfigRepository) {

    fun getSkillFromStructureID(structureID: String) : Skill?{
        return skillConfigRepository.getSkills()
                .map { it.value }
                .firstOrNull { it.structureName == structureID }

    }

}
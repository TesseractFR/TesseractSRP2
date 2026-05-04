package onl.tesseract.srp.repository.generic.skill

import onl.tesseract.srp.repository.hibernate.skill.SkillResultCacheEntity
import java.util.*

interface SkillResultCacheRepository {
    fun save(entity: SkillResultCacheEntity): SkillResultCacheEntity
    fun findByPlayerAndSkill(playerUuid: UUID, skillName: String): SkillResultCacheEntity?
    fun deleteByPlayerAndSkill(playerUuid: UUID, skillName: String)
    fun findAll(): List<SkillResultCacheEntity>
}

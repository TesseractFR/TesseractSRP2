package onl.tesseract.srp.repository.generic.skill

import onl.tesseract.srp.repository.hibernate.skill.CraftTaskEntity
import java.util.*

interface CraftTaskRepository {
    fun save(entity: CraftTaskEntity): CraftTaskEntity
    fun findByPlayer(playerUuid: UUID): List<CraftTaskEntity>
    fun deleteByPlayerAndSkill(playerUuid: UUID, skillName: String)
    fun saveAll(entities: List<CraftTaskEntity>): List<CraftTaskEntity>
    fun findAll(): List<CraftTaskEntity>
    fun delete(entity: CraftTaskEntity)
}

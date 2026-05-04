package onl.tesseract.srp.repository.hibernate.skill

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface SkillResultCacheJpaRepository : JpaRepository<SkillResultCacheEntity, Long> {
    fun findByPlayerUuidAndSkillName(playerUuid: UUID, skillName: String): SkillResultCacheEntity?
    fun deleteByPlayerUuidAndSkillName(playerUuid: UUID, skillName: String)
}

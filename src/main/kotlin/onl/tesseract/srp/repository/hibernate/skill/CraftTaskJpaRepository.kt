package onl.tesseract.srp.repository.hibernate.skill

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CraftTaskJpaRepository : JpaRepository<CraftTaskEntity, Long> {
    fun findByPlayerUuid(playerUuid: UUID): List<CraftTaskEntity>
    fun deleteByPlayerUuidAndSkillName(playerUuid: UUID, skillName: String)
}

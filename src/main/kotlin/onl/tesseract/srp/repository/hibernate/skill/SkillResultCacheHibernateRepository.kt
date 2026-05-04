package onl.tesseract.srp.repository.hibernate.skill

import onl.tesseract.srp.repository.generic.skill.SkillResultCacheRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
open class SkillResultCacheHibernateRepository(private val skillResultCacheJpaRepository: SkillResultCacheJpaRepository) : SkillResultCacheRepository {

    override fun save(entity: SkillResultCacheEntity): SkillResultCacheEntity {
        return skillResultCacheJpaRepository.save(entity)
    }

    override fun findByPlayerAndSkill(playerUuid: UUID, skillName: String): SkillResultCacheEntity? {
        return skillResultCacheJpaRepository.findByPlayerUuidAndSkillName(playerUuid, skillName)
    }

    @Transactional
    override fun deleteByPlayerAndSkill(playerUuid: UUID, skillName: String) {
        skillResultCacheJpaRepository.deleteByPlayerUuidAndSkillName(playerUuid, skillName)
    }

    override fun findAll(): List<SkillResultCacheEntity> {
        return skillResultCacheJpaRepository.findAll()
    }
}

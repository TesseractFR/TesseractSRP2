package onl.tesseract.srp.repository.hibernate.skill

import onl.tesseract.srp.repository.generic.skill.CraftTaskRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
open class CraftTaskHibernateRepository(private val craftTaskJpaRepository: CraftTaskJpaRepository) : CraftTaskRepository {

    override fun save(entity: CraftTaskEntity): CraftTaskEntity {
        return craftTaskJpaRepository.save(entity)
    }

    override fun findByPlayer(playerUuid: UUID): List<CraftTaskEntity> {
        return craftTaskJpaRepository.findByPlayerUuid(playerUuid)
    }

    @Transactional
    override fun deleteByPlayerAndSkill(playerUuid: UUID, skillName: String) {
        craftTaskJpaRepository.deleteByPlayerUuidAndSkillName(playerUuid, skillName)
    }

    override fun saveAll(entities: List<CraftTaskEntity>): List<CraftTaskEntity> {
        return craftTaskJpaRepository.saveAll(entities)
    }

    override fun findAll(): List<CraftTaskEntity> {
        return craftTaskJpaRepository.findAll()
    }
    
    override fun delete(entity: CraftTaskEntity) {
        craftTaskJpaRepository.delete(entity)
    }
}

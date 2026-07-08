package onl.tesseract.srp.skill.adapter.serverside.jpa.repository;

import jakarta.transaction.Transactional;
import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.PlayerSkillEmbeddable;
import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.craft.CraftingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CraftingJpaRepository extends JpaRepository<CraftingEntity, Long> {

    @Transactional
    void deleteByPlayerSkillAndQueueOrder(PlayerSkillEmbeddable playerSkill, int queueOrder);

    @Query("SELECT c FROM CraftingEntity c WHERE c.playerSkill.playerUuid = :playerUuid AND c.playerSkill.skillName = :skillName")
    List<CraftingEntity> findByPlayerSkill(@Param("playerUuid") java.util.UUID playerUuid, @Param("skillName") String skillName);

    @Transactional
    @Modifying
    @Query("DELETE FROM CraftingEntity c WHERE c.playerSkill.playerUuid = :playerUuid AND c.playerSkill.skillName = :skillName AND c.queueOrder >0")
    void deleteQueueByPlayerSkill(@Param("playerUuid") java.util.UUID playerUuid, @Param("skillName") String skillName);

}

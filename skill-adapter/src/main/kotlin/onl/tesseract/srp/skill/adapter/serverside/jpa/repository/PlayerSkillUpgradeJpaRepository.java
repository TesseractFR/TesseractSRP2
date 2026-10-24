package onl.tesseract.srp.skill.adapter.serverside.jpa.repository;

import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.PlayerSkillEmbeddable;
import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.PlayerSkillUpgradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerSkillUpgradeJpaRepository extends JpaRepository<PlayerSkillUpgradeEntity, PlayerSkillEmbeddable> {
}

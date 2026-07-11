package onl.tesseract.srp.skill.adapter.serverside.jpa.repository;

import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.cache.CacheEntity;
import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.PlayerSkillEmbeddable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SkillResultCacheJpaRepository extends JpaRepository<CacheEntity, Long> {

    void deleteByPlayerSkill(PlayerSkillEmbeddable playerSkill);

}

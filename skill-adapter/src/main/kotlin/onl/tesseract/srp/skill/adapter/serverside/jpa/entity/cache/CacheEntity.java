package onl.tesseract.srp.skill.adapter.serverside.jpa.entity.cache;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.ItemEmbeddable;
import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.PlayerSkillEmbeddable;

import java.util.List;

@Entity
@Table(schema = "t_skill_result_cache")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CacheEntity {
    @Id
    private Long id;

    @Embedded
    private PlayerSkillEmbeddable playerSkill;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "t_skill_result_cache_done", joinColumns = {@JoinColumn(name =  "cache_id")})
    private List<ItemEmbeddable> done;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "t_skill_result_cache_garbage", joinColumns = {@JoinColumn(name = "cache_id")})
    private List<ItemEmbeddable> garbage;
}

package onl.tesseract.srp.skill.adapter.serverside.jpa.entity.craft;

import jakarta.persistence.*;
import lombok.*;
import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.PlayerSkillEmbeddable;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_craft_task")
public class CraftingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private PlayerSkillEmbeddable playerSkill;

    private int queueOrder;

    @Embedded
    private RecipeEmbeddable recipe;

    @Embedded
    private CraftBonusEmbeddable craftBonus;

    private Long unitDurationNanos;

    private Long timeLeftNanos;

}

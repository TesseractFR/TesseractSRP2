package onl.tesseract.srp.skill.adapter.serverside.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_skill_upgrades")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerSkillUpgradeEntity {
    @EmbeddedId
    private PlayerSkillEmbeddable key;

    @Column(name = "max_ingredient_quality", nullable = false)
    private int maxIngredientQuality;
}

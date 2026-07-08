package onl.tesseract.srp.skill.adapter.serverside.jpa.entity.craft;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CraftBonusEmbeddable {
    private double qualityBonus;
    private double successBonus;
}

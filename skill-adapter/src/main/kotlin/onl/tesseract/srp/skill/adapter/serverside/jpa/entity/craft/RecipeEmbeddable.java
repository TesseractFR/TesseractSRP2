package onl.tesseract.srp.skill.adapter.serverside.jpa.entity.craft;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import onl.tesseract.srp.common.domain.model.enums.Quality;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeEmbeddable {
    private String recipeID;

    private Quality quality;

    private int quantity;
}

package onl.tesseract.srp.skill.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.skill.domain.model.recipe.Material;
import lombok.NonNull;

public interface ItemRepository {
    int getItemMaxSize(Material material);

    void giveItem(@NonNull PlayerId playerID, Material material, int quantity, Quality quality);

    int getItemNumber(PlayerId player, Material material, Quality quality);

    void removeItems(PlayerId player, Material material, int totalNeeded, Quality quality);
}

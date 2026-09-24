package onl.tesseract.srp.skill.domain.port.serverside;

import onl.tesseract.srp.skill.domain.model.PlayerID;
import onl.tesseract.srp.skill.domain.model.Quality;
import onl.tesseract.srp.skill.domain.model.recipe.Material;
import lombok.NonNull;

public interface ItemRepository {
    int getItemMaxSize(Material material);

    void giveItem(@NonNull PlayerID playerID, Material material, int quantity, Quality quality);

    int getItemNumber(PlayerID player, Material material, Quality quality);

    void removeItems(PlayerID player, Material material, int totalNeeded, Quality quality);
}

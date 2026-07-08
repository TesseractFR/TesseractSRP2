package onl.tesseract.srp.skill.domain.port.serverside;

import onl.tesseract.srp.skill.domain.model.PlayerID;
import onl.tesseract.srp.skill.domain.model.Quality;
import onl.tesseract.srp.skill.domain.model.recipe.Material;
import org.jetbrains.annotations.NotNull;

public interface ItemRepository {
    int getItemMaxSize(Material material);

    void giveItem(@NotNull PlayerID playerID, Material material, int quantity);

    int getItemNumber(PlayerID player, Material material, Quality poor);

    void removeItems(PlayerID player, Material material, int totalNeeded);
}

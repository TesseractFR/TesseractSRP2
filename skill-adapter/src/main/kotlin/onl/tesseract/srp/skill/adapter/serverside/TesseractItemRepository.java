package onl.tesseract.srp.skill.adapter.serverside;

import onl.tesseract.srp.customitem.domain.model.MaterialName;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.skill.domain.model.recipe.Material;
import onl.tesseract.srp.skill.domain.port.serverside.ItemRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class TesseractItemRepository implements ItemRepository {

    private final CustomItemService itemGateway;

    public TesseractItemRepository(CustomItemService itemGateway) {
        this.itemGateway = itemGateway;
    }

    @Override
    public int getItemMaxSize(Material material) {
        return itemGateway.getItemStack(new MaterialName(material.value())).getMaxStackSize();
    }

    @Override
    public void giveItem(@NotNull PlayerId playerID, Material material, int quantity, Quality quality) {
        itemGateway.addItem(playerID.value(), new MaterialName(material.value()),
                onl.tesseract.srp.common.domain.model.enums.Quality.valueOf(quality.toString()), quantity);
    }

    @Override
    public int getItemNumber(PlayerId player, Material material, Quality quality) {
        return itemGateway.getItemQuantity(player.value(), new MaterialName(material.value()), onl.tesseract.srp.common.domain.model.enums.Quality.valueOf(quality.toString()));
    }

    @Override
    public void removeItems(PlayerId player, Material material, int totalNeeded, Quality quality) {
        itemGateway.removeItem(player.value(), new MaterialName(material.value()), onl.tesseract.srp.common.domain.model.enums.Quality.valueOf(quality.toString()), totalNeeded);

    }
}

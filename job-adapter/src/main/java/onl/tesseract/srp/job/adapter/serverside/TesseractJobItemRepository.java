package onl.tesseract.srp.job.adapter.serverside;

import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.customitem.domain.model.MaterialName;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import onl.tesseract.srp.job.domain.model.Material;
import onl.tesseract.srp.job.domain.model.PlayerID;
import onl.tesseract.srp.job.domain.port.serverside.ItemRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class TesseractJobItemRepository implements ItemRepository {
    private final CustomItemService customItemService;

    public TesseractJobItemRepository(CustomItemService customItemService) {
        this.customItemService = customItemService;
    }

    @Override
    public void giveItem(@NotNull PlayerID playerID, Material material, int quantity, Quality quality) {
        customItemService.addItem(playerID.value(),
                new MaterialName(material.value()),
                Quality.valueOf(quality.name()), quantity);
    }
}

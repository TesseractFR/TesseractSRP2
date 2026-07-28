package onl.tesseract.srp.job.adapter.serverside;

import onl.tesseract.srp.customitem.adapter.userside.ItemGateway;
import onl.tesseract.srp.customitem.domain.model.MaterialName;
import onl.tesseract.srp.job.domain.model.Material;
import onl.tesseract.srp.job.domain.model.PlayerID;
import onl.tesseract.srp.job.domain.model.Quality;
import onl.tesseract.srp.job.domain.port.serverside.ItemRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class TesseractJobItemRepository implements ItemRepository {
    private final ItemGateway itemGateway;

    public TesseractJobItemRepository(ItemGateway itemGateway) {
        this.itemGateway = itemGateway;
    }

    @Override
    public void giveItem(@NotNull PlayerID playerID, Material material, int quantity, Quality quality) {
        itemGateway.addItem(playerID.value(),
                new MaterialName(material.value()),
                onl.tesseract.srp.customitem.domain.model.Quality.valueOf(quality.name()), quantity);
    }
}

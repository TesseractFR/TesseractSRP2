package onl.tesseract.srp.job.adapter.serverside;

import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import onl.tesseract.srp.job.domain.model.Material;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.job.domain.port.serverside.JobRewardPort;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class CustomItemJobRewardPort implements JobRewardPort {
    private final CustomItemService customItemService;

    public CustomItemJobRewardPort(CustomItemService customItemService) {
        this.customItemService = customItemService;
    }

    @Override
    public void giveItem(@NotNull PlayerId playerId, Material material, int quantity, Quality quality) {
        customItemService.addItem(playerId.value(),
                new MaterialName(material.value()),
                Quality.valueOf(quality.name()), quantity);
    }
}

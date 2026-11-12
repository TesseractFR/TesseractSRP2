package onl.tesseract.srp.contract.adapter.serverside;

import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerMaterialRepository;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Consulte et retire les matériaux admissibles dans l'inventaire via {@code CustomItemService}.
 */
@org.springframework.stereotype.Component
public class TesseractPlayerMaterialRepository implements PlayerMaterialRepository {

    private final CustomItemService customItemService;

    public TesseractPlayerMaterialRepository(CustomItemService customItemService) {
        this.customItemService = customItemService;
    }

    @Override
    public int countAtLeast(PlayerId player, MaterialName material, Quality minimalQuality) {
        if (!customItemService.isCustomMaterial(material)) {
            return customItemService.getItemQuantity(player.value(), material, Quality.POOR);
        }
        int totalAmount = 0;
        for (Quality quality : qualitiesFrom(minimalQuality)) {
            totalAmount += customItemService.getItemQuantity(player.value(), material, quality);
        }
        return totalAmount;
    }

    @Override
    public void removeAtLeast(PlayerId player, MaterialName material, Quality minimalQuality, int amount) {
        if (amount <= 0 || countAtLeast(player, material, minimalQuality) < amount) {
            return;
        }
        UUID playerUuid = player.value();
        Quality[] qualitiesToTake = customItemService.isCustomMaterial(material) ? qualitiesFrom(minimalQuality) : new Quality[]{Quality.POOR};
        int remainingAmount = amount;
        for (Quality quality : qualitiesToTake) {
            int takenAmount = Math.min(customItemService.getItemQuantity(playerUuid, material, quality), remainingAmount);
            if (takenAmount > 0) {
                customItemService.removeItem(playerUuid, material, quality, takenAmount);
                remainingAmount -= takenAmount;
            }
        }
    }

    private static Quality[] qualitiesFrom(Quality minimalQuality) {
        Quality[] allQualities = Quality.values();
        return Arrays.copyOfRange(allQualities, minimalQuality.ordinal(), allQualities.length);
    }
}

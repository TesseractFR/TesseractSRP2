package onl.tesseract.srp.contract.adapter.serverside;

import onl.tesseract.srp.contract.adapter.utils.QualityConverter;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerMaterialRepository;
import onl.tesseract.srp.customitem.domain.model.MaterialName;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@org.springframework.stereotype.Component
public class TesseractPlayerMaterialRepository implements PlayerMaterialRepository {

    private final CustomItemService customItemService;

    public TesseractPlayerMaterialRepository(CustomItemService customItemService) {
        this.customItemService = customItemService;
    }

    @Override
    public int countAtLeast(PlayerId player, String material, Quality minimalQuality) {
        MaterialName materialName = new MaterialName(material);
        if (!customItemService.isCustomMaterial(materialName)) {
            return customItemService.getItemQuantity(player.value(), materialName, QualityConverter.toCommon(Quality.POOR));
        }
        int totalAmount = 0;
        for (Quality quality : qualitiesFrom(minimalQuality)) {
            totalAmount += customItemService.getItemQuantity(player.value(), materialName, QualityConverter.toCommon(quality));
        }
        return totalAmount;
    }

    @Override
    public Map<Quality, Integer> removeAtLeast(PlayerId player, String material, Quality minimalQuality, int amount) {
        Map<Quality, Integer> removedItems = new EnumMap<>(Quality.class);
        if (amount <= 0 || countAtLeast(player, material, minimalQuality) < amount) {
            return removedItems;
        }
        MaterialName materialName = new MaterialName(material);
        UUID playerUuid = player.value();
        Quality[] qualitiesToTake = customItemService.isCustomMaterial(materialName) ? qualitiesFrom(minimalQuality) : new Quality[]{Quality.POOR};
        int remainingAmount = amount;
        try {
            for (Quality quality : qualitiesToTake) {
                int takenAmount = Math.min(customItemService.getItemQuantity(playerUuid, materialName, QualityConverter.toCommon(quality)), remainingAmount);
                if (takenAmount > 0) {
                    customItemService.removeItem(playerUuid, materialName, QualityConverter.toCommon(quality), takenAmount);
                    removedItems.put(quality, takenAmount);
                    remainingAmount -= takenAmount;
                }
            }
        } catch (RuntimeException e) {
            giveBack(player, material, removedItems);
            throw e;
        }
        if (remainingAmount > 0) {
            giveBack(player, material, removedItems);
            return new EnumMap<>(Quality.class);
        }
        return removedItems;
    }

    @Override
    public void giveBack(PlayerId player, String material, Map<Quality, Integer> itemsByQuality) {
        MaterialName materialName = new MaterialName(material);
        itemsByQuality.forEach((quality, amount) -> customItemService.addItem(player.value(), materialName, QualityConverter.toCommon(quality), amount));
    }

    private static Quality[] qualitiesFrom(Quality minimalQuality) {
        Quality[] allQualities = Quality.values();
        return Arrays.copyOfRange(allQualities, minimalQuality.ordinal(), allQualities.length);
    }
}
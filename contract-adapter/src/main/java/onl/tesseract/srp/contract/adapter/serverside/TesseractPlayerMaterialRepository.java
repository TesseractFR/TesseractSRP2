package onl.tesseract.srp.contract.adapter.serverside;

import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerMaterialRepository;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;

import java.util.*;

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
        int reallyOwned = countAtLeast(player, material, minimalQuality);
        if(amount > reallyOwned) {
            throw new IllegalStateException("Player has less items than requested");
        }
        UUID playerUuid = player.value();
        int remainingAmount = amount;
        for(Quality quality : qualitiesFrom(minimalQuality)) {
            int toTake = Math.min(customItemService.getItemQuantity(playerUuid, material, quality), remainingAmount);
            customItemService.removeItem(playerUuid, material, quality, toTake);
            remainingAmount -= toTake;
        }
    }

    /**
     * Retourne les qualités qui respectent la qualité minimale demandée, triées
     * par ordinal croissant.
     *
     * @param minimalQuality qualité minimale à prendre en compte
     * @return les qualités admissibles, de l'ordinal le plus petit au plus grand
     */
    private static List<Quality> qualitiesFrom(Quality minimalQuality) {
        return Arrays.stream(Quality.values())
                .filter(it->it.isAtMost(minimalQuality))
                .sorted(Comparator.comparingInt(Quality::ordinal))
                .toList();
    }
}

package onl.tesseract.srp.contract.adapter.serverside.material;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.contract.domain.model.exception.NotEnoughMaterialException;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerMaterialPort;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Compte et retire les matériaux des joueurs au moyen de {@link CustomItemService}. */
@Component
public class CustomItemPlayerMaterialPort implements PlayerMaterialPort {

    private final CustomItemService customItemService;

    public CustomItemPlayerMaterialPort(CustomItemService customItemService) {
        this.customItemService = customItemService;
    }

    @Override
    public int countAtLeast(PlayerId player, MaterialName material, Quality minimalQuality) {
        // Un matériau vanilla n'a pas de qualité : il est toujours rangé en POOR.
        if (!customItemService.isCustomMaterial(material)) {
            return customItemService.getItemQuantity(player.value(), material, Quality.POOR);
        }
        int totalQuantity = 0;
        for (Quality quality : findAllQualitiesFrom(minimalQuality)) {
            totalQuantity += customItemService.getItemQuantity(player.value(), material, quality);
        }
        return totalQuantity;
    }

    @Override
    public void removeAtLeast(PlayerId player, MaterialName material, Quality minimalQuality, int quantity) {
        int ownedQuantity = countAtLeast(player, material, minimalQuality);
        if (quantity > ownedQuantity) {
            throw new NotEnoughMaterialException(player, material, quantity, ownedQuantity);
        }
        UUID playerUuid = player.value();
        int remainingQuantity = quantity;
        for (Quality quality : findAllQualitiesFrom(minimalQuality)) {
            int quantityToTake = Math.min(customItemService.getItemQuantity(playerUuid, material, quality), remainingQuantity);
            customItemService.removeItem(playerUuid, material, quality, quantityToTake);
            remainingQuantity -= quantityToTake;
        }
    }

    /** @return les qualités au moins égales à la qualité minimale, de la plus basse à la plus haute */
    private static List<Quality> findAllQualitiesFrom(Quality minimalQuality) {
        return Arrays.stream(Quality.values())
                .filter(quality -> quality.isAtMost(minimalQuality))
                .sorted(Comparator.comparingInt(Quality::ordinal))
                .toList();
    }
}

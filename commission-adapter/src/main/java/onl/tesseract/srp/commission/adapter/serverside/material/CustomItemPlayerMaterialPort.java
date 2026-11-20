package onl.tesseract.srp.commission.adapter.serverside.material;

import onl.tesseract.srp.commission.domain.port.serverside.PlayerMaterialPort;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Compte et retire les matériaux des joueurs au moyen de {@link CustomItemService}. Le nom du bean est
 * explicite car le module contrat déclare une classe du même nom.
 */
@Component("commissionPlayerMaterialPort")
public class CustomItemPlayerMaterialPort implements PlayerMaterialPort {

    private final CustomItemService customItemService;

    public CustomItemPlayerMaterialPort(CustomItemService customItemService) {
        this.customItemService = customItemService;
    }

    @Override
    public int countAtLeast(PlayerId player, MaterialName material, Quality minimalQuality) {
        return findAllQualitiesToCount(material, minimalQuality).stream()
                .mapToInt(quality -> customItemService.getItemQuantity(player.value(), material, quality))
                .sum();
    }

    @Override
    public void removeAtLeast(PlayerId player, MaterialName material, Quality minimalQuality, int quantity) {
        UUID playerUuid = player.value();
        int remainingQuantity = quantity;
        for (Quality quality : findAllQualitiesToCount(material, minimalQuality)) {
            int quantityToTake = Math.min(customItemService.getItemQuantity(playerUuid, material, quality), remainingQuantity);
            if (quantityToTake > 0) {
                customItemService.removeItem(playerUuid, material, quality, quantityToTake);
                remainingQuantity -= quantityToTake;
            }
        }
    }

    /**
     * Un item vanilla n'a pas de qualité : {@link CustomItemService#getItemQuantity} renvoie son total
     * quelle que soit la qualité demandée, il n'est donc compté qu'une fois.
     *
     * @return les qualités à compter, de la plus basse admissible à la plus haute
     */
    private List<Quality> findAllQualitiesToCount(MaterialName material, Quality minimalQuality) {
        if (!customItemService.isCustomMaterial(material)) {
            return List.of(Quality.POOR);
        }
        Quality[] allQualities = Quality.values();
        return Arrays.asList(allQualities).subList(minimalQuality.ordinal(), allQualities.length);
    }
}

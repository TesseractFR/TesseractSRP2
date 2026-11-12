package onl.tesseract.srp.contract.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.itembuilder.ItemLoreBuilder;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.common.adapter.userside.menu.ItemAdderMenu;
import onl.tesseract.srp.common.adapter.utils.QualityAdapter;
import onl.tesseract.srp.contract.adapter.utils.ContractConstantes;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.port.userside.ContractService;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static onl.tesseract.srp.contract.adapter.utils.ContractConstantes.*;

/** Menu de confirmation de livraison ; il prépare les boutons et le lore de l'aperçu du contrat. */
public class ContractDeliveryMenu extends ItemAdderMenu {
    private static final String BACKGROUND_ID = "tesseract:contract_delivery_menu";
    private static final int TEXTURE_OFFSET = -8;

    private static final int CONFIRM = 11;
    private static final int INFO = 13;
    private static final int CANCEL = 15;

    private final PlayerId playerId;
    private final String npcJob;
    private final Contract contract;
    private final ContractService contractService;

    public ContractDeliveryMenu(PlayerId playerId, String npcJob, Contract contract, ContractService contractService) {
        super(MenuSize.Three, BACKGROUND_ID, "", null, TEXTURE_OFFSET);
        this.playerId = playerId;
        this.npcJob = npcJob;
        this.contract = contract;
        this.contractService = contractService;
    }

    @Override
    public void placeButtons(Player viewer) {
        addConfirm();
        addInfo();
        addCancel();
    }

    /** Crée l'icône d'aperçu du contrat et son lore pour le menu de livraison. */
    private void addInfo() {
        ItemStack scrollItem = new ItemBuilder(ContractConstantes.BASE_SCROLL)
                .lore(lore(contractService.countOwnedItems(playerId, contract)))
                .name(Component.text("Contrat · " + StringUtils.capitalize(contract.material().value()), NamedTextColor.GOLD))
                .maxStackSize(1)
                .build();
        addButton(INFO, scrollItem);
    }

    private void addConfirm() {
        addButton(CONFIRM, new ItemBuilder(Material.LIME_CONCRETE)
                .name("Confirmer la livraison", NamedTextColor.GREEN)
                .lore(new ItemLoreBuilder()
                        .append("Les items sont retirés de ton inventaire", NamedTextColor.GRAY)
                        .newline()
                        .append("La récompense est versée aussitôt", NamedTextColor.GRAY)
                        .get())
                .build(), event -> {
            close();
            contractService.deliver(playerId, contract.id(), npcJob);
        });
    }

    private void addCancel() {
        addButton(CANCEL, new ItemBuilder(Material.RED_CONCRETE)
                .name("Annuler", NamedTextColor.RED)
                .lore(new ItemLoreBuilder().append("Tu gardes ton parchemin", NamedTextColor.GRAY).get())
                .build(), event -> close());
    }

    private List<Component> lore(int count) {
        boolean isCraft = contract.type() == ContractType.CRAFT;
        QualityAdapter minimalQuality = QualityAdapter.from(contract.minimalQuality());
        ItemLoreBuilder itemLoreBuilder = new ItemLoreBuilder()
                .append((isCraft ? "Artisanat" : "Récolte") + " · " + StringUtils.capitalize(contract.job()), NamedTextColor.DARK_GRAY)
                .newline()
                .append("À livrer", NamedTextColor.GOLD)
                .append(BULLET + "Quantité : " + contract.quantity(), NamedTextColor.WHITE)
                .append(BULLET + "Qualité minimale : " +  minimalQuality.label(), minimalQuality.color())
                .append(BULLET + "Dans ton inventaire : " + count + " / " + contract.quantity(),
                        count >= contract.quantity() ? NamedTextColor.GREEN : NamedTextColor.RED)
                .newline()
                .append("Récompense", NamedTextColor.GOLD)
                .append(BULLET + "Gain : " + contract.reward() + " lys", NamedTextColor.GREEN)
                .append(BULLET + "Bonus métier : +" + contractService.moneyBonus(playerId,contract) + " lys", NamedTextColor.GREEN)
                .newline()
                .append("Expire le : " + EXPIRATION_DATE_FORMAT.format(contract.expiresAt()), NamedTextColor.RED);

        return itemLoreBuilder.get();
    }
}

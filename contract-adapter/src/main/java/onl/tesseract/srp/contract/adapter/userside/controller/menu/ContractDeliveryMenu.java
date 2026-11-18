package onl.tesseract.srp.contract.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.itembuilder.ItemLoreBuilder;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.common.adapter.userside.menu.ItemAdderMenu;
import onl.tesseract.srp.common.adapter.utils.QualityAdapter;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.adapter.userside.controller.message.ContractMessageFormatter;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.model.exception.ContractException;
import onl.tesseract.srp.contract.domain.port.userside.ContractDeliveryService;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static onl.tesseract.srp.contract.adapter.scroll.ContractScrollItems.BASE_SCROLL;
import static onl.tesseract.srp.contract.adapter.scroll.ContractScrollItems.BULLET;
import static onl.tesseract.srp.contract.adapter.scroll.ContractScrollItems.EXPIRATION_DATE_FORMAT;

/** Menu de confirmation de livraison, avec un aperçu du contrat et de ce que possède le joueur. */
public class ContractDeliveryMenu extends ItemAdderMenu {
    private static final String BACKGROUND_ID = "tesseract:contract_delivery_menu";
    private static final int TEXTURE_OFFSET = -8;

    private static final int CONFIRM_SLOT = 11;
    private static final int INFORMATION_SLOT = 13;
    private static final int CANCEL_SLOT = 15;

    private final PlayerId playerId;
    private final ContractClient npc;
    private final Contract contract;
    private final ContractDeliveryService contractDeliveryService;
    private final ContractMessageFormatter contractMessageFormatter;

    public ContractDeliveryMenu(PlayerId playerId, ContractClient npc, Contract contract,
                                ContractDeliveryService contractDeliveryService,
                                ContractMessageFormatter contractMessageFormatter) {
        super(MenuSize.Three, BACKGROUND_ID, "", null, TEXTURE_OFFSET);
        this.playerId = playerId;
        this.npc = npc;
        this.contract = contract;
        this.contractDeliveryService = contractDeliveryService;
        this.contractMessageFormatter = contractMessageFormatter;
    }

    @Override
    public void placeButtons(Player viewer) {
        addConfirmButton(viewer);
        addInformationButton();
        addCancelButton();
    }

    private void addInformationButton() {
        ItemStack scrollItem = new ItemBuilder(BASE_SCROLL)
                .lore(buildLore())
                .name(Component.text("Contrat · " + StringUtils.capitalize(contract.material().value()), NamedTextColor.GOLD))
                .maxStackSize(1)
                .build();
        addButton(INFORMATION_SLOT, scrollItem);
    }

    private void addConfirmButton(Player viewer) {
        addButton(CONFIRM_SLOT, new ItemBuilder(Material.LIME_CONCRETE)
                .name("Confirmer la livraison", NamedTextColor.GREEN)
                .lore(new ItemLoreBuilder()
                        .append("Les items sont retirés de ton inventaire", NamedTextColor.GRAY)
                        .newline()
                        .append("La récompense est versée aussitôt", NamedTextColor.GRAY)
                        .get())
                .build(), event -> {
            close();
            deliver(viewer);
        });
    }

    private void deliver(Player viewer) {
        try {
            MoneyValue payment = contractDeliveryService.deliver(playerId, contract.id(), npc);
            viewer.sendMessage(contractMessageFormatter.formatNpcDeliverySucceeded(npc, payment));
        } catch (ContractException refusal) {
            viewer.sendMessage(contractMessageFormatter.formatNpcRefusal(npc, refusal));
        }
    }

    private void addCancelButton() {
        addButton(CANCEL_SLOT, new ItemBuilder(Material.RED_CONCRETE)
                .name("Annuler", NamedTextColor.RED)
                .lore(new ItemLoreBuilder().append("Tu gardes ton parchemin", NamedTextColor.GRAY).get())
                .build(), event -> close());
    }

    private List<Component> buildLore() {
        boolean isCraft = contract.type() == ContractType.CRAFT;
        QualityAdapter minimalQuality = QualityAdapter.from(contract.minimalQuality());
        int ownedQuantity = contractDeliveryService.countOwnedItems(playerId, contract);
        boolean hasEnoughItems = contractDeliveryService.hasEnoughItems(playerId, contract);
        String moneyBonus = contractDeliveryService.findMoneyBonus(playerId, contract)
                .map(bonus -> bonus.value().toPlainString())
                .orElse("0");
        return new ItemLoreBuilder()
                .append((isCraft ? "Artisanat" : "Récolte") + " · " + StringUtils.capitalize(contract.client().name()), NamedTextColor.DARK_GRAY)
                .newline(2)
                .append("À livrer", NamedTextColor.GOLD)
                .newline()
                .append(BULLET + "Quantité : " + contract.quantity(), NamedTextColor.WHITE)
                .newline()
                .append(BULLET + "Qualité minimale : " + minimalQuality.label(), minimalQuality.color())
                .newline()
                .append(BULLET + "Dans ton inventaire : " + ownedQuantity + " / " + contract.quantity(),
                        hasEnoughItems ? NamedTextColor.GREEN : NamedTextColor.RED)
                .newline(2)
                .append("Récompense", NamedTextColor.GOLD)
                .newline()
                .append(BULLET + "Gain : " + contract.reward().value() + " lys", NamedTextColor.GREEN)
                .newline()
                .append(BULLET + "Bonus métier : +" + moneyBonus + " lys", NamedTextColor.GREEN)
                .newline(2)
                .append("Expire le : " + EXPIRATION_DATE_FORMAT.format(contract.expiresAt()), NamedTextColor.RED)
                .get();
    }
}

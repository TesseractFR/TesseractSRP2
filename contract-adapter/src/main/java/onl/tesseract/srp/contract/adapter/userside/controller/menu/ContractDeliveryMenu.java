package onl.tesseract.srp.contract.adapter.userside.controller.menu;

import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.common.adapter.userside.menu.ItemAdderMenu;
import onl.tesseract.srp.contract.adapter.utils.ContractDisplay;
import onl.tesseract.srp.contract.adapter.utils.ContractScrollItem;
import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.port.userside.ContractService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ContractDeliveryMenu extends ItemAdderMenu {
    private static final String BACKGROUND_ID = "tesseract:contract_delivery_menu";
    private static final int TEXTURE_OFFSET = -8;

    private static final int CONFIRM = 11;
    private static final int INFO = 13;
    private static final int CANCEL = 15;

    private final PlayerID playerId;
    private final String npcJob;
    private final Contract contract;
    private final ContractService contractService;

    public ContractDeliveryMenu(PlayerID playerId, String npcJob, Contract contract, ContractService contractService) {
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

    private void addInfo() {
        ItemStack icon = ContractScrollItem.create().orElseGet(() -> new ItemStack(Material.PAPER));
        icon.editMeta(meta -> {
            meta.displayName(ContractDisplay.title(contract));
            meta.lore(ContractDisplay.deliveryDetails(contract, contractService.moneyBonus(playerId, contract), contractService.countOwnedItems(playerId, contract)));
        });
        addButton(INFO, icon);
    }

    private void addConfirm() {
        addButton(CONFIRM, new ItemBuilder(Material.LIME_CONCRETE).name("Confirmer la livraison", NamedTextColor.GREEN)
                .lore(List.of(ContractDisplay.text("Les items sont retirés de ton inventaire", NamedTextColor.GRAY),
                        ContractDisplay.text("La récompense est versée aussitôt", NamedTextColor.GRAY))).build(), event -> {
            close();
            contractService.deliver(playerId, contract.id(), npcJob);
        });
    }

    private void addCancel() {
        addButton(CANCEL, new ItemBuilder(Material.RED_CONCRETE).name("Annuler", NamedTextColor.RED)
                .lore(List.of(ContractDisplay.text("Tu gardes ton parchemin", NamedTextColor.GRAY))).build(), event -> close());
    }
}

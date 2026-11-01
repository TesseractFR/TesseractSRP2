package onl.tesseract.srp.contract.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.common.adapter.userside.menu.CustomMenuButton;
import onl.tesseract.srp.common.adapter.userside.menu.ItemAdderMenu;
import onl.tesseract.srp.contract.adapter.utils.ContractDisplay;
import onl.tesseract.srp.contract.adapter.utils.ContractScrollItem;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.port.userside.ContractService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ContractMerchantMenu extends ItemAdderMenu {
    private static final String BACKGROUND_ID = "tesseract:contract_merchant_menu";
    private static final int TEXTURE_OFFSET = -8;
    private static final int MAX_STACK_SIZE = 64;

    private static final int TICKETS = 11;
    private static final int BUY = 13;
    private static final int INFORMATION = 15;

    private final PlayerId playerId;
    private final ContractService contractService;

    public ContractMerchantMenu(PlayerId playerId, ContractService contractService) {
        super(MenuSize.Three, BACKGROUND_ID, "", null, TEXTURE_OFFSET);
        this.playerId = playerId;
        this.contractService = contractService;
    }

    @Override
    public void placeButtons(Player viewer) {
        int tickets = contractService.getTickets(playerId);
        addTickets(tickets);
        addBuy(viewer, tickets);
        addInformation();
    }

    private void addTickets(int tickets) {
        ItemStack icon = new ItemBuilder(Material.NAME_TAG).name("Tes tickets", NamedTextColor.GOLD)
                .lore(List.of(ContractDisplay.line("Restants aujourd'hui : ", String.valueOf(tickets), tickets > 0 ? NamedTextColor.GREEN : NamedTextColor.RED),
                        ContractDisplay.line("Quota par jour : ", String.valueOf(contractService.getDailyQuota(playerId)), NamedTextColor.WHITE))).build();
        icon.setAmount(Math.clamp(tickets, 1, MAX_STACK_SIZE));
        addButton(TICKETS, icon);
    }

    private void addBuy(Player viewer, int tickets) {
        ItemStack icon = ContractScrollItem.create().orElseGet(() -> new ItemStack(Material.PAPER));
        icon.editMeta(meta -> {
            meta.displayName(ContractDisplay.text("Acheter un parchemin", NamedTextColor.GOLD));
            meta.lore(List.of(ContractDisplay.text("Un contrat tiré au hasard,", NamedTextColor.GRAY),
                    ContractDisplay.text("récolte ou artisanat", NamedTextColor.GRAY),
                    Component.empty(),
                    ContractDisplay.line("Coût : ", "1 ticket", tickets > 0 ? NamedTextColor.GREEN : NamedTextColor.RED),
                    Component.empty(),
                    tickets > 0
                            ? ContractDisplay.text("» Clique pour acheter", NamedTextColor.YELLOW)
                            : ContractDisplay.text("Plus de ticket aujourd'hui", NamedTextColor.RED)));
        });
        addButton(BUY, icon, event -> {
            contractService.buyScroll(playerId);
            new ContractMerchantMenu(playerId, contractService).open(viewer);
        });
    }

    private void addInformation() {
        ItemStack icon = getCustomButtonItemStack(CustomMenuButton.MENU_INFORMATION_BUTTON);
        icon.editMeta(meta -> {
            meta.displayName(ContractDisplay.text("Informations", NamedTextColor.GOLD));
            meta.lore(List.of(ContractDisplay.text("• Ton quota de tickets se renouvelle chaque jour", NamedTextColor.GRAY),
                    ContractDisplay.text("• Apporte le parchemin et les matériaux au PNJ indiqué", NamedTextColor.GRAY),
                    ContractDisplay.text("• Tout se livre en une seule fois, n'oublie rien", NamedTextColor.GRAY),
                    ContractDisplay.text("• Un parchemin expiré disparaît de ton inventaire", NamedTextColor.GRAY)));
        });
        addButton(INFORMATION, icon);
    }
}

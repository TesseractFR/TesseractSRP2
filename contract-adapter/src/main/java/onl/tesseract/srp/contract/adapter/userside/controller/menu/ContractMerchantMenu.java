package onl.tesseract.srp.contract.adapter.userside.controller.menu;

import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.itembuilder.ItemLoreBuilder;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.common.adapter.userside.menu.CustomMenuButton;
import onl.tesseract.srp.common.adapter.userside.menu.ItemAdderMenu;
import onl.tesseract.srp.contract.adapter.utils.ContractConstantes;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.port.userside.ContractService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static onl.tesseract.srp.contract.adapter.utils.ContractConstantes.BULLET;

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
        ItemStack icon = new ItemBuilder(Material.NAME_TAG)
                .name("Tes tickets", NamedTextColor.GOLD)
                .lore(new ItemLoreBuilder()
                        .append("Restants aujourd'hui : " + tickets, tickets > 0 ? NamedTextColor.GREEN : NamedTextColor.RED)
                        .newline()
                        .append("Quota par jour : " + contractService.getDailyQuota(playerId), NamedTextColor.WHITE)
                        .get())
                .amount(Math.clamp(tickets, 1, MAX_STACK_SIZE))
                .build();
        addButton(TICKETS, icon);
    }

    private void addBuy(Player viewer, int tickets) {
        ItemStack icon = new ItemBuilder(ContractConstantes.BASE_SCROLL)
                .name("Acheter un parchemin", NamedTextColor.GOLD)
                .lore(new ItemLoreBuilder()
                        .append("Un contrat tiré au hasard,", NamedTextColor.GRAY)
                        .newline()
                        .append("récolte ou artisanat", NamedTextColor.GRAY)
                        .newline(2)
                        .append("Coût : 1 ticket", tickets > 0 ? NamedTextColor.GREEN : NamedTextColor.RED)
                        .newline(2)
                        .append(tickets>0 ? "» Clique pour acheter" : "Plus de ticket aujourd'hui", tickets > 0 ? NamedTextColor.YELLOW : NamedTextColor.RED)
                        .get())
                .build();
        addButton(BUY, icon, event -> {
            contractService.buyScroll(playerId);
            new ContractMerchantMenu(playerId, contractService).open(viewer);
        });
    }

    private void addInformation() {
        ItemStack icon = new ItemBuilder(getCustomButtonItemStack(CustomMenuButton.MENU_INFORMATION_BUTTON))
                .name("Information", NamedTextColor.GOLD)
                .lore(new ItemLoreBuilder()
                        .append(BULLET+"Ton quota de tickets se renouvelle chaque jour", NamedTextColor.GRAY)
                        .newline()
                        .append(BULLET+"Apporte le parchemin et les matériaux au PNJ indiqué", NamedTextColor.GRAY)
                        .newline()
                        .append(BULLET+"Tout se livre en une seule fois, n'oublie rien", NamedTextColor.GRAY)
                        .newline()
                        .append(BULLET+"Un parchemin expiré disparaît de ton inventaire", NamedTextColor.GRAY)
                        .get())
                .build();
        addButton(INFORMATION, icon);
    }
}

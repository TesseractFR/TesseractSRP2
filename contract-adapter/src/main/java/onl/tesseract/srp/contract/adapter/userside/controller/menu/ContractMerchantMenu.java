package onl.tesseract.srp.contract.adapter.userside.controller.menu;

import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.itembuilder.ItemLoreBuilder;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.common.adapter.userside.menu.CustomMenuButton;
import onl.tesseract.srp.common.adapter.userside.menu.ItemAdderMenu;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.adapter.userside.controller.message.ContractMessageFormatter;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.exception.ContractException;
import onl.tesseract.srp.contract.domain.port.userside.ContractPurchaseUseCase;
import onl.tesseract.srp.contract.domain.port.userside.ContractTicketUseCase;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static onl.tesseract.srp.contract.adapter.scroll.ContractScrollItems.BASE_SCROLL;
import static onl.tesseract.srp.contract.adapter.scroll.ContractScrollItems.BULLET;

/** Menu du marchand permettant de consulter ses tickets et d'acheter un parchemin de contrat. */
public class ContractMerchantMenu extends ItemAdderMenu {
    private static final String BACKGROUND_ID = "tesseract:contract_merchant_menu";
    private static final int TEXTURE_OFFSET = -8;
    private static final int MAX_STACK_SIZE = 64;

    private static final int TICKETS_SLOT = 11;
    private static final int BUY_SLOT = 13;
    private static final int INFORMATION_SLOT = 15;

    private final PlayerId playerId;
    private final ContractPurchaseUseCase contractPurchaseUseCase;
    private final ContractTicketUseCase contractTicketUseCase;
    private final ContractMessageFormatter contractMessageFormatter;

    public ContractMerchantMenu(PlayerId playerId, ContractPurchaseUseCase contractPurchaseUseCase,
                                ContractTicketUseCase contractTicketUseCase,
                                ContractMessageFormatter contractMessageFormatter) {
        super(MenuSize.Three, BACKGROUND_ID, "", null, TEXTURE_OFFSET);
        this.playerId = playerId;
        this.contractPurchaseUseCase = contractPurchaseUseCase;
        this.contractTicketUseCase = contractTicketUseCase;
        this.contractMessageFormatter = contractMessageFormatter;
    }

    @Override
    public void placeButtons(Player viewer) {
        int tickets = contractTicketUseCase.getTickets(playerId);
        addTicketsButton(tickets);
        addBuyButton(viewer, tickets);
        addInformationButton();
    }

    private void addTicketsButton(int tickets) {
        ItemStack icon = new ItemBuilder(Material.NAME_TAG)
                .name("Tes tickets", NamedTextColor.GOLD)
                .lore(new ItemLoreBuilder()
                        .append("Restants aujourd'hui : " + tickets, tickets > 0 ? NamedTextColor.GREEN : NamedTextColor.RED)
                        .newline()
                        .append("Quota par jour : " + contractTicketUseCase.getDailyQuota(playerId), NamedTextColor.WHITE)
                        .get())
                .amount(Math.clamp(tickets, 1, MAX_STACK_SIZE))
                .build();
        addButton(TICKETS_SLOT, icon);
    }

    private void addBuyButton(Player viewer, int tickets) {
        boolean hasTicket = tickets > 0;
        ItemStack icon = new ItemBuilder(BASE_SCROLL)
                .name("Acheter un parchemin", NamedTextColor.GOLD)
                .lore(new ItemLoreBuilder()
                        .append("Un contrat tiré au hasard,", NamedTextColor.GRAY)
                        .newline()
                        .append("récolte ou artisanat", NamedTextColor.GRAY)
                        .newline(2)
                        .append("Coût : 1 ticket", hasTicket ? NamedTextColor.GREEN : NamedTextColor.RED)
                        .newline(2)
                        .append(hasTicket ? "» Clique pour acheter" : "Plus de ticket aujourd'hui",
                                hasTicket ? NamedTextColor.YELLOW : NamedTextColor.RED)
                        .get())
                .build();
        addButton(BUY_SLOT, icon, event -> {
            buyScroll(viewer);
            new ContractMerchantMenu(playerId, contractPurchaseUseCase, contractTicketUseCase, contractMessageFormatter)
                    .open(viewer);
        });
    }

    private void buyScroll(Player viewer) {
        try {
            Contract contract = contractPurchaseUseCase.buyScroll(playerId);
            viewer.sendMessage(contractMessageFormatter.formatPurchaseSucceeded(contract,
                    contractTicketUseCase.getTickets(playerId)));
        } catch (ContractException refusal) {
            viewer.sendMessage(contractMessageFormatter.formatRefusal(refusal));
        }
    }

    private void addInformationButton() {
        ItemStack icon = new ItemBuilder(getCustomButtonItemStack(CustomMenuButton.MENU_INFORMATION_BUTTON))
                .name("Information", NamedTextColor.GOLD)
                .lore(new ItemLoreBuilder()
                        .append(BULLET + "Ton quota de tickets se renouvelle chaque jour", NamedTextColor.GRAY)
                        .newline()
                        .append(BULLET + "Apporte le parchemin et les matériaux au PNJ indiqué", NamedTextColor.GRAY)
                        .newline()
                        .append(BULLET + "Tout se livre en une seule fois, n'oublie rien", NamedTextColor.GRAY)
                        .newline()
                        .append(BULLET + "Un parchemin expiré disparaît de ton inventaire", NamedTextColor.GRAY)
                        .get())
                .build();
        addButton(INFORMATION_SLOT, icon);
    }
}

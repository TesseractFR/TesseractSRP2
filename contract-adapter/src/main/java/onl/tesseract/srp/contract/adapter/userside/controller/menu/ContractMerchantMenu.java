package onl.tesseract.srp.contract.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.itembuilder.ItemLoreBuilder;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.contract.adapter.utils.ContractScrollItem;
import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.port.userside.ContractService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ContractMerchantMenu extends Menu {

    private static final int TICKETS = 11;
    private static final int BUY = 15;

    private final PlayerID playerId;
    private final ContractService contractService;

    public ContractMerchantMenu(PlayerID playerId, ContractService contractService) {
        super(MenuSize.Three, Component.text("Marchand de contrats"), null);
        this.playerId = playerId;
        this.contractService = contractService;
    }

    @Override
    public void placeButtons(Player viewer) {
        addTickets();
        addBuy(viewer);
    }

    private void addTickets() {
        int tickets = contractService.getTickets(playerId);
        var lore = new ItemLoreBuilder().newline().append("Restants aujourd'hui : ", NamedTextColor.GRAY).append(String.valueOf(tickets), tickets > 0 ? NamedTextColor.GREEN : NamedTextColor.RED).newline().append("Quota par jour : ", NamedTextColor.GRAY).append(String.valueOf(contractService.getDailyQuota(playerId)), NamedTextColor.WHITE);
        addButton(TICKETS, new ItemBuilder(Material.CLOCK).name("Tickets de contrat", NamedTextColor.GOLD).lore(lore.get()).build(), event -> {
        });
    }

    private void addBuy(Player viewer) {
        var lore = new ItemLoreBuilder().newline().append("Coût : ", NamedTextColor.GRAY).append("1 ticket", NamedTextColor.GREEN).newline().newline().append("Clic : ", NamedTextColor.GOLD).append("Acheter un parchemin aléatoire", NamedTextColor.GRAY);
        ItemStack icon = scrollIcon();
        ItemMeta meta = icon.getItemMeta();
        meta.displayName(Component.text("Acheter un parchemin", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore.get());
        icon.setItemMeta(meta);
        addButton(BUY, icon, event -> {
            contractService.buyScroll(playerId);
            new ContractMerchantMenu(playerId, contractService).open(viewer);
        });
    }

    private ItemStack scrollIcon() {
        return ContractScrollItem.create().orElseGet(() -> new ItemStack(Material.PAPER));
    }
}
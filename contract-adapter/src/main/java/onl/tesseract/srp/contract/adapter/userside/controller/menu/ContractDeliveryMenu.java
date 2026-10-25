package onl.tesseract.srp.contract.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.itembuilder.ItemLoreBuilder;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.contract.adapter.utils.ContractDisplay;
import onl.tesseract.srp.contract.adapter.utils.ContractScrollItem;
import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.port.userside.ContractService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class ContractDeliveryMenu extends Menu {

    private static final int INFO = 11;
    private static final int CONFIRM = 14;
    private static final int CANCEL = 15;

    private final PlayerID playerId;
    private final String npcJob;
    private final Contract contract;
    private final ContractService contractService;

    public ContractDeliveryMenu(PlayerID playerId, String npcJob, Contract contract, ContractService contractService) {
        super(MenuSize.Three, Component.text("Livrer un contrat"), null);
        this.playerId = playerId;
        this.npcJob = npcJob;
        this.contract = contract;
        this.contractService = contractService;
    }

    @Override
    public void placeButtons(Player viewer) {
        addInfo();
        addConfirm();
        addCancel();
    }

    private void addInfo() {
        ItemStack icon = scrollIcon();
        icon.editMeta(meta -> {
            meta.displayName(Component.text("Contrat", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            var lines = new ArrayList<Component>();
            lines.add(Component.empty());
            lines.addAll(ContractDisplay.details(contract, contractService.moneyBonus(playerId, contract)));
            meta.lore(lines);
        });
        addButton(INFO, icon, event -> {
        });
    }

    private void addConfirm() {
        var lore = new ItemLoreBuilder().newline().append("Les items demandés sont retirés de ton inventaire,", NamedTextColor.GRAY).newline().append("et la récompense est versée tout de suite.", NamedTextColor.GRAY);
        addButton(CONFIRM, new ItemBuilder(Material.LIME_CONCRETE).name("Confirmer la livraison", NamedTextColor.GREEN).lore(lore.get()).build(), event -> {
            close();
            contractService.deliver(playerId, contract.id(), npcJob);
        });
    }

    private void addCancel() {
        addButton(CANCEL, new ItemBuilder(Material.RED_CONCRETE).name("Annuler", NamedTextColor.RED).build(), event -> close());
    }

    private ItemStack scrollIcon() {
        return ContractScrollItem.create().orElseGet(() -> new ItemStack(Material.PAPER));
    }
}
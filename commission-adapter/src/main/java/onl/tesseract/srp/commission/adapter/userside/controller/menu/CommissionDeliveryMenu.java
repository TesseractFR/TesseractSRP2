package onl.tesseract.srp.commission.adapter.userside.controller.menu;

import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionMessageFormatter;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.commission.Commission;
import onl.tesseract.srp.commission.domain.model.commission.CommissionDelivery;
import onl.tesseract.srp.commission.domain.model.exception.CommissionException;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardAccessService;
import onl.tesseract.srp.commission.domain.port.userside.CommissionDeliveryService;
import onl.tesseract.srp.common.adapter.userside.menu.ItemAdderMenu;
import onl.tesseract.srp.common.domain.model.PlayerId;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

/** Menu de confirmation de livraison, avec un aperçu de la commission et de ce que le joueur livrerait. */
public class CommissionDeliveryMenu extends ItemAdderMenu {
    private static final String BACKGROUND_ID = "tesseract:commission_board_delivery_menu";
    private static final int TEXTURE_OFFSET = -8;

    private static final int CONFIRM_SLOT = 11;
    private static final int COMMISSION_SLOT = 13;
    private static final int CANCEL_SLOT = 15;

    private final CommissionBoard commissionBoard;
    private final Commission commission;
    private final CommissionBoardMenu commissionBoardMenu;
    private final CommissionDeliveryService commissionDeliveryService;
    private final CommissionBoardAccessService commissionBoardAccessService;
    private final CommissionMessageFormatter commissionMessageFormatter;
    private final ItemStack commissionIcon;

    public CommissionDeliveryMenu(CommissionBoard commissionBoard, Commission commission, CommissionBoardMenu commissionBoardMenu,
                                  CommissionDeliveryService commissionDeliveryService,
                                  CommissionBoardAccessService commissionBoardAccessService,
                                  CommissionMessageFormatter commissionMessageFormatter, ItemStack commissionIcon) {
        super(MenuSize.Three, BACKGROUND_ID, "", null, TEXTURE_OFFSET);
        this.commissionBoard = commissionBoard;
        this.commission = commission;
        this.commissionBoardMenu = commissionBoardMenu;
        this.commissionDeliveryService = commissionDeliveryService;
        this.commissionBoardAccessService = commissionBoardAccessService;
        this.commissionMessageFormatter = commissionMessageFormatter;
        this.commissionIcon = commissionIcon;
    }

    @Override
    public void placeButtons(Player viewer) {
        addButton(COMMISSION_SLOT, commissionIcon);
        ItemStack confirmIcon = new ItemBuilder(Material.LIME_CONCRETE).name("Confirmer la livraison", NamedTextColor.GREEN)
                .lore(List.of(CommissionDisplay.coloredText("Les items seront retirés de ton inventaire", NamedTextColor.GRAY)))
                .build();
        addButton(CONFIRM_SLOT, confirmIcon, () -> {
            deliver(viewer);
            commissionBoardMenu.reopenWithUpToDateBoard(viewer);
        });
        addButton(CANCEL_SLOT, new ItemBuilder(Material.RED_CONCRETE).name("Annuler", NamedTextColor.RED)
                        .lore(List.of(CommissionDisplay.coloredText("Retour au tableau", NamedTextColor.GRAY))).build(),
                () -> commissionBoardMenu.reopenWithUpToDateBoard(viewer));
    }

    private void deliver(Player viewer) {
        try {
            CommissionDelivery delivery = commissionDeliveryService.deliver(new PlayerId(viewer.getUniqueId()), commission.id());
            viewer.sendMessage(commissionMessageFormatter.formatDeliverySucceeded(commission, delivery));
            announceDeliveryToGuild(viewer, delivery);
        } catch (CommissionException refusal) {
            viewer.sendMessage(commissionMessageFormatter.formatRefusal(refusal));
        }
    }

    /** Prévient les autres membres connectés de la guilde de ce que la livraison lui rapporte ; le livreur a déjà son message. */
    private void announceDeliveryToGuild(Player deliverer, CommissionDelivery delivery) {
        commissionMessageFormatter.formatGuildDeliveryNotice(deliverer.getName(), commission, delivery)
                .ifPresent(notice -> commissionBoardAccessService.findAllGuildMembers(commissionBoard).stream()
                        .flatMap(member -> Optional.ofNullable(Bukkit.getPlayer(member.value())).stream())
                        .filter(onlineMember -> !onlineMember.equals(deliverer))
                        .forEach(onlineMember -> onlineMember.sendMessage(notice)));
    }
}

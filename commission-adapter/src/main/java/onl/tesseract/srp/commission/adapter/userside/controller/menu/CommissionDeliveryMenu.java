package onl.tesseract.srp.commission.adapter.userside.controller.menu;

import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionMessageFormatter;
import onl.tesseract.srp.commission.domain.model.commission.Commission;
import onl.tesseract.srp.commission.domain.model.commission.Delivery;
import onl.tesseract.srp.commission.domain.model.exception.CommissionException;
import onl.tesseract.srp.commission.domain.port.userside.CommissionDeliveryService;
import onl.tesseract.srp.common.adapter.userside.menu.ItemAdderMenu;
import onl.tesseract.srp.common.domain.model.PlayerId;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/** Menu de confirmation de livraison, avec un aperçu de la commission et de ce que le joueur livrerait. */
public class CommissionDeliveryMenu extends ItemAdderMenu {
    private static final String BACKGROUND_ID = "tesseract:commission_board_delivery_menu";
    private static final int TEXTURE_OFFSET = -8;

    private static final int CONFIRM_SLOT = 11;
    private static final int COMMISSION_SLOT = 13;
    private static final int CANCEL_SLOT = 15;

    private final Commission commission;
    private final CommissionBoardMenu commissionBoardMenu;
    private final CommissionDeliveryService commissionDeliveryService;
    private final CommissionMessageFormatter commissionMessageFormatter;
    private final ItemStack commissionIcon;

    public CommissionDeliveryMenu(Commission commission, CommissionBoardMenu commissionBoardMenu,
                                  CommissionDeliveryService commissionDeliveryService,
                                  CommissionMessageFormatter commissionMessageFormatter, ItemStack commissionIcon) {
        super(MenuSize.Three, BACKGROUND_ID, "", null, TEXTURE_OFFSET);
        this.commission = commission;
        this.commissionBoardMenu = commissionBoardMenu;
        this.commissionDeliveryService = commissionDeliveryService;
        this.commissionMessageFormatter = commissionMessageFormatter;
        this.commissionIcon = commissionIcon;
    }

    @Override
    public void placeButtons(Player viewer) {
        addButton(COMMISSION_SLOT, commissionIcon);
        addButton(CONFIRM_SLOT, new ItemBuilder(Material.LIME_CONCRETE).name("Confirmer la livraison", NamedTextColor.GREEN)
                .lore(List.of(CommissionDisplay.coloredText("Les items seront retirés de ton inventaire", NamedTextColor.GRAY))).build(), () -> {
            deliver(viewer);
            commissionBoardMenu.reopenWithUpToDateBoard(viewer);
        });
        addButton(CANCEL_SLOT, new ItemBuilder(Material.RED_CONCRETE).name("Annuler", NamedTextColor.RED)
                        .lore(List.of(CommissionDisplay.coloredText("Retour au tableau", NamedTextColor.GRAY))).build(),
                () -> commissionBoardMenu.reopenWithUpToDateBoard(viewer));
    }

    private void deliver(Player viewer) {
        try {
            Delivery delivery = commissionDeliveryService.deliver(new PlayerId(viewer.getUniqueId()), commission.id());
            commissionMessageFormatter.formatDeliverySucceeded(commission, delivery).forEach(viewer::sendMessage);
            commissionBoardMenu.announceDeliveryToGuild(viewer, commission, delivery);
        } catch (CommissionException refusal) {
            viewer.sendMessage(commissionMessageFormatter.formatRefusal(refusal));
        }
    }
}

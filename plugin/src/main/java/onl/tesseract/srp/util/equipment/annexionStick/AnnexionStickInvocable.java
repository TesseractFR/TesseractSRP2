package onl.tesseract.srp.util.equipment.annexionStick;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import onl.tesseract.lib.equipment.Invocable;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.itembuilder.ItemLoreBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Abstract invocable for annexation sticks that allow claiming/unclaiming chunks.
 */
public abstract class AnnexionStickInvocable extends Invocable {

    public AnnexionStickInvocable(UUID playerUUID, boolean isInvoked, int handSlot) {
        super(playerUUID, isInvoked, handSlot);
    }

    public abstract String getBaseCommand();
    public abstract String getDisplayName();
    public abstract Material getMaterial();

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.HAND;
    }

    @Override
    public String getUniqueName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void onUninvoke(Player player, boolean manuelRemoval) {
        // Nothing
    }

    @Override
    public void onInvoke(Player player, boolean manuelInvocation) {
        // Nothing
    }

    @Override
    public void useInInventory(InventoryClickEvent event) {
        // Nothing
    }

    @Override
    public ItemStack createItem() {
        var lore = new ItemLoreBuilder()
                .append("► ", NamedTextColor.GRAY)
                .append("Clic DROIT", NamedTextColor.GREEN)
                .append(" pour annexer un chunk !", NamedTextColor.GRAY)
                .newline()
                .append("► ", NamedTextColor.GRAY)
                .append("Clic GAUCHE", NamedTextColor.RED)
                .append(" pour le désannexer !", NamedTextColor.GRAY)
                .newline()
                .append("Shift + clic pour le retirer", NamedTextColor.YELLOW)
                .get();

        return new ItemBuilder(getMaterial())
                .name(getDisplayName(), NamedTextColor.GOLD, TextDecoration.BOLD)
                .enchanted(true)
                .lore(lore)
                .build();
    }

    @Override
    public void use(PlayerInteractEvent event) {
        boolean isClaim = switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK, RIGHT_CLICK_AIR -> true;
            case LEFT_CLICK_BLOCK, LEFT_CLICK_AIR -> false;
            default -> false;
        };

        String cmd = isClaim ? getBaseCommand() + " claim" : getBaseCommand() + " unclaim";
        event.getPlayer().performCommand(cmd);
        event.setCancelled(true);
    }
}


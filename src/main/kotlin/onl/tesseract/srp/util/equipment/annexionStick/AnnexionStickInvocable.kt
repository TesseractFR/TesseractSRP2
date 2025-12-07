package onl.tesseract.srp.util.equipment.annexionStick

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.lib.equipment.Invocable
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.util.ItemLoreBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.UUID

abstract class AnnexionStickInvocable(
    playerUUID: UUID,
    isInvoked: Boolean = false,
    handSlot: Int = 0
) : Invocable(playerUUID, isInvoked, handSlot) {

    abstract val baseCommand: String
    abstract val displayName: String
    abstract val material: Material

    override val slotType: EquipmentSlot = EquipmentSlot.HAND
    override val uniqueName: String get() = this::class.simpleName!!

    override fun onUninvoke(player: Player, manuelRemoval: Boolean) {/* Rien */}

    override fun onInvoke(player: Player, manuelInvocation: Boolean) {/* Rien */}
    override fun useInInventory(event: InventoryClickEvent) {/* Rien */}

    override fun createItem(): ItemStack {
        val lore = ItemLoreBuilder()
            .append("► ", NamedTextColor.GRAY)
            .append("Clic DROIT", NamedTextColor.GREEN)
            .append(" pour annexer un chunk !", NamedTextColor.GRAY)
            .newline()
            .append("► ", NamedTextColor.GRAY)
            .append("Clic GAUCHE", NamedTextColor.RED)
            .append(" pour le désannexer !", NamedTextColor.GRAY)
            .newline()
            .append("Shift + clic pour le retirer", NamedTextColor.YELLOW)
            .get()

        return ItemBuilder(material)
            .name(displayName, NamedTextColor.GOLD, TextDecoration.BOLD)
            .enchanted(true)
            .lore(lore)
            .build()
    }

    override fun use(event: PlayerInteractEvent) {
        val isClaim = when (event.action) {
            Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR -> true
            Action.LEFT_CLICK_BLOCK, Action.LEFT_CLICK_AIR   -> false
            else -> return
        }

        val cmd = if (isClaim) "$baseCommand claim" else "$baseCommand unclaim"
        event.player.performCommand(cmd)
        event.isCancelled = true
    }
}
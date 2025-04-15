package onl.tesseract.srp.domain.elytra

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.lib.equipment.Invocable
import onl.tesseract.lib.menu.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.*

class ElytraInvocable(
    playerUUID: UUID,
    isInvoked: Boolean = false,
    handSlot: Int = 0
) : Invocable(playerUUID, isInvoked, handSlot) {

    override val slotType: EquipmentSlot = EquipmentSlot.CHEST
    override val uniqueName: String = this::class.simpleName!!

    override fun onUninvoke(player: Player, manuelRemoval: Boolean) {
        // Rien pour le moment
    }
    override fun onInvoke(player: Player, manuelInvocation: Boolean) {
        // Rien pour le moment
    }
    override fun useInInventory(event: InventoryClickEvent) {
        // Rien pour le moment
    }
    override fun use(event: PlayerInteractEvent) {
        // Rien pour le moment
    }

    override fun createItem(): ItemStack {
        return ItemBuilder(Material.ELYTRA)
            .name("Ailes Célestes", NamedTextColor.AQUA, TextDecoration.BOLD)
            .enchanted(true)
            .lore()
            .append(Component.text("Ailes magiques forgées par les êtres Célestes.", NamedTextColor.GRAY))
            .buildLore()
            .build()
    }
}

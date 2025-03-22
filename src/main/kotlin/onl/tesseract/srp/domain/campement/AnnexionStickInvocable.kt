package onl.tesseract.srp.domain.campement

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.equipment.Invocable
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.lib.util.ItemBuilder
import onl.tesseract.lib.util.ItemLoreBuilder
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.*

class AnnexationStickInvocable(
    playerUUID: UUID,
    private val campementService: CampementService,
    private val equipmentService: EquipmentService
) : Invocable(playerUUID) {

    override val slotType: EquipmentSlot = EquipmentSlot.HAND
    override val uniqueName: String = "annexation_stick"

    override fun onUninvoke(player: Player, manuelRemoval: Boolean) {
    }

    override fun onInvoke(player: Player, manuelInvocation: Boolean) {
    }

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

        return ItemBuilder(Material.STICK)
            .name("Bâton d'Annexion", NamedTextColor.GOLD, TextDecoration.BOLD)
            .lore(lore)
            .build()
    }

    override fun use(event: PlayerInteractEvent) {
        val player = event.player
        val chunk = "${player.location.chunk.x},${player.location.chunk.z}"

        val claim = when (event.action) {
            org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK,
            org.bukkit.event.block.Action.RIGHT_CLICK_AIR -> true

            org.bukkit.event.block.Action.LEFT_CLICK_BLOCK,
            org.bukkit.event.block.Action.LEFT_CLICK_AIR -> false

            else -> return
        }


        player.sendMessage(campementService.handleClaimUnclaim(player.uniqueId, chunk, claim))
        event.isCancelled = true
    }

    override fun useInInventory(event: InventoryClickEvent) {
        if (event.isShiftClick) {
            event.isCancelled = true
            equipmentService.uninvoke(event.whoClicked as Player, this)
            event.whoClicked.sendMessage(ChatFormats.CHAT.append(Component.text("Tu as retiré ton Bâton d'Annexion.")))
        }
    }
}

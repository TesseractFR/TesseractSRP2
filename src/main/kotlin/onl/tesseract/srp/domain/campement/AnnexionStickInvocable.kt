package onl.tesseract.srp.domain.campement

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.lib.equipment.Invocable
import onl.tesseract.lib.util.ItemBuilder
import onl.tesseract.lib.util.ItemLoreBuilder
import onl.tesseract.srp.service.campement.CampementService
import onl.tesseract.srp.service.campement.InteractionAllowResult
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.*

class AnnexionStickInvocable(
    playerUUID: UUID,
    private val campementService: CampementService,
    isInvoked: Boolean = false,
    handSlot: Int = 0
) : Invocable(playerUUID, isInvoked, handSlot) {

    override val slotType: EquipmentSlot = EquipmentSlot.HAND
    override val uniqueName: String = this::class.simpleName!!

    override fun onUninvoke(player: Player, manuelRemoval: Boolean) {}

    override fun onInvoke(player: Player, manuelInvocation: Boolean) {}
    override fun useInInventory(event: InventoryClickEvent) {}

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
            .enchanted(true)
            .lore(lore)
            .build()
    }

    override fun use(event: PlayerInteractEvent) {
        val player = event.player
        val chunk = player.location.chunk
        if (campementService.canInteractInChunk(player.uniqueId, chunk) != InteractionAllowResult.Allow) {
            return
        }
        if (!campementService.hasCampement(player)) {
            event.isCancelled = true
            return
        }
        val claim = when (event.action) {
            Action.RIGHT_CLICK_BLOCK,
            Action.RIGHT_CLICK_AIR -> true

            Action.LEFT_CLICK_BLOCK,
            Action.LEFT_CLICK_AIR -> false

            else -> return
        }
        campementService.handleClaimUnclaim(player, chunk, claim)
        event.isCancelled = true
    }
}

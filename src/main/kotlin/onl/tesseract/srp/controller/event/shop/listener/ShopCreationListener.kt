package onl.tesseract.srp.controller.event.shop.listener

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.service.shop.ShopService
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class ShopCreationListener(
    private val shopService: ShopService,
) : Listener {

    val shopblockMaterial: List<Material> =listOf(Material.CHEST, Material.TRAPPED_CHEST, Material.BARREL)

    @EventHandler
    fun onPlayerInteractWithChest(event: PlayerInteractEvent) {
        // Si il tape le coffre en sneak
        if (event.action != Action.LEFT_CLICK_BLOCK && event.player.isSneaking) return

        // Si y'a un block on le recup sinon fin
        val clickedBlock = event.clickedBlock ?: return

        // Si le block n'est pas un block possible de shop
        if (clickedBlock.type !in shopblockMaterial) return

        val player = event.player
        val itemInHand = player.inventory.itemInMainHand
        // Si le joueur n'a pas d'item en main
        if (itemInHand.type == Material.AIR) {
            return
        }

        //S'il ne peut pas creer de shop
        if(!shopService.canCreateShop(player,clickedBlock)) return

        event.isCancelled = true
        
        // On demande le prix pour l'item en main
        player.sendMessage(
            Component.text("Écris le prix unitaire pour ", NamedTextColor.YELLOW) +
            Component.translatable(itemInHand, NamedTextColor.GOLD) +
            Component.text(" dans le chat:", NamedTextColor.YELLOW)
        )

        shopService.initiateShopCreation(player, clickedBlock, itemInHand)
    }
}
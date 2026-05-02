package onl.tesseract.srp.infrastructure.inventory

import onl.tesseract.srp.domain.port.PlayerInventoryPort
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BukkitPlayerInventoryGateway : PlayerInventoryPort {
    
    override fun getItemNumber(player: UUID, item: ItemStack): Int {
        val bukkitPlayer = Bukkit.getPlayer(player) ?: return 0
        val inventory = bukkitPlayer.inventory
        
        var count = 0
        for (content in inventory.contents) {
            if (content != null && content.isSimilar(item)) {
                count += content.amount
            }
        }
        return count
    }

    override fun removeItems(player: UUID, item: ItemStack, amount: Int) {
        val bukkitPlayer = Bukkit.getPlayer(player) ?: return
        val inventory = bukkitPlayer.inventory
        var remaining = amount
        for (i in 0 until inventory.size) {
            val content = inventory.getItem(i)
            if (content != null && content.isSimilar(item)) {
                val toRemove = minOf(remaining, content.amount)
                content.amount -= toRemove
                remaining -= toRemove
                if (remaining <= 0) break
            }
        }
    }

    override fun giveItems(player: UUID, items: List<ItemStack>) {
        val bukkitPlayer = Bukkit.getPlayer(player) ?: return
        val remaining = bukkitPlayer.inventory.addItem(*items.toTypedArray())
        if (remaining.isNotEmpty()) {
            remaining.values.forEach { bukkitPlayer.world.dropItemNaturally(bukkitPlayer.location, it) }
        }
    }
}

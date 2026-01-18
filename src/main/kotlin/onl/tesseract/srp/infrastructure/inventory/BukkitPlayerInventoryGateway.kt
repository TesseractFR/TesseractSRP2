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
}

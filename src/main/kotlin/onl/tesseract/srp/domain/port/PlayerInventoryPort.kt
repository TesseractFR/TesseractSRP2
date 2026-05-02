package onl.tesseract.srp.domain.port

import org.bukkit.inventory.ItemStack
import java.util.UUID

interface PlayerInventoryPort {
    fun getItemNumber(player: UUID,item: ItemStack) : Int
    fun removeItems(player: UUID, item: ItemStack, amount: Int)
    fun giveItems(player: UUID, items: List<ItemStack>)
}
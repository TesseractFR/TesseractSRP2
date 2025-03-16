package onl.tesseract.srp.controller.menu.job

import org.bukkit.entity.HumanEntity
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

class CustomInventoryView(private val bottomInventory: Inventory, private val topInventory: Inventory, private val player: HumanEntity) : InventoryView {

    override fun getTopInventory(): Inventory = topInventory

    override fun getBottomInventory(): Inventory = bottomInventory

    override fun getPlayer(): HumanEntity = player

    override fun getType(): InventoryType = InventoryType.CHEST

    override fun setItem(slot: Int, item: ItemStack?) {
        if (slot < topInventory.size) {
            topInventory.setItem(slot, item)
        } else {
            bottomInventory.setItem(slot, item)
        }
    }

    override fun getItem(slot: Int): ItemStack? {
        return if (slot < 0) null
        else if (slot < topInventory.size) {
            topInventory.getItem(slot)
        } else {
            bottomInventory.getItem(convertSlot(slot))
        }
    }

    override fun setCursor(item: ItemStack?) {
        player.setItemOnCursor(item)
    }

    override fun getCursor(): ItemStack {
        return player.itemOnCursor
    }

    override fun getInventory(rawSlot: Int): Inventory? {
        return when {
            rawSlot < 0 -> null
            rawSlot < topInventory.size -> topInventory
            rawSlot < topInventory.size + bottomInventory.size -> bottomInventory
            else -> null
        }
    }

    override fun convertSlot(rawSlot: Int): Int {
        if (getInventory(rawSlot) == topInventory)
            return rawSlot
        else if (getInventory(rawSlot) == bottomInventory) {
            val bottomSlot = rawSlot - topInventory.size
            if (bottomSlot in (27..35))
                return bottomSlot - 27
            return bottomSlot + 9
        }
        return -1
    }

    override fun getSlotType(slot: Int): InventoryType.SlotType {
        return InventoryType.SlotType.CONTAINER
    }

    override fun close() {
        player.closeInventory()
    }

    override fun countSlots(): Int {
        return topInventory.size + bottomInventory.size
    }

    override fun setProperty(prop: InventoryView.Property, value: Int): Boolean {
        return false
    }

    override fun getTitle(): String {
        return "Hello world!"
    }

    override fun getOriginalTitle(): String {
        return "Hello world!"
    }

    override fun setTitle(title: String) {

    }
}
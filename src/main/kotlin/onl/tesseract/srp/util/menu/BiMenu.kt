package onl.tesseract.srp.util.menu

import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import net.kyori.adventure.text.Component
import onl.tesseract.lib.menu.AButton
import onl.tesseract.lib.menu.Button
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.service.PluginService
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.lib.task.TaskScheduler
import onl.tesseract.srp.PLUGIN_INSTANCE
import onl.tesseract.srp.controller.menu.job.CustomInventoryView
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.function.Consumer

/**
 * A menu that can make use of both top and bottom inventories to display buttons.
 */
abstract class BiMenu(size: MenuSize, title: Component, previous: Menu? = null) : Menu(size, title, previous) {

    private var npc: NPC? = null

    private val bottomButtons: MutableMap<Int, AButton> = mutableMapOf()

    override fun open(viewer: Player) {
        val topInventory = Bukkit.createInventory(null, size.size, title)
        val fakePlayer = getOrCreateInventoryNPC()
        this.view = CustomInventoryView(fakePlayer.inventory, topInventory, viewer)
        this.viewer = viewer
        viewer.openInventory(view)
        ServiceContainer[PluginService::class.java].registerEventListener(this)
        placeButtons(viewer)
        placeBottomButtons(viewer)
    }

    /**
     * Implement this method to place buttons in bottom inventory. Bottom buttons must be placed with [addBottomButton].
     */
    protected abstract fun placeBottomButtons(viewer: Player)

    protected fun addBottomButton(index: Int, item: ItemStack, function: Consumer<InventoryClickEvent>? = null) {
        val button = Button(item, function)
        bottomButtons[index] = button
        button.draw(this, index, AButton.Side.Bottom)
    }

    private fun getOrCreateInventoryNPC(): Player {
        val registry = CitizensAPI.getNamedNPCRegistry("BottomInventories")
            ?: CitizensAPI.createInMemoryNPCRegistry("BottomInventories")

        return registry.createNPC(EntityType.PLAYER, "Inventory-${hashCode()}").let { npc ->
            this.npc = npc
            if (!npc.isSpawned) {
                npc.spawn(Location(Bukkit.getWorld("world"), 0.0, 5.0, 0.0))
                (npc.entity as Player).isInvisible = true
            }
            return@let npc.entity as Player
        }
    }

    @EventHandler
    override fun onClick(event: InventoryClickEvent) {
        this.view ?: return
        if (event.inventory !in listOf(this.view?.topInventory, this.view?.bottomInventory))
            return

        if (event.clickedInventory == null || event.clickedInventory !in listOf(this.view?.topInventory, this.view?.bottomInventory))
            return
        if (event.currentItem == null) return
        // If the clicked item is a button
        val realSlot = this.view?.convertSlot(event.rawSlot)
        val clickedButton = if (event.clickedInventory == this.view?.topInventory)
            getButtons()[realSlot]
        else
            bottomButtons[realSlot]
        clickedButton?.let {
            ServiceContainer[TaskScheduler::class.java].runLater(1) {
                it.onClick(event)
            }
            event.isCancelled = true
        }
    }

    @EventHandler
    override fun onClose(event: InventoryCloseEvent) {
        if (event.inventory == this.view?.topInventory) {
            viewer = null
            view = null
            ServiceContainer[PluginService::class.java].unregisterEventListener(this)
            object : BukkitRunnable() {
                override fun run() {
                    (event.player as Player).updateInventory()
                    npc?.destroy()
                }
            }.runTaskLater(PLUGIN_INSTANCE, 1)
        }
    }
}
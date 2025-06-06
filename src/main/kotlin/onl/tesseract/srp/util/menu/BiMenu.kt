package onl.tesseract.srp.util.menu

import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.npc.NPC
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import onl.tesseract.lib.menu.*
import onl.tesseract.lib.service.PluginService
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.lib.task.TaskScheduler
import onl.tesseract.srp.PLUGIN_INSTANCE
import onl.tesseract.srp.controller.menu.job.CustomInventoryView
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.function.Consumer

/**
 * A menu that can make use of both top and bottom inventories to display buttons. Bottom buttons must be placed with [addBottomButton].
 */
abstract class BiMenu(size: MenuSize, title: Component, previous: Menu? = null) : Menu(size, title, previous) {

    private var npc: NPC? = null

    private val bottomButtons: MutableMap<Int, AButton> = mutableMapOf()

    override fun open(viewer: Player) {
        val serializedTitle = LegacyComponentSerializer.legacySection().serialize(title)
        val topInventory = Bukkit.createInventory(null, size.size, title)
        val fakePlayer = getOrCreateInventoryNPC(viewer)
        this.view = CustomInventoryView(fakePlayer.inventory, topInventory, viewer, serializedTitle)
        this.viewer = viewer
        viewer.openInventory(view)
        ServiceContainer[PluginService::class.java].registerEventListener(this)
        placeButtons(viewer)
    }

    protected fun addBottomButton(index: Int, item: ItemStack, function: Consumer<InventoryClickEvent>? = null) {
        val button = Button(item, function)
        bottomButtons[index] = button
        button.draw(this, index, AButton.Side.Bottom)
    }

    protected fun addBottomButton(index: Int, async: () -> ItemStack, function: Consumer<InventoryClickEvent>? = null) {
        val button = AsyncButton(async, PLUGIN_INSTANCE, function)
        bottomButtons[index] = button
        button.draw(this, index, AButton.Side.Bottom)
    }

    private fun getOrCreateInventoryNPC(viewer: Player): Player {
        val registry = CitizensAPI.getNamedNPCRegistry("BottomInventories")
            ?: CitizensAPI.createInMemoryNPCRegistry("BottomInventories")

        return registry.createNPC(EntityType.PLAYER, "Inventory-${hashCode()}").let { npc ->
            this.npc = npc
            if (!npc.isSpawned) {
                val spawnLocation = viewer.world.spawnLocation.clone()
                spawnLocation.y = 5.0
                npc.spawn(spawnLocation)
                (npc.entity as Player).isInvisible = true
            }
            return@let npc.entity as Player
        }
    }

    override fun clear() {
        super.clear()
        view?.bottomInventory?.clear()
        bottomButtons.clear()
    }

    fun clearTop() {
        super.clear()
    }

    fun addBottomBackButton(index: Int = 0) {
        if (previous == null) return
        addBottomButton(
            index,
            ItemBuilder(getBackButton())
                .name("Retour")
                .color(NamedTextColor.RED)
                .build()
        ) {
            this.viewer?.let { this.previous?.open(it) }
        }
    }

    fun addBottomCloseButton(index: Int = 8) {
        addBottomButton(
            index,
            ItemBuilder(getCloseButton())
                .name("Fermer")
                .color(NamedTextColor.DARK_RED)
                .build()
        ) {
            this.close()
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
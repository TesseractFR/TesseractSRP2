package onl.tesseract.srp.common.adapter.userside.menu;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import onl.tesseract.lib.Tick;
import onl.tesseract.lib.menu.AButton;
import onl.tesseract.lib.menu.AsyncButton;
import onl.tesseract.lib.menu.Button;
import onl.tesseract.lib.menu.ItemBuilder;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.lib.service.PluginService;
import onl.tesseract.lib.service.ServiceContainer;
import onl.tesseract.lib.task.TaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * A menu that can make use of both top and bottom inventories to display buttons. Bottom buttons must be placed with {@link #addBottomButton(int, ItemStack, Consumer)}.
 */
public abstract class BiMenu extends Menu {

    private NPC npc;
    private final Map<Integer, AButton> bottomButtons = new ConcurrentHashMap<>();

    public BiMenu(MenuSize size, Component title, Menu previous) {
        super(size, title, previous);
    }

    @Override
    public void addButton(int index, AButton button) {
        if (index >= getSize()) {
            addBottomButton(9 + (index - getSize()), button);
            return;
        }
        super.addButton(index, button);
    }

    @Override
    public void open(Player viewer) {
        String serializedTitle = LegacyComponentSerializer.legacySection().serialize(getTitle());
        var topInventory = Bukkit.createInventory(null, getSize(), getTitle());
        Player fakePlayer = getOrCreateInventoryNPC(viewer);
        this.view = new CustomInventoryView(fakePlayer.getInventory(), topInventory, viewer, serializedTitle);
        this.viewer = viewer;
        viewer.openInventory(view);
        ServiceContainer.get(PluginService.class).registerEventListener(this);
        placeButtons(viewer);
    }

    protected void addBottomButton(int index, ItemStack item, Consumer<InventoryClickEvent> function) {
        AButton button = new Button(item, function);
        addBottomButton(index, button);
    }

    protected void addBottomButton(int index, java.util.function.Supplier<ItemStack> async, Consumer<InventoryClickEvent> function) {
        AButton button = new AsyncButton(async, ServiceContainer.get(Plugin.class), function);
        addBottomButton(index, button);
    }

    protected void addBottomButton(int index, AButton button) {
        bottomButtons.put(index, button);
        button.draw(this, index, AButton.Side.Bottom);
    }

    private Player getOrCreateInventoryNPC(Player viewer) {
        var registry = CitizensAPI.getNamedNPCRegistry("BottomInventories");
        if (registry == null) {
            registry = CitizensAPI.createInMemoryNPCRegistry("BottomInventories");
        }

        npc = registry.createNPC(EntityType.PLAYER, "Inventory-" + hashCode());
        if (!npc.isSpawned()) {
            var spawnLocation = viewer.getWorld().getSpawnLocation().clone();
            spawnLocation.setY(spawnLocation.getY() + 5.0);
            npc.spawn(spawnLocation);
            ((Player) npc.getEntity()).setInvisible(true);
        }
        return (Player) npc.getEntity();
    }

    @Override
    public void clear() {
        super.clear();
        if (view != null) {
            view.getBottomInventory().clear();
        }
        bottomButtons.clear();
    }

    public void clearTop() {
        super.clear();
    }

    public void addBottomBackButton(int index) {
        if (getPrevious() != null) {
            addBottomButton(index,
                    new ItemBuilder(getBackButton())
                            .name("Retour")
                            .color(NamedTextColor.RED)
                            .build(),
                    event -> {
                        if (viewer != null) {
                            getPrevious().open(viewer);
                        }
                    });
        }
    }

    public void addBottomBackButton() {
        addBottomBackButton(0);
    }

    public void addBottomCloseButton(int index) {
        addBottomButton(index,
                new ItemBuilder(getCloseButton())
                        .name("Fermer")
                        .color(NamedTextColor.DARK_RED)
                        .build(),
                event -> close());
    }

    public void addBottomCloseButton() {
        addBottomCloseButton(8);
    }

    @EventHandler
    @Override
    public void onClick(InventoryClickEvent event) {
        if (view == null) return;
        if (event.getInventory() != view.getTopInventory() && event.getInventory() != view.getBottomInventory()) {
            return;
        }

        if (event.getClickedInventory() == null ||
                event.getClickedInventory() != view.getTopInventory() &&
                        event.getClickedInventory() != view.getBottomInventory()) {
            return;
        }
        if (event.getCurrentItem() == null) return;

        // If the clicked item is a button
        Integer realSlot = view.convertSlot(event.getRawSlot());
        AButton clickedButton;
        if (event.getClickedInventory() == view.getTopInventory()) {
            var buttons = getButtons();
            if (buttons != null && realSlot != null && realSlot >= 0 && realSlot < buttons.size()) {
                clickedButton = buttons.get(realSlot);
            } else {
                return;
            }
        } else {
            clickedButton = bottomButtons.get(realSlot);
        }

        if (clickedButton != null) {
            ServiceContainer.get(TaskScheduler.class).runLater(new Tick(1), () -> {
                clickedButton.onClick(event);
            });
            event.setCancelled(true);
        }
    }

    @EventHandler
    @Override
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory() == view.getTopInventory()) {
            viewer = null;
            view = null;
            ServiceContainer.get(PluginService.class).unregisterEventListener(this);
            new BukkitRunnable() {
                @Override
                public void run() {
                    ((Player) event.getPlayer()).updateInventory();
                    if (npc != null) {
                        npc.destroy();
                    }
                }
            }.runTaskLater(ServiceContainer.get(Plugin.class), 1);
        }
    }
}


package onl.tesseract.srp.territory.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.itembuilder.ItemLoreBuilder;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public abstract class DecoratedMenu extends Menu {

    protected DecoratedMenu(MenuSize size, Component title, Menu previous) {
        super(size, title, previous);
    }

    protected List<Integer> graySlots()      { return List.of(); }
    protected List<Integer> purpleSlots()    { return List.of(); }
    protected List<Integer> cyanSlots()      { return List.of(); }
    protected List<Integer> greenSlots()     { return List.of(); }
    protected List<Integer> limeSlots()      { return List.of(); }
    protected List<Integer> whiteSlots()     { return List.of(); }
    protected List<Integer> lightBlueSlots() { return List.of(); }
    protected List<Integer> blueSlots()      { return List.of(); }

    protected void placeDecorations() {
        Map<Material, List<Integer>> slotsByMaterial = Map.of(
                Material.GRAY_STAINED_GLASS_PANE, graySlots(),
                Material.PURPLE_STAINED_GLASS_PANE, purpleSlots(),
                Material.CYAN_STAINED_GLASS_PANE, cyanSlots(),
                Material.GREEN_STAINED_GLASS_PANE, greenSlots(),
                Material.LIME_STAINED_GLASS_PANE, limeSlots(),
                Material.WHITE_STAINED_GLASS_PANE, whiteSlots(),
                Material.LIGHT_BLUE_STAINED_GLASS_PANE, lightBlueSlots(),
                Material.BLUE_STAINED_GLASS_PANE, blueSlots());
        slotsByMaterial.forEach((material, slots) -> {
            ItemStack pane = new ItemBuilder(material).name(" ").build();
            slots.forEach(slot -> addButton(slot, pane));
        });
    }

    protected void button(int slot, ItemStack item, Consumer<ClickType> action) {
        addButton(slot, item, event -> action.accept(event.getClick()));
    }

    protected static String playerName(UUID id) {
        String name = Bukkit.getOfflinePlayer(id).getName();
        return name != null ? name : id.toString();
    }

    protected static int maxPage(int total, int pageSize) {
        return total == 0 ? 0 : (total - 1) / pageSize;
    }

    protected static int clampPage(int page, int total, int pageSize) {
        return Math.clamp(page, 0, maxPage(total, pageSize));
    }

    protected void addPagination(Player viewer, int page, int total, int pageSize,
                                 int prevSlot, int nextSlot, IntFunction<Menu> menuForPage) {
        int max = maxPage(total, pageSize);
        var lore = new ItemLoreBuilder()
                .append("Page " + (page + 1) + " / " + (max + 1), NamedTextColor.GRAY)
                .get();
        if (page > 0) {
            button(prevSlot, new ItemBuilder(Material.ARROW)
                            .name("Page précédente", NamedTextColor.YELLOW).lore(lore).build(),
                    click -> menuForPage.apply(page - 1).open(viewer));
        }
        if (page < max) {
            button(nextSlot, new ItemBuilder(Material.ARROW)
                            .name("Page suivante", NamedTextColor.YELLOW).lore(lore).build(),
                    click -> menuForPage.apply(page + 1).open(viewer));
        }
    }
}
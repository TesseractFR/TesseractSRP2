package onl.tesseract.srp.skill.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemMetaUtil {
    static void displayName(ItemStack item, Component c) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.displayName(c);
        item.setItemMeta(meta);
    }

    static void lore(ItemStack item, List<Component> lore) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.lore(lore);
        item.setItemMeta(meta);
    }
}

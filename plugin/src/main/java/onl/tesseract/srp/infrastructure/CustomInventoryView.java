package onl.tesseract.srp.infrastructure;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class CustomInventoryView implements InventoryView {

    private final Inventory bottomInventory;
    private final Inventory topInventory;
    private final HumanEntity player;
    private String title;

    public CustomInventoryView(Inventory bottomInventory, Inventory topInventory, HumanEntity player, String title) {
        this.bottomInventory = bottomInventory;
        this.topInventory = topInventory;
        this.player = player;
        this.title = title;
    }

    @Override
    public Inventory getTopInventory() { return topInventory; }

    @Override
    public Inventory getBottomInventory() { return bottomInventory; }

    @Override
    public HumanEntity getPlayer() { return player; }

    @Override
    public InventoryType getType() { return InventoryType.CHEST; }

    @Override
    public void setItem(int slot, ItemStack item) {
        if (slot < topInventory.getSize()) {
            topInventory.setItem(slot, item);
        } else {
            bottomInventory.setItem(slot, item);
        }
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0) return null;
        if (slot < topInventory.getSize()) {
            return topInventory.getItem(slot);
        } else {
            return bottomInventory.getItem(convertSlot(slot));
        }
    }

    @Override
    public void setCursor(ItemStack item) {
        player.setItemOnCursor(item);
    }

    @Override
    public ItemStack getCursor() {
        return player.getItemOnCursor();
    }

    @Override
    public Inventory getInventory(int rawSlot) {
        if (rawSlot < 0) return null;
        if (rawSlot < topInventory.getSize()) return topInventory;
        if (rawSlot < topInventory.getSize() + bottomInventory.getSize()) return bottomInventory;
        return null;
    }

    @Override
    public int convertSlot(int rawSlot) {
        if (getInventory(rawSlot) == topInventory) return rawSlot;
        if (getInventory(rawSlot) == bottomInventory) {
            int bottomSlot = rawSlot - topInventory.getSize();
            if (bottomSlot >= 27 && bottomSlot <= 35) return bottomSlot - 27;
            return bottomSlot + 9;
        }
        return -1;
    }

    @Override
    public InventoryType.SlotType getSlotType(int slot) { return InventoryType.SlotType.CONTAINER; }

    @Override
    public void open() {}

    @Override
    public void close() { player.closeInventory(); }

    @Override
    public int countSlots() { return topInventory.getSize() + bottomInventory.getSize(); }

    @Override
    public boolean setProperty(InventoryView.Property prop, int value) { return false; }

    @Deprecated
    @Override
    public String getTitle() { return title; }

    @Deprecated
    @Override
    public String getOriginalTitle() { return title; }

    @Deprecated
    @Override
    public void setTitle(String title) { this.title = title; }
}


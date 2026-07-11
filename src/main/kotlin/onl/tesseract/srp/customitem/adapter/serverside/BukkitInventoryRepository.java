package onl.tesseract.srp.customitem.adapter.serverside;

import onl.tesseract.srp.customitem.adapter.CustomItemstackMapper;
import onl.tesseract.srp.customitem.domain.model.CustomItem;
import onl.tesseract.srp.customitem.domain.model.MaterialName;
import onl.tesseract.srp.customitem.domain.port.serverside.InventoryRepository;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BukkitInventoryRepository implements InventoryRepository {
    private final CustomItemstackMapper itemstackMapper;

    public BukkitInventoryRepository(CustomItemstackMapper itemstackMapper) {
        this.itemstackMapper = itemstackMapper;
    }

    public PlayerInventory getPlayerInventory(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Player not found");
        }
        return player.getInventory();
    }


    @Override
    public int getCustomQuantity(UUID player, CustomItem customItem) {
        PlayerInventory inventory = getPlayerInventory(player);
        AtomicInteger count = new AtomicInteger();
        ItemStack refItem = itemstackMapper.toItemStack(customItem);
        inventory.forEach(itemStack -> {
            if (itemStack.isSimilar(refItem)) {
                count.addAndGet(itemStack.getAmount());
            }
        });
        return count.get();
    }

    @Override
    public int getVanillaQuantity(UUID player, MaterialName materialName) {
        AtomicInteger count = new AtomicInteger();
        PlayerInventory inventory = getPlayerInventory(player);
        Material material = Material.valueOf(materialName.value());
        inventory.forEach(itemStack -> {
            if (itemStack == null) return;
            if (material.equals(itemStack.getType()) && !isCustomItem(itemStack)) {
                count.addAndGet(itemStack.getAmount());
            }
        });
        return count.get();
    }

    @Override
    public void removeCustomItem(UUID player, CustomItem customItem, int amount) {
        PlayerInventory inventory = getPlayerInventory(player);
        ItemStack refItem = itemstackMapper.toItemStack(customItem);
        removeItemStack(inventory, refItem, amount);
    }

    private static void removeItemStack(PlayerInventory inventory, ItemStack refItem, int amount) {
        AtomicInteger count = new AtomicInteger(amount);
        for(int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack == null) continue;
            if (itemStack.isSimilar(refItem)) {
                int removeAmount = Math.min(count.get(), itemStack.getAmount());
                count.addAndGet(-removeAmount);
                itemStack.setAmount(itemStack.getAmount() - removeAmount);
                if (itemStack.getAmount() <= 0) {
                    inventory.setItem(i, null);
                }
                if (count.get() <= 0) {
                    break;
                }
            }
        }
    }

    @Override
    public void removeVanillaItem(UUID player, MaterialName materialName, int amount) {
        PlayerInventory inventory = getPlayerInventory(player);
        ItemStack refItem = new ItemStack(Material.valueOf(materialName.value()));
        removeItemStack(inventory, refItem, amount);

    }

    @Override
    public void addCustomItem(UUID player, CustomItem customItem, int amount) {
        PlayerInventory inventory = getPlayerInventory(player);
        ItemStack refItem = itemstackMapper.toItemStack(customItem);
        addItemStack(inventory, refItem, amount);

    }

    @Override
    public void addVanillaItem(UUID player, MaterialName materialName, int amount) {
        PlayerInventory inventory = getPlayerInventory(player);
        ItemStack refItem = new ItemStack(Material.valueOf(materialName.value()));
        addItemStack(inventory, refItem, amount);
    }

    private void addItemStack(PlayerInventory inventory, ItemStack refItem, int amount) {
        while (amount > 0) {
            int stackSize = Math.min(amount, refItem.getMaxStackSize());
            inventory.addItem(refItem.asQuantity(stackSize));
            amount -= stackSize;
        }
    }


    public boolean isCustomItem(ItemStack itemStack) {
        return itemstackMapper.isCustom(itemStack);
    }
}

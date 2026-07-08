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
        inventory.forEach(itemStack -> {
            if (itemStack.isSimilar(itemstackMapper.toItemStack(customItem))) {
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

    public boolean isCustomItem(ItemStack itemStack) {
        return itemstackMapper.isCustom(itemStack);
    }
}

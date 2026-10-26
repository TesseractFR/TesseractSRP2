package onl.tesseract.srp.contract.adapter.serverside;

import net.kyori.adventure.text.Component;
import onl.tesseract.srp.contract.adapter.serverside.yaml.YamlContractConfigRepository;
import onl.tesseract.srp.contract.adapter.utils.ContractDisplay;
import onl.tesseract.srp.contract.adapter.utils.ContractScrollItem;
import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.Quality;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerInventoryRepository;
import onl.tesseract.srp.customitem.domain.model.MaterialName;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

@org.springframework.stereotype.Component
public class TesseractPlayerInventoryRepository implements PlayerInventoryRepository {

    private final CustomItemService customItems;
    private final YamlContractConfigRepository config;

    public TesseractPlayerInventoryRepository(CustomItemService customItems, YamlContractConfigRepository config) {
        this.customItems = customItems;
        this.config = config;
    }

    @Override
    public int countAtLeast(PlayerID player, String material, Quality min) {
        MaterialName name = new MaterialName(material);
        if (!customItems.isCustomMaterial(name)) {
            return customItems.getItemQuantity(player.value(), name, toCommon(Quality.POOR));
        }
        int total = 0;
        for (Quality quality : qualitiesFrom(min)) {
            total += customItems.getItemQuantity(player.value(), name, toCommon(quality));
        }
        return total;
    }

    @Override
    public Map<Quality, Integer> removeAtLeast(PlayerID player, String material, Quality min, int amount) {
        Map<Quality, Integer> removed = new EnumMap<>(Quality.class);
        if (amount <= 0 || countAtLeast(player, material, min) < amount) {
            return removed;
        }
        MaterialName name = new MaterialName(material);
        UUID uuid = player.value();
        Quality[] qualities = customItems.isCustomMaterial(name) ? qualitiesFrom(min) : new Quality[]{Quality.POOR};
        int remaining = amount;
        try {
            for (Quality quality : qualities) {
                int taken = Math.min(customItems.getItemQuantity(uuid, name, toCommon(quality)), remaining);
                if (taken > 0) {
                    customItems.removeItem(uuid, name, toCommon(quality), taken);
                    removed.put(quality, taken);
                    remaining -= taken;
                }
            }
        } catch (RuntimeException e) {
            giveBack(player, material, removed);
            throw e;
        }
        if (remaining > 0) {
            giveBack(player, material, removed);
            return new EnumMap<>(Quality.class);
        }
        return removed;
    }

    @Override
    public void giveBack(PlayerID player, String material, Map<Quality, Integer> items) {
        MaterialName name = new MaterialName(material);
        items.forEach((quality, amount) -> customItems.addItem(player.value(), name, toCommon(quality), amount));
    }

    @Override
    public boolean canReceiveScroll(PlayerID player) {
        Player online = Bukkit.getPlayer(player.value());
        return online != null && online.getInventory().firstEmpty() != -1;
    }

    @Override
    public boolean giveScroll(PlayerID player, Contract contract) {
        Player online = Bukkit.getPlayer(player.value());
        if (online == null || contract.id() == null) {
            return false;
        }
        Optional<ItemStack> base = ContractScrollItem.create();
        if (base.isEmpty()) {
            return false;
        }
        ItemStack stack = base.get();
        ItemMeta meta = stack.getItemMeta();
        ContractScrollItem.writeContractId(meta, contract.id());
        meta.lore(lore(contract));
        meta.setMaxStackSize(1);
        stack.setItemMeta(meta);
        return online.getInventory().addItem(stack).isEmpty();
    }

    @Override
    public boolean hasScroll(PlayerID player, long contractId) {
        Player online = Bukkit.getPlayer(player.value());
        return online != null && slotOf(online.getInventory(), contractId) >= 0;
    }

    @Override
    public boolean removeScroll(PlayerID player, long contractId) {
        Player online = Bukkit.getPlayer(player.value());
        if (online == null) {
            return false;
        }
        PlayerInventory inventory = online.getInventory();
        int slot = slotOf(inventory, contractId);
        if (slot < 0) {
            return false;
        }
        inventory.setItem(slot, null);
        return true;
    }

    private static int slotOf(PlayerInventory inventory, long contractId) {
        ItemStack[] contents = inventory.getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            OptionalLong id = ContractScrollItem.readContractId(contents[slot]);
            if (id.isPresent() && id.getAsLong() == contractId) {
                return slot;
            }
        }
        return -1;
    }

    private List<Component> lore(Contract contract) {
        List<Component> lines = new ArrayList<>(ContractDisplay.details(contract));
        lines.add(config.getNpcName(contract.job()).map(ContractDisplay::footer).orElseGet(() -> ContractDisplay.footer(contract)));
        return lines;
    }

    private static Quality[] qualitiesFrom(Quality min) {
        Quality[] all = Quality.values();
        return Arrays.copyOfRange(all, min.ordinal(), all.length);
    }

    private static onl.tesseract.srp.common.domain.model.enums.Quality toCommon(Quality quality) {
        return onl.tesseract.srp.common.domain.model.enums.Quality.valueOf(quality.name());
    }
}
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

    private final CustomItemService customItemService;
    private final YamlContractConfigRepository yamlContractConfigRepository;

    public TesseractPlayerInventoryRepository(CustomItemService customItemService, YamlContractConfigRepository yamlContractConfigRepository) {
        this.customItemService = customItemService;
        this.yamlContractConfigRepository = yamlContractConfigRepository;
    }

    @Override
    public int countAtLeast(PlayerID player, String material, Quality minimalQuality) {
        MaterialName materialName = new MaterialName(material);
        if (!customItemService.isCustomMaterial(materialName)) {
            return customItemService.getItemQuantity(player.value(), materialName, toCommon(Quality.POOR));
        }
        int totalAmount = 0;
        for (Quality quality : qualitiesFrom(minimalQuality)) {
            totalAmount += customItemService.getItemQuantity(player.value(), materialName, toCommon(quality));
        }
        return totalAmount;
    }

    @Override
    public Map<Quality, Integer> removeAtLeast(PlayerID player, String material, Quality minimalQuality, int amount) {
        Map<Quality, Integer> removedItems = new EnumMap<>(Quality.class);
        if (amount <= 0 || countAtLeast(player, material, minimalQuality) < amount) {
            return removedItems;
        }
        MaterialName materialName = new MaterialName(material);
        UUID playerUuid = player.value();
        Quality[] qualitiesToTake = customItemService.isCustomMaterial(materialName) ? qualitiesFrom(minimalQuality) : new Quality[]{Quality.POOR};
        int remainingAmount = amount;
        try {
            for (Quality quality : qualitiesToTake) {
                int takenAmount = Math.min(customItemService.getItemQuantity(playerUuid, materialName, toCommon(quality)), remainingAmount);
                if (takenAmount > 0) {
                    customItemService.removeItem(playerUuid, materialName, toCommon(quality), takenAmount);
                    removedItems.put(quality, takenAmount);
                    remainingAmount -= takenAmount;
                }
            }
        } catch (RuntimeException e) {
            giveBack(player, material, removedItems);
            throw e;
        }
        if (remainingAmount > 0) {
            giveBack(player, material, removedItems);
            return new EnumMap<>(Quality.class);
        }
        return removedItems;
    }

    @Override
    public void giveBack(PlayerID player, String material, Map<Quality, Integer> itemsByQuality) {
        MaterialName materialName = new MaterialName(material);
        itemsByQuality.forEach((quality, amount) -> customItemService.addItem(player.value(), materialName, toCommon(quality), amount));
    }

    @Override
    public boolean canReceiveScroll(PlayerID player) {
        Player onlinePlayer = Bukkit.getPlayer(player.value());
        return onlinePlayer != null && onlinePlayer.getInventory().firstEmpty() != -1;
    }

    @Override
    public boolean giveScroll(PlayerID player, Contract contract) {
        Player onlinePlayer = Bukkit.getPlayer(player.value());
        if (onlinePlayer == null || contract.id() == null) {
            return false;
        }
        Optional<ItemStack> baseScroll = ContractScrollItem.create();
        if (baseScroll.isEmpty()) {
            return false;
        }
        ItemStack scrollItem = baseScroll.get();
        ItemMeta scrollMeta = scrollItem.getItemMeta();
        ContractScrollItem.writeContractId(scrollMeta, contract.id());
        ContractScrollItem.writeExpiry(scrollMeta, contract.expiresAt());
        scrollMeta.lore(lore(contract));
        scrollMeta.setMaxStackSize(1);
        scrollItem.setItemMeta(scrollMeta);
        return onlinePlayer.getInventory().addItem(scrollItem).isEmpty();
    }

    @Override
    public boolean hasScroll(PlayerID player, long contractId) {
        Player onlinePlayer = Bukkit.getPlayer(player.value());
        return onlinePlayer != null && slotOf(onlinePlayer.getInventory(), contractId) >= 0;
    }

    @Override
    public boolean removeScroll(PlayerID player, long contractId) {
        Player onlinePlayer = Bukkit.getPlayer(player.value());
        if (onlinePlayer == null) {
            return false;
        }
        PlayerInventory inventory = onlinePlayer.getInventory();
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
            OptionalLong scrollContractId = ContractScrollItem.readContractId(contents[slot]);
            if (scrollContractId.isPresent() && scrollContractId.getAsLong() == contractId) {
                return slot;
            }
        }
        return -1;
    }

    private List<Component> lore(Contract contract) {
        List<Component> loreLines = new ArrayList<>(ContractDisplay.details(contract));
        loreLines.add(yamlContractConfigRepository.getNpcName(contract.job()).map(ContractDisplay::footer).orElseGet(() -> ContractDisplay.footer(contract)));
        return loreLines;
    }

    private static Quality[] qualitiesFrom(Quality minimalQuality) {
        Quality[] allQualities = Quality.values();
        return Arrays.copyOfRange(allQualities, minimalQuality.ordinal(), allQualities.length);
    }

    private static onl.tesseract.srp.common.domain.model.enums.Quality toCommon(Quality quality) {
        return onl.tesseract.srp.common.domain.model.enums.Quality.valueOf(quality.name());
    }
}
package onl.tesseract.srp.contract.adapter.serverside;

import net.kyori.adventure.text.Component;
import onl.tesseract.srp.contract.adapter.serverside.yaml.YamlContractConfigRepository;
import onl.tesseract.srp.contract.adapter.utils.ContractDisplay;
import onl.tesseract.srp.contract.adapter.utils.ContractScrollItem;
import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.port.serverside.ContractScrollRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

@org.springframework.stereotype.Component
public class BukkitContractScrollRepository implements ContractScrollRepository {

    private final YamlContractConfigRepository yamlContractConfigRepository;

    public BukkitContractScrollRepository(YamlContractConfigRepository yamlContractConfigRepository) {
        this.yamlContractConfigRepository = yamlContractConfigRepository;
    }

    @Override
    public boolean canReceive(PlayerID player) {
        Player onlinePlayer = Bukkit.getPlayer(player.value());
        return onlinePlayer != null && onlinePlayer.getInventory().firstEmpty() != -1;
    }

    @Override
    public boolean give(PlayerID player, Contract contract, boolean hasJobBonus) {
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
        scrollMeta.lore(lore(contract, hasJobBonus));
        scrollMeta.setMaxStackSize(1);
        scrollItem.setItemMeta(scrollMeta);
        return onlinePlayer.getInventory().addItem(scrollItem).isEmpty();
    }

    @Override
    public boolean has(PlayerID player, long contractId) {
        Player onlinePlayer = Bukkit.getPlayer(player.value());
        return onlinePlayer != null && slotOf(onlinePlayer.getInventory(), contractId) >= 0;
    }

    @Override
    public boolean remove(PlayerID player, long contractId) {
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

    private List<Component> lore(Contract contract, boolean hasJobBonus) {
        List<Component> loreLines = new ArrayList<>(ContractDisplay.scrollDetails(contract, hasJobBonus));
        loreLines.add(yamlContractConfigRepository.getNpcName(contract.job()).map(ContractDisplay::footer).orElseGet(() -> ContractDisplay.footer(contract)));
        return loreLines;
    }
}
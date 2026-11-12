package onl.tesseract.srp.contract.adapter.serverside;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.itembuilder.ItemLoreBuilder;
import onl.tesseract.srp.common.adapter.utils.QualityAdapter;
import onl.tesseract.srp.contract.adapter.serverside.yaml.YamlContractConfigRepository;
import onl.tesseract.srp.contract.adapter.utils.ContractConstantes;
import onl.tesseract.srp.contract.adapter.utils.ContractItemUtils;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.port.serverside.ContractScrollRepository;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Optional;

import static onl.tesseract.srp.contract.adapter.utils.ContractConstantes.*;


@org.springframework.stereotype.Component
public class BukkitContractScrollRepository implements ContractScrollRepository {
    private final YamlContractConfigRepository yamlContractConfigRepository;

    public BukkitContractScrollRepository(YamlContractConfigRepository yamlContractConfigRepository) {
        this.yamlContractConfigRepository = yamlContractConfigRepository;
    }

    @Override
    public boolean canReceiveScroll(PlayerId player) {
        Player onlinePlayer = Bukkit.getPlayer(player.value());
        return onlinePlayer != null && onlinePlayer.getInventory().firstEmpty() != -1;
    }

    @Override
    public boolean giveScroll(PlayerId player, Contract contract, boolean hasJobBonus) {
        Player onlinePlayer = Bukkit.getPlayer(player.value());
        if (onlinePlayer == null || contract.id() == null) {
            return false;
        }
        ItemStack scrollItem = new ItemBuilder(ContractConstantes.BASE_SCROLL)
                .lore(lore(contract))
                .name(Component.text("Contrat · " + StringUtils.capitalize(contract.material().value()), NamedTextColor.GOLD))
                .persistData(CONTRACT_ID_KEY, PersistentDataType.LONG, contract.id())
                .persistData(EXPIRES_AT_KEY, PersistentDataType.LONG, contract.expiresAt().toEpochMilli())
                .maxStackSize(1)
                .build();
        return onlinePlayer.getInventory().addItem(scrollItem).isEmpty();
    }

    @Override
    public boolean hasScroll(PlayerId player, long contractId) {
        Player onlinePlayer = Bukkit.getPlayer(player.value());
        return onlinePlayer != null && slotOf(onlinePlayer.getInventory(), contractId) >= 0;
    }

    @Override
    public boolean removeScroll(PlayerId player, long contractId) {
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
            Optional<Long> scrollContractId = ContractItemUtils.readContractId(contents[slot]);
            if (scrollContractId.isPresent() && scrollContractId.get() == contractId) {
                return slot;
            }
        }
        return -1;
    }

    private List<Component> lore(Contract contract) {
        boolean isCraft = contract.type() == ContractType.CRAFT;
        QualityAdapter minimalQuality = QualityAdapter.from(contract.minimalQuality());
        ItemLoreBuilder itemLoreBuilder = new ItemLoreBuilder()
                .append((isCraft ? "Artisanat" : "Récolte") + " · " + StringUtils.capitalize(contract.job()), NamedTextColor.DARK_GRAY)
                .newline()
                .append("À livrer", NamedTextColor.GOLD)
                .newline()
                .append(BULLET + "Quantité : " + contract.quantity(), NamedTextColor.WHITE)
                .newline()
                .append(BULLET + "Qualité minimale : " +  minimalQuality.label(), minimalQuality.color())
                .newline()
                .append("Récompense", NamedTextColor.GOLD)
                .newline()
                .append(BULLET + "Gain : " + contract.reward() + " lys", NamedTextColor.GREEN)
                .newline()
                .append("Expire le : " + EXPIRATION_DATE_FORMAT.format(contract.expiresAt()), NamedTextColor.RED)
                .newline()
                .newline()
                .append(yamlContractConfigRepository.getNpcName(contract.job())
                        .map(it->"» Apporte-le à " + it)
                        .orElse(contract.type() == ContractType.CRAFT ? "PNJ de la compétence" : "PNJ du métier")
                        , NamedTextColor.YELLOW);

        return itemLoreBuilder.get();
    }

}

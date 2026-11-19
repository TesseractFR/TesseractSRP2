package onl.tesseract.srp.contract.adapter.serverside.scroll;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.itembuilder.ItemLoreBuilder;
import onl.tesseract.srp.common.adapter.utils.QualityAdapter;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.adapter.scroll.ContractScrollReader;
import onl.tesseract.srp.contract.adapter.serverside.yaml.ContractNpcNameConfig;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractId;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.port.serverside.ContractScrollPort;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static onl.tesseract.srp.contract.adapter.scroll.ContractScrollItems.BASE_SCROLL;
import static onl.tesseract.srp.contract.adapter.scroll.ContractScrollItems.BULLET;
import static onl.tesseract.srp.contract.adapter.scroll.ContractScrollItems.CONTRACT_ID_KEY;
import static onl.tesseract.srp.contract.adapter.scroll.ContractScrollItems.EXPIRATION_DATE_FORMAT;
import static onl.tesseract.srp.contract.adapter.scroll.ContractScrollItems.EXPIRES_AT_KEY;

/**
 * Range les parchemins de contrat dans l'inventaire Bukkit des joueurs en ligne. Un parchemin est
 * reconnu par l'identifiant de contrat écrit dans ses données persistantes.
 */
@org.springframework.stereotype.Component
public class BukkitContractScrollPort implements ContractScrollPort {
    private final ContractNpcNameConfig contractNpcNameConfig;

    public BukkitContractScrollPort(ContractNpcNameConfig contractNpcNameConfig) {
        this.contractNpcNameConfig = contractNpcNameConfig;
    }

    @Override
    public boolean canReceiveScroll(PlayerId player) {
        return findOnlinePlayer(player)
                .map(onlinePlayer -> onlinePlayer.getInventory().firstEmpty() != -1)
                .orElse(false);
    }

    @Override
    public boolean giveScroll(PlayerId player, Contract contract) {
        return findOnlinePlayer(player)
                .map(onlinePlayer -> onlinePlayer.getInventory().addItem(buildScroll(contract)).isEmpty())
                .orElse(false);
    }

    @Override
    public boolean hasScroll(PlayerId player, ContractId contractId) {
        return findOnlinePlayer(player)
                .flatMap(onlinePlayer -> findScrollSlot(onlinePlayer.getInventory(), contractId))
                .isPresent();
    }

    @Override
    public boolean removeScroll(PlayerId player, ContractId contractId) {
        Optional<PlayerInventory> inventory = findOnlinePlayer(player).map(Player::getInventory);
        Optional<Integer> slot = inventory.flatMap(playerInventory -> findScrollSlot(playerInventory, contractId));
        slot.ifPresent(scrollSlot -> inventory.ifPresent(playerInventory -> playerInventory.setItem(scrollSlot, null)));
        return slot.isPresent();
    }

    private static Optional<Player> findOnlinePlayer(PlayerId player) {
        return Optional.ofNullable(Bukkit.getPlayer(player.value()));
    }

    private static Optional<Integer> findScrollSlot(PlayerInventory inventory, ContractId contractId) {
        ItemStack[] contents = inventory.getContents();
        return IntStream.range(0, contents.length)
                .filter(slot -> ContractScrollReader.readContractId(contents[slot]).filter(contractId::equals).isPresent())
                .boxed()
                .findFirst();
    }

    private ItemStack buildScroll(Contract contract) {
        return new ItemBuilder(BASE_SCROLL)
                .lore(buildLore(contract))
                .name(Component.text("Contrat · " + StringUtils.capitalize(contract.material().value()), NamedTextColor.GOLD))
                .persistData(CONTRACT_ID_KEY, PersistentDataType.LONG, contract.id().value())
                .persistData(EXPIRES_AT_KEY, PersistentDataType.LONG, contract.expiresAt().toEpochMilli())
                .maxStackSize(1)
                .build();
    }

    private List<Component> buildLore(Contract contract) {
        boolean isCraft = contract.type() == ContractType.CRAFT;
        QualityAdapter minimalQuality = QualityAdapter.from(contract.minimalQuality());
        return new ItemLoreBuilder()
                .append((isCraft ? "Artisanat" : "Récolte") + " · " + StringUtils.capitalize(contract.client().name()), NamedTextColor.DARK_GRAY)
                .newline(2)
                .append("À livrer", NamedTextColor.GOLD)
                .newline()
                .append(BULLET + "Quantité : " + contract.quantity(), NamedTextColor.WHITE)
                .newline()
                .append(BULLET + "Qualité minimale : ", NamedTextColor.WHITE)
                .append(minimalQuality.label(), minimalQuality.color())
                .newline(2)
                .append("Récompense", NamedTextColor.GOLD)
                .newline()
                .append(BULLET + "Gain : " + contract.reward().value() + " lys", NamedTextColor.GREEN)
                .newline(2)
                .append("Expire le : " + EXPIRATION_DATE_FORMAT.format(contract.expiresAt()), NamedTextColor.RED)
                .newline()
                .newline()
                .append(contractNpcNameConfig.findNpcName(contract.client())
                        .map(npcName -> "» Apporte-le à " + npcName)
                        .orElse(isCraft ? "PNJ de la compétence" : "PNJ du métier"), NamedTextColor.YELLOW)
                .get();
    }
}

package onl.tesseract.srp.contract.adapter.utils;

import dev.lone.itemsadder.api.CustomStack;
import onl.tesseract.srp.common.adapter.userside.menu.CustomMenuButton;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

public final class ContractScrollItem {

    private static final NamespacedKey CONTRACT_ID_KEY = Objects.requireNonNull(NamespacedKey.fromString("srp:contract_id"));
    private static final NamespacedKey EXPIRES_AT_KEY = Objects.requireNonNull(NamespacedKey.fromString("srp:contract_expires_at"));

    private ContractScrollItem() {
    }

    public static Optional<ItemStack> create() {
        CustomStack customStack = CustomStack.getInstance(CustomMenuButton.MENU_CONTRACT_SCROLL_BUTTON.getItemTag().value());
        return customStack == null ? Optional.empty() : Optional.of(customStack.getItemStack());
    }

    public static void writeContractId(ItemMeta itemMeta, long contractId) {
        itemMeta.getPersistentDataContainer().set(CONTRACT_ID_KEY, PersistentDataType.LONG, contractId);
    }

    public static void writeExpiry(ItemMeta itemMeta, Instant expiresAt) {
        itemMeta.getPersistentDataContainer().set(EXPIRES_AT_KEY, PersistentDataType.LONG, expiresAt.toEpochMilli());
    }

    public static Optional<Instant> readExpiry(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return Optional.empty();
        }
        Long expiresAtMillis = itemStack.getItemMeta().getPersistentDataContainer().get(EXPIRES_AT_KEY, PersistentDataType.LONG);
        return expiresAtMillis == null ? Optional.empty() : Optional.of(Instant.ofEpochMilli(expiresAtMillis));
    }

    public static boolean isExpired(ItemStack itemStack, Instant now) {
        return readContractId(itemStack).isPresent() && readExpiry(itemStack).map(now::isAfter).orElse(false);
    }

    public static OptionalLong readContractId(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return OptionalLong.empty();
        }
        Long contractId = itemStack.getItemMeta().getPersistentDataContainer().get(CONTRACT_ID_KEY, PersistentDataType.LONG);
        return contractId == null ? OptionalLong.empty() : OptionalLong.of(contractId);
    }
}
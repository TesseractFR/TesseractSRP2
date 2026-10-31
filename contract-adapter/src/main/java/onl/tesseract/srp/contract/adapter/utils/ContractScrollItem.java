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

    private static final NamespacedKey KEY = Objects.requireNonNull(NamespacedKey.fromString("srp:contract_id"));

    private static final NamespacedKey EXPIRES_KEY = Objects.requireNonNull(NamespacedKey.fromString("srp:contract_expires_at"));

    private ContractScrollItem() {
    }

    public static Optional<ItemStack> create() {
        CustomStack custom = CustomStack.getInstance(CustomMenuButton.MENU_CONTRACT_SCROLL_BUTTON.getItemTag().value());
        return custom == null ? Optional.empty() : Optional.of(custom.getItemStack());
    }

    public static void writeContractId(ItemMeta meta, long contractId) {
        meta.getPersistentDataContainer().set(KEY, PersistentDataType.LONG, contractId);
    }

    public static void writeExpiry(ItemMeta meta, Instant expiresAt) {
        meta.getPersistentDataContainer().set(EXPIRES_KEY, PersistentDataType.LONG, expiresAt.toEpochMilli());
    }

    public static Optional<Instant> readExpiry(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return Optional.empty();
        }
        Long millis = stack.getItemMeta().getPersistentDataContainer().get(EXPIRES_KEY, PersistentDataType.LONG);
        return millis == null ? Optional.empty() : Optional.of(Instant.ofEpochMilli(millis));
    }

    public static boolean isExpired(ItemStack stack, Instant now) {
        return readContractId(stack).isPresent() && readExpiry(stack).map(now::isAfter).orElse(false);
    }

    public static OptionalLong readContractId(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return OptionalLong.empty();
        }
        Long id = stack.getItemMeta().getPersistentDataContainer().get(KEY, PersistentDataType.LONG);
        return id == null ? OptionalLong.empty() : OptionalLong.of(id);
    }
}
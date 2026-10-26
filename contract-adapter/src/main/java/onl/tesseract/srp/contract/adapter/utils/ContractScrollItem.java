package onl.tesseract.srp.contract.adapter.utils;

import dev.lone.itemsadder.api.CustomStack;
import onl.tesseract.srp.common.adapter.userside.menu.CustomMenuButton;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

public final class ContractScrollItem {

    private static final NamespacedKey KEY = Objects.requireNonNull(NamespacedKey.fromString("srp:contract_id"));

    private ContractScrollItem() {
    }

    public static Optional<ItemStack> create() {
        CustomStack custom = CustomStack.getInstance(CustomMenuButton.MENU_CONTRACT_SCROLL_BUTTON.getItemTag().value());
        return custom == null ? Optional.empty() : Optional.of(custom.getItemStack());
    }

    public static void writeContractId(ItemMeta meta, long contractId) {
        meta.getPersistentDataContainer().set(KEY, PersistentDataType.LONG, contractId);
    }

    public static OptionalLong readContractId(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return OptionalLong.empty();
        }
        Long id = stack.getItemMeta().getPersistentDataContainer().get(KEY, PersistentDataType.LONG);
        return id == null ? OptionalLong.empty() : OptionalLong.of(id);
    }
}
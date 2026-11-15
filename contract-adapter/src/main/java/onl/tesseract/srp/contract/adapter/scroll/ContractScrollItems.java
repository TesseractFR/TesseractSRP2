package onl.tesseract.srp.contract.adapter.scroll;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Ressources Bukkit des parchemins de contrat : item de base, clés de données persistantes et format d'affichage. */
public final class ContractScrollItems {
    public static final String SCROLL_ITEM_ID = "tesseract:contract_scroll";
    public static final NamespacedKey CONTRACT_ID_KEY = NamespacedKey.fromString("srp:contract_id");
    public static final NamespacedKey EXPIRES_AT_KEY = NamespacedKey.fromString("srp:contract_expires_at");
    public static final DateTimeFormatter EXPIRATION_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM HH:mm").withZone(ZoneId.systemDefault());
    public static final String BULLET = "• ";
    public static final ItemStack BASE_SCROLL = CustomStack.getInstance(SCROLL_ITEM_ID).getItemStack();

    private ContractScrollItems() {
    }

    /** @return {@code true} si l'item a le type de l'item de base des parchemins */
    public static boolean hasScrollType(ItemStack itemStack) {
        return itemStack.getType() == BASE_SCROLL.getType();
    }
}

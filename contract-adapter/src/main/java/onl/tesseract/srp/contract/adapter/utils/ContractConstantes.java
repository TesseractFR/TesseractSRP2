package onl.tesseract.srp.contract.adapter.utils;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Constantes et ressources Bukkit communes aux interfaces et parchemins de contrat. */
public interface ContractConstantes {
    String MENU_CONTRACT_SCROLL_BUTTON = "tesseract:contract_scroll";
    NamespacedKey CONTRACT_ID_KEY = NamespacedKey.fromString("srp:contract_id");
    NamespacedKey EXPIRES_AT_KEY = NamespacedKey.fromString("srp:contract_expires_at");
    DateTimeFormatter EXPIRATION_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM HH:mm").withZone(ZoneId.systemDefault());
    String BULLET = "• ";
    ItemStack BASE_SCROLL = CustomStack.getInstance(MENU_CONTRACT_SCROLL_BUTTON).getItemStack();
}

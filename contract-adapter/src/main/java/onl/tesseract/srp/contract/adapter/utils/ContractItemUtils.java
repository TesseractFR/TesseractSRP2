package onl.tesseract.srp.contract.adapter.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

import static onl.tesseract.srp.contract.adapter.utils.ContractConstantes.CONTRACT_ID_KEY;

/** Utilitaires de lecture des métadonnées d'identification des parchemins de contrat. */
public class ContractItemUtils {
    public static Optional<Long> readContractId(ItemStack itemStack) {
        Long contractId = itemStack.getItemMeta().getPersistentDataContainer().get(CONTRACT_ID_KEY, PersistentDataType.LONG);
        return Optional.ofNullable(contractId);
    }
}

package onl.tesseract.srp.contract.adapter.scroll;

import onl.tesseract.srp.contract.domain.model.contract.ContractId;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

import static onl.tesseract.srp.contract.adapter.scroll.ContractScrollItems.CONTRACT_ID_KEY;

/** Lit l'identifiant de contrat inscrit dans les données persistantes d'un parchemin. */
public final class ContractScrollReader {
    private ContractScrollReader() {
    }

    /** @return l'identifiant du contrat, ou {@link Optional#empty()} si l'item n'est pas un parchemin de contrat */
    public static Optional<ContractId> readContractId(ItemStack itemStack) {
        return Optional.ofNullable(itemStack)
                .map(ItemStack::getItemMeta)
                .map(ItemMeta::getPersistentDataContainer)
                .map(container -> container.get(CONTRACT_ID_KEY, PersistentDataType.LONG))
                .map(ContractId::new);
    }
}

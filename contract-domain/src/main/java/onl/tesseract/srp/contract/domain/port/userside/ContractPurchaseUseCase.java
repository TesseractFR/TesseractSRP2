package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.exception.NoContractAvailableException;
import onl.tesseract.srp.contract.domain.model.exception.NoContractTicketLeftException;
import onl.tesseract.srp.contract.domain.model.exception.ScrollInventoryFullException;

/** Cas d'usage d'achat d'un parchemin de contrat. */
public interface ContractPurchaseUseCase {
    /**
     * Fait acheter un parchemin au joueur contre un ticket. Le ticket n'est consommé que si le
     * contrat est enregistré et le parchemin remis ; sinon il est restitué.
     *
     * @return le contrat acheté
     * @throws NoContractTicketLeftException si le joueur n'a plus de ticket
     * @throws ScrollInventoryFullException si le parchemin ne peut pas être remis
     * @throws NoContractAvailableException si le catalogue ne permet de générer aucun contrat
     */
    Contract buyScroll(PlayerId buyer);
}

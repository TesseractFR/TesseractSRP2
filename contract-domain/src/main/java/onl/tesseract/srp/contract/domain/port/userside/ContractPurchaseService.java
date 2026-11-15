package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractDraft;
import onl.tesseract.srp.contract.domain.model.exception.NoContractAvailableException;
import onl.tesseract.srp.contract.domain.model.exception.NoContractTicketLeftException;
import onl.tesseract.srp.contract.domain.model.exception.ScrollInventoryFullException;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractScrollPort;

/** Fait acheter des parchemins de contrat aux joueurs contre des tickets. */
public class ContractPurchaseService {

    private final ContractRepository contractRepository;
    private final ContractGenerator contractGenerator;
    private final ContractScrollPort contractScrollPort;
    private final ContractTicketService contractTicketService;

    public ContractPurchaseService(ContractRepository contractRepository, ContractGenerator contractGenerator,
                                   ContractScrollPort contractScrollPort, ContractTicketService contractTicketService) {
        this.contractRepository = contractRepository;
        this.contractGenerator = contractGenerator;
        this.contractScrollPort = contractScrollPort;
        this.contractTicketService = contractTicketService;
    }

    /**
     * Fait acheter un parchemin au joueur contre un ticket. Le ticket n'est consommé que si le
     * contrat est enregistré et le parchemin remis ; sinon il est rendu.
     *
     * @return le contrat acheté
     * @throws NoContractTicketLeftException si le joueur n'a plus de ticket
     * @throws ScrollInventoryFullException si le parchemin ne peut pas être remis
     * @throws NoContractAvailableException si le catalogue ne permet de générer aucun contrat
     */
    public Contract buyScroll(PlayerId buyer) {
        if (!contractTicketService.hasTicket(buyer)) {
            throw new NoContractTicketLeftException(buyer);
        }
        if (!contractScrollPort.canReceiveScroll(buyer)) {
            throw new ScrollInventoryFullException(buyer);
        }
        ContractDraft draft = contractGenerator.generate()
                .orElseThrow(() -> new NoContractAvailableException(buyer));

        contractTicketService.withdrawTicket(buyer);
        Contract contract = createContractOrRefundTicket(buyer, draft);
        if (!contractScrollPort.giveScroll(buyer, contract)) {
            contractRepository.delete(contract.id());
            contractTicketService.refundTicket(buyer);
            throw new ScrollInventoryFullException(buyer);
        }
        return contract;
    }

    /** Le ticket est déjà dépensé : il faut le rendre si l'enregistrement échoue, avant de laisser remonter l'erreur. */
    private Contract createContractOrRefundTicket(PlayerId buyer, ContractDraft draft) {
        try {
            return contractRepository.create(draft);
        } catch (RuntimeException e) {
            contractTicketService.refundTicket(buyer);
            throw e;
        }
    }
}

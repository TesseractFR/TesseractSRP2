package onl.tesseract.srp.contract.domain.model.exception;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.ContractId;

/** Le joueur ne possède pas le parchemin du contrat qu'il veut livrer. */
public final class ContractScrollNotOwnedException extends ContractException {
    private final PlayerId player;
    private final ContractId contractId;

    public ContractScrollNotOwnedException(PlayerId player, ContractId contractId) {
        super("Le joueur " + player.value() + " ne possède pas le parchemin du contrat " + contractId.value());
        this.player = player;
        this.contractId = contractId;
    }

    public PlayerId getPlayer() {
        return player;
    }

    public ContractId getContractId() {
        return contractId;
    }
}

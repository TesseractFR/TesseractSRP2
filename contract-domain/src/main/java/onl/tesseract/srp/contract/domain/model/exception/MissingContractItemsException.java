package onl.tesseract.srp.contract.domain.model.exception;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.ContractId;

/** Le joueur ne possède pas assez de matériaux à la qualité demandée pour livrer le contrat. */
public final class MissingContractItemsException extends ContractException {
    private final PlayerId player;
    private final ContractId contractId;
    private final int missingQuantity;

    public MissingContractItemsException(PlayerId player, ContractId contractId, int missingQuantity) {
        super("Il manque " + missingQuantity + " matériaux au joueur " + player.value() + " pour le contrat " + contractId.value());
        this.player = player;
        this.contractId = contractId;
        this.missingQuantity = missingQuantity;
    }

    public PlayerId getPlayer() {
        return player;
    }

    public ContractId getContractId() {
        return contractId;
    }

    public int getMissingQuantity() {
        return missingQuantity;
    }
}

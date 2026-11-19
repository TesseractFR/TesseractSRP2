package onl.tesseract.srp.contract.domain.model.exception;

import onl.tesseract.srp.common.domain.model.PlayerId;

/** Le joueur n'a plus de ticket pour acheter un parchemin. */
public final class NoContractTicketLeftException extends ContractException {
    private final PlayerId player;

    public NoContractTicketLeftException(PlayerId player) {
        super("Le joueur " + player.value() + " n'a plus de ticket de contrat");
        this.player = player;
    }

    public PlayerId getPlayer() {
        return player;
    }
}

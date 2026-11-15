package onl.tesseract.srp.contract.domain.model.exception;

import onl.tesseract.srp.common.domain.model.PlayerId;

/** L'inventaire du joueur ne peut pas recevoir le parchemin acheté. */
public final class ScrollInventoryFullException extends ContractException {
    private final PlayerId player;

    public ScrollInventoryFullException(PlayerId player) {
        super("L'inventaire du joueur " + player.value() + " ne peut pas recevoir de parchemin");
        this.player = player;
    }

    public PlayerId getPlayer() {
        return player;
    }
}

package onl.tesseract.srp.contract.domain.model.exception;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.item.MaterialName;

/** Le joueur possède moins de matériaux que la quantité à lui retirer. */
public final class NotEnoughMaterialException extends ContractException {
    private final PlayerId player;
    private final MaterialName material;
    private final int requestedQuantity;
    private final int ownedQuantity;

    public NotEnoughMaterialException(PlayerId player, MaterialName material, int requestedQuantity, int ownedQuantity) {
        super("Le joueur " + player.value() + " possède " + ownedQuantity + " " + material.value() + " sur " + requestedQuantity + " demandés");
        this.player = player;
        this.material = material;
        this.requestedQuantity = requestedQuantity;
        this.ownedQuantity = ownedQuantity;
    }

    public PlayerId getPlayer() {
        return player;
    }

    public MaterialName getMaterial() {
        return material;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getOwnedQuantity() {
        return ownedQuantity;
    }
}

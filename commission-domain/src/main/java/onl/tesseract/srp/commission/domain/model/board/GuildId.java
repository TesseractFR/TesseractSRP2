package onl.tesseract.srp.commission.domain.model.board;

import java.util.UUID;

/** Identifiant de la guilde propriétaire d'un tableau. */
public record GuildId(UUID value) {
    /** Vérifie que l'identifiant est renseigné. */
    public GuildId {
        if (value == null) {
            throw new IllegalArgumentException("L'identifiant d'une guilde doit être renseigné");
        }
    }
}

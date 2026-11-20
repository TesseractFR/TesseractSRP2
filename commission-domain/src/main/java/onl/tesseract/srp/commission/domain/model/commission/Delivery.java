package onl.tesseract.srp.commission.domain.model.commission;

import lombok.NonNull;
import onl.tesseract.srp.commission.domain.model.board.BoardId;
import onl.tesseract.srp.common.domain.model.PlayerId;

/**
 * Ce qu'un joueur livre à une commission et ce que la livraison rapporte. Les montants sont en lys
 * et peuvent valoir zéro quand la part livrée est trop petite pour rapporter une unité.
 *
 * @param playerEarnings          gain du livreur, taxe déduite
 * @param guildTax                taxe d'usage versée à la banque de la guilde, zéro pour un membre ou au spawn
 * @param guildXp                 XP donnée à la guilde propriétaire, zéro au spawn
 * @param isGuildMember           {@code true} si le livreur est membre de la guilde propriétaire
 * @param completesCommission     {@code true} si la livraison termine la commission
 */
public record Delivery(@NonNull CommissionId commissionId, @NonNull BoardId boardId,
                       @NonNull PlayerId deliverer, int itemCount, int playerEarnings, int guildTax, int guildXp,
                       boolean isGuildMember, boolean completesCommission) {

    /** @return {@code true} si le joueur n'a aucun item à livrer */
    public boolean isEmpty() {
        return itemCount == 0;
    }

    /** @return valeur des items livrés, avant déduction de la taxe */
    public int getItemsValue() {
        return playerEarnings + guildTax;
    }
}

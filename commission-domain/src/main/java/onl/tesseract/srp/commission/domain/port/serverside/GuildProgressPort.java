package onl.tesseract.srp.commission.domain.port.serverside;

import onl.tesseract.srp.commission.domain.model.board.GuildId;

/** Port d'accès au niveau et à la banque des guildes, pour payer les améliorations. */
public interface GuildProgressPort {
    /** @return niveau actuel de la guilde */
    int getGuildLevel(GuildId guildId);

    /** @return solde de la banque de la guilde, en lys */
    int getBankBalance(GuildId guildId);

    /**
     * Retire le montant de la banque de la guilde.
     *
     * @return {@code false} si la banque n'a pas assez de lys : rien n'est retiré
     */
    boolean withdrawFromBank(GuildId guildId, int amount);
}

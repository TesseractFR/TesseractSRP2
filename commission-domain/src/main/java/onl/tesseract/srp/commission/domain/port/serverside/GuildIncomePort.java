package onl.tesseract.srp.commission.domain.port.serverside;

import onl.tesseract.srp.commission.domain.model.board.GuildId;

/** Port de versement aux guildes de ce que leur rapportent les livraisons sur leur tableau. */
public interface GuildIncomePort {
    /** Verse à la guilde la taxe d'usage, en lys, et l'XP d'une livraison. */
    void addCommissionIncome(GuildId guildId, int usageTax, int xp);
}

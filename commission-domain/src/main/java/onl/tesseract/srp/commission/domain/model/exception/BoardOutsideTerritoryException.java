package onl.tesseract.srp.commission.domain.model.exception;

import onl.tesseract.srp.commission.domain.model.board.GuildId;

/** Le tableau ne serait pas entièrement sur le territoire de la guilde. */
public final class BoardOutsideTerritoryException extends CommissionException {
    private final GuildId guildId;

    public BoardOutsideTerritoryException(GuildId guildId) {
        super("Le tableau doit être entièrement sur le territoire de la guilde " + guildId.value());
        this.guildId = guildId;
    }

    public GuildId getGuildId() {
        return guildId;
    }
}

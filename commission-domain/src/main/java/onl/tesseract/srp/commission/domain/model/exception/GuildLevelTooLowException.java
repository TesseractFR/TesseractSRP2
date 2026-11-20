package onl.tesseract.srp.commission.domain.model.exception;

import onl.tesseract.srp.commission.domain.model.board.GuildId;

/** Le niveau de la guilde est trop bas pour le palier d'amélioration. */
public final class GuildLevelTooLowException extends CommissionException {
    private final GuildId guildId;
    private final int requiredLevel;
    private final int guildLevel;

    public GuildLevelTooLowException(GuildId guildId, int requiredLevel, int guildLevel) {
        super("La guilde " + guildId.value() + " est niveau " + guildLevel + ", il faut le niveau " + requiredLevel);
        this.guildId = guildId;
        this.requiredLevel = requiredLevel;
        this.guildLevel = guildLevel;
    }

    public GuildId getGuildId() {
        return guildId;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public int getGuildLevel() {
        return guildLevel;
    }
}

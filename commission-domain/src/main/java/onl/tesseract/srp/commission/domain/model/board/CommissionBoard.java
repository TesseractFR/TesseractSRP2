package onl.tesseract.srp.commission.domain.model.board;

import lombok.Getter;

import java.util.List;
import java.util.Optional;

/**
 * Tableau de commissions, au spawn ou d'une guilde. Un tableau de guilde a une partie réservée à ses
 * membres, des améliorations et une taxe d'usage. La pose du tableau dans le monde est gérée par
 * le moteur des structures. Le tableau du spawn n'a que la partie publique, ne s'améliore pas et ne
 * prélève aucune taxe : seul son nombre de commissions par type suit la configuration.
 */
public class CommissionBoard {
    @Getter
    private final CommissionBoardId id;
    // Absent pour le tableau du spawn.
    private final GuildId guildId;
    @Getter
    private CommissionBoardLevels levels;
    @Getter
    private UsageTaxRate usageTaxRate;

    private CommissionBoard(CommissionBoardId id, GuildId guildId, CommissionBoardLevels levels, UsageTaxRate usageTaxRate) {
        this.id = id;
        this.guildId = guildId;
        this.levels = levels;
        this.usageTaxRate = usageTaxRate;
    }

    /** @return le tableau du spawn, sans taxe, avec ce nombre de commissions par type et les autres niveaux au minimum */
    public static CommissionBoard spawnBoard(CommissionBoardId id, int slotsPerType) {
        return new CommissionBoard(id, null, CommissionBoardLevels.forSpawnBoard(slotsPerType), UsageTaxRate.ZERO);
    }

    /** @return un tableau de guilde */
    public static CommissionBoard guildBoard(CommissionBoardId id, GuildId guildId,
                                             CommissionBoardLevels levels, UsageTaxRate usageTaxRate) {
        return new CommissionBoard(id, guildId, levels, usageTaxRate);
    }

    /** @return {@code true} pour le tableau du spawn */
    public boolean isSpawnBoard() {
        return guildId == null;
    }

    /** @return la guilde propriétaire, ou {@link Optional#empty()} pour le tableau du spawn */
    public Optional<GuildId> findGuildId() {
        return Optional.ofNullable(guildId);
    }

    /** @return les parties du tableau : la partie publique, plus la partie guilde sur un tableau de guilde */
    public List<CommissionBoardPart> findAllParts() {
        return isSpawnBoard() ? List.of(CommissionBoardPart.PUBLIC) : List.of(CommissionBoardPart.PUBLIC, CommissionBoardPart.GUILD);
    }

    /** @return la taxe d'usage due sur ce montant, arrondie à l'unité inférieure */
    public int computeUsageTaxOn(int amount) {
        return usageTaxRate.computeTaxOn(amount);
    }

    /**
     * Monte l'amélioration d'un palier. Le prix et les conditions sont vérifiés par le service.
     *
     * @throws IllegalStateException sur le tableau du spawn, qui ne s'améliore pas
     */
    public void upgrade(CommissionBoardUpgradeType upgradeType) {
        requireGuildBoard();
        levels = levels.withUpgrade(upgradeType);
    }

    /** @throws IllegalStateException sur le tableau du spawn, qui ne prélève aucune taxe */
    public void changeUsageTax(UsageTaxRate newUsageTaxRate) {
        requireGuildBoard();
        usageTaxRate = newUsageTaxRate;
    }

    /**
     * Fait suivre au tableau du spawn le nombre de commissions par type de la configuration.
     *
     * @throws IllegalStateException sur un tableau de guilde, dont les slots ne changent que par amélioration
     */
    public void changeSpawnSlots(int slotsPerType) {
        if (!isSpawnBoard()) {
            throw new IllegalStateException("Seul le tableau du spawn suit les slots de la configuration : " + id.value());
        }
        levels = CommissionBoardLevels.forSpawnBoard(slotsPerType);
    }

    private void requireGuildBoard() {
        if (isSpawnBoard()) {
            throw new IllegalStateException("Le tableau du spawn ne s'améliore pas et ne prélève aucune taxe : " + id.value());
        }
    }
}

package onl.tesseract.srp.commission.domain.model.board;

import lombok.Getter;
import lombok.NonNull;
import onl.tesseract.srp.commission.domain.model.exception.InvalidUsageTaxRateException;

import java.util.List;
import java.util.Optional;

/**
 * Tableau de commissions, au spawn ou d'une guilde. Un tableau de guilde a une partie réservée à ses
 * membres, des améliorations et une taxe d'usage. La pose du tableau dans le monde est gérée par
 * le moteur des structures. Le tableau du spawn n'a que la partie publique, ne s'améliore pas et ne
 * prélève aucune taxe.
 */
public class CommissionBoard {
    /** Taux de taxe d'usage maximal qu'une guilde peut fixer. */
    public static final double MAX_USAGE_TAX_RATE = 0.5;

    @Getter
    private final CommissionBoardId id;
    // Absent pour le tableau du spawn.
    private final GuildId guildId;
    @Getter
    private CommissionBoardLevels levels;
    @Getter
    private double usageTaxRate;

    private CommissionBoard(CommissionBoardId id, GuildId guildId, CommissionBoardLevels levels, double usageTaxRate) {
        this.id = id;
        this.guildId = guildId;
        this.levels = levels;
        this.usageTaxRate = requireValidUsageTaxRate(usageTaxRate);
    }

    /** @return le tableau du spawn, qui ne taxe pas ; ses niveaux viennent de {@link CommissionBoardLevels#forSpawnBoard} */
    public static CommissionBoard spawnBoard(@NonNull CommissionBoardId id, @NonNull CommissionBoardLevels levels) {
        return new CommissionBoard(id, null, levels, 0);
    }

    /** @return un tableau de guilde */
    public static CommissionBoard guildBoard(@NonNull CommissionBoardId id, @NonNull GuildId guildId, @NonNull CommissionBoardLevels levels,
                                             double usageTaxRate) {
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

    /** La taxe est arrondie à l'unité inférieure : le joueur n'est jamais taxé plus que le taux affiché. */
    public int computeUsageTaxOn(int amount) {
        return (int) Math.floor(amount * usageTaxRate);
    }

    /** Monte l'amélioration d'un palier. Le prix et les conditions sont vérifiés par le service. */
    public void upgrade(CommissionBoardUpgradeType upgradeType) {
        levels = levels.withUpgrade(upgradeType);
    }

    /** Le tableau du spawn ne s'améliore pas : seul son nombre de commissions par type change, pour suivre la configuration. */
    public void changeSpawnSlots(int slotsPerType) {
        levels = CommissionBoardLevels.forSpawnBoard(slotsPerType);
    }

    /**
     * @throws InvalidUsageTaxRateException si le taux sort de l'intervalle [0, {@link #MAX_USAGE_TAX_RATE}]
     */
    public void changeUsageTax(double newUsageTaxRate) {
        usageTaxRate = requireValidUsageTaxRate(newUsageTaxRate);
    }

    /** @throws InvalidUsageTaxRateException si le taux sort de l'intervalle [0, {@link #MAX_USAGE_TAX_RATE}] */
    private static double requireValidUsageTaxRate(double usageTaxRate) {
        if (usageTaxRate < 0 || usageTaxRate > MAX_USAGE_TAX_RATE) {
            throw new InvalidUsageTaxRateException(usageTaxRate);
        }
        return usageTaxRate;
    }
}

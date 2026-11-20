package onl.tesseract.srp.commission.domain.model.board;

import lombok.Getter;
import lombok.NonNull;
import onl.tesseract.srp.commission.domain.model.exception.InvalidUsageTaxRateException;
import onl.tesseract.srp.common.domain.model.Coordinate;

import java.util.List;
import java.util.Optional;

/**
 * Tableau de commissions, au spawn ou d'une guilde. Un tableau de guilde a une partie réservée à ses
 * membres, des améliorations, une taxe d'usage, et il est posé sur son territoire. Le tableau du
 * spawn n'a que la partie publique, ne s'améliore pas et ne prélève aucune taxe.
 */
public class CommissionBoard {
    /** Taux de taxe d'usage maximal qu'une guilde peut fixer. */
    public static final double MAX_USAGE_TAX_RATE = 0.5;
    /** Temps laissé au joueur pour choisir l'emplacement du tableau. */
    public static final int BOARD_PLACEMENT_SECONDS = 30;

    @Getter
    private final BoardId id;
    // Absent pour le tableau du spawn.
    private final GuildId guildId;
    @Getter
    private BoardLevels levels;
    @Getter
    private double usageTaxRate;
    // Absent tant que la guilde n'a pas posé son tableau. Le spawn n'en a pas : le staff y pose lui-même ses tableaux.
    private Coordinate furniturePosition;

    private CommissionBoard(BoardId id, GuildId guildId, BoardLevels levels, double usageTaxRate) {
        this.id = id;
        this.guildId = guildId;
        this.levels = levels;
        this.usageTaxRate = requireValidUsageTaxRate(usageTaxRate);
    }

    /** @return le tableau du spawn, qui ne taxe pas ; ses niveaux viennent de {@link BoardLevels#spawn} */
    public static CommissionBoard spawnBoard(@NonNull BoardId id, @NonNull BoardLevels levels) {
        return new CommissionBoard(id, null, levels, 0);
    }

    /** @return un tableau de guilde, pas encore posé ; il se pose ensuite avec {@link #placeFurnitureAt} */
    public static CommissionBoard guildBoard(@NonNull BoardId id, @NonNull GuildId guildId, @NonNull BoardLevels levels,
                                             double usageTaxRate) {
        return new CommissionBoard(id, guildId, levels, usageTaxRate);
    }

    /** @return {@code true} pour le tableau du spawn */
    public boolean isSpawn() {
        return guildId == null;
    }

    /** @return la guilde propriétaire, ou {@link Optional#empty()} pour le tableau du spawn */
    public Optional<GuildId> findGuildId() {
        return Optional.ofNullable(guildId);
    }

    /** @return la position du tableau, ou {@link Optional#empty()} s'il n'est pas posé */
    public Optional<Coordinate> findFurniturePosition() {
        return Optional.ofNullable(furniturePosition);
    }

    /** @return les parties du tableau : la partie publique, plus la partie guilde sur un tableau de guilde */
    public List<BoardPart> findAllParts() {
        return isSpawn() ? List.of(BoardPart.PUBLIC) : List.of(BoardPart.PUBLIC, BoardPart.GUILD);
    }

    /** La taxe est arrondie à l'unité inférieure : le joueur n'est jamais taxé plus que le taux affiché. */
    public int computeUsageTaxOn(int amount) {
        return (int) Math.floor(amount * usageTaxRate);
    }

    /** Monte l'amélioration d'un palier. Le prix et les conditions sont vérifiés par le service. */
    public void upgrade(UpgradeType upgradeType) {
        levels = levels.upgrade(upgradeType);
    }

    /** Le tableau du spawn ne s'améliore pas : seul son nombre de commissions par type change, pour suivre la configuration. */
    public void changeSpawnSlots(int slotsPerType) {
        levels = BoardLevels.spawn(slotsPerType);
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

    /** Enregistre la nouvelle position du tableau. Retirer l'ancien tableau du monde est à la charge de l'appelant. */
    public void placeFurnitureAt(@NonNull Coordinate newFurniturePosition) {
        furniturePosition = newFurniturePosition;
    }
}

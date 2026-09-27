package onl.tesseract.srp.common.domain.model;

import lombok.Getter;
import lombok.Setter;
import onl.tesseract.srp.common.domain.model.enums.PlayerRank;
import onl.tesseract.srp.common.domain.model.exception.NotEnoughIlluminationPointsException;
import onl.tesseract.srp.common.domain.model.exception.NotEnoughMoneyException;

import java.util.UUID;

@Getter
public class SrpPlayer {
    private final UUID uniqueId;
    @Setter
    private PlayerRank rank;
    private int money;
    private int illuminationPoints;
    @Setter
    private String titleID;

    public SrpPlayer(UUID uniqueId, PlayerRank rank, int money, String titleID, int illuminationPoints) {
        this.uniqueId = uniqueId;
        this.rank = rank;
        this.money = money;
        this.titleID = titleID;
        this.illuminationPoints = illuminationPoints;
    }

    public SrpPlayer(UUID uniqueId) {
        this(uniqueId, PlayerRank.Survivant, 0, PlayerRank.Survivant.getTitle().getId(), 0);
    }

    public int addMoney(int amount) {
        if (amount + money < 0)
            throw new NotEnoughMoneyException("Money cannot go below 0 (adding " + amount + " to base value " + money + ")");
        money += amount;
        return money;
    }

    public int addIlluminationPoints(int amount) {
        if (illuminationPoints + amount < 0)
            throw new NotEnoughIlluminationPointsException("Illumination points cannot go below 0 " +
                    "(adding " + amount + " to " + illuminationPoints + ")");
        illuminationPoints += amount;
        return illuminationPoints;
    }

    /**
     * Assign the next rank to the player, and withdraw money.
     * @return False if the player already has the last rank or does not have enough money to buy the rank
     */
    public boolean buyNextRank() {
        PlayerRank nextRank = rank.next();
        if (nextRank == null) return false;
        if (money < nextRank.getCost()) return false;
        addMoney(-nextRank.getCost());
        rank = nextRank;
        return true;
    }
}


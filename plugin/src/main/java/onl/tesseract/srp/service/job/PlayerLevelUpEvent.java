package onl.tesseract.srp.service.job;

import onl.tesseract.srp.domain.job.JobSkill;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import java.util.UUID;

/**
 * Called when a player gains a job level
 */
public class PlayerLevelUpEvent extends Event {
    private final UUID playerID;
    /**
     * New job level
     */
    private final int level;
    /**
     * Amount of levels passed simultaneously
     */
    private final int passedLevel;

    public PlayerLevelUpEvent(UUID playerID, int level, int passedLevel) {
        this.playerID = playerID;
        this.level = level;
        this.passedLevel = passedLevel;
    }

    public UUID getPlayerID() { return playerID; }
    public int getLevel() { return level; }
    public int getPassedLevel() { return passedLevel; }

    @Override
    public HandlerList getHandlers() { return handlerList; }

    @Override
    public String toString() { return "Player " + playerID + " has passed level " + level + " (+" + passedLevel + ")"; }

    private static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() { return handlerList; }
}


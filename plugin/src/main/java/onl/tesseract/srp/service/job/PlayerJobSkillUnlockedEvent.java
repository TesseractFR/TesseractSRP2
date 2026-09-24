package onl.tesseract.srp.service.job;

import onl.tesseract.srp.domain.job.JobSkill;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import java.util.UUID;

public class PlayerJobSkillUnlockedEvent extends Event {
    private final UUID playerID;
    private final JobSkill unlocked;

    public PlayerJobSkillUnlockedEvent(UUID playerID, JobSkill unlocked) {
        this.playerID = playerID;
        this.unlocked = unlocked;
    }

    public UUID getPlayerID() { return playerID; }
    public JobSkill getUnlocked() { return unlocked; }

    @Override
    public HandlerList getHandlers() { return handlerList; }

    private static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() { return handlerList; }
}


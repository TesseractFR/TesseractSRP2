package onl.tesseract.srp.service.job;

import onl.tesseract.srp.domain.item.CustomItem;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import java.util.UUID;

public class JobLootItemEvent extends Event {
    private final UUID playerID;
    private final CustomItem item;
    private final int xp;

    public JobLootItemEvent(UUID playerID, CustomItem item, int xp) {
        this.playerID = playerID;
        this.item = item;
        this.xp = xp;
    }

    public UUID getPlayerID() { return playerID; }
    public CustomItem getItem() { return item; }
    public int getXp() { return xp; }

    @Override
    public HandlerList getHandlers() { return handlerList; }

    private static final HandlerList handlerList = new HandlerList();

    public static HandlerList getHandlerList() { return handlerList; }
}


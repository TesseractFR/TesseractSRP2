package onl.tesseract.srp.territory.adapter.userside.event.territory;

import java.util.UUID;

import onl.tesseract.srp.territory.domain.model.Territory;
import onl.tesseract.srp.territory.domain.model.TerritoryChunk;
import onl.tesseract.srp.territory.domain.port.userside.TerritoryBorderService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public abstract class TerritoryBorderDisplayListener<TC extends TerritoryChunk, T extends Territory<TC>>
        implements Listener {

    private final TerritoryBorderService<TC, T> borderService;

    protected TerritoryBorderDisplayListener(TerritoryBorderService<TC, T> borderService) {
        this.borderService = borderService;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        borderService.clearBorders(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        borderService.clearBorders(event.getPlayer().getUniqueId());
    }

    protected void updateBorders(UUID playerId) {
        borderService.refreshBordersIfShowing(playerId);
    }
}

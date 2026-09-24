package onl.tesseract.srp.territory.domain.port.userside;


import lombok.Getter;
import onl.tesseract.srp.territory.domain.model.Territory;
import onl.tesseract.srp.territory.domain.model.TerritoryChunk;
import onl.tesseract.srp.territory.domain.model.enums.result.BorderResult;
import onl.tesseract.srp.territory.domain.port.serverside.TerritoryBorderTaskScheduler;

import java.util.UUID;

@Getter
public abstract class TerritoryBorderService<TC extends TerritoryChunk, T extends Territory<TC>> {

    protected final TerritoryBorderTaskScheduler scheduler;
    protected final TerritoryService<TC, T> territoryService;

    protected TerritoryBorderService(TerritoryBorderTaskScheduler scheduler, TerritoryService<TC, T> territoryService) {
        this.scheduler = scheduler;
        this.territoryService = territoryService;
    }
    /**
     * Toggles the display of camp borders for a player.
     * If borders are currently shown, they will be cleared; if not, they will be displayed.
     * @param playerId The UUID of the player.
     * @param currentWorld The name of the world the player is currently in.
     */
    public BorderResult toggleBorders(UUID playerId, String currentWorld) {
        if (!getTerritoryService().isCorrectWorld(currentWorld)) {
            return BorderResult.INVALID_WORLD;
        }
        T territory = getTerritoryService().getByPlayer(playerId);
        if (territory == null) {
            return BorderResult.TERRITORY_NOT_FOUND;
        }
        if (isShowingBorders(playerId)) {
            clearBorders(playerId);
            return BorderResult.CLEAR_BORDERS;
        } else {
            showBorders(playerId);
            return BorderResult.SHOW_BORDERS;
        }
    }

    /**
     * Displays the camp borders using End Rod particles.
     * @param player The player who will see the borders.
     */
    private void showBorders(UUID player) {
        clearBorders(player);
        T territory = getTerritoryService().getByPlayer(player);
        if (territory == null) return;
        java.util.Collection<TC> chunks = territory.getChunks();
        if (chunks == null || chunks.isEmpty()) return;

        java.util.List<onl.tesseract.srp.common.domain.model.ChunkCoord> chunkCoords = new java.util.ArrayList<>();
        for (TC chunk : chunks) {
            chunkCoords.add(chunk.getChunkCoord());
        }
        getScheduler().schedule(player, chunkCoords);
    }

    /**
     * Clears the previously displayed borders for a player.
     * @param player The player whose borders should be cleared.
     */
    public void clearBorders(UUID player) {
        getScheduler().cancel(player);
    }

    /**
     * Checks if the borders are currently being displayed for a player.
     * @param player The player to check.
     * @return True if borders are active, False otherwise.
     */
    private boolean isShowingBorders(UUID player) {
        return getScheduler().isActive(player);
    }

    /**
     * Refreshes the borders for a player if they are currently being displayed.
     * @param playerId The UUID of the player.
     */
    public void refreshBordersIfShowing(UUID playerId) {
        if (!isShowingBorders(playerId)) return;
        showBorders(playerId);
    }
}

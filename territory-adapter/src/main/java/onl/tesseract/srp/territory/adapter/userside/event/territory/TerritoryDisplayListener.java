package onl.tesseract.srp.territory.adapter.userside.event.territory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.srp.common.adapter.mapper.ChunkCoordMapper;
import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.territory.domain.model.campement.Campement;
import onl.tesseract.srp.territory.domain.model.event.TerritoryClaimEvent;
import onl.tesseract.srp.territory.domain.model.event.TerritoryUnclaimEvent;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementService;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.springframework.context.event.EventListener;

/**
 * Displays the camp name or "Nature" depending on the chunk the player moves into.
 */
@org.springframework.stereotype.Component
public class TerritoryDisplayListener implements Listener {
    private final CampementService campementService;
    private final GuildService guildService;
    private final Map<UUID, UUID> lastTerritory = new HashMap<>();

    public TerritoryDisplayListener(CampementService campementService, GuildService guildService) {
        this.campementService = campementService;
        this.guildService = guildService;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getTo() == null || event.getFrom().getChunk().equals(event.getTo().getChunk())) {
            return;
        }
        updatePlayerDisplay(player, ChunkCoordMapper.toChunkCoord(event.getTo().getChunk()));
    }

    @EventListener
    public void onChunkClaim(TerritoryClaimEvent<?> event) {
        Player player = Bukkit.getPlayer(event.getPlayerId());
        if (player == null) {
            return;
        }
        updatePlayerDisplay(player, ChunkCoordMapper.toChunkCoord(player.getChunk()));
    }

    @EventListener
    public void onChunkUnclaim(TerritoryUnclaimEvent<?> event) {
        Player player = Bukkit.getPlayer(event.getPlayerId());
        if (player == null) {
            return;
        }
        updatePlayerDisplay(player, ChunkCoordMapper.toChunkCoord(player.getChunk()));
    }

    public void updatePlayerDisplay(Player player, ChunkCoord toChunkCoord) {
        Campement campement = campementService.getByChunk(toChunkCoord);
        Guild guild = guildService.getByChunk(toChunkCoord);
        UUID territory = campement != null ? campement.getId() : guild != null ? guild.getId() : null;

        if (territory == null) {
            if (lastTerritory.remove(player.getUniqueId()) != null) {
                player.sendActionBar(Component.text("[Nature]", NamedTextColor.GREEN));
            }
            return;
        }

        if (!territory.equals(lastTerritory.get(player.getUniqueId()))) {
            lastTerritory.put(player.getUniqueId(), territory);
            if (campement != null) {
                String ownerName = Bukkit.getOfflinePlayer(campement.getOwnerID()).getName();
                if (ownerName == null) {
                    ownerName = "Inconnu";
                }
                player.sendActionBar(Component.text("[Campement de " + ownerName + "]", NamedTextColor.GOLD));
            } else if (guild != null) {
                player.sendActionBar(Component.text("[" + guild.getName() + "]", NamedTextColor.GOLD));
            }
        }
    }
}


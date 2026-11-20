package onl.tesseract.srp.commission.adapter.serverside.placement;

import onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionMessageFormatter;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.port.serverside.BoardPlacementSessionPort;
import onl.tesseract.srp.common.domain.model.PlayerId;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Garde en mémoire les joueurs qui posent le tableau de leur guilde, avec un minuteur par joueur. Le
 * minuteur affiche le temps restant dans la barre d'action et prévient le joueur quand il est écoulé.
 */
@Component
public class BukkitBoardPlacementSessionPort implements BoardPlacementSessionPort {
    private static final long TICKS_PER_SECOND = 20L;

    private final Plugin plugin;
    private final CommissionMessageFormatter commissionMessageFormatter;
    private final Map<PlayerId, PlacementSession> sessions = new HashMap<>();

    public BukkitBoardPlacementSessionPort(Plugin plugin, CommissionMessageFormatter commissionMessageFormatter) {
        this.plugin = plugin;
        this.commissionMessageFormatter = commissionMessageFormatter;
    }

    @Override
    public void startSession(PlayerId player, GuildId guildId) {
        endSession(player);
        Instant expiresAt = Instant.now().plusSeconds(CommissionBoard.BOARD_PLACEMENT_SECONDS);
        BukkitTask timerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> showTimeLeft(player, expiresAt), 0, TICKS_PER_SECOND);
        sessions.put(player, new PlacementSession(guildId, timerTask));
        findOnlinePlayer(player).ifPresent(onlinePlayer -> onlinePlayer.sendMessage(commissionMessageFormatter.formatBoardPlacementStarted()));
    }

    @Override
    public Optional<GuildId> findGuildOfPlacingPlayer(PlayerId player) {
        return Optional.ofNullable(sessions.get(player)).map(PlacementSession::guildId);
    }

    @Override
    public boolean endSession(PlayerId player) {
        Optional<PlacementSession> session = Optional.ofNullable(sessions.remove(player));
        session.ifPresent(endedSession -> endedSession.timerTask().cancel());
        return session.isPresent();
    }

    private void showTimeLeft(PlayerId player, Instant expiresAt) {
        int secondsLeft = (int) Duration.between(Instant.now(), expiresAt).toSeconds();
        if (secondsLeft > 0) {
            findOnlinePlayer(player).ifPresent(onlinePlayer ->
                    onlinePlayer.sendActionBar(commissionMessageFormatter.formatBoardPlacementCountdown(secondsLeft)));
            return;
        }
        endSession(player);
        findOnlinePlayer(player).ifPresent(onlinePlayer -> onlinePlayer.sendMessage(commissionMessageFormatter.formatBoardPlacementExpired()));
    }

    private static Optional<Player> findOnlinePlayer(PlayerId player) {
        return Optional.ofNullable(Bukkit.getPlayer(player.value()));
    }

    private record PlacementSession(GuildId guildId, BukkitTask timerTask) {
    }
}

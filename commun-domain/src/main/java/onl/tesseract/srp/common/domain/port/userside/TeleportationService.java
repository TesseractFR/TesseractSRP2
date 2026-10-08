package onl.tesseract.srp.common.domain.port.userside;

import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.port.serverside.TeleportationPort;
import onl.tesseract.srp.common.domain.port.serverside.TeleportationSettingsPort;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/** Coordinates delayed teleportation without depending on Bukkit or another platform. */
public class TeleportationService {
    private final TeleportationPort teleportationPort;
    private final TeleportationSettingsPort settingsPort;

    public TeleportationService(TeleportationPort teleportationPort, TeleportationSettingsPort settingsPort) {
        this.teleportationPort = Objects.requireNonNull(teleportationPort);
        this.settingsPort = Objects.requireNonNull(settingsPort);
    }


    public void teleport(PlayerId playerId, Coordinate destination, Runnable callback) {
        var settings = settingsPort.getSettings(playerId);
        teleport(playerId, destination, settings.duration(), settings.animationId(), callback);
    }

    public void teleport(PlayerId playerId, Coordinate destination, int duration,
                         String animationId, Runnable callback) {
        Objects.requireNonNull(playerId);
        Objects.requireNonNull(destination);
        if (duration < 0) throw new IllegalArgumentException("Teleport duration cannot be negative");

        Coordinate start = teleportationPort.getLocation(playerId);
        teleportationPort.applyTeleportEffects(playerId, duration);
        teleportationPort.playPortalSound(playerId);
        teleportationPort.animate(start,animationId, duration);
        if (teleportationPort.isDestinationChunkLoaded(destination)) {
            teleportationPort.animate(destination, animationId, duration);
        }

        AtomicInteger elapsedTicks = new AtomicInteger();
        TeleportationPort.Cancellable[] task = new TeleportationPort.Cancellable[1];
        task[0] = teleportationPort.runTimer(0L, 10L, () -> {
            if (elapsedTicks.addAndGet(10) >= duration) {
                task[0].cancel();
                teleportationPort.teleport(playerId, destination);
                if (callback != null) callback.run();
                return;
            }
            if (!teleportationPort.isOnline(playerId)) {
                task[0].cancel();
                return;
            }
            if (!sameBlock(start, teleportationPort.getLocation(playerId))) {
                task[0].cancel();
                teleportationPort.removeBlindness(playerId);
                teleportationPort.sendCancelledMessage(playerId);
            }
        });
    }

    private boolean sameBlock(Coordinate first, Coordinate second) {
        return first != null && second != null && Objects.equals(first.chunkCoord().world(), second.chunkCoord().world())
                && (int) Math.floor(first.x()) == (int) Math.floor(second.x())
                && (int) Math.floor(first.y()) == (int) Math.floor(second.y())
                && (int) Math.floor(first.z()) == (int) Math.floor(second.z());
    }
}

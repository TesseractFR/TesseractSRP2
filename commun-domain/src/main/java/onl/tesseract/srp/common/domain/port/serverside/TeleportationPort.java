package onl.tesseract.srp.common.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.common.domain.model.PlayerId;

public interface TeleportationPort {
    Coordinate getLocation(PlayerId playerId);
    boolean isOnline(PlayerId playerId);
    boolean isDestinationChunkLoaded(Coordinate destination);
    void applyTeleportEffects(PlayerId playerId, int duration);
    void playPortalSound(PlayerId playerId);
    void animate(Coordinate location,String animationId, double duration);
    void teleport(PlayerId playerId, Coordinate destination);
    void removeBlindness(PlayerId playerId);
    void sendCancelledMessage(PlayerId playerId);
    Cancellable runTimer(long initialDelayTicks, long periodTicks, Runnable task);

    interface Cancellable {
        void cancel();
    }
}

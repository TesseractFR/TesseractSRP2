package onl.tesseract.srp.common.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;

/** Reads the player's rank delay and selected animation from their profile and boutique. */
public interface TeleportationSettingsPort {
    Settings getSettings(PlayerId playerId);

    record Settings(int duration, String animationId) {
    }
}

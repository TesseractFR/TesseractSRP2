package onl.tesseract.srp.common.adapter.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.port.serverside.TeleportationSettingsPort;
import onl.tesseract.srp.common.domain.port.userside.PlayerService;
import onl.tesseract.core.boutique.BoutiqueService;
import org.springframework.stereotype.Component;

@Component
public class TeleportationSettingsPortImpl implements TeleportationSettingsPort {

    private final PlayerService playerService;
    private final BoutiqueService boutiqueService;

    public TeleportationSettingsPortImpl(PlayerService playerService, BoutiqueService boutiqueService) {
        this.playerService = playerService;
        this.boutiqueService = boutiqueService;
    }

    @Override
    public Settings getSettings(PlayerId playerId) {
        return new Settings(playerService.getPlayer(playerId).getRank().getTpDelay(),
                boutiqueService.getPlayerBoutiqueInfo(playerId.value()).getActiveTpAnimation().name());
    }
}

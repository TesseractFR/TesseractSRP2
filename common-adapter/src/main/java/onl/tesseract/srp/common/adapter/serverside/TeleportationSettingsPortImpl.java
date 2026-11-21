package onl.tesseract.srp.common.adapter.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.port.serverside.TeleportationSettingsPort;
import onl.tesseract.srp.player.domain.port.userside.SrpPlayerProfileService;
import onl.tesseract.core.boutique.BoutiqueService;
import org.springframework.stereotype.Component;

@Component
public class TeleportationSettingsPortImpl implements TeleportationSettingsPort {

    private final SrpPlayerProfileService playerProfileService;
    private final BoutiqueService boutiqueService;

    public TeleportationSettingsPortImpl(SrpPlayerProfileService playerProfileService, BoutiqueService boutiqueService) {
        this.playerProfileService = playerProfileService;
        this.boutiqueService = boutiqueService;
    }

    @Override
    public Settings getSettings(PlayerId playerId) {
        return new Settings(playerProfileService.getPlayer(playerId).getRank().getTpDelay(),
                boutiqueService.getPlayerBoutiqueInfo(playerId.value()).getActiveTpAnimation().name());
    }
}

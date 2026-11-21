package onl.tesseract.srp.territory.adapter.serverside.repository;

import onl.tesseract.srp.territory.domain.port.serverside.SrpPlayerRepository;
import onl.tesseract.srp.common.domain.model.enums.PlayerRank;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.port.userside.SrpPlayerProfileService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SrpPlayerRepositoryImpl implements SrpPlayerRepository {

    private final SrpPlayerProfileService playerProfileService;

    public SrpPlayerRepositoryImpl(SrpPlayerProfileService playerProfileService) {
        this.playerProfileService = playerProfileService;
    }

    @Override
    public boolean hasRequiredGuildRank(UUID playerId) {
        return playerProfileService.getPlayer(new PlayerId(playerId)).getRank().ordinal() >= PlayerRank.Baron.ordinal();
    }

    @Override
    public int getCampLevel(UUID playerId) {
        return playerProfileService.getPlayer(new PlayerId(playerId)).getRank().getCampLevel();
    }
}


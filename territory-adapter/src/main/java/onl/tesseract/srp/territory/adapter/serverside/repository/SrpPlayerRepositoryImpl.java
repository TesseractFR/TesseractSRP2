package onl.tesseract.srp.territory.adapter.serverside.repository;

import onl.tesseract.srp.territory.domain.port.serverside.SrpPlayerRepository;
import onl.tesseract.srp.common.domain.model.enums.PlayerRank;
import onl.tesseract.srp.common.domain.port.userside.PlayerService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SrpPlayerRepositoryImpl implements SrpPlayerRepository {

    private final PlayerService playerService;

    public SrpPlayerRepositoryImpl(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public boolean hasRequiredGuildRank(UUID playerId) {
        return playerService.getPlayer(playerId).getRank().ordinal() >= PlayerRank.Baron.ordinal();
    }

    @Override
    public int getCampLevel(UUID playerId) {
        return playerService.getPlayer(playerId).getRank().getCampLevel();
    }
}


package onl.tesseract.srp.territory.adapter.serverside.service;

import onl.tesseract.srp.territory.domain.port.userside.guild.MoneyService;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.port.userside.PlayerMoneyService;
import onl.tesseract.srp.common.domain.port.userside.SrpPlayerProfileService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MoneyServiceImpl implements MoneyService {

    private final SrpPlayerProfileService playerProfileService;
    private final PlayerMoneyService playerMoneyService;

    public MoneyServiceImpl(SrpPlayerProfileService playerProfileService, PlayerMoneyService playerMoneyService) {
        this.playerProfileService = playerProfileService;
        this.playerMoneyService = playerMoneyService;
    }

    @Override
    public int getPlayerMoney(UUID player) {
        return playerProfileService.getPlayer(new PlayerId(player)).getMoney();
    }

    @Override
    public boolean payCreation(UUID player, int amount) {
        return amount > 0 && playerMoneyService.adjustMoney(new PlayerId(player), -amount);
    }

    @Override
    public boolean transfertToGuild(UUID player, int amount) {
        return amount > 0 && playerMoneyService.adjustMoney(new PlayerId(player), -amount);
    }

    @Override
    public boolean transfertFromGuild(UUID player, int amount) {
        return amount > 0 && playerMoneyService.adjustMoney(new PlayerId(player), amount);
    }
}

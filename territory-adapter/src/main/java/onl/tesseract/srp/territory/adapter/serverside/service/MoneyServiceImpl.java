package onl.tesseract.srp.territory.adapter.serverside.service;

import onl.tesseract.srp.territory.domain.port.userside.guild.MoneyService;
import onl.tesseract.srp.common.domain.port.userside.PlayerService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MoneyServiceImpl implements MoneyService {

    private final PlayerService playerService;

    public MoneyServiceImpl(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public int getPlayerMoney(UUID player) {
        return playerService.getPlayer(player).getMoney();
    }

    @Override
    public boolean payCreation(UUID player, int amount) {
        return amount > 0 && playerService.adjustMoney(player, -amount);
    }

    @Override
    public boolean transfertToGuild(UUID player, int amount) {
        return amount > 0 && playerService.adjustMoney(player, -amount);
    }

    @Override
    public boolean transfertFromGuild(UUID player, int amount) {
        return amount > 0 && playerService.adjustMoney(player, amount);
    }
}

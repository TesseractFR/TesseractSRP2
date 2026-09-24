package onl.tesseract.srp.territory.adapter.serverside.service;

import onl.tesseract.srp.territory.domain.port.userside.guild.MoneyService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MoneyServiceImpl implements MoneyService {

    @Override
    public int getPlayerMoney(UUID player) {
        // TODO: Implement based on economy plugin (Vault, etc.)
        return 0;
    }

    @Override
    public void payCreation(UUID player, int amount) {
        // TODO: Implement payment logic
    }

    @Override
    public void transfertToGuild(UUID player, int amount) {
        // TODO: Implement transfer to guild
    }

    @Override
    public void transfertFromGuild(UUID player, int amount) {
        // TODO: Implement transfer from guild
    }

    @Override
    public void transfertToGuildStaff(int amount) {
        // TODO: Implement staff transfer
    }
}

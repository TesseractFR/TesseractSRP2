package onl.tesseract.srp.common.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.PlayerRank;
import onl.tesseract.srp.common.domain.model.SrpPlayer;
import onl.tesseract.srp.common.domain.port.serverside.PlayerRepository;

import java.util.UUID;

/** User-side operations for player data and progression. */
public class PlayerService {

    private final PlayerRepository repository;

    public PlayerService(PlayerRepository repository) {
        this.repository = repository;
    }

    /** Return the stored player, or a new player with the default values. */
    public SrpPlayer getPlayer(UUID playerId) {
        return getPlayer(new PlayerId(playerId));
    }

    public SrpPlayer getPlayer(PlayerId playerId) {
        SrpPlayer player = repository.getById(playerId);
        return player != null ? player : new SrpPlayer(playerId);
    }

    /** Set a player's rank and matching title. Returns false when it is already set. */
    public boolean setRank(UUID playerId, PlayerRank rank) {
        SrpPlayer player = getPlayer(playerId);
        if (player.getRank() == rank) return false;
        updateRank(player, rank);
        savePlayer(player);
        return true;
    }

    /** Buy the next rank, charging its cost from the player's balance. */
    public boolean buyNextRank(UUID playerId) {
        SrpPlayer player = getPlayer(playerId);
        if (!player.buyNextRank()) return false;
        synchronizeTitle(player);
        savePlayer(player);
        return true;
    }

    /** Apply a money adjustment if the resulting balance remains non-negative. */
    public boolean adjustMoney(UUID playerId, int amount) {
        SrpPlayer player = getPlayer(playerId);
        if ((long) player.getMoney() + amount < 0) return false;
        player.addMoney(amount);
        savePlayer(player);
        return true;
    }

    /** Apply an illumination point adjustment if the resulting total remains non-negative. */
    public boolean adjustIlluminationPoints(UUID playerId, int amount) {
        SrpPlayer player = getPlayer(playerId);
        if ((long) player.getIlluminationPoints() + amount < 0) return false;
        player.addIlluminationPoints(amount);
        savePlayer(player);
        return true;
    }

    public void savePlayer(SrpPlayer player) {
        repository.save(player);
    }

    private void updateRank(SrpPlayer player, PlayerRank rank) {
        player.setRank(rank);
        synchronizeTitle(player);
    }

    private void synchronizeTitle(SrpPlayer player) {
        player.setTitleID(player.getRank().getTitle().getId());
    }
}

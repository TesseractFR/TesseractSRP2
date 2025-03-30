package onl.tesseract.srp.service.player

import onl.tesseract.srp.domain.player.PlayerRank
import onl.tesseract.srp.domain.player.SrpPlayer
import onl.tesseract.srp.repository.hibernate.player.SrpPlayerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
open class SrpPlayerService(private val repository: SrpPlayerRepository) {

    open fun getPlayer(id: UUID): SrpPlayer {
        return repository.getById(id) ?: SrpPlayer(id)
    }

    /**
     * Set the rank of the player.
     * @return True if the rank was updated, false if the player already had this rank.
     */
    open fun setRank(playerID: UUID, rank: PlayerRank): Boolean {
        val player = getPlayer(playerID)
        if (player.rank == rank) return false
        player.rank = rank
        savePlayer(player)
        return true
    }

    @Transactional
    open fun giveMoney(playerID: UUID, amount: Int): Boolean {
        val player = getPlayer(playerID)
        if (player.money + amount >= 0) {
            player.addMoney(amount)
            savePlayer(player)
            return true
        } else {
            return false
        }
    }

    protected open fun savePlayer(player: SrpPlayer) {
        repository.save(player)
    }
}
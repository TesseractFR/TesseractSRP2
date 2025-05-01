package onl.tesseract.srp.testutils.fixture

import onl.tesseract.srp.domain.player.PlayerRank
import onl.tesseract.srp.domain.player.SrpPlayer
import onl.tesseract.srp.repository.hibernate.player.SrpPlayerRepository
import java.util.*

interface SrpPlayerDomainTest {

    val playerRepository: SrpPlayerRepository

    fun player(money: Int = 0, rank: PlayerRank = PlayerRank.Baron): SrpPlayer {
        val srpPlayer = SrpPlayer(UUID.randomUUID(), money = money, rank = rank)
        return playerRepository.save(srpPlayer)
    }
}
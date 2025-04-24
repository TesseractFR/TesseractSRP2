package onl.tesseract.srp.service.guild

import onl.tesseract.srp.domain.guild.Guild
import onl.tesseract.srp.domain.money.ledger.TransactionSubType
import onl.tesseract.srp.domain.money.ledger.TransactionType
import onl.tesseract.srp.repository.hibernate.guild.GuildRepository
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.Location
import org.springframework.stereotype.Service
import java.util.*

@Service
class GuildService(private val guildRepository: GuildRepository, private val playerService: SrpPlayerService) {

    fun createGuild(playerID: UUID, location: Location, guildName: String): GuildCreationResult {
        val guild = Guild(-1, playerID, guildName, location)
        val didPay = playerService.takeMoney(
            playerID,
            10_000,
            type = TransactionType.Guild,
            subType = TransactionSubType.Guild.Creation,
            details = guild.id.toString()
        )
        if (!didPay) return GuildCreationResult.failed(GuildCreationResult.Reason.NotEnoughMoney)
        val createdGuild = guildRepository.save(guild)
        return GuildCreationResult.success(createdGuild)
    }
}

data class GuildCreationResult(val guild: Guild?, val reason: Reason) {

    enum class Reason { Success, NotEnoughMoney }

    companion object {
        fun failed(reason: Reason) = GuildCreationResult(null, reason)
        fun success(guild: Guild) = GuildCreationResult(guild, Reason.Success)
    }
}
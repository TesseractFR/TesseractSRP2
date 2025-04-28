package onl.tesseract.srp.service.guild

import jakarta.transaction.Transactional
import onl.tesseract.srp.domain.guild.Guild
import onl.tesseract.srp.domain.money.ledger.TransactionSubType
import onl.tesseract.srp.domain.money.ledger.TransactionType
import onl.tesseract.srp.domain.world.SrpWorld
import onl.tesseract.srp.repository.hibernate.guild.GuildRepository
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.Location
import org.springframework.stereotype.Service
import java.util.*

private const val SPAWN_PROTECTION_DISTANCE = 150

@Service
open class GuildService(private val guildRepository: GuildRepository, private val playerService: SrpPlayerService) {

    @Transactional
    open fun createGuild(playerID: UUID, location: Location, guildName: String): GuildCreationResult {
        if (location.world.name != SrpWorld.GuildWorld.bukkitName)
            return GuildCreationResult.failed(GuildCreationResult.Reason.InvalidWorld)

        if (guildRepository.findGuildByName(guildName) != null)
            return GuildCreationResult.failed(GuildCreationResult.Reason.NameTaken)
        if (guildRepository.findGuildByLeader(playerID) != null)
            return GuildCreationResult.failed(GuildCreationResult.Reason.PlayerHasGuild)

        if (location.distance(location.world.spawnLocation) <= SPAWN_PROTECTION_DISTANCE)
            return GuildCreationResult.failed(GuildCreationResult.Reason.NearSpawn)

        val guild = Guild(-1, playerID, guildName, location)
        val didPay = playerService.takeMoney(
            playerID,
            10_000,
            type = TransactionType.Guild,
            subType = TransactionSubType.Guild.Creation,
            details = guild.id.toString()
        )
        if (!didPay) return GuildCreationResult.failed(GuildCreationResult.Reason.NotEnoughMoney)
        guild.claimInitialChunks()

        val existingGuild = guild.chunks.any { guildRepository.findGuildByChunk(it) != null }
        if (existingGuild)
            return GuildCreationResult.failed(GuildCreationResult.Reason.NearGuild)

        val createdGuild = guildRepository.save(guild)
        return GuildCreationResult.success(createdGuild)
    }
}

data class GuildCreationResult(val guild: Guild?, val reason: Reason) {

        enum class Reason { Success, NotEnoughMoney, InvalidWorld, NearSpawn, NearGuild, NameTaken, PlayerHasGuild }

    companion object {
        fun failed(reason: Reason) = GuildCreationResult(null, reason)
        fun success(guild: Guild) = GuildCreationResult(guild, Reason.Success)
    }
}
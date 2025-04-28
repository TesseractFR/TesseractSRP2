package onl.tesseract.srp.service.guild

import jakarta.transaction.Transactional
import onl.tesseract.srp.domain.campement.CampementChunk
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
private const val GUILD_COST = 10_000

@Service
open class GuildService(private val guildRepository: GuildRepository, private val playerService: SrpPlayerService) {

    @Transactional
    open fun createGuild(playerID: UUID, location: Location, guildName: String): GuildCreationResult {
        val checkResult = guildCreationChecks(playerID, location, guildName)
        if (checkResult != GuildCreationResult.Reason.Success)
            return GuildCreationResult.failed(checkResult)

        val guild = Guild(-1, playerID, guildName, location)
        playerService.takeMoney(
            playerID,
            amount = GUILD_COST,
            type = TransactionType.Guild,
            subType = TransactionSubType.Guild.Creation,
            details = guild.id.toString()
        )
        guild.claimInitialChunks()
        val createdGuild = guildRepository.save(guild)
        return GuildCreationResult.success(createdGuild)
    }

    protected open fun guildCreationChecks(
        playerID: UUID,
        location: Location,
        guildName: String
    ): GuildCreationResult.Reason {
        if (location.world.name != SrpWorld.GuildWorld.bukkitName)
            return GuildCreationResult.Reason.InvalidWorld
        if (guildRepository.findGuildByName(guildName) != null)
            return GuildCreationResult.Reason.NameTaken
        if (guildRepository.findGuildByLeader(playerID) != null)
            return GuildCreationResult.Reason.PlayerHasGuild

        if (location.distance(location.world.spawnLocation) <= SPAWN_PROTECTION_DISTANCE)
            return GuildCreationResult.Reason.NearSpawn

        val srpPlayer = playerService.getPlayer(playerID)
        if (srpPlayer.money < GUILD_COST)
            return GuildCreationResult.Reason.NotEnoughMoney

        if (!checkFirstClaimAvailable(location)) {
            return GuildCreationResult.Reason.NearGuild
        }
        return GuildCreationResult.Reason.Success
    }

    protected open fun checkFirstClaimAvailable(location: Location): Boolean {
        val spawnChunk = location.chunk
        for (x in -3..3) {
            for (z in -3..3) {
                val guild = guildRepository.findGuildByChunk(CampementChunk(spawnChunk.x + x, spawnChunk.z + z))
                if (guild != null)
                    return false
            }
        }
        return true
    }
}

data class GuildCreationResult(val guild: Guild?, val reason: Reason) {

    enum class Reason { Success, NotEnoughMoney, InvalidWorld, NearSpawn, NearGuild, NameTaken, PlayerHasGuild }

    companion object {
        fun failed(reason: Reason) = GuildCreationResult(null, reason)
        fun success(guild: Guild) = GuildCreationResult(guild, Reason.Success)
    }
}
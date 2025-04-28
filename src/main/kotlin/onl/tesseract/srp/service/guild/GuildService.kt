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
private const val GUILD_PROTECTION_RADIUS = 3

@Service
open class GuildService(private val guildRepository: GuildRepository, private val playerService: SrpPlayerService) {

    @Transactional
    open fun createGuild(playerID: UUID, location: Location, guildName: String): GuildCreationResult {
        val checkResult = guildCreationChecks(playerID, location, guildName)
        if (checkResult.isNotEmpty())
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

    /**
     * Execute creation checks
     * @return The list of creation errors, or empty list if all checks passed
     */
    protected open fun guildCreationChecks(
        playerID: UUID,
        location: Location,
        guildName: String
    ): List<GuildCreationResult.Reason> {
        val errorList: MutableList<GuildCreationResult.Reason> = mutableListOf()
        if (location.world.name != SrpWorld.GuildWorld.bukkitName)
            errorList += GuildCreationResult.Reason.InvalidWorld
        if (guildRepository.findGuildByName(guildName) != null)
            errorList += GuildCreationResult.Reason.NameTaken
        if (guildRepository.findGuildByLeader(playerID) != null)
            errorList += GuildCreationResult.Reason.PlayerHasGuild

        if (location.distance(location.world.spawnLocation) <= SPAWN_PROTECTION_DISTANCE)
            errorList += GuildCreationResult.Reason.NearSpawn

        val srpPlayer = playerService.getPlayer(playerID)
        if (srpPlayer.money < GUILD_COST)
            errorList += GuildCreationResult.Reason.NotEnoughMoney

        if (!checkFirstClaimAvailable(location)) {
            errorList += GuildCreationResult.Reason.NearGuild
        }
        return errorList
    }

    protected open fun checkFirstClaimAvailable(location: Location): Boolean {
        val spawnChunk = location.chunk
        for (x in -GUILD_PROTECTION_RADIUS..GUILD_PROTECTION_RADIUS) {
            for (z in -GUILD_PROTECTION_RADIUS..GUILD_PROTECTION_RADIUS) {
                val guild = guildRepository.findGuildByChunk(CampementChunk(spawnChunk.x + x, spawnChunk.z + z))
                if (guild != null)
                    return false
            }
        }
        return true
    }
}

data class GuildCreationResult(val guild: Guild?, val reason: List<Reason>) {

    enum class Reason { NotEnoughMoney, InvalidWorld, NearSpawn, NearGuild, NameTaken, PlayerHasGuild }

    fun isSuccess(): Boolean = reason.isEmpty() && guild != null

    companion object {
        fun failed(reasons: List<Reason>) = GuildCreationResult(null, reasons)
        fun success(guild: Guild) = GuildCreationResult(guild, emptyList())
    }
}
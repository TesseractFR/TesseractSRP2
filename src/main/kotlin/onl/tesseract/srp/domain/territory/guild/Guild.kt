package onl.tesseract.srp.domain.territory.guild

import onl.tesseract.srp.domain.guild.GuildRank
import onl.tesseract.srp.domain.territory.ChunkCoord
import onl.tesseract.srp.domain.territory.Territory
import onl.tesseract.srp.domain.territory.guild.enum.GuildRole
import onl.tesseract.srp.domain.world.SrpWorld
import org.bukkit.Location
import java.util.*

class Guild(
    var id: Int,
    val name: String,
    spawnLocation: Location,
    money: Int = 0,
    val moneyLedgerID: UUID = UUID.randomUUID(),
    memberContainer: GuildMemberContainerImpl,
    var visitorSpawnLocation: Location? = null,
    var level: Int = 1,
    var xp: Int = 0,
    var rank: GuildRank = GuildRank.HAMEAU,
) : GuildMemberContainer by memberContainer, Territory<GuildChunk>(spawnLocation) {
    override fun getValidWorld(): String {
        return SrpWorld.GuildWorld.name
    }

    var money: Int = money

    constructor(id: Int, leaderId: UUID, name: String, spawnLocation: Location)
            : this(id, name, spawnLocation, memberContainer = GuildMemberContainerImpl(leaderId))

    /**
     * @throws IllegalStateException If the guild already has chunks
     */
    fun claimInitialChunks() {
        check(_chunks.isEmpty())

        val spawnChunk = spawnLocation.chunk
        for (x in -1..1) {
            for (z in -1..1) {
                _chunks.add(GuildChunk(ChunkCoord(spawnChunk.x + x, spawnChunk.z + z, spawnChunk.world.name), this))
            }
        }
    }

    fun setVisitorSpawnpoint(newLocation: Location): Boolean {
        if (!_chunks.contains(GuildChunk(ChunkCoord(newLocation), this))) return false
        visitorSpawnLocation = newLocation
        return true
    }

    fun addMoney(amount: Int) {
        money += amount
    }

    fun addXp(amount: Int) {
        xp += amount
    }

    /**
     * @throws IllegalArgumentException If the player is not a member of the guild
     */
    fun getMemberRole(member: UUID): GuildRole {
        return members.find { it.playerID == member }?.role
            ?: throw IllegalArgumentException("Player $member is not a member of guild $id")
    }

    override fun initChunk(location: Location): GuildChunk {
        return GuildChunk(ChunkCoord(location), this)
    }

    override fun createClaimEvent(player: UUID): GuildChunkClaimEvent {
        return GuildChunkClaimEvent(player)
    }

    override fun createUnclaimEvent(player: UUID): GuildChunkUnclaimEvent {
        return GuildChunkUnclaimEvent(player)
    }

    override fun canClaim(player: UUID): Boolean {
        val role = getMemberRole(player)
        return role.canClaim()
    }
}






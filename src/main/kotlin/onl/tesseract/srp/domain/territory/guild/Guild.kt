package onl.tesseract.srp.domain.territory.guild

import onl.tesseract.srp.domain.territory.enum.SetSpawnResult
import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.domain.commun.Coordinate
import onl.tesseract.srp.domain.territory.container.DefaultVisitorSpawnContainer
import onl.tesseract.srp.domain.territory.Territory
import onl.tesseract.srp.domain.territory.container.VisitorSpawnContainer
import onl.tesseract.srp.domain.territory.guild.enum.GuildRank
import onl.tesseract.srp.domain.territory.guild.enum.GuildRole
import onl.tesseract.srp.domain.territory.guild.event.GuildChunkClaimEvent
import onl.tesseract.srp.domain.territory.guild.event.GuildChunkUnclaimEvent
import java.util.*

class Guild(
    id: UUID = UUID.randomUUID(),
    val name: String,
    spawnLocation: Coordinate,
    money: Int = 0,
    val moneyLedgerID: UUID = UUID.randomUUID(),
    memberContainer: GuildMemberContainer,
    visitorSpawnLocation: Coordinate = spawnLocation,
    var level: Int = 1,
    var xp: Int = 0,
    var rank: GuildRank = GuildRank.HAMEAU,
    val visitorSpawnContainer: VisitorSpawnContainer = DefaultVisitorSpawnContainer(visitorSpawnLocation)
) : GuildMemberContainer by memberContainer, VisitorSpawnContainer by visitorSpawnContainer, Territory<GuildChunk>(id,spawnLocation) {

    var money: Int = money

    constructor(id: UUID, leaderId: UUID, name: String, spawnLocation: Coordinate)
            : this(id, name, spawnLocation, memberContainer = GuildMemberContainerImpl(leaderId))

    /**
     * @throws IllegalStateException If the guild already has chunks
     */
    override fun claimInitialChunks() {
        check(_chunks.isEmpty())

        val spawnChunk = getSpawnpoint().chunkCoord
        for (x in -1..1) {
            for (z in -1..1) {
                _chunks.add(GuildChunk(ChunkCoord(spawnChunk.x + x, spawnChunk.z + z, spawnChunk.world), this))
            }
        }
    }

    override fun setVisitorSpawnpoint(newLocation: Coordinate, player: UUID): SetSpawnResult {
        if(!hasChunk(newLocation.chunkCoord))return SetSpawnResult.OUTSIDE_TERRITORY
        if(!canSetSpawn(player)) return SetSpawnResult.NOT_ALLOWED
        return visitorSpawnContainer.setVisitorSpawnpoint(newLocation,player)
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

    override fun initChunk(chunkCoord: ChunkCoord): GuildChunk {
        return GuildChunk(chunkCoord, this)
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

    override fun canSetSpawn(player: UUID): Boolean {
        val role = getMemberRole(player)
        return role.canSetSpawn()
    }

    override fun canBuild(player: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override fun canOpenChest(player: UUID): Boolean {
        TODO("Not yet implemented")
    }

    fun canInvite(sender: UUID): Boolean {
        val role = getMemberRole(sender)
        return role.canInvite()
    }

}






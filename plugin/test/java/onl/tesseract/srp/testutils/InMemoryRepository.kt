package onl.tesseract.srp.testutils

import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.territory.campement.Campement
import onl.tesseract.srp.domain.territory.guild.Guild
import onl.tesseract.srp.domain.territory.guild.GuildChunk
import onl.tesseract.srp.domain.player.SrpPlayer
import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.domain.territory.guild.enum.GuildRole
import onl.tesseract.srp.repository.generic.player.SrpPlayerRepository
import onl.tesseract.srp.repository.generic.territory.CampementRepository
import onl.tesseract.srp.repository.generic.territory.GuildRepository
import java.util.*

abstract class InMemoryRepository<T, ID> : Repository<T, ID> {

    protected val elements: MutableMap<ID, T> = mutableMapOf()

    override fun getById(id: ID): T? = elements[id]

    override fun save(entity: T): T {
        elements[idOf(entity)] = entity
        return entity
    }
}

class SrpPlayerInMemoryRepository : SrpPlayerRepository, InMemoryRepository<SrpPlayer, UUID>() {

    override fun idOf(entity: SrpPlayer): UUID = entity.uniqueId
}

class GuildInMemoryRepository : GuildRepository, InMemoryRepository<Guild, UUID>() {

    override fun idOf(entity: Guild): UUID = entity.id

    override fun findGuildByChunk(chunk: GuildChunk): Guild? {
        return elements.values.find { chunk in it.getChunks() }
    }

    override fun findGuildByName(name: String): Guild? {
        return elements.values.find { it.name == name }
    }

    override fun findGuildByLeader(leaderID: UUID): Guild? {
        return elements.values.find { it.leaderId == leaderID }
    }

    override fun findGuildByMember(memberID: UUID): Guild? {
        return elements.values.find { guild ->
            guild.members.any { it.playerID == memberID }
        }
    }

    override fun findMemberRole(playerID: UUID): GuildRole? {
        val guild = elements.values.find { g -> g.members.any { it.playerID == playerID } } ?: return null
        return guild.members.firstOrNull { it.playerID == playerID }?.role
    }

    override fun areChunksClaimed(chunks: Collection<GuildChunk>): Boolean {
        return elements.values.any { guild ->
            chunks.any { guild.getChunks().contains(it) }
        }
    }

    override fun findAll(): Collection<Guild> {
        return elements.values
    }

    override fun deleteById(id: UUID) {
        elements.remove(id)
    }

    override fun save(entity: Guild): Guild {
        val newId = UUID.randomUUID()
        elements[newId] = entity
        return entity
    }

    override fun findnByPlayer(player: UUID): Guild? {
        return elements.values.find { g -> g.members.any { it.playerID == player } }
    }
}

class CampementInMemoryRepository : CampementRepository, InMemoryRepository<Campement, UUID>() {

    override fun idOf(entity: Campement): UUID = entity.ownerID

    override fun deleteById(id: UUID) {
        elements.remove(id)
    }

    override fun isChunkClaimed(chunkCoord: ChunkCoord): Boolean {
        return elements.values.any { camp -> camp.getChunks().any { it.chunkCoord == chunkCoord } }
    }

    override fun getCampementByChunk(chunkCoord: ChunkCoord): Campement? {
        return elements.values.firstOrNull { camp -> camp.getChunks().any { it.chunkCoord == chunkCoord } }
    }


    override fun findAll(): List<Campement> = elements.values.toList()
    override fun findnByPlayer(player: UUID): Campement? {
        return elements[player]
    }
}

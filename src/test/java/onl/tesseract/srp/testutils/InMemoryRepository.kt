package onl.tesseract.srp.testutils

import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.campement.Campement
import onl.tesseract.srp.domain.guild.Guild
import onl.tesseract.srp.domain.guild.GuildChunk
import onl.tesseract.srp.domain.guild.GuildRole
import onl.tesseract.srp.domain.player.SrpPlayer
import onl.tesseract.srp.repository.hibernate.CampementRepository
import onl.tesseract.srp.repository.hibernate.guild.GuildRepository
import onl.tesseract.srp.repository.hibernate.player.SrpPlayerRepository
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

class GuildInMemoryRepository : GuildRepository, InMemoryRepository<Guild, Int>() {

    override fun idOf(entity: Guild): Int = entity.id

    override fun findGuildByChunk(chunk: GuildChunk): Guild? {
        return elements.values.find { chunk in it.chunks }
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
            chunks.any { guild.chunks.contains(it) }
        }
    }

    override fun findAll(): Collection<Guild> {
        return elements.values
    }

    override fun deleteById(id: Int) {
        elements.remove(id)
    }

    override fun save(entity: Guild): Guild {
        val newId = if (entity.id == -1) (elements.keys.maxOrNull() ?: 0) + 1 else entity.id
        val saved = Guild(
            id = newId,
            name = entity.name,
            spawnLocation = entity.spawnLocation,
            money = entity.money,
            moneyLedgerID = entity.moneyLedgerID,
            chunks = entity.chunks,
            memberContainer = onl.tesseract.srp.domain.guild.GuildMemberContainerImpl(
                entity.leaderId,
                entity.members,
                entity.invitations,
                entity.joinRequests
            )
        )
        elements[newId] = saved
        return saved
    }
}

class CampementInMemoryRepository : CampementRepository, InMemoryRepository<Campement, UUID>() {

    override fun idOf(entity: Campement): UUID = entity.ownerID

    override fun deleteById(id: UUID) {
        elements.remove(id)
    }

    override fun isChunkClaimed(x: Int, z: Int): Boolean =
        elements.values.any { camp -> camp.chunks.any { it.x == x && it.z == z } }

    override fun getCampementByChunk(x: Int, z: Int): Campement? =
        elements.values.firstOrNull { camp -> camp.chunks.any { it.x == x && it.z == z } }

    override fun findAll(): List<Campement> = elements.values.toList()
}

package onl.tesseract.srp.testutils

import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.campement.CampementChunk
import onl.tesseract.srp.domain.guild.Guild
import onl.tesseract.srp.domain.player.SrpPlayer
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

    override fun findGuildByChunk(chunk: CampementChunk): Guild? {
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

    override fun areChunksClaimed(chunks: Collection<CampementChunk>): Boolean {
        return elements.values.any { guild ->
            chunks.any { guild.chunks.contains(it) }
        }
    }

    override fun findAll(): Collection<Guild> {
        return elements.values
    }
}

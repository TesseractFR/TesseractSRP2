package onl.tesseract.srp.testutils

import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.guild.Guild
import onl.tesseract.srp.domain.player.SrpPlayer
import onl.tesseract.srp.repository.hibernate.guild.GuildRepository
import onl.tesseract.srp.repository.hibernate.player.SrpPlayerRepository
import java.util.*

abstract class InMemoryRepository<T, ID> : Repository<T, ID> {

    private val elements: MutableMap<ID, T> = mutableMapOf()

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
}
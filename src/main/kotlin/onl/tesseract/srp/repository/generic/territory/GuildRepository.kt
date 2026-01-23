package onl.tesseract.srp.repository.generic.territory

import onl.tesseract.srp.domain.territory.guild.Guild
import onl.tesseract.srp.domain.territory.guild.GuildChunk
import onl.tesseract.srp.domain.territory.guild.enum.GuildRole
import java.util.UUID

interface GuildRepository : TerritoryRepository<Guild, UUID> {
    fun findGuildByChunk(chunk: GuildChunk): Guild?
    fun areChunksClaimed(chunks: Collection<GuildChunk>): Boolean
    fun findGuildByName(name: String): Guild?
    fun findGuildByLeader(leaderID: UUID): Guild?
    fun findGuildByMember(memberID: UUID): Guild?
    fun findMemberRole(playerID: UUID): GuildRole?
    fun findAll(): Collection<Guild>
    fun deleteById(id: UUID)
}
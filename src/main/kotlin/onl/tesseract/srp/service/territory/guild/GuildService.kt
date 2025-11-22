package onl.tesseract.srp.service.territory.guild

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.srp.DomainEventPublisher
import onl.tesseract.srp.domain.commun.enum.*
import onl.tesseract.srp.domain.money.ledger.TransactionSubType
import onl.tesseract.srp.domain.money.ledger.TransactionType
import onl.tesseract.srp.domain.player.PlayerRank
import onl.tesseract.srp.domain.territory.ChunkCoord
import onl.tesseract.srp.domain.territory.Coordinate
import onl.tesseract.srp.domain.territory.enum.CreationResult
import onl.tesseract.srp.domain.territory.enum.KickResult
import onl.tesseract.srp.domain.territory.enum.LeaveResult
import onl.tesseract.srp.domain.territory.enum.SetSpawnResult
import onl.tesseract.srp.domain.territory.guild.Guild
import onl.tesseract.srp.domain.territory.guild.GuildChunk
import onl.tesseract.srp.domain.territory.guild.enum.GuildRank
import onl.tesseract.srp.domain.territory.guild.enum.GuildRole
import onl.tesseract.srp.domain.territory.guild.enum.GuildSpawnKind
import onl.tesseract.srp.domain.territory.guild.enum.GuildInvitationResult
import onl.tesseract.srp.domain.territory.guild.enum.GuildUpgradeResult
import onl.tesseract.srp.domain.territory.guild.event.GuildInvitationEvent
import onl.tesseract.srp.domain.territory.guild.event.GuildLevelUpEvent
import onl.tesseract.srp.domain.world.SrpWorld
import onl.tesseract.srp.repository.generic.territory.TerritoryChunkRepository
import onl.tesseract.srp.repository.hibernate.guild.GuildRepository
import onl.tesseract.srp.service.money.MoneyLedgerService
import onl.tesseract.srp.service.money.TransferService
import onl.tesseract.srp.service.player.SrpPlayerService
import onl.tesseract.srp.service.territory.TerritoryService
import onl.tesseract.srp.util.*
import org.springframework.stereotype.Service
import java.util.*

private const val GUILD_COST = 10_000
const val GUILD_BORDER_COMMAND = "/guild border"
private const val XP_PER_LVL_MULTIPLICATOR: Int = 1000

@Service
open class GuildService(
    private val guildRepository: GuildRepository,
    private val playerService: SrpPlayerService,
    eventService: DomainEventPublisher,
    private val ledgerService: MoneyLedgerService,
    private val transferService: TransferService,
    territoryChunkRepository: TerritoryChunkRepository
) : TerritoryService<GuildChunk, Guild, Int>(guildRepository,territoryChunkRepository,eventService) {
    @PostConstruct
    fun registerInServiceContainer() {
        ServiceContainer.getInstance().registerService(GuildService::class.java, this)
    }

    override fun isCorrectWorld(worldName: String) =
        worldName == SrpWorld.GuildWorld.bukkitName

    override fun interactionOutcomeWhenNoOwner(): InteractionAllowResult =
        InteractionAllowResult.Ignore

    override fun isMemberOrTrusted(territory: Guild, playerId: UUID): Boolean {
        val playerGuild = guildRepository.findGuildByMember(playerId) ?: return false
        return playerGuild.id == territory.id
    }


    private fun getGuild(guildID: Int): Guild {
        val guild = guildRepository.getById(guildID)
            ?: throw IllegalArgumentException("Guild not found with id $guildID")
        return guild
    }
    open fun getAllGuilds(): Collection<Guild> = guildRepository.findAll()
    open fun getGuildByLeader(leaderId: UUID) = guildRepository.findGuildByLeader(leaderId)
    open fun getGuildByMember(memberId: UUID) = guildRepository.findGuildByMember(memberId)
    open fun getGuildByChunk(chunk: ChunkCoord) = getByChunk(chunk)
    open fun getMemberRole(playerID: UUID): GuildRole? = guildRepository.findMemberRole(playerID)

    @Transactional
    open fun createGuild(playerID: UUID, coordinate: Coordinate, guildName: String): CreationResult {
        val srpPlayer = playerService.getPlayer(playerID)
        if(srpPlayer.money<GUILD_COST) return CreationResult.NOT_ENOUGH_MONEY
        if(srpPlayer.rank < PlayerRank.Baron) return CreationResult.RANK_TOO_LOW
        if(getByName(guildName)!=null) return CreationResult.NAME_TAKEN
        val result = isCreationAvailable(playerID,coordinate.chunkCoord)
        if(result != CreationResult.SUCCESS) return result
        val guild = Guild(-1, playerID, guildName, coordinate)
        playerService.takeMoney(
            playerID, GUILD_COST, TransactionType.Guild, TransactionSubType.Guild.Creation, guild.id.toString()
        )
        guild.claimInitialChunks()
        guildRepository.save(guild)
        return result
    }

    private fun getByName(guildName: String): Guild? {
        return guildRepository.findGuildByName(guildName)
    }

    fun setSpawnpoint(requesterId: UUID, coordinate: Coordinate, kind: GuildSpawnKind): SetSpawnResult {
        if(kind == GuildSpawnKind.PRIVATE)return setSpawnpoint(requesterId,coordinate)
        return setVisitorSpawnpoint(requesterId,coordinate)
    }


    fun setVisitorSpawnpoint(player: UUID, coordinate: Coordinate): SetSpawnResult {
        val guild = getByPlayer(player) ?: return SetSpawnResult.TERRITORY_NOT_FOUND
        val result = guild.setVisitorSpawnpoint(coordinate,player)
        if(result == SetSpawnResult.SUCCESS){
            guildRepository.save(guild)
        }
        return result
    }

    open fun getPrivateSpawn(guildId: Int): Coordinate? = getById(guildId)?.getSpawnpoint()

    open fun getVisitorSpawn(guildId: Int): Coordinate? = getById(guildId)?.getVisitorSpawnpoint()

    @Transactional
    open fun deleteGuildAsLeader(leaderId: UUID): Boolean {
        val guild = guildRepository.findGuildByLeader(leaderId) ?: return false
        guildRepository.deleteById(guild.id)
        return true
    }

    @Transactional
    open fun deleteGuildAsStaff(guildId: Int): Boolean {
        guildRepository.deleteById(guildId)
        return true
    }

    @Transactional
    open fun invite(sender : UUID, target: UUID): GuildInvitationResult {
        if(sender == target) return GuildInvitationResult.SAME_PLAYER
        if(getGuildByMember(target) != null) return GuildInvitationResult.HAS_GUILD
        val guild = getGuildByMember(sender)?: return GuildInvitationResult.TERRITORY_NOT_FOUND
        if(!guild.canInvite(sender)) return GuildInvitationResult.NOT_ALLOWED

        if (guild.joinRequests.contains(target)){
            guild.join(target)
            guildRepository.save(guild)
            return GuildInvitationResult.SUCCESS_JOIN
        }
        guild.invitePlayer(target)
        eventService.publish(GuildInvitationEvent(guild.name,sender,target))
        guildRepository.save(guild)
        return GuildInvitationResult.SUCCESS_INVITE

    }

    open fun acceptInvitation(guildName: String, playerID: UUID): Boolean {
        val guild = getByName(guildName)?: return false
        val accepted = when {
            guild.members.any { it.playerID == playerID } -> false
            playerID !in guild.invitations -> false
            else -> {
                guild.join(playerID)
                guildRepository.save(guild)
                true
            }
        }
        return accepted
    }


    open fun declineInvitation(guildName: String, playerID: UUID): Boolean {
        val guild = getByName(guildName)?: return false
        val removed = guild.removeInvitation(playerID)
        if (removed) guildRepository.save(guild)
        return removed
    }


    open fun addMemberAsStaff(guildID: Int, playerID: UUID) {
        val guild = getGuild(guildID)
        if (guildRepository.findGuildByMember(playerID) != null)
            return
        guild.join(playerID)
        guildRepository.save(guild)
    }

    @Transactional
    open fun setMemberRoleAsStaff(
        guildID: Int,
        targetID: UUID,
        newRole: GuildRole,
        replacementLeaderID: UUID? = null
    ): StaffSetRoleResult {
        val guild = getGuild(guildID)
        fun findMember(id: UUID) = guild.members.first { it.playerID == id }
        val outcome: StaffSetRoleResult = when {
            findMember(targetID).role == newRole -> StaffSetRoleResult.SAME_ROLE
            newRole == GuildRole.Leader -> {
                val oldLeader = findMember(guild.leaderId)
                val target    = findMember(targetID)
                oldLeader.role = GuildRole.Citoyen
                target.role    = GuildRole.Leader
                guild.leaderId = targetID
                StaffSetRoleResult.SUCCESS
            }
            targetID == guild.leaderId -> when (replacementLeaderID) {
                null      -> StaffSetRoleResult.NEED_NEW_LEADER
                targetID  -> StaffSetRoleResult.NEW_LEADER_SAME_AS_TARGET
                else      -> {
                    val newLeader = findMember(replacementLeaderID)
                    val oldLeader = findMember(targetID)
                    newLeader.role = GuildRole.Leader
                    oldLeader.role = newRole
                    guild.leaderId = replacementLeaderID
                    StaffSetRoleResult.SUCCESS
                }
            }
            else -> {
                findMember(targetID).role = newRole
                StaffSetRoleResult.SUCCESS
            }
        }
        if (outcome == StaffSetRoleResult.SUCCESS) {
            guildRepository.save(guild)
        }
        return outcome
    }

    @Transactional
    open fun kickMember(guildID: Int, requesterID: UUID, targetID: UUID): KickResult {
        val guild = getGuild(guildID)
        return when {
            guild.leaderId != requesterID -> KickResult.NotAuthorized
            guild.members.none { it.playerID == targetID } -> KickResult.NotMember
            guild.leaderId == targetID -> KickResult.CannotKickLeader
            else -> {
                guild.removeMember(targetID)
                guildRepository.save(guild)
                KickResult.Success
            }
        }
    }

    @Transactional
    open fun kickMemberAsStaff(guildID: Int, targetID: UUID): KickResult {
        val guild = getGuild(guildID)
        return when {
            guild.members.none { it.playerID == targetID } -> KickResult.NotMember
            guild.leaderId == targetID -> KickResult.CannotKickLeader
            else -> {
                guild.removeMember(targetID)
                guildRepository.save(guild)
                KickResult.Success
            }
        }
    }

    @Transactional
    open fun leaveGuild(playerID: UUID): LeaveResult {
        val guild = checkNotNull(guildRepository.findGuildByMember(playerID)) {
            "Player $playerID is not in a guild"
        }
        if (guild.leaderId == playerID) return LeaveResult.LeaderMustDelete
        guild.removeMember(playerID)
        guildRepository.save(guild)
        return LeaveResult.Success
    }

    @Transactional
    open fun depositMoney(guildID: Int, playerID: UUID, amount: UInt): Boolean {
        val guild = getGuild(guildID)

        if (playerService.getPlayer(playerID).money < amount) {
            return false
        }
        require(!(guild.members.none { it.playerID == playerID })) {
            "Player $playerID is not a member of guild $guildID"
        }
        transferService.transferMoney(
            amount.toInt(),
            TransactionType.Guild,
            TransactionSubType.Guild.BankTransfer,
            "$playerID"
        ) {
            playerService.fromPlayer(playerID)
            toGuild(guildID)
        }
        return true
    }

    @Transactional
    open fun withdrawMoney(guildID: Int, playerID: UUID, amount: UInt): Boolean {
        val guild = getGuild(guildID)
        require(guild.getMemberRole(playerID).canWithdrawMoney())

        if (guild.money < amount) return false
        transferService.transferMoney(
            amount.toInt(),
            TransactionType.Guild,
            TransactionSubType.Guild.BankTransfer,
            "$playerID"
        ) {
            fromGuild(guildID)
            playerService.toPlayer(playerID)
        }
        return true
    }

    open fun moneyTransaction(
        guildID: Int,
        amount: Int,
        transactionBuilder: TransferService.TransferTransactionBuilder
    ) {
        val guild = getGuild(guildID)
        require(guild.money + amount >= 0) {
            "Guild $guildID does not have enough money (current = ${guild.money}, to pay = ${amount})"
        }
        if (amount < 0)
            transactionBuilder.from = ledgerService.getGuildLedger(guild.moneyLedgerID)
        else
            transactionBuilder.to = ledgerService.getGuildLedger(guild.moneyLedgerID)
        guild.addMoney(amount)
        guildRepository.save(guild)
    }

    open fun giveMoneyAsStaff(guildID: Int, amount: Int) {
        val guild = getGuild(guildID)
        guild.addMoney(amount)
        ledgerService.recordTransaction(
            from = ledgerService.getServerLedger(),
            to = ledgerService.getGuildLedger(guild.moneyLedgerID),
            amount = amount,
            type = TransactionType.Staff,
            subType = TransactionSubType.Staff.Give,
        )
        guildRepository.save(guild)
    }

    private fun xpToNextLevel(level: Int): Int = XP_PER_LVL_MULTIPLICATOR * level

    @Transactional
    open fun addGuildXp(guildId: Int, amount: Int) {
        val guild = getGuild(guildId)
        guild.addXp(amount.coerceAtLeast(0))
        guildRepository.save(guild)
        upgradeGuildLevel(guildId)
    }

    @Transactional
    open fun addLevel(guildId: Int, amount: Int) {
        val guild = getGuild(guildId)
        guild.level += amount.coerceAtLeast(0)
        guild.xp = 0
        guildRepository.save(guild)
    }

    open fun setLevel(guildId: Int, level: Int) {
        val guild = getGuild(guildId)
        guild.level = level.coerceAtLeast(1)
        guild.xp = 0
        guildRepository.save(guild)
    }

    open fun upgradeGuildLevel(guildId: Int): Boolean {
        val guild = getGuild(guildId)
        val need = xpToNextLevel(guild.level)
        if (guild.xp < need) return false

        guild.xp -= need
        guild.level += 1
        guildRepository.save(guild)
        eventService.publish(GuildLevelUpEvent(guild,guild.level))
        return true
    }

    open fun upgradeRank(guildId: Int, to: GuildRank): GuildUpgradeResult {
        val g = getGuild(guildId)
        return when {
            to <= g.rank ->
                GuildUpgradeResult.ALREADY_AT_OR_ABOVE
            g.level < to.minLevel ->
                GuildUpgradeResult.RANK_LOCKED
            g.money < to.cost ->
                GuildUpgradeResult.NOT_ENOUGH_MONEY
            else -> {
                g.money -= to.cost
                g.rank = to
                guildRepository.save(g)
                GuildUpgradeResult.SUCCESS
            }
        }
    }

    open fun setRank(guildId: Int, rank: GuildRank) {
        val g = getGuild(guildId)
        g.rank = rank
        guildRepository.save(g)
    }
}

data class GuildCreationResult(val guild: Guild?, val reason: CreationResult? = null) {
    companion object {
        fun failed(reasons: CreationResult) = GuildCreationResult(null, reasons)
        fun success(guild: Guild) = GuildCreationResult(guild)
    }
}



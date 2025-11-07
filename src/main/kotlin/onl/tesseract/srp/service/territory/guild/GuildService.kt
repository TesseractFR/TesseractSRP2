package onl.tesseract.srp.service.territory.guild

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.chat.ChatEntryService
import onl.tesseract.lib.event.EventService
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.commun.enum.CreationResult
import onl.tesseract.srp.domain.commun.enum.SetSpawnResult
import onl.tesseract.srp.domain.guild.GuildRank
import onl.tesseract.srp.domain.money.ledger.TransactionSubType
import onl.tesseract.srp.domain.money.ledger.TransactionType
import onl.tesseract.srp.domain.player.PlayerRank
import onl.tesseract.srp.domain.territory.guild.Guild
import onl.tesseract.srp.domain.territory.guild.GuildChunk
import onl.tesseract.srp.domain.territory.guild.enum.GuildRole
import onl.tesseract.srp.domain.territory.guild.enum.GuildSpawnKind
import onl.tesseract.srp.domain.world.SrpWorld
import onl.tesseract.srp.repository.generic.territory.TerritoryChunkRepository
import onl.tesseract.srp.repository.hibernate.guild.GuildRepository
import onl.tesseract.srp.service.money.MoneyLedgerService
import onl.tesseract.srp.service.money.TransferService
import onl.tesseract.srp.service.player.SrpPlayerService
import onl.tesseract.srp.service.territory.TerritoryService
import onl.tesseract.srp.util.*
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Player
import org.springframework.stereotype.Service
import java.util.*

private const val SPAWN_PROTECTION_RADIUS = 150
private const val GUILD_COST = 10_000
private const val GUILD_PROTECTION_RADIUS = 3
const val GUILD_BORDER_COMMAND = "/guild border"
private const val XP_PER_LVL_MULTIPLICATOR: Int = 1000

@Service
open class GuildService(
    private val guildRepository: GuildRepository,
    private val playerService: SrpPlayerService,
    private val eventService: EventService,
    private val ledgerService: MoneyLedgerService,
    private val transferService: TransferService,
    private val chatEntryService: ChatEntryService,
    territoryChunkRepository: TerritoryChunkRepository
) : TerritoryService<GuildChunk, Guild, Int>(guildRepository,territoryChunkRepository,eventService) {
    @PostConstruct
    fun registerInServiceContainer() {
        ServiceContainer.getInstance().registerService(GuildService::class.java, this)
    }

    override val spawnProtectionRadius: Int = SPAWN_PROTECTION_RADIUS
    override val territoryProtectionRadius: Int = GUILD_PROTECTION_RADIUS

    private fun guild(id: Int): Guild =
        guildRepository.getById(id) ?: error("Guild not found: $id")

    override fun isCorrectWorld(loc: Location) =
        loc.world.name == SrpWorld.GuildWorld.bukkitName

    override fun isAuthorizedToSetSpawn(territory: Guild, requesterId: UUID): Boolean =
        territory.getMemberRole(requesterId) == GuildRole.Leader

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
    open fun getGuildByChunk(chunk: Chunk) = getByChunk(chunk)
    open fun getMemberRole(playerID: UUID): GuildRole? = guildRepository.findMemberRole(playerID)

    @Transactional
    open fun createGuild(playerID: UUID, location: Location, guildName: String): CreationResult {
        val srpPlayer = playerService.getPlayer(playerID)
        if(srpPlayer.money<GUILD_COST) return CreationResult.NOT_ENOUGH_MONEY
        if(srpPlayer.rank < PlayerRank.Baron) return CreationResult.RANK_TOO_LOW
        if(getByName(guildName)!=null) return CreationResult.NAME_TAKEN
        val result = isCreationAvailable(playerID,location)
        if(result != CreationResult.SUCCESS) return result
        val guild = Guild(-1, playerID, guildName, location)
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

    fun setSpawnpoint(territory: Guild, requesterId: UUID, newLoc: Location, kind: GuildSpawnKind): SetSpawnResult {
        if(kind == GuildSpawnKind.PRIVATE)return setSpawnpoint(territory,requesterId,newLoc)
        return setVisitorSpawnpoint(territory,requesterId,newLoc)
    }


    fun setVisitorSpawnpoint(territory: Guild, requesterId: UUID, newLoc: Location): SetSpawnResult {
        val inside = territory.hasChunk(newLoc)

        val result = when {
            !isAuthorizedToSetSpawn(territory, requesterId) -> SetSpawnResult.NOT_AUTHORIZED
            !isCorrectWorld(newLoc) -> SetSpawnResult.INVALID_WORLD
            !inside -> SetSpawnResult.OUTSIDE_TERRITORY
            persistVisitorSpawn(territory, newLoc) -> SetSpawnResult.SUCCESS
            else -> SetSpawnResult.OUTSIDE_TERRITORY
        }
        return result
    }

    protected fun persistVisitorSpawn(territory: Guild, loc: Location): Boolean{
        val ok = territory.setVisitorSpawnpoint(loc)
        if (ok) guildRepository.save(territory)
        return ok
    }


    open fun getPrivateSpawn(guildId: Int): Location? = guild(guildId).spawnLocation


    open fun getVisitorSpawn(guildId: Int): Location? = guild(guildId).visitorSpawnLocation

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
    open fun invite(guildID: Int, playerID: UUID): InvitationResult {
        val guild = getGuild(guildID)

        if (guildRepository.findGuildByMember(playerID) != null)
            return InvitationResult.Failed

        val result = if (guild.joinRequests.contains(playerID)) {
            guild.join(playerID)
            InvitationResult.Joined
        } else {
            guild.invitePlayer(playerID)
            InvitationResult.Invited
        }
        guildRepository.save(guild)
        return result
    }
    @Transactional
    open fun handleGuildInvitation(sender: Player, target: Player) {
        val role = getMemberRole(sender.uniqueId)
        val guild = if (role == GuildRole.Leader) getGuildByLeader(sender.uniqueId) else null

        val error: Component? = when {
            role == null ->
                GuildChatError + "Tu n'as pas de guilde. Rejoins-en une existante ou crées-en une nouvelle."
            role != GuildRole.Leader ->
                GuildChatError + "Tu n'es pas le chef de ta guilde."
            target.uniqueId == sender.uniqueId ->
                GuildChatError + "Tu ne peux pas t'inviter toi-même."
            guild == null ->
                GuildChatError + "Tu n'as pas de guilde."
            else -> null
        }
        if (error != null) {
            sender.sendMessage(error)
            return
        }

        if (guild != null) {
            when (invite(guild.id, target.uniqueId)) {
                InvitationResult.Failed -> {
                    sender.sendMessage(GuildChatError + "${target.name} est déjà dans une guilde.")
                }
                InvitationResult.Joined -> {
                    sender.sendMessage(GuildChatSuccess + "${target.name} a rejoint la guilde !")
                    target.sendMessage(GuildChatSuccess + "Tu as rejoint la guilde ${guild.name}.")
                }
                InvitationResult.Invited -> {
                    sender.sendMessage(GuildChatFormat + "${target.name} a été invité. En attente d'acceptation.")
                    target.sendMessage(GuildChatFormat + "${sender.name} t'invite à rejoindre la guilde ${guild.name}.")

                    val acceptButton = Component.text("✔ Accepter")
                        .color(NamedTextColor.GREEN)
                        .clickEvent(chatEntryService.clickCommand(target) {
                            if (acceptInvitation(guild.id, target.uniqueId)) {
                                target.sendMessage(GuildChatSuccess + "Tu as rejoint la guilde ${guild.name}.")
                                sender.sendMessage(GuildChatSuccess + "${target.name} a rejoint la guilde.")
                            }
                        })

                    val denyButton = Component.text("✖ Refuser")
                        .color(NamedTextColor.RED)
                        .clickEvent(chatEntryService.clickCommand(target) {
                            if (declineInvitation(guild.id, target.uniqueId)) {
                                target.sendMessage(GuildChatError + "Invitation refusée.")
                                sender.sendMessage(GuildChatError + "${target.name} a refusé l'invitation.")
                            }
                        })

                    target.sendMessage(acceptButton.append(Component.text(" ")).append(denyButton))
                }
            }
        }
    }

    @Transactional
    open fun acceptInvitation(guildID: Int, playerID: UUID): Boolean {
        val guild = getGuild(guildID)
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

    @Transactional
    open fun declineInvitation(guildID: Int, playerID: UUID): Boolean {
        val guild = getGuild(guildID)
        val removed = guild.removeInvitation(playerID)
        if (removed) guildRepository.save(guild)
        return removed
    }

    @Transactional
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
        notifyGuildLevelUp(guild)
        return true
    }

    private fun notifyGuildLevelUp(guild: Guild) {
        val server = Bukkit.getServer()
        val msg: Component =
            GuildChatSuccess +
                    "Ta guilde " + Component.text(guild.name, NamedTextColor.GREEN) +
                    " est passée au niveau " + Component.text(guild.level.toString(), NamedTextColor.GOLD) + " !"

        val recipients = (guild.members.map { it.playerID } + guild.leaderId).distinct()
        recipients.forEach { uuid ->
            server.getPlayer(uuid)?.sendMessage(msg)
        }
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

enum class GuildSetSpawnResult { SUCCESS, NOT_AUTHORIZED, INVALID_WORLD, OUTSIDE_TERRITORY }
enum class InvitationResult { Invited, Joined, Failed }
enum class KickResult { Success, NotMember, NotAuthorized, CannotKickLeader }
enum class LeaveResult { Success, LeaderMustDelete }
enum class GuildUnclaimResult { SUCCESS, ALREADY_CLAIMED, NOT_AUTHORIZED, LAST_CHUNK, SPAWNPOINT_CHUNK }
enum class GuildUpgradeResult { SUCCESS, RANK_LOCKED, NOT_ENOUGH_MONEY, ALREADY_AT_OR_ABOVE }
enum class StaffSetRoleResult { SUCCESS, SAME_ROLE, NEED_NEW_LEADER, NEW_LEADER_SAME_AS_TARGET }

package onl.tesseract.srp.service.guild

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.chat.ChatEntryService
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.campement.CampementChunk
import onl.tesseract.srp.domain.guild.Guild
import onl.tesseract.srp.domain.guild.GuildRole
import onl.tesseract.srp.domain.money.ledger.TransactionSubType
import onl.tesseract.srp.domain.money.ledger.TransactionType
import onl.tesseract.srp.domain.player.PlayerRank
import onl.tesseract.srp.domain.world.SrpWorld
import onl.tesseract.srp.repository.hibernate.guild.GuildRepository
import onl.tesseract.srp.service.money.MoneyLedgerService
import onl.tesseract.srp.service.money.TransferService
import onl.tesseract.srp.service.player.SrpPlayerService
import onl.tesseract.srp.util.GuildChatError
import onl.tesseract.srp.util.GuildChatFormat
import onl.tesseract.srp.util.GuildChatSuccess
import onl.tesseract.srp.util.compareTo
import org.bukkit.Location
import org.bukkit.entity.Player
import org.springframework.stereotype.Service
import java.util.*

private const val SPAWN_PROTECTION_DISTANCE = 150
private const val GUILD_COST = 10_000
private const val GUILD_PROTECTION_RADIUS = 3

@Service
open class GuildService(
    private val guildRepository: GuildRepository,
    private val playerService: SrpPlayerService,
    private val ledgerService: MoneyLedgerService,
    private val transferService: TransferService,
    private val chatEntryService: ChatEntryService
) {
    @PostConstruct
    fun registerInServiceContainer() {
        ServiceContainer.getInstance().registerService(GuildService::class.java, this)
    }

    open fun getGuildByLeader(leaderId: UUID) = guildRepository.findGuildByLeader(leaderId)
    open fun getGuildByMember(memberId: UUID) = guildRepository.findGuildByMember(memberId)
    open fun getMemberRole(playerID: UUID): GuildRole? = guildRepository.findMemberRole(playerID)

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

    @Transactional
    open fun deleteGuildAsLeader(leaderId: UUID): Boolean {
        val guild = guildRepository.findGuildByLeader(leaderId) ?: return false
        guildRepository.deleteById(guild.id)
        return true
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
        if (guildRepository.findGuildByMember(playerID) != null) {
            return listOf(GuildCreationResult.Reason.PlayerHasGuild)
        }
        val errorList: MutableList<GuildCreationResult.Reason> = mutableListOf()
        if (location.world.name != SrpWorld.GuildWorld.bukkitName)
            errorList += GuildCreationResult.Reason.InvalidWorld
        if (guildRepository.findGuildByName(guildName) != null)
            errorList += GuildCreationResult.Reason.NameTaken
        if (location.distance(location.world.spawnLocation) <= SPAWN_PROTECTION_DISTANCE)
            errorList += GuildCreationResult.Reason.NearSpawn

        val srpPlayer = playerService.getPlayer(playerID)
        if (srpPlayer.money < GUILD_COST)
            errorList += GuildCreationResult.Reason.NotEnoughMoney
        if (srpPlayer.rank < PlayerRank.Baron)
            errorList += GuildCreationResult.Reason.Rank

        if (!checkFirstClaimAvailable(location)) {
            errorList += GuildCreationResult.Reason.NearGuild
        }
        return errorList
    }

    protected open fun checkFirstClaimAvailable(location: Location): Boolean {
        val chunks: MutableList<CampementChunk> = mutableListOf()
        val spawnChunk = location.chunk
        for (x in -GUILD_PROTECTION_RADIUS..GUILD_PROTECTION_RADIUS) {
            for (z in -GUILD_PROTECTION_RADIUS..GUILD_PROTECTION_RADIUS) {
                chunks += CampementChunk(spawnChunk.x + x, spawnChunk.z + z)
            }
        }
        return !guildRepository.areChunksClaimed(chunks)
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
    open fun join(guildID: Int, playerID: UUID): JoinResult {
        val guild = getGuild(guildID)

        if (guildRepository.findGuildByMember(playerID) != null)
            return JoinResult.Failed

        val result = if (guild.invitations.contains(playerID)) {
            guild.join(playerID)
            JoinResult.Joined
        } else {
            guild.askToJoin(playerID)
            JoinResult.Requested
        }
        guildRepository.save(guild)
        return result
    }

    @Transactional
    open fun kickMember(guildID: Int, requesterID: UUID, targetID: UUID): KickResult {
        val guild = getGuild(guildID)

        val result = when {
            guild.leaderId != requesterID -> KickResult.NotAuthorized
            else -> {
                guild.removeMember(targetID)
                guildRepository.save(guild)
                KickResult.Success
            }
        }
        return result
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

    private fun getGuild(guildID: Int): Guild {
        val guild = guildRepository.getById(guildID)
            ?: throw IllegalArgumentException("Guild not found with id $guildID")
        return guild
    }
}

data class GuildCreationResult(val guild: Guild?, val reason: List<Reason>) {

    enum class Reason { NotEnoughMoney, InvalidWorld, NearSpawn, NearGuild, NameTaken, PlayerHasGuild, Rank }

    fun isSuccess(): Boolean = reason.isEmpty() && guild != null

    companion object {
        fun failed(reasons: List<Reason>) = GuildCreationResult(null, reasons)
        fun success(guild: Guild) = GuildCreationResult(guild, emptyList())
    }
}

enum class InvitationResult { Invited, Joined, Failed }
enum class JoinResult { Joined, Requested, Failed }
enum class KickResult { Success, NotMember, NotAuthorized, CannotKickLeader }
enum class LeaveResult { Success, LeaderMustDelete }

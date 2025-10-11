package onl.tesseract.srp.service.guild

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.chat.ChatEntryService
import onl.tesseract.lib.event.EventService
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.controller.command.guild.NO_GUILD_MESSAGE
import onl.tesseract.srp.controller.event.guild.GuildChunkClaimEvent
import onl.tesseract.srp.controller.event.guild.GuildChunkUnclaimEvent
import onl.tesseract.srp.domain.guild.Guild
import onl.tesseract.srp.domain.guild.GuildChunk
import onl.tesseract.srp.domain.guild.GuildRank
import onl.tesseract.srp.domain.guild.GuildRole
import onl.tesseract.srp.domain.money.ledger.TransactionSubType
import onl.tesseract.srp.domain.money.ledger.TransactionType
import onl.tesseract.srp.domain.player.PlayerRank
import onl.tesseract.srp.domain.world.SrpWorld
import onl.tesseract.srp.repository.hibernate.guild.GuildRepository
import onl.tesseract.srp.service.money.MoneyLedgerService
import onl.tesseract.srp.service.money.TransferService
import onl.tesseract.srp.service.player.SrpPlayerService
import onl.tesseract.srp.util.*
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Player
import org.springframework.stereotype.Service
import java.util.*

private const val SPAWN_PROTECTION_DISTANCE = 150
private const val GUILD_COST = 10_000
private const val GUILD_PROTECTION_RADIUS = 3
private const val GUILD_BORDER_COMMAND = "/guild border"

@Service
open class GuildService(
    private val guildRepository: GuildRepository,
    private val playerService: SrpPlayerService,
    private val eventService: EventService,
    private val ledgerService: MoneyLedgerService,
    private val transferService: TransferService,
    private val chatEntryService: ChatEntryService
) {
    @PostConstruct
    fun registerInServiceContainer() {
        ServiceContainer.getInstance().registerService(GuildService::class.java, this)
    }

    private fun getGuild(guildID: Int): Guild {
        val guild = guildRepository.getById(guildID)
            ?: throw IllegalArgumentException("Guild not found with id $guildID")
        return guild
    }
    open fun getAllGuilds(): Collection<Guild> = guildRepository.findAll()
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
        else if (location.distance(location.world.spawnLocation) <= SPAWN_PROTECTION_DISTANCE)
            errorList += GuildCreationResult.Reason.NearSpawn
        if (guildRepository.findGuildByName(guildName) != null)
            errorList += GuildCreationResult.Reason.NameTaken

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
        val chunks: MutableList<GuildChunk> = mutableListOf()
        val spawnChunk = location.chunk
        for (x in -GUILD_PROTECTION_RADIUS..GUILD_PROTECTION_RADIUS) {
            for (z in -GUILD_PROTECTION_RADIUS..GUILD_PROTECTION_RADIUS) {
                chunks += GuildChunk(spawnChunk.x + x, spawnChunk.z + z)
            }
        }
        return !guildRepository.areChunksClaimed(chunks)
    }

    enum class GuildSpawnKind { PRIVATE, VISITOR }

    open fun setSpawnpoint(
        guildID: Int,
        requesterID: UUID,
        newLocation: Location,
        kind: GuildSpawnKind = GuildSpawnKind.PRIVATE
    ): GuildSetSpawnResult {
        val guild = getGuild(guildID)
        val result = if (newLocation.world.name != SrpWorld.GuildWorld.bukkitName) {
            GuildSetSpawnResult.INVALID_WORLD
        } else if (guild.getMemberRole(requesterID) != GuildRole.Leader) {
            GuildSetSpawnResult.NOT_AUTHORIZED
        } else {
            val ok = when (kind) {
                GuildSpawnKind.PRIVATE  -> guild.setSpawnpoint(newLocation)
                GuildSpawnKind.VISITOR  -> guild.setVisitorSpawnpoint(newLocation)
            }
            if (ok) GuildSetSpawnResult.SUCCESS else GuildSetSpawnResult.OUTSIDE_TERRITORY
        }
        if (result == GuildSetSpawnResult.SUCCESS) {
            guildRepository.save(guild)
        }
        return result
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

    /**
     * Claim a chunk for the guild, ensuring it is adjacent to existing chunks or is the first chunk.
     */
    open fun claimChunk(guildID: Int, requesterID: UUID, chunk: Chunk): GuildClaimResult {
        val guild = getGuild(guildID)
        val target = GuildChunk(chunk.x, chunk.z)
        val result = when {
            guild.getMemberRole(requesterID) != GuildRole.Leader ->
                GuildClaimResult.NOT_AUTHORIZED
            target in guild.chunks ->
                GuildClaimResult.ALREADY_OWNED
            guildRepository.areChunksClaimed(listOf(target)) ->
                GuildClaimResult.ALREADY_CLAIMED
            !(guild.chunks.isEmpty() ||
                    TerritoryChunks.isAdjacentToAny(guild.chunks, target, { it.x }, { it.z })) ->
                GuildClaimResult.NOT_ADJACENT
            else -> {
                guild.addChunk(target)
                guildRepository.save(guild)
                eventService.callEvent(GuildChunkClaimEvent(requesterID, target))
                GuildClaimResult.SUCCESS
            }
        }
        return result
    }

    /**
     * Unclaim a chunk from the guild, ensuring it remains connected and does not remove the spawn chunk.
     */
    open fun unclaimChunk(guildID: Int, requesterID: UUID, chunk: Chunk): GuildUnclaimResult {
        val guild = getGuild(guildID)
        val target = GuildChunk(chunk.x, chunk.z)
        val result = when {
            guild.getMemberRole(requesterID) != GuildRole.Leader ->
                GuildUnclaimResult.NOT_AUTHORIZED
            target !in guild.chunks ->
                GuildUnclaimResult.ALREADY_CLAIMED
            guild.chunks.size == 1 ->
                GuildUnclaimResult.LAST_CHUNK
            target == GuildChunk(guild.spawnLocation) ->
                GuildUnclaimResult.SPAWNPOINT_CHUNK
            !TerritoryChunks.isUnclaimValid(guild.chunks, target, { it.x }, { it.z }) ->
                GuildUnclaimResult.NOT_AUTHORIZED
            else -> {
                guild.removeChunk(target)
                guildRepository.save(guild)
                eventService.callEvent(GuildChunkUnclaimEvent(requesterID, target))
                GuildUnclaimResult.SUCCESS
            }
        }
        return result
    }

    /**
     * Handle the claim or unclaim of a chunk for the guild of the player.
     * Sends messages to the player based on the result.
     */
    open fun handleClaimUnclaim(player: Player, chunk: Chunk, claim: Boolean) {
        val guild = getGuildByMember(player.uniqueId)
        if (guild == null) {
            player.sendMessage(GuildChatError + NO_GUILD_MESSAGE)
            return
        }
        if (chunk.world.name != SrpWorld.GuildWorld.bukkitName) {
            player.sendMessage(GuildChatError + "Tu ne peux pas claim dans ce monde.")
            return
        }
        if (claim) {
            when (claimChunk(guild.id, player.uniqueId, chunk)) {
                GuildClaimResult.SUCCESS -> player.sendMessage(
                    GuildChatSuccess + "Le chunk (${chunk.x}, ${chunk.z}) a été annexé avec succès pour la guilde."
                )
                GuildClaimResult.ALREADY_OWNED -> player.sendMessage(
                    GuildChatFormat + "Ta guilde possède déjà ce chunk. Visualise les bordures avec " +
                            Component.text(GUILD_BORDER_COMMAND, NamedTextColor.GOLD) + "."
                )
                GuildClaimResult.ALREADY_CLAIMED -> player.sendMessage(
                    GuildChatError + "Ce chunk appartient à une autre guilde. " +
                            "Visualise les bordures de ta guilde avec " +
                            Component.text(GUILD_BORDER_COMMAND, NamedTextColor.GOLD) + "."
                )
                GuildClaimResult.NOT_ADJACENT -> player.sendMessage(
                    GuildChatError + "Tu dois sélectionner un chunk collé au territoire de ta guilde. " +
                            "Visualise les bordures avec " +
                            Component.text(GUILD_BORDER_COMMAND, NamedTextColor.GOLD) + "."
                )
                GuildClaimResult.NOT_AUTHORIZED -> player.sendMessage(
                    GuildChatError + "Tu n'es pas autorisé à annexer un chunk pour la guilde."
                )
            }
        } else {
            when (unclaimChunk(guild.id, player.uniqueId, chunk)) {
                GuildUnclaimResult.SUCCESS -> player.sendMessage(
                    GuildChatSuccess + "Le chunk (${chunk.x}, ${chunk.z}) a été retiré de ta guilde."
                )
                GuildUnclaimResult.ALREADY_CLAIMED -> player.sendMessage(
                    GuildChatError + "Ce chunk ne fait pas partie du territoire de ta guilde. " +
                            "Visualise les bordures avec " +
                            Component.text(GUILD_BORDER_COMMAND, NamedTextColor.GOLD) + "."
                )
                GuildUnclaimResult.LAST_CHUNK -> player.sendMessage(
                    GuildChatError + "Tu ne peux pas retirer le dernier chunk de ta guilde ! " +
                            "Si tu veux supprimer ta guilde, utilise " +
                            Component.text("/guild delete", NamedTextColor.GOLD) + "."
                )
                GuildUnclaimResult.SPAWNPOINT_CHUNK -> player.sendMessage(
                    GuildChatError + "Tu ne peux pas désannexer ce chunk, il contient le point de spawn de " +
                            "ta guilde. Déplace-le dans un autre chunk avec " +
                            Component.text("/guild setspawn", NamedTextColor.GOLD) + " avant de retirer celui-ci."
                )
                GuildUnclaimResult.NOT_AUTHORIZED -> player.sendMessage(
                    GuildChatError + "Tu n'as pas la permission OU ce retrait diviserait ta guilde " +
                            "en plusieurs parties. Visualise les bordures avec " +
                            Component.text(GUILD_BORDER_COMMAND, NamedTextColor.GOLD) + "."
                )
            }
        }
    }

    private fun xpToNextLevel(level: Int): Int = XP_PER_LVL_MULTIPLICATOR * level

    @Transactional
    open fun addGuildXp(guildId: Int, amount: Int) {
        val guild = getGuild(guildId)
        guild.addXp(amount.coerceAtLeast(0))
        guildRepository.save(guild)
        upgradeGuildLevel(guildId)
    }

    open fun setLevel(guildId: Int, level: Int) {
        val g = getGuild(guildId)
        g.level = level.coerceAtLeast(1)
        g.xp = 0
        guildRepository.save(g)
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
        val server = Bukkit.getServer() ?: return // ne pas enlever le return, Bukkit.getServer() peut être null en test
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

    companion object {
        const val XP_PER_LVL_MULTIPLICATOR: Int = 1000
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

enum class GuildSetSpawnResult { SUCCESS, NOT_AUTHORIZED, INVALID_WORLD, OUTSIDE_TERRITORY }
enum class InvitationResult { Invited, Joined, Failed }
enum class JoinResult { Joined, Requested, Failed }
enum class KickResult { Success, NotMember, NotAuthorized, CannotKickLeader }
enum class LeaveResult { Success, LeaderMustDelete }
enum class GuildClaimResult { SUCCESS, ALREADY_OWNED, ALREADY_CLAIMED, NOT_ADJACENT, NOT_AUTHORIZED }
enum class GuildUnclaimResult { SUCCESS, ALREADY_CLAIMED, NOT_AUTHORIZED, LAST_CHUNK, SPAWNPOINT_CHUNK }
enum class GuildUpgradeResult { SUCCESS, RANK_LOCKED, NOT_ENOUGH_MONEY, ALREADY_AT_OR_ABOVE }

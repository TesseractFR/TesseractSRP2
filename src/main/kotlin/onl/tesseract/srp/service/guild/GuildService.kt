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
import onl.tesseract.srp.domain.guild.*
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

private const val SPAWN_PROTECTION_RADIUS = 150
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
    open fun getGuildByChunk(chunkX: Int, chunkZ: Int) = guildRepository.findGuildByChunk(GuildChunk(chunkX, chunkZ))
    open fun getMemberRole(playerID: UUID): GuildRole? = guildRepository.findMemberRole(playerID)

    @Transactional
    open fun createGuild(playerID: UUID, location: Location, guildName: String): GuildCreationResult {
        val player = playerService.getPlayer(playerID)

        val errors = TerritoryClaimManager.performCreationChecks(
            location = location,
            player = player,
            policy = TerritoryClaimManager.CreationPolicy(
                isCorrectWorld = { it.world.name == SrpWorld.GuildWorld.bukkitName },
                spawnProtectionRadius = SPAWN_PROTECTION_RADIUS,
                protectionRadius = GUILD_PROTECTION_RADIUS,
                minMoney = GUILD_COST,
                minRank = PlayerRank.Baron
            ),
            alreadyHasTerritory = { guildRepository.findGuildByMember(playerID) != null },
            isNameTaken = { guildRepository.findGuildByName(guildName) != null },
            isChunkTaken = { cx, cz -> guildRepository.areChunksClaimed(listOf(GuildChunk(cx, cz))) }
        )

        if (errors.isNotEmpty()) {
            val mapped = errors.map {
                when (it) {
                    CreationError.ALREADY_HAS_TERRITORY      -> GuildCreationResult.Reason.PlayerHasGuild
                    CreationError.INVALID_WORLD              -> GuildCreationResult.Reason.InvalidWorld
                    CreationError.NEAR_SPAWN                 -> GuildCreationResult.Reason.NearSpawn
                    CreationError.NAME_TAKEN                 -> GuildCreationResult.Reason.NameTaken
                    CreationError.NOT_ENOUGH_MONEY           -> GuildCreationResult.Reason.NotEnoughMoney
                    CreationError.RANK_TOO_LOW               -> GuildCreationResult.Reason.Rank
                    CreationError.TOO_CLOSE_TO_OTHER_TERRITORY -> GuildCreationResult.Reason.NearGuild
                    CreationError.ON_OTHER_TERRITORY -> GuildCreationResult.Reason.OnOtherGuild
                }
            }
            return GuildCreationResult.failed(mapped)
        }
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

    @Transactional
    open fun deleteGuildAsStaff(guildId: Int): Boolean {
        guildRepository.deleteById(guildId)
        return true
    }

    enum class GuildSpawnKind { PRIVATE, VISITOR }

    open fun setSpawnpoint(
        guildID: Int,
        requesterID: UUID,
        newLocation: Location,
        kind: GuildSpawnKind
    ): GuildSetSpawnResult {
        val guild = getGuild(guildID)
        val res = TerritorySpawnManager.setSpawn(
            newLocation,
            policy = TerritorySpawnManager.SetSpawnPolicy(
                isCorrectWorld = { it.world.name == SrpWorld.GuildWorld.bukkitName },
                requireInsideTerritory = true
            ),
            io = TerritorySpawnManager.SetSpawnOperations(
                authorized = { guild.getMemberRole(requesterID) == GuildRole.Leader },
                isInsideTerritory = { loc -> guild.chunks.contains(GuildChunk(loc.chunk.x, loc.chunk.z)) },
                setAndPersist = { loc ->
                    val ok = when (kind) {
                        GuildSpawnKind.PRIVATE -> guild.setSpawnpoint(loc)
                        GuildSpawnKind.VISITOR -> guild.setVisitorSpawnpoint(loc)
                    }
                    if (ok) guildRepository.save(guild)
                    ok
                }
            )
        )
        return when (res) {
            TerritorySpawnManager.SetSpawnResult.SUCCESS           -> GuildSetSpawnResult.SUCCESS
            TerritorySpawnManager.SetSpawnResult.NOT_AUTHORIZED    -> GuildSetSpawnResult.NOT_AUTHORIZED
            TerritorySpawnManager.SetSpawnResult.INVALID_WORLD     -> GuildSetSpawnResult.INVALID_WORLD
            TerritorySpawnManager.SetSpawnResult.OUTSIDE_TERRITORY -> GuildSetSpawnResult.OUTSIDE_TERRITORY
        }
    }

    open fun getPrivateSpawn(guildId: Int): Location? = getGuild(guildId).spawnLocation
    open fun getVisitorSpawn(guildId: Int): Location? = getGuild(guildId).visitorSpawnLocation

    open fun canInteractInChunk(playerID: UUID, chunk: Chunk): InteractionAllowResult {
        val isGuildWorld = chunk.world.name == SrpWorld.GuildWorld.bukkitName
        if (!isGuildWorld) return InteractionAllowResult.Ignore

        val owner = guildRepository.findGuildByChunk(GuildChunk(chunk.x, chunk.z))
        val playerGuild = guildRepository.findGuildByMember(playerID)
        return when {
            owner == null                   -> InteractionAllowResult.Ignore
            playerGuild == null             -> InteractionAllowResult.Deny
            playerGuild.id == owner.id      -> InteractionAllowResult.Allow
            else                            -> InteractionAllowResult.Deny
        }
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

    /**
     * Claim a chunk for the guild, ensuring it is adjacent to existing chunks or is the first chunk.
     */
    open fun claimChunk(guildID: Int, requesterID: UUID, chunk: Chunk): GuildClaimResult {
        val guild = getGuild(guildID)
        val target = GuildChunk(chunk.x, chunk.z)

        val claimRes = TerritoryClaimManager.claim(
            owned = guild.chunks,
            target = target,
            policy = ClaimPolicy(
                requireAdjacent = true,
                allowFirstAnywhere = true,
                protectionRadius = GUILD_PROTECTION_RADIUS
            ),
            io = ClaimOperations(
                authorized = { guild.getMemberRole(requesterID) == GuildRole.Leader },
                takenElsewhere = { c -> guildRepository.areChunksClaimed(listOf(c)) },
                addAndPersist = { c ->
                    guild.addChunk(c)
                    guildRepository.save(guild)
                    eventService.callEvent(GuildChunkClaimEvent(requesterID, c))
                },
                coords = { c -> c.x to c.z },
                isTakenAt = { x, z ->
                    val otherGuild = guildRepository.findGuildByChunk(GuildChunk(x, z))
                    otherGuild != null && otherGuild.id != guildID
                }
            )
        )

        return when (claimRes) {
            ClaimResult.SUCCESS         -> GuildClaimResult.SUCCESS
            ClaimResult.ALREADY_OWNED   -> GuildClaimResult.ALREADY_OWNED
            ClaimResult.ALREADY_TAKEN   -> GuildClaimResult.ALREADY_CLAIMED
            ClaimResult.NOT_ADJACENT    -> GuildClaimResult.NOT_ADJACENT
            ClaimResult.NOT_ALLOWED     -> GuildClaimResult.NOT_AUTHORIZED
            ClaimResult.TOO_CLOSE       -> GuildClaimResult.TOO_CLOSE
        }
    }

    /**
     * Unclaim a chunk from the guild, ensuring it remains connected and does not remove the spawn chunk.
     */
    open fun unclaimChunk(guildID: Int, requesterID: UUID, chunk: Chunk): GuildUnclaimResult {
        val guild = getGuild(guildID)
        val target = GuildChunk(chunk.x, chunk.z)

        val unclaimRes = TerritoryClaimManager.unclaim(
            owned = guild.chunks,
            target = target,
            policy = ClaimPolicy(
                forbidLastRemoval = true,
                forbidSpawnRemoval = true,
                keepConnected = true
            ),
            io = UnclaimOperations(
                authorized = { guild.getMemberRole(requesterID) == GuildRole.Leader },
                isSpawnChunk = { c -> c == GuildChunk(guild.spawnLocation)
                        || c == guild.visitorSpawnLocation?.let { GuildChunk(it) }
                               },
                removeAndPersist = { c ->
                    guild.removeChunk(c)
                    guildRepository.save(guild)
                    eventService.callEvent(GuildChunkUnclaimEvent(requesterID, c))
                },
                coords = { c -> c.x to c.z }
            )
        )
        return when (unclaimRes) {
            UnclaimResult.SUCCESS         -> GuildUnclaimResult.SUCCESS
            UnclaimResult.NOT_OWNED       -> GuildUnclaimResult.ALREADY_CLAIMED
            UnclaimResult.NOT_ALLOWED     -> GuildUnclaimResult.NOT_AUTHORIZED
            UnclaimResult.LAST_CHUNK      -> GuildUnclaimResult.LAST_CHUNK
            UnclaimResult.IS_SPAWN_CHUNK  -> GuildUnclaimResult.SPAWNPOINT_CHUNK
        }
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

                GuildClaimResult.TOO_CLOSE -> player.sendMessage(
                    GuildChatError + "Tu ne peux pas annexer ce chunk, il est trop proche d'une autre guilde."
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
                    GuildChatError + "Tu ne peux pas désannexer ce chunk, il contient un point de spawn de " +
                            "ta guilde. Déplace-le dans un autre chunk avec " +
                            Component.text("/guild setspawn (private/visitor)", NamedTextColor.GOLD)
                            + " avant de retirer celui-ci."
                )
                GuildUnclaimResult.NOT_AUTHORIZED -> player.sendMessage(
                    GuildChatError + "Tu ne peux pas désannexer ce chunk, cela diviserait ta guilde " +
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

    companion object {
        const val XP_PER_LVL_MULTIPLICATOR: Int = 1000
    }

}

data class GuildCreationResult(val guild: Guild?, val reason: List<Reason>) {

    enum class Reason { NotEnoughMoney, InvalidWorld, NearSpawn, NearGuild, OnOtherGuild,
        NameTaken, PlayerHasGuild, Rank }

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
enum class GuildClaimResult { SUCCESS, ALREADY_OWNED, ALREADY_CLAIMED, NOT_ADJACENT, NOT_AUTHORIZED, TOO_CLOSE }
enum class GuildUnclaimResult { SUCCESS, ALREADY_CLAIMED, NOT_AUTHORIZED, LAST_CHUNK, SPAWNPOINT_CHUNK }
enum class GuildUpgradeResult { SUCCESS, RANK_LOCKED, NOT_ENOUGH_MONEY, ALREADY_AT_OR_ABOVE }
enum class StaffSetRoleResult {
    SUCCESS,
    SAME_ROLE,
    NEED_NEW_LEADER,
    NEW_LEADER_SAME_AS_TARGET
}

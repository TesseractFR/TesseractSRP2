package onl.tesseract.srp.controller.command.guild

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.CommandInstanceProvider
import onl.tesseract.commandBuilder.annotation.Argument
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.lib.chat.ChatEntryService
import onl.tesseract.lib.command.argument.PlayerArg
import onl.tesseract.lib.command.argument.StringArg
import onl.tesseract.lib.menu.MenuService
import onl.tesseract.lib.util.append
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.controller.command.argument.guild.GuildArg
import onl.tesseract.srp.controller.command.argument.guild.GuildMembersArg
import onl.tesseract.srp.controller.command.argument.guild.GuildSpawnKindArg
import onl.tesseract.srp.controller.menu.guild.GuildMenu
import onl.tesseract.srp.domain.territory.enum.result.BorderResult
import onl.tesseract.srp.domain.territory.enum.result.ClaimResult
import onl.tesseract.srp.domain.territory.enum.result.CreationResult
import onl.tesseract.srp.domain.territory.enum.result.KickResult
import onl.tesseract.srp.domain.territory.enum.result.LeaveResult
import onl.tesseract.srp.domain.territory.enum.result.SetSpawnResult
import onl.tesseract.srp.domain.territory.enum.result.UnclaimResult
import onl.tesseract.srp.domain.territory.guild.enum.GuildRole
import onl.tesseract.srp.domain.territory.guild.enum.GuildSpawnKind
import onl.tesseract.srp.domain.territory.guild.enum.GuildInvitationResult
import onl.tesseract.srp.mapper.toChunkCoord
import onl.tesseract.srp.mapper.toCoordinate
import onl.tesseract.srp.mapper.toLocation
import onl.tesseract.srp.repository.hibernate.guild.GuildRepository
import onl.tesseract.srp.service.TeleportationService
import onl.tesseract.srp.service.equipment.annexionStick.AnnexionStickService
import onl.tesseract.srp.service.territory.guild.*
import onl.tesseract.srp.util.*
import onl.tesseract.srp.util.equipment.annexionStick.GuildAnnexionStickInvocable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

val NO_GUILD_MESSAGE: Component = GuildChatError
        .plus("Tu ne possèdes pas de guilde. Rejoins ou crées-en une avec " )
        .append("/guild create <nom>", NamedTextColor.GOLD)
private const val GUILD_BORDER_COMMAND = "/guild border"
private val GUILD_BORDER_MESSAGE: Component =
    Component.text("Visualise les bordures avec ")
            .append(GUILD_BORDER_COMMAND, NamedTextColor.GOLD)
            .append(".")
private val NOT_IN_GUILD_WORLD_MESSAGE =
    GuildChatError + "Tu n'es pas dans le bon monde, cette commande n’est utilisable que dans le monde des guildes."

@Suppress("TooManyFunctions")
@Command(name = "guild")
@SpringComponent
class GuildCommand(
    provider: CommandInstanceProvider,
    private val guildService: GuildService,
    private val guildRepository: GuildRepository,
    private val guildBorderService: GuildBorderService,
    private val chatEntryService: ChatEntryService,
    private val menuService: MenuService,
    private val teleportService: TeleportationService,
    private val annexionStickService: AnnexionStickService,
) : CommandContext(provider) {
    @Command(name = "create", playerOnly = true, description = "Créer une nouvelle guilde")
    fun createGuild(sender: Player, @Argument("nom") nameArg: StringArg) {
        val result = guildService.createGuild(sender.uniqueId, sender.location.toCoordinate(), nameArg.get())
        val msg = when (result) {
            CreationResult.NOT_ENOUGH_MONEY ->
                GuildChatError + "Tu n'as pas assez d'argent pour créer ta guilde."

            CreationResult.INVALID_WORLD -> NOT_IN_GUILD_WORLD_MESSAGE

            CreationResult.NEAR_SPAWN ->
                GuildChatError + "Tu es trop proche du spawn pour créer ta guilde."

            CreationResult.TOO_CLOSE_TO_OTHER_TERRITORY ->
                GuildChatError + "Impossible de créer ta guilde ici, tu es trop proche d'une autre guilde."

            CreationResult.NAME_TAKEN ->
                GuildChatError + "Ce nom de guilde est déjà pris, choisis-en un autre."

            CreationResult.ALREADY_HAS_TERRITORY ->
                GuildChatError + "Tu es déjà dans une guilde. Quitte-la pour pouvoir en créer une nouvelle."

            CreationResult.RANK_TOO_LOW ->
                GuildChatError + "Tu n'as pas le grade minimal nécessaire pour créer une guilde (Baron)."

            CreationResult.ON_OTHER_TERRITORY -> {
                GuildChatError + "Tu ne peux pas créer une guilde ici, " +
                        "tu es sur un autre territoire."
            }

            CreationResult.SUCCESS -> GuildChatSuccess + "Nouvelle guilde créée sous le nom de ${nameArg.get()}"
        }
        sender.sendMessage(msg)
    }


    @Command(name = "delete", playerOnly = true, description = "Supprimer sa guilde.")
    fun deleteGuild(sender: Player) {
        val role = guildService.getMemberRole(sender.uniqueId)
                ?: return sender.sendMessage(NO_GUILD_MESSAGE)

        if (role != GuildRole.Leader)
            return sender.sendMessage(GuildChatError + "Tu n'as pas l'autorisation pour supprimer ta guilde.")
        menuService.openConfirmationMenu(
            sender,
            NamedTextColor.RED + "⚠ Es-tu sûr de vouloir supprimer ta guilde ?",
            null
        ) {
            val ok = guildService.deleteGuildAsLeader(sender.uniqueId)
            if (ok) {
                sender.sendMessage(GuildChatSuccess + "Ta guilde a été supprimée avec succès.")
            } else {
                sender.sendMessage(GuildChatError + "Suppression impossible.")
            }
        }
    }

    @Command(name = "menu", playerOnly = true, description = "Ouvrir le menu des guildes.")
    fun openMenu(sender: Player) {
        val guild = guildService.getGuildByLeader(sender.uniqueId)
        if (guild == null) {
            sender.sendMessage(NO_GUILD_MESSAGE)
            return
        }
        GuildMenu(sender.uniqueId, guildService, guildRepository, chatEntryService)
                .open(sender)
    }

    @Command(name = "invite", playerOnly = true, description = "Inviter un joueur dans sa guilde.")
    fun invite(sender: Player, @Argument("joueur") player: PlayerArg) {
        val target = player.get()
        when(guildService.invite(sender.uniqueId, target.uniqueId)){
            GuildInvitationResult.TERRITORY_NOT_FOUND -> sender.sendMessage(NO_GUILD_MESSAGE)
            GuildInvitationResult.NOT_ALLOWED ->
                sender.sendMessage(GuildChatError + "Tu n'es pas autorisé à utiliser cette commande.")
            GuildInvitationResult.SAME_PLAYER ->
                sender.sendMessage(GuildChatError + "Tu ne peux pas t'inviter toi même.")
            GuildInvitationResult.HAS_GUILD ->
                sender.sendMessage(GuildChatError + "Ce joueur est déjà dans une guilde.")
            GuildInvitationResult.SUCCESS_JOIN -> {
                val senderGuild = guildService.getGuildByMember(sender.uniqueId)!!
                sender.sendMessage(GuildChatSuccess + "${target.name} a rejoint votre guilde.")
                target.sendMessage(GuildChatSuccess + "Vous avez rejoint la guilde ${senderGuild.name}.")
            }
            GuildInvitationResult.SUCCESS_INVITE -> {
                sender.sendMessage(GuildChatFormat + "Votre invitation a bien été envoyée à ${target.name}")
            }
        }
    }

    @Command(name = "kick", playerOnly = true, description = "Exclure un membre de sa guilde.")
    fun kick(sender: Player, @Argument("joueur") targetName: GuildMembersArg) {
        val target = Bukkit.getOfflinePlayer(targetName.get())
        when (guildService.kickMember(sender.uniqueId, target.uniqueId)) {
            KickResult.TERRITORY_NOT_FOUND -> sender.sendMessage(NO_GUILD_MESSAGE)
            KickResult.NOT_MEMBER ->
                sender.sendMessage(GuildChatError + "${target.name} n'est pas membre de ta guilde.")
            KickResult.NOT_ALLOWED ->
                sender.sendMessage(GuildChatError + "Tu n'es pas autorisé à exclure des membres.")
            KickResult.CANNOT_KICK_LEADER ->
                sender.sendMessage(GuildChatError + "Tu ne peux pas exclure le chef de la guilde.")
            KickResult.SUCCESS -> menuService.openConfirmationMenu(
                sender,
                NamedTextColor.RED + "⚠ Es-tu sûr de vouloir exclure ${target.name} de la guilde ?",
                null
            ) {
                sender.sendMessage(GuildChatSuccess + "${target.name} a été exclu(e) de la guilde.")
            }
        }
    }


    @Command(name = "leave", playerOnly = true, description = "Quitter sa guilde.")
    fun leave(sender: Player) {
        when (guildService.leaveGuild(sender.uniqueId)) {
            LeaveResult.TERRITORY_NOT_FOUND -> sender.sendMessage(NO_GUILD_MESSAGE)
            LeaveResult.LEADER_MUST_DELETE -> {
                sender.sendMessage(GuildChatError + "Tu es le/la chef(fe) de la guilde. " +
                        "Supprime-la ou transfère le leadership.")
            }
            LeaveResult.SUCCESS -> {
                menuService.openConfirmationMenu(
                    sender,
                    NamedTextColor.RED + "⚠ Es-tu sûr de vouloir quitter la guilde ?",
                    null
                ) {
                    sender.sendMessage(GuildChatSuccess + "Tu as quitté ta guilde.")
                }
            }
        }
    }

    @Command(name = "claim", playerOnly = true, description = "Annexer un chunk pour la guilde.")
    fun claimChunk(sender: Player) {
        when (guildService.claimChunk(sender.uniqueId, sender.location.toChunkCoord())) {
            ClaimResult.TERRITORY_NOT_FOUND -> sender.sendMessage(NO_GUILD_MESSAGE)
            ClaimResult.SUCCESS -> sender.sendMessage(
                GuildChatSuccess + "Le chunk (${sender.chunk.x}, ${sender.chunk.z}) " +
                        "a été annexé avec succès pour la guilde."
            )

            ClaimResult.ALREADY_OWNED -> sender.sendMessage(
                GuildChatFormat + "Ta guilde possède déjà ce chunk. " + GUILD_BORDER_MESSAGE
            )

            ClaimResult.ALREADY_OTHER -> sender.sendMessage(
                GuildChatError + "Ce chunk appartient à une autre guilde. " + GUILD_BORDER_MESSAGE
            )

            ClaimResult.NOT_ADJACENT -> sender.sendMessage(
                GuildChatError + "Tu dois sélectionner un chunk collé au territoire de ta guilde. "
                        + GUILD_BORDER_MESSAGE
            )

            ClaimResult.NOT_ALLOWED -> sender.sendMessage(
                GuildChatError + "Tu n'es pas autorisé(e) à utiliser cette commande."
            )

            ClaimResult.TOO_CLOSE -> sender.sendMessage(
                GuildChatError + "Tu ne peux pas annexer ce chunk, il est trop proche d'une autre guilde."
            )

            ClaimResult.INVALID_WORLD -> sender.sendMessage(NOT_IN_GUILD_WORLD_MESSAGE)
        }
    }

    @Command(name = "unclaim", playerOnly = true, description = "Retirer un chunk de la guilde.")
    fun unclaimChunk(sender: Player) {
        when (guildService.unclaimChunk(sender.uniqueId, sender.location.toChunkCoord())) {
            UnclaimResult.SUCCESS -> sender.sendMessage(
                GuildChatSuccess + "Le chunk (${sender.chunk.x}, ${sender.chunk.z}) a été retiré de ta guilde."
            )

            UnclaimResult.NOT_OWNED -> sender.sendMessage(
                GuildChatError + "Ce chunk ne fait pas partie du territoire de ta guilde. "
            )

            UnclaimResult.LAST_CHUNK -> sender.sendMessage(
                GuildChatError + "Tu ne peux pas retirer le dernier chunk de ta guilde ! " +
                        "Si tu veux supprimer ta guilde, utilise " +
                        Component.text("/guild delete", NamedTextColor.GOLD) + "."
            )

            UnclaimResult.IS_SPAWN_CHUNK -> sender.sendMessage(
                GuildChatError + "Tu ne peux pas désannexer ce chunk, il contient un point de spawn de " +
                        "ta guilde. Déplace-le dans un autre chunk avec " +
                        Component.text("/guild setspawn (private/visitor)", NamedTextColor.GOLD) +
                        " avant de retirer celui-ci."
            )

            UnclaimResult.SPLIT -> sender.sendMessage(
                GuildChatError + "Tu ne peux pas désannexer ce chunk, cela diviserait ta guilde " +
                        "en plusieurs parties. " + GUILD_BORDER_MESSAGE
            )

            UnclaimResult.NOT_ALLOWED -> sender.sendMessage(GuildChatError +
                    "Tu n'es pas autorisé(e) à utiliser cette commande.")
            UnclaimResult.TERRITORY_NOT_FOUND -> sender.sendMessage(NO_GUILD_MESSAGE)
        }
    }

    @Command(name = "border", playerOnly = true, description = "Afficher/Masquer les bordures de ta guilde.")
    fun toggleGuildBorder(sender: Player) {
        val result = guildBorderService.toggleBorders(sender.uniqueId, sender.world.name)
        val msg = when (result) {
            BorderResult.SHOW_BORDERS -> CampementChatSuccess + "Les bordures de ta guilde sont maintenant visibles !"
            BorderResult.CLEAR_BORDERS -> CampementChatFormat + "Les bordures de ta guilde ont été masquées."
            BorderResult.TERRITORY_NOT_FOUND -> NO_GUILD_MESSAGE
            BorderResult.INVALID_WORLD -> NOT_IN_GUILD_WORLD_MESSAGE
        }
        sender.sendMessage(msg)
    }

    @Command(
        name = "setspawn",
        playerOnly = true,
        description = "Définir le spawn privé (défaut) ou visiteurs."
    )
    fun setGuildSpawn(
        sender: Player,
        @Argument("type", optional = true) kindArg: GuildSpawnKindArg?,
    ) {
        val kind = kindArg?.get() ?: GuildSpawnKind.PRIVATE
        when (guildService.setSpawnpoint(sender.uniqueId, sender.location.toCoordinate(), kind)) {
            SetSpawnResult.SUCCESS -> {
                val label = if (kind == GuildSpawnKind.PRIVATE) "privé" else "visiteurs"
                sender.sendMessage(GuildChatSuccess + "Le point de spawn $label de la guilde a été défini ici.")
            }

            SetSpawnResult.NOT_ALLOWED ->
                sender.sendMessage(GuildChatError + "Tu n'as pas l'autorisation de changer le spawn.")

            SetSpawnResult.OUTSIDE_TERRITORY ->
                sender.sendMessage(
                    GuildChatError + "Tu dois être dans un chunk de ta guilde pour définir le spawn. "
                            + GUILD_BORDER_MESSAGE
                )

            SetSpawnResult.TERRITORY_NOT_FOUND -> sender.sendMessage(NO_GUILD_MESSAGE)

        }
    }

    @Command(
        name = "spawn",
        playerOnly = true,
        description = "Se téléporter au spawn de sa guilde (sans argument) ou d’une autre guilde."
    )
    fun teleportToGuildSpawn(sender: Player, @Argument("guilde", optional = true) nameArg: GuildArg?) {
        val targetName = nameArg?.get()?.name
        val (guild, errorMsg) = if (targetName == null) {
            guildService.getGuildByMember(sender.uniqueId) to (NO_GUILD_MESSAGE)
        } else {
            guildRepository.findGuildByName(targetName) to (GuildChatError + "La guilde \"$targetName\" n’existe pas.")
        }
        val targetGuild = guild ?: run {
            sender.sendMessage(errorMsg); return
        }
        val destination = if (targetName == null) {
            guildService.getPrivateSpawn(targetGuild.id)
        } else {
            guildService.getVisitorSpawn(targetGuild.id) ?: guildService.getPrivateSpawn(targetGuild.id)
        }

        if (destination == null) {
            sender.sendMessage(GuildChatError + "Aucun spawn défini pour cette guilde.")
            return
        }
        teleportService.teleport(sender, destination.toLocation()) {
            val msg = if (targetName == null)
                GuildChatSuccess + "Tu as été téléporté au spawn de ta guilde."
            else
                GuildChatSuccess + "Tu as été téléporté au spawn de la guilde ${targetGuild.name}."
            sender.sendMessage(msg)
        }
    }

    @Command(name = "stick", description = "Recevoir un Bâton d'annexion de guilde.")
    fun giveGuildStick(sender: Player) {
        annexionStickService.giveStick(
            sender.uniqueId,
            GuildAnnexionStickInvocable::class,
            ::GuildAnnexionStickInvocable
        )
    }

}

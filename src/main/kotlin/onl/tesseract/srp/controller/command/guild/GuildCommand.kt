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
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.controller.command.argument.guild.GuildArg
import onl.tesseract.srp.controller.command.argument.guild.GuildMembersArg
import onl.tesseract.srp.controller.command.argument.guild.GuildSpawnKindArg
import onl.tesseract.srp.controller.menu.guild.GuildMenu
import onl.tesseract.srp.domain.territory.enum.ClaimResult
import onl.tesseract.srp.domain.territory.enum.CreationResult
import onl.tesseract.srp.domain.territory.enum.KickResult
import onl.tesseract.srp.domain.territory.enum.LeaveResult
import onl.tesseract.srp.domain.territory.enum.SetSpawnResult
import onl.tesseract.srp.domain.territory.enum.UnclaimResult
import onl.tesseract.srp.domain.territory.guild.enum.GuildRole
import onl.tesseract.srp.domain.territory.guild.enum.GuildSpawnKind
import onl.tesseract.srp.domain.territory.guild.enum.GuildInvitationResult
import onl.tesseract.srp.domain.world.SrpWorld
import onl.tesseract.srp.mapper.toChunkCoord
import onl.tesseract.srp.mapper.toCoordinate
import onl.tesseract.srp.mapper.toLocation
import onl.tesseract.srp.repository.hibernate.guild.GuildRepository
import onl.tesseract.srp.service.TeleportationService
import onl.tesseract.srp.service.territory.guild.*
import onl.tesseract.srp.util.GuildChatError
import onl.tesseract.srp.util.GuildChatFormat
import onl.tesseract.srp.util.GuildChatSuccess
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

val NO_GUILD_MESSAGE: Component =
    Component.text("Tu n'as pas de guilde. Rejoins-en une existante ou crées-en une nouvelle avec ") +
            Component.text("/guild create <nom>", NamedTextColor.GOLD) +
            Component.text(".")
private val GUILD_BORDER_MESSAGE: Component =
    Component.text("Visualise les bordures avec ")
            .append(Component.text(GUILD_BORDER_COMMAND, NamedTextColor.GOLD))
            .append(Component.text("."))
private val GUILD_WORLD = SrpWorld.GuildWorld.bukkitName
private val NOT_IN_GUILD_WORLD_MESSAGE =
    GuildChatError + "Tu n'es pas dans le bon monde, cette commande n’est utilisable que dans le monde des guildes."

@Command(name = "guild")
@SpringComponent
class GuildCommand(
    provider: CommandInstanceProvider,
    private val guildService: GuildService,
    private val guildRepository: GuildRepository,
    private val guildBorderRenderer: GuildBorderRenderer,
    private val chatEntryService: ChatEntryService,
    private val menuService: MenuService,
    private val teleportService: TeleportationService,
) : CommandContext(provider) {

    private inline fun inGuildWorld(sender: Player, block: () -> Unit) {
        if (sender.world.name != GUILD_WORLD) {
            sender.sendMessage(NOT_IN_GUILD_WORLD_MESSAGE)
            return
        }
        block()
    }

    @Command(name = "create", playerOnly = true, description = "Créer une nouvelle guilde")
    fun createGuild(sender: Player, @Argument("nom") nameArg: StringArg) = inGuildWorld(sender) {
        val result = guildService.createGuild(sender.uniqueId, sender.location.toCoordinate(), nameArg.get())
        val msg = when (result) {
            CreationResult.NOT_ENOUGH_MONEY ->
                GuildChatError + "Tu n'as pas assez d'argent pour créer ta guilde."

            CreationResult.INVALID_WORLD ->
                GuildChatError + "Tu ne peux pas créer de guilde dans ce monde."

            CreationResult.NEAR_SPAWN ->
                GuildChatError + "Tu es trop proche du spawn pour créer ta guilde."

            CreationResult.TOO_CLOSE_TO_OTHER_TERRITORY ->
                GuildChatError + "Tu es trop proche d'une autre guilde."

            CreationResult.NAME_TAKEN ->
                GuildChatError + "Ce nom de guilde est déjà pris, choisis-en un autre."

            CreationResult.ALREADY_HAS_TERRITORY ->
                GuildChatError + "Tu es déjà dans une guilde. Quitte-la pour pouvoir en créer une nouvelle."

            CreationResult.RANK_TOO_LOW ->
                GuildChatError + "Tu n'as pas le grade minimal nécessaire pour créer une guilde (Baron)."

            CreationResult.ON_OTHER_TERRITORY -> {
                sender.location.chunk
                val other = guildService.getGuildByChunk(sender.location.chunk)
                val guildName = other?.name ?: "une autre guilde"
                GuildChatError + "Tu ne peux pas créer une guilde ici, " +
                        "tu es sur le territoire de la guilde $guildName."
            }

            CreationResult.SUCCESS -> GuildChatSuccess + "Nouvelle guilde créée sous le nom de ${nameArg.get()}"
        }
        sender.sendMessage(msg)
    }


    @Command(name = "delete", playerOnly = true, description = "Supprimer sa guilde.")
    fun deleteGuild(sender: Player) {
        val role = guildService.getMemberRole(sender.uniqueId)
                ?: return sender.sendMessage(GuildChatError + NO_GUILD_MESSAGE)

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
            sender.sendMessage(GuildChatError + NO_GUILD_MESSAGE)
            return
        }
        GuildMenu(sender.uniqueId, guildService, guildRepository, chatEntryService)
                .open(sender)
    }

    @Command(name = "invite", playerOnly = true, description = "Inviter un joueur dans sa guilde.")
    fun invite(sender: Player, @Argument("joueur") player: PlayerArg) {
        val target = player.get()
        when(guildService.invite(sender.uniqueId, target.uniqueId)){
            GuildInvitationResult.TERRITORY_NOT_FOUND -> sender.sendMessage(Component.text("")
                    .append(GuildChatError)
                    .plus("Tu ne possèdes pas de guilde. Réjoins ou crées-en une avec " )
                    .append("/guild create <nom>", NamedTextColor.GOLD))
            GuildInvitationResult.SAME_PLAYER -> sender.sendMessage(GuildChatError + "Tu ne peux pas t'inviter toi même.")
            GuildInvitationResult.HAS_GUILD -> sender.sendMessage(GuildChatError + "Ce joueur est déjà dans une guilde.")
            GuildInvitationResult.NOT_ALLOWED -> sender.sendMessage(GuildChatError + "Tu n'es pas autorisé à utiliser cette commande.")
            GuildInvitationResult.SUCCESS_JOIN -> {
                val senderGuild = guildService.getGuildByMember(sender.uniqueId)!!
                sender.sendMessage(GuildChatSuccess + "${target.name} a rejoint votre guilde.")
                target.sendMessage(GuildChatSuccess + "Vous avez rejoint la guilde ${senderGuild.name}.")
            }
            GuildInvitationResult.SUCCESS_INVITE -> {
                val senderGuild = guildService.getGuildByMember(sender.uniqueId)!!
                sender.sendMessage(GuildChatSuccess + "Votre invitation a bien été envoyée à ${target.name}")
                target.sendMessage(GuildChatFormat + "${sender.name} vous invite dans la guilde ${senderGuild.name}.")
            }
        }
    }

    @Command(name = "kick", playerOnly = true, description = "Exclure un membre de sa guilde.")
    fun kick(sender: Player, @Argument("joueur") targetName: GuildMembersArg) {
        val role = guildService.getMemberRole(sender.uniqueId)
        if (role == null) {
            sender.sendMessage(GuildChatError + NO_GUILD_MESSAGE)
            return
        }
        if (role != GuildRole.Leader) {
            sender.sendMessage(GuildChatError + "Tu n'as pas la permission pour exclure quelqu'un de ta guilde.")
            return
        }
        val guild = guildService.getGuildByLeader(sender.uniqueId)!!
        val target = Bukkit.getOfflinePlayer(targetName.get())
        when {
            target.uniqueId == sender.uniqueId -> {
                sender.sendMessage(GuildChatError + "Tu ne peux pas t’exclure toi-même.")
            }

            guild.members.none { it.playerID == target.uniqueId } -> {
                sender.sendMessage(GuildChatError + "${target.name} n'est pas membre de ta guilde.")
            }

            else -> {
                menuService.openConfirmationMenu(
                    sender,
                    NamedTextColor.RED + "⚠ Es-tu sûr de vouloir exclure ${target.name} de la guilde ?",
                    null
                ) {
                    when (guildService.kickMember(guild.id, sender.uniqueId, target.uniqueId)) {
                        KickResult.Success -> {
                            sender.sendMessage(GuildChatSuccess + "${target.name} a été exclu(e) de la guilde.")
                        }

                        KickResult.NotMember ->
                            sender.sendMessage(GuildChatError + "${target.name} n'est pas membre de ta guilde.")

                        KickResult.NotAuthorized ->
                            sender.sendMessage(GuildChatError + "Tu n'es pas autorisé à exclure des membres.")

                        KickResult.CannotKickLeader ->
                            sender.sendMessage(GuildChatError + "Tu ne peux pas exclure le chef de la guilde.")
                    }
                }
            }
        }
    }

    @Command(name = "leave", playerOnly = true, description = "Quitter sa guilde.")
    fun leave(sender: Player) {
        val guild = guildService.getGuildByMember(sender.uniqueId)
                ?: return sender.sendMessage(GuildChatError + NO_GUILD_MESSAGE)

        if (guild.leaderId == sender.uniqueId) {
            sender.sendMessage(
                GuildChatError + "Tu es le chef de ta guilde. Supprime-la ou transfère le leadership pour partir.")
            return
        }
        menuService.openConfirmationMenu(
            sender,
            NamedTextColor.RED + "⚠ Es-tu sûr de vouloir quitter la guilde ?",
            null
        ) {
            when (guildService.leaveGuild(sender.uniqueId)) {
                LeaveResult.Success -> {
                    sender.sendMessage(GuildChatSuccess + "Tu as quitté la guilde ${guild.name}.")
                }

                LeaveResult.LeaderMustDelete -> {
                    sender.sendMessage(
                        GuildChatError + "Tu es le chef de la guilde. Supprime-la ou transfère le leadership.")
                }
            }
        }
    }

    @Command(name = "claim", playerOnly = true, description = "Annexer un chunk pour la guilde.")
    fun claimChunk(sender: Player) {
        when (guildService.claimChunk(sender.uniqueId, sender.location)) {
            ClaimResult.NOT_EXIST -> sender.sendMessage(GuildChatError + NO_GUILD_MESSAGE)
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
                GuildChatError + "Tu n'es pas autorisé à annexer un chunk pour la guilde."
            )

            ClaimResult.TOO_CLOSE -> sender.sendMessage(
                GuildChatError + "Tu ne peux pas annexer ce chunk, il est trop proche d'une autre guilde."
            )

            ClaimResult.INVALID_WORLD -> sender.sendMessage(NOT_IN_GUILD_WORLD_MESSAGE)
        }
    }

    @Command(name = "unclaim", playerOnly = true, description = "Retirer un chunk de la guilde.")
    fun unclaimChunk(sender: Player) {
        when (guildService.unclaimChunk(sender.uniqueId, sender.location)) {
            UnclaimResult.SUCCESS -> sender.sendMessage(
                GuildChatSuccess + "Le chunk (${sender.chunk.x}, ${sender.chunk.z}) a été retiré de ta guilde."
            )

            UnclaimResult.NOT_OWNED -> sender.sendMessage(
                GuildChatError + "Ce chunk ne fait pas partie du territoire de ta guilde. " + GUILD_BORDER_MESSAGE
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
            UnclaimResult.TERRITORY_NOT_FOUND -> sender.sendMessage(GuildChatError + NO_GUILD_MESSAGE)
        }
    }

    @Command(name = "border", playerOnly = true, description = "Afficher/Masquer les bordures de ta guilde.")
    fun toggleGuildBorder(sender: Player) = inGuildWorld(sender) {
        val guild = guildService.getGuildByMember(sender.uniqueId)
        if (guild == null) {
            sender.sendMessage(GuildChatError + NO_GUILD_MESSAGE)
            return
        }
        if (guildBorderRenderer.isShowingBorders(sender)) {
            guildBorderRenderer.clearBorders(sender)
            sender.sendMessage(GuildChatFormat + "Les bordures de ta guilde ont été masquées.")
        } else {
            guildBorderRenderer.showBorders(sender, guild.getChunks())
            sender.sendMessage(GuildChatSuccess + "Les bordures de ta guilde sont maintenant visibles !")
        }
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
        when (guildService.setSpawnpoint(sender.uniqueId, sender.location, kind)) {
            SetSpawnResult.SUCCESS -> {
                val label = if (kind == GuildSpawnKind.PRIVATE) "privé" else "visiteurs"
                sender.sendMessage(GuildChatSuccess + "Le point de spawn $label de la guilde a été défini ici.")
            }

            SetSpawnResult.NOT_AUTHORIZED ->
                sender.sendMessage(GuildChatError + "Tu n'as pas l'autorisation de changer le spawn.")

            SetSpawnResult.OUTSIDE_TERRITORY ->
                sender.sendMessage(
                    GuildChatError + "Tu dois être dans un chunk de ta guilde pour définir le spawn. "
                            + GUILD_BORDER_MESSAGE
                )

            SetSpawnResult.TERRITORY_NOT_FOUND -> sender.sendMessage(GuildChatError + NO_GUILD_MESSAGE)

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
            guildService.getGuildByMember(sender.uniqueId) to (GuildChatError + NO_GUILD_MESSAGE)
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
        teleportService.teleport(sender, destination) {
            val msg = if (targetName == null)
                GuildChatSuccess + "Tu as été téléporté au spawn de ta guilde."
            else
                GuildChatSuccess + "Tu as été téléporté au spawn de la guilde ${targetGuild.name}."
            sender.sendMessage(msg)
        }
    }

}

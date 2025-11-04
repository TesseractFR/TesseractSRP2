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
import onl.tesseract.srp.domain.guild.GuildRole
import onl.tesseract.srp.domain.world.SrpWorld
import onl.tesseract.srp.repository.hibernate.guild.GuildRepository
import onl.tesseract.srp.service.TeleportationService
import onl.tesseract.srp.service.guild.*
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
        val result = guildService.createGuild(sender.uniqueId, sender.location, nameArg.get())
        result.reason
            .map { reason ->
                when (reason) {
                    GuildCreationResult.Reason.NotEnoughMoney ->
                        GuildChatError + "Tu n'as pas assez d'argent pour créer ta guilde."
                    GuildCreationResult.Reason.InvalidWorld ->
                        GuildChatError + "Tu ne peux pas créer de guilde dans ce monde."
                    GuildCreationResult.Reason.NearSpawn ->
                        GuildChatError + "Tu es trop proche du spawn pour créer ta guilde."
                    GuildCreationResult.Reason.NearGuild ->
                        GuildChatError + "Tu es trop proche d'une autre guilde."
                    GuildCreationResult.Reason.NameTaken ->
                        GuildChatError + "Ce nom de guilde est déjà pris, choisis-en un autre."
                    GuildCreationResult.Reason.PlayerHasGuild ->
                        GuildChatError + "Tu es déjà dans une guilde. Quitte-la pour pouvoir en créer une nouvelle."
                    GuildCreationResult.Reason.Rank ->
                        GuildChatError + "Tu n'as pas le grade minimal nécessaire pour créer une guilde (Baron)."
                    GuildCreationResult.Reason.OnOtherGuild -> {
                        val chunk = sender.location.chunk
                        val other = guildService.getGuildByChunk(chunk.x, chunk.z)
                        val guildName = other?.name ?: "une autre guilde"
                        GuildChatError + "Tu ne peux pas créer une guilde ici, " +
                                "tu es sur le territoire de la guilde $guildName."
                    }
                }
            }
            .forEach { sender.sendMessage(it) }
        if (result.guild != null) {
            sender.sendMessage(GuildChatSuccess + "Nouvelle guilde créée sous le nom de ${nameArg.get()}")
        }
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
        guildService.handleGuildInvitation(sender, target)
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
    fun claimChunk(sender: Player) = inGuildWorld(sender) {
        guildService.handleClaimUnclaim(sender, sender.chunk, claim = true)
    }

    @Command(name = "unclaim", playerOnly = true, description = "Retirer un chunk de la guilde.")
    fun unclaimChunk(sender: Player) = inGuildWorld(sender) {
        guildService.handleClaimUnclaim(sender, sender.chunk, claim = false)
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
            guildBorderRenderer.showBorders(sender, guild.chunks)
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
        @Argument("type", optional = true) kindArg: GuildSpawnKindArg?
    ) = inGuildWorld(sender) {
        val guild = guildService.getGuildByMember(sender.uniqueId)
            ?: return sender.sendMessage(GuildChatError + NO_GUILD_MESSAGE)

        val kind = kindArg?.get() ?: GuildService.GuildSpawnKind.PRIVATE
        when (guildService.setSpawnpoint(guild.id, sender.uniqueId, sender.location, kind)) {
            GuildSetSpawnResult.SUCCESS -> {
                val label = if (kind == GuildService.GuildSpawnKind.PRIVATE) "privé" else "visiteurs"
                sender.sendMessage(GuildChatSuccess + "Le point de spawn $label de la guilde a été défini ici.")
            }
            GuildSetSpawnResult.NOT_AUTHORIZED ->
                sender.sendMessage(GuildChatError + "Tu n'as pas l'autorisation de changer le spawn.")
            GuildSetSpawnResult.INVALID_WORLD ->
                sender.sendMessage(GuildChatError + "Tu ne peux pas définir le spawn dans ce monde.")
            GuildSetSpawnResult.OUTSIDE_TERRITORY ->
                sender.sendMessage(
                    GuildChatError + "Tu dois être dans un chunk de ta guilde pour définir le spawn. " +
                            "Visualise les bordures avec " + Component.text("/guild border", NamedTextColor.GOLD) + "."
                )
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

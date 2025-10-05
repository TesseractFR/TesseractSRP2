package onl.tesseract.srp.controller.command.guild

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
import onl.tesseract.srp.controller.command.argument.GuildMembersArg
import onl.tesseract.srp.controller.menu.guild.GuildMenu
import onl.tesseract.srp.domain.guild.GuildRole
import onl.tesseract.srp.repository.hibernate.guild.GuildRepository
import onl.tesseract.srp.service.guild.*
import onl.tesseract.srp.util.GuildChatError
import onl.tesseract.srp.util.GuildChatSuccess
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

private val NO_GUILD_MESSAGE = "Tu n'as pas de guilde. Rejoins-en une existante ou crées-en une nouvelle."

@Command(name = "guild")
@SpringComponent
class GuildCommand(
    provider: CommandInstanceProvider,
    private val guildService: GuildService,
    private val guildRepository: GuildRepository,
    private val chatEntryService: ChatEntryService,
    private val menuService: MenuService,
) : CommandContext(provider) {

    @Command(name = "create", playerOnly = true, description = "Créer une nouvelle guilde")
    fun createGuild(sender: Player, @Argument("nom") nameArg: StringArg) {
        val (guild, errorReason) = guildService.createGuild(sender.uniqueId, sender.location, nameArg.get())

        errorReason.map { reason ->
            return@map when (reason) {
                GuildCreationResult.Reason.NotEnoughMoney ->
                    GuildChatError + "Tu n'as pas assez d'argent pour créer ta guilde."

                GuildCreationResult.Reason.InvalidWorld ->
                    GuildChatError + "Tu ne peux pas créer de guilde dans ce monde."

                GuildCreationResult.Reason.NearSpawn ->
                    GuildChatError + "Tu es trop proche du spawn pour créer ta guilde."

                GuildCreationResult.Reason.NearGuild -> GuildChatError + "Tu es trop proche d'une autre guilde."
                GuildCreationResult.Reason.NameTaken ->
                    GuildChatError + "Ce nom de guilde est déjà pris, choisis-en un autre."

                GuildCreationResult.Reason.PlayerHasGuild ->
                    GuildChatError + "Tu es déjà dans une guilde. Quitte-la pour pouvoir en créer une nouvelle."
                GuildCreationResult.Reason.Rank ->
                    GuildChatError + "Tu n'as pas le grade minimal nécessaire pour créer une guilde (Baron)."
            }
        }.forEach { message -> sender.sendMessage(message) }

        if (guild != null) {
            sender.sendMessage(GuildChatSuccess + "Nouvelle guilde créée sous le nom de " + nameArg.get())
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
            ?: return sender.sendMessage(GuildChatError + NO_GUILD_MESSAGE)

        if (role != GuildRole.Leader)
            return sender.sendMessage(GuildChatError + "Tu n'as pas la permission pour exclure quelqu'un de ta guilde.")

        val guild = guildService.getGuildByLeader(sender.uniqueId)!!
        val target = Bukkit.getOfflinePlayer(targetName.get())
        if (target.uniqueId == sender.uniqueId) {
            sender.sendMessage(GuildChatError + "Tu ne peux pas t’exclure toi-même.")
            return
        }
        if (guild.members.none { it.playerID == target.uniqueId }) {
            sender.sendMessage(GuildChatError + "${target.name} n'est pas membre de ta guilde.")
            return
        }
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

    @Command(name = "leave", playerOnly = true, description = "Quitter sa guilde.")
    fun leave(sender: Player) {
        val guild = guildService.getGuildByMember(sender.uniqueId)
            ?: return sender.sendMessage(GuildChatError + "Tu n'as pas de guilde.")

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

}

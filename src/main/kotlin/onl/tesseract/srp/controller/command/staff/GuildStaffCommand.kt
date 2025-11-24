package onl.tesseract.srp.controller.command.staff

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.commandBuilder.annotation.Argument
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.lib.command.argument.IntegerCommandArgument
import onl.tesseract.lib.command.argument.PlayerArg
import onl.tesseract.lib.menu.MenuService
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.controller.command.argument.guild.GuildArg
import onl.tesseract.srp.controller.command.argument.guild.GuildMembersRoleArg
import onl.tesseract.srp.controller.command.argument.guild.GuildMembersArg
import onl.tesseract.srp.controller.command.argument.guild.GuildRankArg
import onl.tesseract.srp.domain.commun.enum.StaffSetRoleResult
import onl.tesseract.srp.domain.territory.enum.KickResult
import onl.tesseract.srp.service.territory.guild.GuildService
import onl.tesseract.srp.util.*
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "guild")
class GuildStaffCommand(
    private val guildService: GuildService,
    private val menuService: MenuService
) {
    @Command(name = "delete", description = "Supprimer une guilde")
    fun deleteGuild(
        @Argument("guild") guildArg: GuildArg,
        sender: Player
    ) {
        menuService.openConfirmationMenu(
            sender,
            NamedTextColor.RED + "⚠ Es-tu sûr de vouloir supprimer la guilde ${guildArg.get().name} ?",
            null
        ) {
            val ok = guildService.deleteGuildAsStaff(guildArg.get().id)
            if (ok) {
                sender.sendMessage(StaffChatSuccess + "La guilde ${guildArg.get().name} a été supprimée avec succès.")
            } else {
                sender.sendMessage(StaffChatError + "Suppression impossible.")
            }
        }
    }

    @Command(name = "members", description = "Gérer les membres d'une guilde")
    @Component
    class MembersCommand(private val guildService: GuildService) {
        @Command(name = "get", description = "Voir les membres d'une guilde")
        fun getMembers(
            @Argument("guild") guildArg: GuildArg,
            sender: Player
        ) {
            val guild = guildArg.get()
            sender.sendMessage(StaffChatFormat + "Membres de la guilde ${guild.name} :")
            guild.members.forEach { member ->
                val playerName = Bukkit.getOfflinePlayer(member.playerID).name ?: member.playerID.toString()
                sender.sendMessage(StaffChatFormat + "- $playerName : ${member.role}")
            }
        }

        @Command(name = "add", description = "Ajouter un membre à une guilde")
        fun addMember(
            @Argument("guild") guildArg: GuildArg,
            @Argument("playerName") playerArg: PlayerArg,
            sender: CommandSender
        ) {
            val player = playerArg.get()
            if (player == guildArg.get().members.map { Bukkit.getOfflinePlayer(it.playerID) }
                    .firstOrNull { it.uniqueId == player.uniqueId }) {
                sender.sendMessage(StaffChatError + "Le joueur ${player.name} fait déjà partie de la guilde.")
                return
            }
            guildService.addMemberAsStaff(guildArg.get().id, player.uniqueId)
            sender.sendMessage(
                StaffChatSuccess
                        + "Opération effectuée - ${playerArg.get().name} ajouté à la guilde ${guildArg.get().name}")
        }

        @Command(name = "kick", description = "Expulser un joueur d'une guilde")
        fun kickMember(
            @Argument("guild") guildArg: GuildArg,
            @Argument("playerName") playerArg: GuildMembersArg,
            sender: CommandSender
        ) {
            val guild = guildArg.get()
            val player = Bukkit.getOfflinePlayer(playerArg.get())
            val result = guildService.kickMember(guild.leaderId, player.uniqueId)
            when (result) {
                KickResult.TERRITORY_NOT_FOUND ->
                    sender.sendMessage(StaffChatError + "Le joueur ${player.name} ne fait partie d'aucune guilde.")
                KickResult.NOT_MEMBER ->
                    sender.sendMessage(StaffChatError + "Le joueur ${player.name} ne fait pas partie de la guilde.")
                KickResult.CANNOT_KICK_LEADER ->
                    sender.sendMessage(StaffChatError + "Impossible d'expulser le leader de la guilde. Change son rôle.")
                KickResult.NOT_ALLOWED ->
                    sender.sendMessage(StaffChatError + "Tu n'es pas autorisé à expulser ce joueur.")
                KickResult.SUCCESS ->
                    sender.sendMessage(StaffChatSuccess + "Opération effectuée - ${player.name} expulsé de la guilde ${guild.name}")
            }
        }

        @Command(
            name = "setRole",
            description = "Définir le rôle d'un joueur dans une guilde. " +
                    "Si on rétrograde le leader actuel, indiquer [newLeader]."
        )
        fun setRole(
            sender: CommandSender,
            @Argument("guild") guildArg: GuildArg,
            @Argument("player") playerArg: GuildMembersArg,
            @Argument("role") roleArg: GuildMembersRoleArg,
            @Argument("newLeader", optional = true) newLeaderArg: GuildMembersArg?
        ) {
            val guild = guildArg.get()
            val playerName = playerArg.get()
            val role = roleArg.get()

            val target = guild.members
                .map { Bukkit.getOfflinePlayer(it.playerID) }
                .firstOrNull {
                    val name = it.name
                    name != null && name.equals(playerName, true) || it.uniqueId.toString() == playerName
                } ?: run {
                    sender.sendMessage(StaffChatError + "Joueur \"$playerName\" introuvable.")
                    return
                }

            val oldLeader = Bukkit.getOfflinePlayer(guild.leaderId)
            val replacementLeader = newLeaderArg?.get()?.let { Bukkit.getOfflinePlayer(it) }

            val result = guildService.setMemberRoleAsStaff(
                guildID = guild.id,
                targetID = target.uniqueId,
                newRole = role,
                replacementLeaderID = replacementLeader?.uniqueId
            )

            when (result) {
                StaffSetRoleResult.SUCCESS -> {
                    if (role.name.equals("Leader", true)) {
                        sender.sendMessage(
                            StaffChatSuccess + "Leader de la guilde ${guild.name} défini sur " +
                                    "${target.name}. "
                                    + "L'ancien leader ${oldLeader.name} a été passé Citoyen."
                        )
                    } else {
                        if (replacementLeader != null) {
                            sender.sendMessage(
                                StaffChatSuccess + "Rôle de ${target.name} défini à ${role.name} " +
                                        "et ${replacementLeader.name} à Leader pour la guilde ${guild.name}."
                            )
                        }
                    }
                }
                StaffSetRoleResult.NEED_NEW_LEADER -> {
                    sender.sendMessage(
                        StaffChatError + "La cible est le leader actuel. Indique un nouveau leader : " +
                                "/staffrole set ${guild.name} $playerName ${role.name} <newLeader>"
                    )
                }
                StaffSetRoleResult.NEW_LEADER_SAME_AS_TARGET -> {
                    sender.sendMessage(StaffChatError +
                            "Le nouveau leader ne peut pas être le même joueur que la cible.")
                }
                StaffSetRoleResult.SAME_ROLE -> {
                    sender.sendMessage(StaffChatError +
                            "${target.name ?: target.uniqueId} a déjà le rôle ${role.name} " +
                            "dans la guilde ${guild.name}.")
                }
            }
        }
    }

    @Command(name = "money", description = "Gérer l'argent d'une guilde")
    @Component
    class MoneyCommand(private val guildService: GuildService) {

        @Command(name = "give")
        fun giveMoney(
            @Argument("guild") guildArg: GuildArg,
            @Argument("amount") amountArg: IntegerCommandArgument,
            sender: CommandSender
        ) {
            guildService.giveMoneyAsStaff(guildArg.get().id, amountArg.get())
            sender.sendMessage(StaffChatSuccess + "Opération effectuée")
        }

        @Command(name = "take")
        fun takeMoney(
            @Argument("guild") guildArg: GuildArg,
            @Argument("amount") amountArg: IntegerCommandArgument,
            sender: CommandSender
        ) {
            guildService.giveMoneyAsStaff(guildArg.get().id, -amountArg.get())
            sender.sendMessage(StaffChatSuccess + "Opération effectuée")
        }

        @Command(name = "get")
        fun getMoney(@Argument("guild") guildArg: GuildArg, sender: CommandSender) {
            val money = guildArg.get().money
            val name = guildArg.get().name
            sender.sendMessage(StaffChatFormat + "$name : $money Lys")
        }
    }

    @Command(name = "level", description = "Gérer le niveau d'une guilde")
    @Component
    class LevelCommand(private val guildService: GuildService) {
        @Command(name = "set")
        fun setLevel(
            @Argument("guild") guildArg: GuildArg,
            @Argument("level") levelArg: IntegerCommandArgument,
            sender: CommandSender
        ) {
            guildService.setLevel(guildArg.get().id, levelArg.get())
            sender.sendMessage(
                StaffChatSuccess + "Opération effectuée - niveau de ${guildArg.get().name} défini à ${levelArg.get()}"
            )
        }

        @Command(name = "get")
        fun getLevel(@Argument("guild") guildArg: GuildArg, sender: CommandSender) {
            val level = guildArg.get().level
            val name = guildArg.get().name
            sender.sendMessage(StaffChatFormat +"$name : niveau $level")
        }

        @Command(name = "addLevel")
        fun addLevel(
            @Argument("guild") guildArg: GuildArg,
            @Argument("amount") levelArg: IntegerCommandArgument,
            sender: CommandSender
        ) {
            guildService.addLevel(guildArg.get().id, levelArg.get())
            sender.sendMessage(
                StaffChatSuccess + "Opération effectuée - ${levelArg.get()} niveaux ajoutés à ${guildArg.get().name}"
            )
        }

        @Command(name = "addXp")
        fun addXp(
            @Argument("guild") guildArg: GuildArg,
            @Argument("amount") xpArg: IntegerCommandArgument,
            sender: CommandSender
        ) {
            guildService.addGuildXp(guildArg.get().id, xpArg.get())
            sender.sendMessage(
                StaffChatSuccess + "Opération effectuée - ${xpArg.get()} XP ajoutés à ${guildArg.get().name}"
            )
        }
    }

    @Command(name = "rank", description = "Gérer le rang d'une guilde")
    @Component
    class RankCommand(private val guildService: GuildService) {
        @Command(name = "set")
        fun setRank(
            @Argument("guild") guildArg: GuildArg,
            @Argument("rank") rankArg: GuildRankArg,
            sender: CommandSender
        ) {
            guildService.setRank(guildArg.get().id, rankArg.get())
            sender.sendMessage(
                StaffChatSuccess +
                        "Opération effectuée - rang de la guilde ${guildArg.get().name} défini à ${rankArg.get()}")
        }

        @Command(name = "get")
        fun getRank(@Argument("guild") guildArg: GuildArg, sender: CommandSender) {
            val rank = guildArg.get().rank
            val name = guildArg.get().name
            sender.sendMessage(StaffChatFormat +"$name : rang $rank")
        }
    }

}

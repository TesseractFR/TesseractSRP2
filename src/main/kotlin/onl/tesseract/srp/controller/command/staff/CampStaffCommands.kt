package onl.tesseract.srp.controller.command.staff

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.commandBuilder.annotation.Argument
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.Perm
import onl.tesseract.lib.command.argument.PlayerArg
import onl.tesseract.lib.menu.MenuService
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.controller.command.argument.CampOwnerArg
import onl.tesseract.srp.controller.command.argument.TrustedPlayerArg
import onl.tesseract.srp.domain.territory.enum.CreationResult
import onl.tesseract.srp.domain.territory.enum.TrustResult
import onl.tesseract.srp.domain.territory.enum.UntrustResult
import onl.tesseract.srp.mapper.toCoordinate
import onl.tesseract.srp.service.territory.campement.CampementBorderService
import onl.tesseract.srp.service.territory.campement.CampementService
import onl.tesseract.srp.util.CampementChatError
import onl.tesseract.srp.util.CampementChatFormat
import onl.tesseract.srp.util.CampementChatSuccess
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

private val NO_CAMPEMENT_MESSAGE: Component = CampementChatError +"Ce joueur ne possède pas de campement."

@SpringComponent
@Command(name = "camp", permission = Perm("staff"), playerOnly = true)
class CampStaffCommands(
    private val campementService: CampementService,
    private val campementBorderService: CampementBorderService,
    private var menuService: MenuService,
) {

    @Command(name = "create", description = "Créer un campement pour un joueur")
    fun createCamp(sender: Player, @Argument("joueur") targetName: PlayerArg) {
        val target = targetName.get()
        val uuid = target.uniqueId
        val location = target.location
        val chunk = location.chunk
        val chunkKey = "${chunk.x},${chunk.z}"
        val result = campementService.createCampement(uuid, location.toCoordinate())
        sender.sendMessage(
        when (result) {
            CreationResult.ALREADY_HAS_TERRITORY -> CampementChatError + "${target.name} possède déjà un campement."
            CreationResult.INVALID_WORLD -> CampementChatError + "Tu ne peux pas créer de campement dans ce monde."
            CreationResult.NEAR_SPAWN -> CampementChatError + "Trop proche du spawn pour créer un campement."
            CreationResult.NAME_TAKEN,
            CreationResult.NOT_ENOUGH_MONEY,
            CreationResult.RANK_TOO_LOW -> return
            CreationResult.TOO_CLOSE_TO_OTHER_TERRITORY -> CampementChatError + "Un autre territoire est trop proche d’ici."
            CreationResult.ON_OTHER_TERRITORY -> {
                CampementChatError + "Impossible de créer un campement ici, " +
                        "tu es sur un autre territoire."
            }
            CreationResult.SUCCESS ->
                CampementChatSuccess + "Campement créé pour ${target.name} dans le chunk $chunkKey."
        })
        if (result == CreationResult.SUCCESS) {
            target.sendMessage(
                CampementChatSuccess + "Un administrateur t'a créé un campement dans le chunk $chunkKey."
            )
        }
    }

    @Command(name = "delete", description = "Supprimer le campement d'un joueur")
    fun deleteCamp(sender: Player, @Argument("joueur") ownerName: CampOwnerArg) {
        val owner = Bukkit.getOfflinePlayer(ownerName.get())
        val campement = campementService.getCampementByOwner(sender.uniqueId)
            ?: return sender.sendMessage(NO_CAMPEMENT_MESSAGE)

        menuService.openConfirmationMenu(
            sender,
            NamedTextColor.RED + "⚠ Es-tu sûr de vouloir supprimer le campement de ${owner.name} ?",
            null
        ) {
            campementService.deleteCampement(campement.ownerID)
            owner.player?.let { campementBorderService.clearBorders(it.uniqueId) }

            sender.sendMessage(CampementChatSuccess + "Le campement de ${owner.name} a été supprimé avec succès.")
            owner.player?.sendMessage(CampementChatError + "Ton campement a été supprimé par un administrateur.")
        }
    }

    @Command(name = "trust", description = "Ajouter un membre (target) dans le campement d'un joueur (owner)")
    fun trustPlayer(sender: CommandSender,
                    @Argument("owner") ownerName: CampOwnerArg,
                    @Argument("target") targetName: PlayerArg) {
        val owner = Bukkit.getOfflinePlayer(ownerName.get())
        val target = targetName.get()

        if (owner.uniqueId == target.uniqueId) {
            sender.sendMessage(CampementChatFormat +
                    "Impossible d'ajouter le propriétaire lui-même en tant que joueur de confiance.")
            return
        }

        val success = campementService.trust(owner.uniqueId, target.uniqueId)
        when (success) {
            TrustResult.NOT_ALLOWED -> return
            TrustResult.SUCCESS -> {
                sender.sendMessage(CampementChatSuccess + "${target.name} a été ajouté dans le campement de ${owner.name}.")
                target.sendMessage(CampementChatSuccess + "Tu as été ajouté au campement de ${owner.name} " +
                        "en tant que joueur de confiance.")
            }
            TrustResult.ALREADY_TRUST -> sender.sendMessage(CampementChatFormat + "${target.name} est déjà dans la liste de confiance.")
            TrustResult.TERRITORY_NOT_FOUND -> sender.sendMessage(NO_CAMPEMENT_MESSAGE)
        }
    }


    @Command(name = "untrust", description = "Retirer un joueur de confiance (target) du campement d'un joueur (owner)")
    fun untrustPlayer(sender: CommandSender,
                      @Argument("owner") ownerName: CampOwnerArg,
                      @Argument("target") targetName: TrustedPlayerArg) {
        val owner = Bukkit.getOfflinePlayer(ownerName.get())
        val target = Bukkit.getOfflinePlayer(targetName.get())

        campementService.getCampementByOwner(owner.uniqueId) ?: run {
            sender.sendMessage(CampementChatError + "${owner.name} ne possède pas de campement.")
            return
        }

        val success = campementService.untrust(owner.uniqueId, target.uniqueId)
        when(success){
            UntrustResult.NOT_ALLOWED -> return
            UntrustResult.NOT_TRUST -> sender.sendMessage(CampementChatError + "Ce joueur n'est pas dans la liste de confiance de ce campement.")
            UntrustResult.SUCCESS -> sender.sendMessage(CampementChatSuccess + "${target.name} a été retiré du campement de ${owner.name}.")
            UntrustResult.TERRITORY_NOT_FOUND -> sender.sendMessage(NO_CAMPEMENT_MESSAGE)
        }
    }

    @Command(name = "getTrustedPlayers", description = "Obtenir la liste des joueurs de confiance d'un campement")
    fun getTrustedPlayers(sender: CommandSender, @Argument("owner") ownerName: CampOwnerArg) {
        val owner = Bukkit.getOfflinePlayer(ownerName.get())
        val campement = campementService.getCampementByOwner(owner.uniqueId) ?: run {
            sender.sendMessage(CampementChatFormat + "${owner.name} ne possède pas de campement.")
            return
        }

        val trusted = campement.getTrusted().mapNotNull {
            Bukkit.getOfflinePlayer(it).name
        }

        if (trusted.isEmpty()) {
            sender.sendMessage(CampementChatFormat + "Aucun joueur de confiance pour le campement de ${owner.name}.")
        } else {
            sender.sendMessage(CampementChatFormat +
                    "Joueurs de confiance du campement de ${owner.name} : ${trusted.joinToString(", ")}")
        }
    }
}

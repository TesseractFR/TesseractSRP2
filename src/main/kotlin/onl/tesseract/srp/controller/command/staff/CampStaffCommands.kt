package onl.tesseract.srp.controller.command.staff

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.commandBuilder.annotation.*
import onl.tesseract.lib.command.argument.PlayerArg
import onl.tesseract.lib.menu.MenuService
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.controller.command.argument.CampOwnerArg
import onl.tesseract.srp.controller.command.argument.TrustedPlayerArg
import onl.tesseract.srp.service.campement.CampementBorderRenderer
import onl.tesseract.srp.service.campement.CampementCreationResult
import onl.tesseract.srp.service.campement.CampementService
import onl.tesseract.srp.util.CampementChatError
import onl.tesseract.srp.util.CampementChatFormat
import onl.tesseract.srp.util.CampementChatSuccess
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "camp", permission = Perm("staff"), playerOnly = true)
class CampStaffCommands(
    private val campementService: CampementService,
    private val borderRenderer: CampementBorderRenderer,
    private var menuService: MenuService,
) {

    @Command(name = "create", description = "Créer un campement pour un joueur")
    fun createCamp(sender: Player, @Argument("joueur") targetName: PlayerArg) {
        val target = targetName.get()
        val uuid = target.uniqueId
        val location = target.location
        val chunk = location.chunk
        val chunkKey = "${chunk.x},${chunk.z}"
        val result = campementService.createCampement(uuid, location)

        result.reason
            .map { reason ->
                when (reason) {
                    CampementCreationResult.Reason.InvalidWorld ->
                        CampementChatError + "Tu ne peux pas créer de campement dans ce monde."
                    CampementCreationResult.Reason.NearSpawn ->
                        CampementChatError + "Trop proche du spawn pour créer un campement."
                    CampementCreationResult.Reason.NearCampement ->
                        CampementChatError + "Un autre campement est trop proche d’ici."
                    CampementCreationResult.Reason.AlreadyHasCampement ->
                        CampementChatError + "${target.name} possède déjà un campement."
                    CampementCreationResult.Reason.OnOtherCampement -> {
                        val other = campementService.getCampementByChunk(chunk.x, chunk.z)
                        val ownerName = other?.ownerID?.let { Bukkit.getOfflinePlayer(it).name } ?: "un autre joueur"
                        CampementChatError + "Impossible de créer un campement ici, " +
                                "tu es sur le territoire de $ownerName."
                    }
                    CampementCreationResult.Reason.Ignored -> return
                }
            }
            .forEach { sender.sendMessage(it) }

        if (result.campement != null) {
            sender.sendMessage(
                CampementChatSuccess + "Campement créé pour ${target.name} dans le chunk $chunkKey."
            )
            target.sendMessage(
                CampementChatSuccess + "Un administrateur t'a créé un campement dans le chunk $chunkKey."
            )
        } else if (result.reason.isEmpty()) {
            sender.sendMessage(CampementChatError + "Échec de création du campement dans le chunk $chunkKey.")
        }
    }

    @Command(name = "delete", description = "Supprimer le campement d'un joueur")
    fun deleteCamp(sender: Player, @Argument("joueur") ownerName: CampOwnerArg) {
        val owner = Bukkit.getOfflinePlayer(ownerName.get())
        val campement = campementService.getCampementByOwner(owner.uniqueId)

        if (campement == null) {
            sender.sendMessage(CampementChatError + "${owner.name} ne possède pas de campement.")
            return
        }

        menuService.openConfirmationMenu(
            sender,
            NamedTextColor.RED + "⚠ Es-tu sûr de vouloir supprimer le campement de ${owner.name} ?",
            null
        ) {
            campementService.deleteCampement(campement.ownerID)
            owner.player?.let { borderRenderer.clearBorders(it) }

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

        val ownerUUID = owner.uniqueId
        val targetUUID = target.uniqueId

        if (ownerUUID == targetUUID) {
            sender.sendMessage(CampementChatFormat +
                    "Impossible d'ajouter le propriétaire lui-même en tant que joueur de confiance.")
            return
        }

        val success = campementService.trustPlayer(ownerUUID, targetUUID)
        if (success) {
            sender.sendMessage(CampementChatSuccess + "${target.name} a été ajouté dans le campement de ${owner.name}.")
            target.sendMessage(CampementChatSuccess + "Tu as été ajouté au campement de ${owner.name} " +
                    "en tant que joueur de confiance.")
        } else {
            sender.sendMessage(CampementChatFormat + "${target.name} est déjà dans la liste de confiance.")
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

        val success = campementService.untrustPlayer(owner.uniqueId, target.uniqueId)
        if (success) {
            sender.sendMessage(CampementChatSuccess + "${target.name} a été retiré du campement de ${owner.name}.")
        } else {
            sender.sendMessage(CampementChatError + "Erreur lors du retrait de ${target.name}.")
        }
    }

    @Command(name = "getTrustedPlayers", description = "Obtenir la liste des joueurs de confiance d'un campement")
    fun getTrustedPlayers(sender: CommandSender, @Argument("owner") ownerName: CampOwnerArg) {
        val owner = Bukkit.getOfflinePlayer(ownerName.get())
        val campement = campementService.getCampementByOwner(owner.uniqueId) ?: run {
            sender.sendMessage(CampementChatFormat + "${owner.name} ne possède pas de campement.")
            return
        }

        val trusted = campement.trustedPlayers.mapNotNull {
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

package onl.tesseract.srp.controller.command.campement

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.annotation.Argument
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.lib.command.argument.PlayerArg
import onl.tesseract.lib.equipment.EquipmentMenu
import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.menu.MenuService
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.SrpCommandInstanceProvider
import onl.tesseract.srp.controller.command.argument.CampOwnerArg
import onl.tesseract.srp.controller.command.argument.TrustedPlayerArg
import onl.tesseract.srp.domain.campement.AnnexionStickInvocable
import onl.tesseract.srp.service.TeleportationService
import onl.tesseract.srp.service.campement.CampementBorderRenderer
import onl.tesseract.srp.service.campement.CampementCreationResult
import onl.tesseract.srp.service.campement.CampementService
import onl.tesseract.srp.util.CampementChatError
import onl.tesseract.srp.util.CampementChatFormat
import onl.tesseract.srp.util.CampementChatSuccess
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "campement", playerOnly = true)
class CampementCommands(
    private var campementService: CampementService,
    private var borderRenderer: CampementBorderRenderer,
    private var equipmentService: EquipmentService,
    private var menuService: MenuService,
    private var teleportService: TeleportationService,
    commandInstanceProvider: SrpCommandInstanceProvider
) : CommandContext(commandInstanceProvider) {

    @Command(name = "create", description = "Créer un nouveau campement.")
    fun createCampement(sender: Player) {
        val result = campementService.createCampement(sender.uniqueId, sender.location)
        result.reason
            .map { reason ->
                when (reason) {
                    CampementCreationResult.Reason.InvalidWorld ->
                        CampementChatError + "Tu ne peux pas créer de campement dans ce monde."
                    CampementCreationResult.Reason.NearSpawn ->
                        CampementChatError + "Tu es trop proche du spawn pour créer un campement."
                    CampementCreationResult.Reason.NearCampement ->
                        CampementChatError + "Tu es trop proche d'un autre campement, tu ne peux pas en créer un ici."
                    CampementCreationResult.Reason.AlreadyHasCampement ->
                        CampementChatError + "Tu possèdes déjà un campement."
                    CampementCreationResult.Reason.OnOtherCampement -> {
                        val chunk = sender.location.chunk
                        val other = campementService.getCampementByChunk(chunk.x, chunk.z)
                        val ownerName = other?.ownerID?.let { Bukkit.getOfflinePlayer(it).name } ?: "un autre joueur"
                        CampementChatError + "Tu ne peux pas créer un campement ici, " +
                                "tu es sur le territoire de $ownerName."
                    }
                    CampementCreationResult.Reason.Ignored -> return
                }
            }
            .forEach { sender.sendMessage(it) }

        if (result.campement != null) {
            sender.sendMessage(
                CampementChatSuccess +
                        "Campement créé avec succès ! Tu peux désormais t'installer confortablement dans ce chunk ;)"
            )
        }
    }

    @Command(name = "delete", description = "Supprimer son campement.")
    fun deleteCampement(sender: Player) {
        if (!campementService.hasCampement(sender)) return
        val playerID = sender.uniqueId
        val campement = campementService.getCampementByOwner(playerID)!!

        menuService.openConfirmationMenu(
            sender,
            NamedTextColor.RED + "⚠ Es-tu sûr de vouloir supprimer ton campement ?",
            null
        ) {
            campementService.deleteCampement(campement.ownerID)
            borderRenderer.clearBorders(sender)
            sender.sendMessage(CampementChatSuccess + "Ton campement a été supprimé avec succès.")
        }
    }

    @Command(name = "setspawn", description = "Placer un nouveau point de spawn de campement.")
    fun setSpawn(sender: Player) {
        if (!campementService.hasCampement(sender)) return
        if (campementService.setSpawnpoint(sender.uniqueId, sender.location)) {
            sender.sendMessage(CampementChatSuccess + "Nouveau point de spawn défini ici !")
        } else {
            sender.sendMessage(CampementChatError + "Impossible de déplacer le point de spawn ici.")
        }
    }

    @Command(
        name = "spawn",
        description = "Se téléporter au spawn de son campement (pas d'argument dans la commande) ou de celui d’un autre joueur."
    )
    fun teleportToCampementSpawn(sender: Player, @Argument("joueur", optional = true) ownerName: CampOwnerArg?) {
        val targetName = ownerName?.get()
        if (targetName == null || targetName == sender.name
        ) {
            if (!campementService.hasCampement(sender)) return
            val campement = campementService.getCampementByOwner(sender.uniqueId) ?: return
            teleportService.teleport(sender, campement.spawnLocation) {
                sender.sendMessage(CampementChatSuccess + "Tu as été téléporté à ton campement.")
            }
            return
        }
        val target = Bukkit.getOfflinePlayer(targetName)
        val campement = campementService.getCampementByOwner(target.uniqueId)
        if (campement == null) {
            sender.sendMessage(CampementChatError + "${target.name} ne possède pas de campement.")
            return
        }
        teleportService.teleport(sender, campement.spawnLocation) {
            sender.sendMessage(CampementChatSuccess + "Tu as été téléporté au campement de ${target.name}.")
        }
    }

    @Command(name = "claim", description = "Annexer un chunk libre")
    fun claimChunk(sender: Player) {
        if (!campementService.hasCampement(sender)) return
        campementService.handleClaimUnclaim(sender, sender.chunk, claim = true)
    }

    @Command(name = "unclaim", description = "Désannexer un chunk de son campement.")
    fun unclaimChunk(sender: Player) {
        if (!campementService.hasCampement(sender)) return
        campementService.handleClaimUnclaim(sender, sender.chunk, claim = false)
    }

    @Command(
        name = "trust",
        description = "Ajouter un joueur de confiance dans son campement."
    )
    fun trustPlayer(sender: Player, @Argument("joueur") targetPlayerArg: PlayerArg) {
        if (!campementService.hasCampement(sender)) return
        val ownerID = sender.uniqueId
        val trustedPlayerID = targetPlayerArg.get().uniqueId

        if (ownerID == trustedPlayerID) {
            sender.sendMessage(CampementChatFormat + "C'est bien, tu as confiance en toi ! Mais bon, t'es déjà propriétaire :)")
            return
        }

        val success = campementService.trustPlayer(ownerID, trustedPlayerID)
        if (success) {
            sender.sendMessage(CampementChatSuccess + "${targetPlayerArg.get().name} a été ajouté en tant que joueur de confiance dans ton campement !")
            targetPlayerArg.get().sendMessage(CampementChatSuccess + "Tu as été ajouté en tant que joueur de confiance dans le campement de ${sender.name}.")
        } else {
            sender.sendMessage(CampementChatError + "Impossible d'ajouter ce joueur. Assure-toi d'être le propriétaire du campement et que le joueur n'est pas déjà ajouté.")
        }
    }

    @Command(
        name = "untrust",
        description = "Retirer un joueur de confiance de son campement."
    )
    fun untrustPlayer(sender: Player, @Argument("joueur") targetName: TrustedPlayerArg) {
        if (!campementService.hasCampement(sender)) return
        val ownerID = sender.uniqueId
        val campement = campementService.getCampementByOwner(ownerID)!!

        val target = Bukkit.getOfflinePlayer(targetName.get())
        val trustedPlayerUUID = target.uniqueId
        if (!campement.trustedPlayers.contains(trustedPlayerUUID)) {
            sender.sendMessage(CampementChatError + "Ce joueur n'est pas dans ta liste de confiance.")
            return
        }

        val success = campementService.untrustPlayer(ownerID, trustedPlayerUUID)
        if (success) {
            sender.sendMessage(CampementChatSuccess + "${target.name} a été retiré de la liste des joueurs de confiance de ton campement !")
        } else {
            sender.sendMessage(CampementChatError + "Erreur lors du retrait de ${target.name}.")
        }
    }

    @Command(name = "border", description = "Afficher/Masquer les bordures de son campement.")
    fun toggleBorder(sender: Player) {
        if (!campementService.hasCampement(sender)) return
        val campement = campementService.getCampementByOwner(sender.uniqueId)!!

        if (borderRenderer.isShowingBorders(sender)) {
            borderRenderer.clearBorders(sender)
            sender.sendMessage(CampementChatFormat + "Les bordures de ton campement ont été masquées.")
        } else {
            borderRenderer.showBorders(sender, campement.chunks.map { listOf(it.x, it.z) })
            sender.sendMessage(CampementChatSuccess + "Les bordures de ton campement sont maintenant visibles !")
        }
    }

    @Command(name = "stick", description = "Recevoir un Bâton d'annexion.")
    fun giveStick(sender: Player) {
        if (!campementService.hasCampement(sender)) return
        val equipment = equipmentService.getEquipment(sender.uniqueId)
        val existing = equipment.get(AnnexionStickInvocable::class.java)

        val invocable = existing ?: AnnexionStickInvocable(sender.uniqueId, campementService).also {
            equipmentService.add(sender.uniqueId, it)
        }

        val inv = sender.inventory
        val hasFreeSlotInHotbar = (0..8).any { inv.getItem(it) == null }

        if (hasFreeSlotInHotbar) {
            equipmentService.invoke(sender, AnnexionStickInvocable::class.java)
            sender.sendMessage(CampementChatFormat + "Tu as reçu un Bâton d'Annexion.")
        } else {
            val menu = EquipmentMenu(sender, equipmentService)
            menu.mainHandInvocationMenu(invocable, sender)
        }
    }

}

package onl.tesseract.srp.controller.command.campement

import net.kyori.adventure.text.Component
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
import onl.tesseract.srp.domain.territory.campement.AnnexionStickInvocable
import onl.tesseract.srp.domain.commun.enum.ClaimResult
import onl.tesseract.srp.domain.commun.enum.CreationResult
import onl.tesseract.srp.domain.commun.enum.SetSpawnResult
import onl.tesseract.srp.domain.commun.enum.UnclaimResult
import onl.tesseract.srp.service.TeleportationService
import onl.tesseract.srp.service.territory.campement.CAMP_BORDER_COMMAND
import onl.tesseract.srp.service.territory.campement.CampementBorderRenderer
import onl.tesseract.srp.service.territory.campement.CampementService
import onl.tesseract.srp.util.CampementChatError
import onl.tesseract.srp.util.CampementChatFormat
import onl.tesseract.srp.util.CampementChatSuccess
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

private val CAMPEMENT_BORDER_MESSAGE: Component =
    Component.text("Visualise les bordures avec ")
        .append(Component.text(CAMP_BORDER_COMMAND, NamedTextColor.GOLD))
        .append(Component.text("."))

@SpringComponent
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
        val msg = when (result) {
            CreationResult.INVALID_WORLD -> CampementChatError + "Tu ne peux pas créer de campement dans ce monde."
            CreationResult.NEAR_SPAWN -> CampementChatError + "Tu es trop proche du spawn pour créer un campement."
            CreationResult.TOO_CLOSE_TO_OTHER_TERRITORY -> CampementChatError + "Tu es trop proche d'un autre campement, tu ne peux pas en créer un ici."
            CreationResult.ALREADY_HAS_TERRITORY -> CampementChatError + "Tu possèdes déjà un campement."
            CreationResult.ON_OTHER_TERRITORY -> {
                val other = campementService.getByChunk(sender.location)
                val ownerName = other?.ownerID?.let { Bukkit.getOfflinePlayer(it).name } ?: "un autre joueur"
                CampementChatError + "Tu ne peux pas créer un campement ici, " +
                        "tu es sur le territoire de $ownerName."
            }
            CreationResult.RANK_TOO_LOW,
            CreationResult.NAME_TAKEN,
            CreationResult.NOT_ENOUGH_MONEY -> return
            CreationResult.SUCCESS -> CampementChatSuccess +
                    "Campement créé avec succès ! Tu peux désormais t'installer confortablement dans ce chunk ;)"
        }
        sender.sendMessage(msg)
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

    @Command(
        name = "spawn",
        description = "Se téléporter au spawn de son campement (pas d'argument) ou de celui d’un autre joueur."
    )
    fun teleportToCampementSpawn(sender: Player, @Argument("joueur", optional = true) ownerName: CampOwnerArg?) {
        val targetName = ownerName?.get()
        if (targetName == null || targetName == sender.name) {
            if (!campementService.hasCampement(sender)) return
            val loc = campementService.getCampSpawn(sender.uniqueId)
            if (loc == null) {
                sender.sendMessage(CampementChatError + "Aucun spawn défini pour ton campement.")
                return
            }
            teleportService.teleport(sender, loc) {
                sender.sendMessage(CampementChatSuccess + "Tu as été téléporté à ton campement.")
            }
            return
        }
        val target = Bukkit.getOfflinePlayer(targetName)
        val loc = campementService.getCampSpawn(target.uniqueId)
        if (loc == null) {
            sender.sendMessage(CampementChatError + "${target.name} ne possède pas de campement.")
            return
        }
        teleportService.teleport(sender, loc) {
            sender.sendMessage(CampementChatSuccess + "Tu as été téléporté au campement de ${target.name}.")
        }
    }

    @Command(name = "setspawn", description = "Placer un nouveau point de spawn de campement.")
    fun setCampementSpawn(sender: Player) {
        if (!campementService.hasCampement(sender)) return

        when (campementService.setSpawnpoint(sender.uniqueId, sender.location)) {
            SetSpawnResult.SUCCESS -> sender.sendMessage(CampementChatSuccess + "Nouveau point de spawn défini ici !")

            SetSpawnResult.NOT_AUTHORIZED -> sender.sendMessage(CampementChatError + "Tu n'es pas autorisé à changer le point de spawn.")
            SetSpawnResult.OUTSIDE_TERRITORY -> sender.sendMessage(CampementChatError +
                    "Tu dois être dans un chunk de ton campement pour définir le spawn. " + CAMPEMENT_BORDER_MESSAGE)
            SetSpawnResult.NOT_EXIST -> TODO()
        }
    }

    @Command(name = "claim", description = "Annexer un chunk libre")
    fun claimChunk(sender: Player) {
        when (campementService.claimChunk(sender.uniqueId, sender.location)) {
            ClaimResult.SUCCESS -> sender.sendMessage(
                CampementChatSuccess + "Le chunk (${sender.chunk.x}, ${sender.chunk.z}) a été annexé avec succès.")
            ClaimResult.ALREADY_OWNED -> sender.sendMessage(
                CampementChatFormat + "Tu possèdes déjà ce chunk. " + CAMPEMENT_BORDER_MESSAGE)
            ClaimResult.ALREADY_OTHER -> sender.sendMessage(
                CampementChatError + "Ce chunk appartient à un autre campement. " + CAMPEMENT_BORDER_MESSAGE)
            ClaimResult.NOT_ADJACENT -> sender.sendMessage(
                CampementChatError + "Tu dois sélectionner un chunk collé à ton campement. " + CAMPEMENT_BORDER_MESSAGE)
            ClaimResult.TOO_CLOSE -> sender.sendMessage(
                CampementChatError + "Tu ne peux pas annexer ce chunk, il est trop proche d’un autre campement. "
                        + CAMPEMENT_BORDER_MESSAGE)
            ClaimResult.NOT_ALLOWED -> sender.sendMessage(
                CampementChatError + "Tu n'es pas autorisé à annexer ce chunk.")
            ClaimResult.NOT_EXIST -> CampementChatError + "Tu n'as pas de campement."
            ClaimResult.INVALID_WORLD -> sender.sendMessage(CampementChatError + "Tu ne peux pas claim dans ce monde.")
        }
    }

    @Command(name = "unclaim", description = "Désannexer un chunk de son campement.")
    fun unclaimChunk(sender: Player) {
        when(campementService.unclaimChunk(sender.uniqueId,sender.location)){
            UnclaimResult.SUCCESS -> sender.sendMessage(CampementChatSuccess
                    + "Le chunk (${sender.chunk.x}, ${sender.chunk.z}) a été retiré de ton campement.")
            UnclaimResult.NOT_OWNED -> sender.sendMessage(CampementChatError
                    + "Ce chunk ne fait pas partie de ton campement. " + CAMPEMENT_BORDER_MESSAGE)
            UnclaimResult.NOT_ALLOWED -> TODO()
            UnclaimResult.LAST_CHUNK -> TODO()
            UnclaimResult.IS_SPAWN_CHUNK -> TODO()
            UnclaimResult.NOT_EXIST -> TODO()
            UnclaimResult.SPLIT -> sender.sendMessage(CampementChatError
                    + "Impossible de désannexer ce chunk (dernier chunk, chunk de spawn, ou cela diviserait ton campement). "
                    + CAMPEMENT_BORDER_MESSAGE)
        }
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
            sender.sendMessage(CampementChatFormat + "C'est bien, tu as confiance en toi ! " +
                    "Mais bon, t'es déjà propriétaire :)")
            return
        }

        val success = campementService.trustPlayer(ownerID, trustedPlayerID)
        if (success) {
            sender.sendMessage(CampementChatSuccess + "${targetPlayerArg.get().name} a été ajouté " +
                    "en tant que joueur de confiance dans ton campement !")
            targetPlayerArg.get().sendMessage(CampementChatSuccess + "Tu as été ajouté en tant que " +
                    "joueur de confiance dans le campement de ${sender.name}.")
        } else {
            sender.sendMessage(CampementChatError + "Impossible d'ajouter ce joueur. " +
                    "Assure-toi d'être le propriétaire du campement et que le joueur n'est pas déjà ajouté.")
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
            sender.sendMessage(CampementChatSuccess + "${target.name} a été retiré de la " +
                    "liste des joueurs de confiance de ton campement !")
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
            borderRenderer.showBorders(sender, campement.getChunks().map { listOf(it.chunkCoord.x, it.chunkCoord.z) })
            sender.sendMessage(CampementChatSuccess + "Les bordures de ton campement sont maintenant visibles !")
        }
    }

    @Command(name = "stick", description = "Recevoir un Bâton d'annexion.")
    fun giveStick(sender: Player) {
        if (!campementService.hasCampement(sender)) return
        val equipment = equipmentService.getEquipment(sender.uniqueId)
        val existing = equipment.get(AnnexionStickInvocable::class.java)

        val invocable = existing ?: AnnexionStickInvocable(sender.uniqueId).also {
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

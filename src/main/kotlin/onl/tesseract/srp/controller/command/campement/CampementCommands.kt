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
import onl.tesseract.lib.util.append
import onl.tesseract.srp.SrpCommandInstanceProvider
import onl.tesseract.srp.controller.command.argument.CampOwnerArg
import onl.tesseract.srp.controller.command.argument.TrustedPlayerArg
import onl.tesseract.srp.domain.territory.enum.ClaimResult
import onl.tesseract.srp.domain.territory.enum.CreationResult
import onl.tesseract.srp.domain.territory.enum.SetSpawnResult
import onl.tesseract.srp.domain.territory.enum.UnclaimResult
import onl.tesseract.srp.domain.territory.enum.TrustResult
import onl.tesseract.srp.domain.territory.enum.UntrustResult
import onl.tesseract.srp.mapper.toChunkCoord
import onl.tesseract.srp.mapper.toCoordinate
import onl.tesseract.srp.mapper.toLocation
import onl.tesseract.srp.service.TeleportationService
import onl.tesseract.srp.service.territory.campement.CAMP_BORDER_COMMAND
import onl.tesseract.srp.service.territory.campement.CampementBorderRenderer
import onl.tesseract.srp.service.territory.campement.CampementService
import onl.tesseract.srp.util.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

private val CAMPEMENT_BORDER_MESSAGE: Component =
    Component.text("Visualise les bordures avec ")
        .append(CAMP_BORDER_COMMAND, NamedTextColor.GOLD)
        .append(".")
private val NO_CAMPEMENT_MESSAGE: Component =
        CampementChatError
        .plus("Tu ne possèdes pas de campement. Crées-en un avec " )
        .append("/campement create", NamedTextColor.GOLD)
private val NOT_IN_CAMPEMENT_WORLD_MESSAGE =
    CampementChatError + "Tu n'es pas dans le bon monde, cette commande n’est utilisable que dans le monde des campements."


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
        val result = campementService.createCampement(sender.uniqueId, sender.location.toCoordinate())
        val msg = when (result) {
            CreationResult.INVALID_WORLD -> NOT_IN_CAMPEMENT_WORLD_MESSAGE
            CreationResult.NEAR_SPAWN -> CampementChatError + "Tu es trop proche du spawn pour créer un campement."
            CreationResult.TOO_CLOSE_TO_OTHER_TERRITORY -> CampementChatError + "Tu es trop proche d'un autre campement, tu ne peux pas en créer un ici."
            CreationResult.ALREADY_HAS_TERRITORY -> CampementChatError + "Tu possèdes déjà un campement."
            CreationResult.ON_OTHER_TERRITORY -> {
                CampementChatError + "Tu ne peux pas créer un campement ici, " +
                        "tu es sur un autre territoire."
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
        val playerID = sender.uniqueId
        val campement = campementService.getCampementByOwner(playerID) ?: return sender.sendMessage(NO_CAMPEMENT_MESSAGE)
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
            if (campementService.getCampementByOwner(sender.uniqueId) == null) {
                return sender.sendMessage(NO_CAMPEMENT_MESSAGE)
            }
            val loc = campementService.getCampSpawn(sender.uniqueId)
            if (loc == null) {
                sender.sendMessage(CampementChatError + "Aucun spawn défini pour ton campement.")
                return
            }
            teleportService.teleport(sender, loc.toLocation()) {
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
        teleportService.teleport(sender, loc.toLocation()) {
            sender.sendMessage(CampementChatSuccess + "Tu as été téléporté au campement de ${target.name}.")
        }
    }

    @Command(name = "setspawn", description = "Placer un nouveau point de spawn de campement.")
    fun setCampementSpawn(sender: Player) {
        when (campementService.setSpawnpoint(sender.uniqueId, sender.location.toCoordinate())) {
            SetSpawnResult.SUCCESS -> sender.sendMessage(CampementChatSuccess + "Nouveau point de spawn défini ici !")

            SetSpawnResult.NOT_ALLOWED -> sender.sendMessage(CampementChatError + "Tu n'es pas autorisé à changer le point de spawn.")
            SetSpawnResult.OUTSIDE_TERRITORY -> sender.sendMessage(CampementChatError +
                    "Tu dois être dans un chunk de ton campement pour définir le spawn. " + CAMPEMENT_BORDER_MESSAGE)
            SetSpawnResult.TERRITORY_NOT_FOUND -> sender.sendMessage(NO_CAMPEMENT_MESSAGE)
        }
    }

    @Command(name = "claim", description = "Annexer un chunk libre")
    fun claimChunk(sender: Player) {
        when (campementService.claimChunk(sender.uniqueId, sender.location.toChunkCoord())) {
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
            ClaimResult.TERRITORY_NOT_FOUND -> sender.sendMessage(NO_CAMPEMENT_MESSAGE)
            ClaimResult.INVALID_WORLD -> sender.sendMessage(CampementChatError + "Tu ne peux pas claim dans ce monde.")
        }
    }

    @Command(name = "unclaim", description = "Désannexer un chunk de son campement.")
    fun unclaimChunk(sender: Player) {
        when(campementService.unclaimChunk(sender.uniqueId,sender.location.toChunkCoord())){
            UnclaimResult.SUCCESS -> sender.sendMessage(CampementChatSuccess
                    + "Le chunk (${sender.chunk.x}, ${sender.chunk.z}) a été retiré de ton campement.")
            UnclaimResult.NOT_OWNED -> sender.sendMessage(CampementChatError
                    + "Ce chunk ne fait pas partie de ton campement. " + CAMPEMENT_BORDER_MESSAGE)
            UnclaimResult.NOT_ALLOWED -> sender.sendMessage(CampementChatError +
            "Tu n'es pas autorisé(e) à utiliser cette commande.")
            UnclaimResult.LAST_CHUNK -> sender.sendMessage(CampementChatError +
                    "Tu ne peux pas désannexer ton dernier chunk de campement.")
            UnclaimResult.IS_SPAWN_CHUNK -> sender.sendMessage(CampementChatError +
                    "Tu ne peux pas désannexer ce chunk, il contient ton point de spawn.")
            UnclaimResult.TERRITORY_NOT_FOUND -> sender.sendMessage(NO_CAMPEMENT_MESSAGE)
            UnclaimResult.SPLIT -> sender.sendMessage(CampementChatError
                    + "Impossible de désannexer ce chunk, cela diviserait ton campement en 2 parties). "
                    + CAMPEMENT_BORDER_MESSAGE)
        }
    }

    @Command(
        name = "trust",
        description = "Ajouter un joueur de confiance dans son campement."
    )
    fun trustPlayer(sender: Player, @Argument("joueur") targetPlayerArg: PlayerArg) {
        val ownerID = sender.uniqueId
        val trustedPlayerID = targetPlayerArg.get().uniqueId

        if (ownerID == trustedPlayerID) {
            sender.sendMessage(CampementChatFormat + "C'est bien, tu as confiance en toi ! " +
                    "Mais bon, t'es déjà propriétaire :)")
            return
        }

        val success = campementService.trust(ownerID, trustedPlayerID)
        when(success){
            TrustResult.NOT_ALLOWED -> sender.sendMessage(CampementChatError + "Impossible d'ajouter ce joueur. " +
                    "Assure-toi d'être le propriétaire du campement et que le joueur n'est pas déjà ajouté.")
            TrustResult.SUCCESS -> {
                sender.sendMessage(CampementChatSuccess + "${targetPlayerArg.get().name} a été ajouté " +
                        "en tant que joueur de confiance dans ton campement !")
                targetPlayerArg.get().sendMessage(CampementChatSuccess + "Tu as été ajouté en tant que " +
                        "joueur de confiance dans le campement de ${sender.name}.")
            }
            TrustResult.ALREADY_TRUST -> sender.sendMessage(CampementChatFormat + "Ce joueur est déjà ajouté à ton campement.")
            TrustResult.TERRITORY_NOT_FOUND -> sender.sendMessage(NO_CAMPEMENT_MESSAGE)
        }
    }

    @Command(
        name = "untrust",
        description = "Retirer un joueur de confiance de son campement."
    )
    fun untrustPlayer(sender: Player, @Argument("joueur") targetName: TrustedPlayerArg) {
        val ownerID = sender.uniqueId
        val target = Bukkit.getOfflinePlayer(targetName.get())
        val trustedPlayerUUID = target.uniqueId

        val success = campementService.untrust(ownerID, trustedPlayerUUID)

        when(success){
            UntrustResult.NOT_ALLOWED -> sender.sendMessage(CampementChatError + "Tu n'es pas autorisé à utiliser cette commande.")
            UntrustResult.NOT_TRUST -> sender.sendMessage(CampementChatError + "Ce joueur n'est pas dans ta liste de confiance.")
            UntrustResult.SUCCESS ->             sender.sendMessage(CampementChatSuccess + "${target.name} a été retiré de la " +
                    "liste des joueurs de confiance de ton campement !")
            UntrustResult.TERRITORY_NOT_FOUND -> sender.sendMessage(NO_CAMPEMENT_MESSAGE)
        }
    }

    // TODO() Refaire la classe de gestion des bordures avec l'archi (+gestion mauvais monde)
    @Command(name = "border", description = "Afficher/Masquer les bordures de son campement.")
    fun toggleBorder(sender: Player) {
        val campement = campementService.getCampementByOwner(sender.uniqueId)
            ?: return sender.sendMessage(NO_CAMPEMENT_MESSAGE)

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

package onl.tesseract.srp.controller.menu.campement

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuService
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.ItemLoreBuilder
import onl.tesseract.lib.util.plus
import onl.tesseract.lib.util.toComponent
import onl.tesseract.srp.domain.territory.campement.Campement
import onl.tesseract.srp.mapper.toLocation
import onl.tesseract.srp.service.TeleportationService
import onl.tesseract.srp.service.territory.campement.CampementService
import onl.tesseract.srp.util.CampementChatError
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

private const val TELEPORT_BUTTON_INDEX = 10
private const val GENERAL_INFO_BUTTON_INDEX = 12
private const val TRUSTED_PLAYERS_BUTTON_INDEX = 14
private const val SHOPS_BUTTON_INDEX = 16
private val CAMP_DATE_FORMATTER =
    DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault())

class CampementMenu(
    val playerID: UUID,
    private val campementService: CampementService,
    private val menuService: MenuService,
    private val teleportService: TeleportationService,
    previous: Menu? = null
) : CampementBaseMenu(MenuSize.Three, "Campement".toComponent(), previous) {

    override fun placeButtons(viewer: Player) {
        val campement = campementService.getCampementByOwner(playerID) ?: return close()
        placeDecorations()
        addTeleportButton(viewer, campement.id)
        addGeneralInfoButton(campement)
        addTrustedPlayersButton(viewer)
        addShopsButton()
        addBackButton()
        addCloseButton()
    }

    private fun addTeleportButton(viewer: Player, campId: UUID) {
        addButton(
            TELEPORT_BUTTON_INDEX,
            ItemBuilder(Material.ENDER_PEARL)
                .name("Téléportation", NamedTextColor.GOLD)
                .lore(
                    ItemLoreBuilder()
                        .append("Se téléporter au spawn de son campement", NamedTextColor.GRAY)
                        .get()
                )
                .build()
        ) {
            val spawn = campementService.getCampSpawn(campId)
            if (spawn == null) {
                viewer.sendMessage(CampementChatError + "Aucun spawn défini pour ce campement.")
                return@addButton
            }
            close()
            teleportService.teleport(viewer, spawn.toLocation())
        }
    }

    private fun addGeneralInfoButton(campement: Campement) {
        addButton(
            GENERAL_INFO_BUTTON_INDEX,
            ItemBuilder(Material.BLUE_BANNER)
                .name("Informations générales", NamedTextColor.GOLD)
                .lore(buildGeneralInfoLore(campement).get())
                .build()
        ) { /* info only */ }
    }

    private fun buildGeneralInfoLore(campement: Campement): ItemLoreBuilder {
        val ownerName = Bukkit.getOfflinePlayer(campement.ownerID).name ?: "Inconnu"
        return ItemLoreBuilder()
            .append("Propriétaire : ", NamedTextColor.GRAY)
            .append(ownerName, NamedTextColor.GOLD)
            .newline()
            .append("Niveau : ", NamedTextColor.GRAY)
            .append(campement.campLevel.toString(), NamedTextColor.GREEN)
            .newline()
            .append("Date de création : ", NamedTextColor.GRAY)
            .append(CAMP_DATE_FORMATTER.format(campement.creationDate), NamedTextColor.WHITE)
            .newline()
            .append("Nombre de chunks : ", NamedTextColor.GRAY)
            .append("${campement.getChunks().size}", NamedTextColor.AQUA)
    }

    private fun addTrustedPlayersButton(viewer: Player) {
        addButton(
            TRUSTED_PLAYERS_BUTTON_INDEX,
            ItemBuilder(Material.PLAYER_HEAD)
                .name("Joueurs de confiance", NamedTextColor.GOLD)
                .lore(
                    ItemLoreBuilder()
                        .append("Gérer les joueurs autorisés sur votre campement", NamedTextColor.GRAY)
                        .get()
                )
                .build()
        ) {
            CampementTrustedPlayersMenu(playerID, campementService, menuService, 0, this).open(viewer)
        }
    }

    private fun addShopsButton() {
        addButton(
            SHOPS_BUTTON_INDEX,
            ItemBuilder(Material.CHEST)
                .name("Shops", NamedTextColor.GOLD)
                .lore(
                    ItemLoreBuilder()
                        .append("Voir les shops du campement", NamedTextColor.GRAY)
                        .get()
                )
                .build()
        ) { /* TODO */ }
    }
}
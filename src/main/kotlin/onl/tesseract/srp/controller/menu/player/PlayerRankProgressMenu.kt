package onl.tesseract.srp.controller.menu.player

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.menu.Button
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.profile.PlayerProfileService
import onl.tesseract.lib.task.TaskScheduler
import onl.tesseract.lib.util.ItemLoreBuilder
import onl.tesseract.lib.util.menu.InventoryHeadIcons
import onl.tesseract.lib.util.plus
import onl.tesseract.lib.util.toComponent
import onl.tesseract.srp.domain.player.PlayerRank
import onl.tesseract.srp.domain.player.SrpPlayer
import onl.tesseract.srp.service.player.SrpPlayerService
import onl.tesseract.srp.util.menu.BiMenu
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*

private const val SCROLL_SPREAD: Int = 4

class PlayerRankProgressMenu(private val playerID: UUID,
                             private val playerService: SrpPlayerService,
                             private val profileService: PlayerProfileService,
                             private val scheduler: TaskScheduler,
                             previous: Menu?) :
    BiMenu(MenuSize.One, "Grades".toComponent(), previous) {

    override fun placeButtons(viewer: Player) {
        val player = playerService.getPlayer(playerID)
        val scroll = player.rank.ordinal * SCROLL_SPREAD

        placeButtons(viewer, scroll)
    }

    private fun placeButtons(viewer: Player, scroll: Int) {
        if (scroll < 0) return placeButtons(viewer, 0)
        val player = playerService.getPlayer(playerID)

        val buttonLine = computeButtonLine(player, viewer)
        if (scroll > buttonLine.size - 9) return placeButtons(viewer, buttonLine.size - 9)

        for (inventoryIndex in 0 until 9) {
            val lineIndex = inventoryIndex + scroll
            val button = buttonLine[lineIndex]
            addButton(inventoryIndex, button)
        }

        addScrollButtons(scroll, viewer)

        addBottomButton(SCROLL_SPREAD, ItemBuilder(Material.PISTON)
            .name("Affichage compacte")
            .lore()
            .newline()
            .append("Clique pour voir tous les grades", NamedTextColor.GRAY)
            .buildLore()
            .build()) {

        }

        placePlayerInfo(player)
        addBottomBackButton()
        addBottomCloseButton()
        addMenuUsage()
    }

    private fun addScrollButtons(scroll: Int, viewer: Player) {
        addBottomButton(21, ItemBuilder(Material.PLAYER_HEAD)
            .customHead(InventoryHeadIcons.LEFT_ARROW.data, InventoryHeadIcons.LEFT_ARROW.signature)
            .name("Gauche")
            .build()
        ) {
            placeButtons(viewer, scroll - 1)
        }
        addBottomButton(23, ItemBuilder(Material.PLAYER_HEAD)
            .customHead(InventoryHeadIcons.RIGHT_ARROW.data, InventoryHeadIcons.LEFT_ARROW.signature)
            .name("Droite")
            .build()
        ) {
            placeButtons(viewer, scroll + 1)
        }
    }

    private fun computeButtonLine(player: SrpPlayer, viewer: Player): List<Button> {
        val buttons = mutableListOf<Button>()
        PlayerRank.entries.forEach { rank ->
            val rankButton = getRankButton(player, rank, viewer)
            buttons.add(rankButton)

            rank.next()?.let { nextRank ->
                val ratio = if (rank < player.rank)
                    3
                else if (rank > player.rank)
                    0
                else
                    (3f * player.money.toFloat() / nextRank.cost).toInt()
                for (i in 1.. 3) {
                    val material = if (ratio < i)
                        Material.RED_STAINED_GLASS_PANE
                    else
                        Material.GREEN_STAINED_GLASS_PANE
                    buttons.add(Button(ItemBuilder(material).name("").build()))
                }
            }
        }
        return buttons
    }

    private fun getRankButton(player: SrpPlayer, rank: PlayerRank, viewer: Player): Button {
        val lore = ItemLoreBuilder().newline()
        when {
            rank == player.rank -> lore.append("Grade actuel", NamedTextColor.GREEN)
            rank == player.rank.next() -> {
                lore.append("Prochain grade", NamedTextColor.GREEN)
                    .newline()
                    .append(NamedTextColor.GRAY + "Coût : ${rank.cost} lys")
            }
            rank < player.rank -> lore.append("Obtenu", NamedTextColor.GRAY)
            else -> lore.append("Bloqué", NamedTextColor.RED)
        }

        return Button(
            ItemBuilder(rank.icon)
                .name(rank.name)
                .enchanted(rank <= player.rank)
                .lore(lore.get())
                .build(),
            {
                if (playerService.getPlayer(playerID).rank.next() != rank)
                    return@Button
                if (playerService.buyNextRank(playerID)) {
                    viewer.playSound(viewer.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
                    val scroll = (rank.ordinal - 1) * SCROLL_SPREAD
                    repeat(SCROLL_SPREAD) { i ->
                        scheduler.runTimer(delay = (10 * (i + 1)).toLong(), 0, 0) {
                            placeButtons(viewer, scroll + i + 1)
                        }
                    }
                }
            })
    }

    private fun placePlayerInfo(player: SrpPlayer) {
        addBottomButton(9, {
            ItemBuilder(profileService.getPlayerHead(playerID))
                .name("Mes informations", NamedTextColor.GREEN)
                .lore()
                .newline()
                .addField("Argent", NamedTextColor.GOLD + "${player.money} Lys")
                .addField("Grade", NamedTextColor.GOLD + "${player.rank}")
                .buildLore()
                .build()
        })
    }

    private fun addMenuUsage() {
        addBottomButton(
            17, ItemBuilder(Material.OAK_SIGN)
                .name("Comment utiliser ce menu ?")
                .lore()
                .newline()
                .append(
                    NamedTextColor.GRAY
                            + "La vue du haut montre les différents grades obtenables. Utilise les flèches pour te déplacer de gauche à droite"
                            + " et voir les grades suivants. Tu es par défaut centré sur le prochain grade disponible."
                )
                .buildLore()
                .build()
        )
    }
}
package onl.tesseract.srp.controller.menu.elytra

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.equipment.Equipment
import onl.tesseract.lib.event.equipment.invocable.Elytra
import onl.tesseract.lib.menu.*
import onl.tesseract.lib.profile.PlayerProfileService
import onl.tesseract.lib.util.ItemLoreBuilder
import onl.tesseract.lib.util.menu.InventoryHeadIcons
import onl.tesseract.lib.util.plus
import onl.tesseract.lib.util.toComponent
import onl.tesseract.srp.domain.elytra.EnumElytraUpgrade
import onl.tesseract.srp.domain.player.SrpPlayer
import onl.tesseract.srp.service.elytra.ElytraUpgradeService
import onl.tesseract.srp.service.player.SrpPlayerService
import onl.tesseract.srp.util.menu.BiMenu
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*

class ElytraUpgradeMenu(
    val playerID: UUID,
    val equipment: Equipment,
    private val playerService: SrpPlayerService,
    private val profileService: PlayerProfileService,
    private val upgrade: EnumElytraUpgrade,
    private val upgradeService: ElytraUpgradeService,
    previous: Menu?
) : BiMenu(MenuSize.One, upgrade.displayName.toComponent(), previous) {

    override fun placeButtons(viewer: Player) {
        val elytra = equipment.get(Elytra::class.java) ?: return
        val level = upgradeService.getLevel(elytra, upgrade)
        val scroll = level * 4
        placeButtons(viewer, elytra, scroll)
    }

    private fun placeButtons(viewer: Player, elytra: Elytra, scroll: Int) {
        val player = playerService.getPlayer(playerID)
        val buttonLine = computeButtonLine(player, elytra, viewer)
        val safeScroll = scroll.coerceIn(0, buttonLine.size - 9)

        clear()
        for (i in 0 until 9) {
            val index = safeScroll + i
            if (index in buttonLine.indices) {
                addButton(i, buttonLine[index])
            }
        }

        addBottomButton(21, ItemBuilder(Material.PLAYER_HEAD)
            .customHead(InventoryHeadIcons.LEFT_ARROW.data, InventoryHeadIcons.LEFT_ARROW.signature)
            .name("Gauche")
            .build()) {
            placeButtons(viewer, elytra, safeScroll - 1)
        }

        addBottomButton(23, ItemBuilder(Material.PLAYER_HEAD)
            .customHead(InventoryHeadIcons.RIGHT_ARROW.data, InventoryHeadIcons.RIGHT_ARROW.signature)
            .name("Droite")
            .build()) {
            placeButtons(viewer, elytra, safeScroll + 1)
        }

        addBottomButton(4, ItemBuilder(Material.PISTON)
            .name("Affichage compacte")
            .lore()
            .newline()
            .append("Clique pour voir tous les niveaux", NamedTextColor.GRAY)
            .buildLore()
            .build()) {}

        placePlayerInfo(player)
        addBottomBackButton()
        addBottomCloseButton()
        addMenuUsage()
    }

    private fun computeButtonLine(player: SrpPlayer, elytra: Elytra, viewer: Player): List<Button> {
        val buttons = mutableListOf<Button>()
        val currentLevel = upgradeService.getLevel(elytra, upgrade)
        val maxLevel = upgradeService.getMaxLevel()

        for (level in 0 until maxLevel) {
            val levelButton = getUpgradeButton(elytra, level, currentLevel, viewer)
            buttons.add(levelButton)

            if (level == currentLevel && level + 1 < maxLevel) {
                val nextPrice = upgradeService.getPriceForLevel(currentLevel + 1) ?: continue
                val ratio = (3f * player.money / nextPrice).toInt().coerceAtMost(3)

                repeat(3) { i ->
                    val material = if (ratio > i)
                        Material.LIME_STAINED_GLASS_PANE
                    else
                        Material.RED_STAINED_GLASS_PANE
                    buttons.add(Button(ItemBuilder(material).name("").build()))
                }
            }
        }

        return buttons
    }

    private fun getUpgradeButton(elytra: Elytra, level: Int, currentLevel: Int, viewer: Player): Button {
        val price = upgradeService.getPriceForLevel(level)
            ?: return Button(ItemBuilder(Material.BARRIER).name("Erreur").build())
        val lore = ItemLoreBuilder()
        when {
            level == currentLevel -> lore.append("Niveau actuel", NamedTextColor.GREEN)
            level == currentLevel + 1 -> lore.append("Prochain niveau", NamedTextColor.GREEN)
            level < currentLevel -> lore.append("Débloqué", NamedTextColor.GRAY)
            else -> lore.append("Bloqué", NamedTextColor.RED)
        }
        lore.newline()

        when (upgrade) {
            EnumElytraUpgrade.SPEED ->
                lore.append("Augmente la vitesse en vol.", NamedTextColor.YELLOW)
            EnumElytraUpgrade.PROTECTION ->
                lore.append("Ajoute de l’armure pendant le vol.", NamedTextColor.YELLOW)
            EnumElytraUpgrade.BOOST_CHARGE -> {
                val count = Elytra.getBoostCount(level)
                lore.append("Boosts max : $count", NamedTextColor.YELLOW)
            }
            EnumElytraUpgrade.RECOVERY -> {
                val seconds = Elytra.getRecoveryTime(level) / 1000
                lore.append("Vitesse de rechargement : 1 boost / ${seconds}s", NamedTextColor.YELLOW)
            }
        }
        lore.newline()
        if (level == currentLevel + 1) {
            lore.append("Coût : $price lys", NamedTextColor.GRAY)
        }

        return Button(
            ItemBuilder(Material.BEACON)
                .name("Niveau ${level + 1}")
                .enchanted(level <= currentLevel)
                .lore(lore.get())
                .build(),
            {
                if (level != currentLevel + 1) return@Button
                if (playerService.buyNextElytraUpgrade(playerID, elytra, upgrade)) {
                    viewer.playSound(viewer.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                    placeButtons(viewer, elytra, (level - 1) * 4)
                }
            }
        )
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
                    NamedTextColor.GRAY +
                            "La vue du haut montre les niveaux d'amélioration. Utilise les flèches pour te déplacer et débloquer les niveaux suivants."
                )
                .buildLore()
                .build()
        )
    }
}

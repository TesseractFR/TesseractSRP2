package onl.tesseract.srp.controller.menu.elytra

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.lib.equipment.Equipment
import onl.tesseract.lib.event.equipment.invocable.Elytra
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.profile.PlayerProfileService
import onl.tesseract.srp.domain.elytra.EnumElytraUpgrade
import onl.tesseract.srp.domain.player.SrpPlayer
import onl.tesseract.srp.service.elytra.ElytraUpgradeService
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*

class ElytraUpgradeMenu(
    private val playerID: UUID,
    private val equipment: Equipment,
    private val upgradeService: ElytraUpgradeService,
    private val playerService: SrpPlayerService,
    private val playerProfileService: PlayerProfileService
) : Menu(MenuSize.Two, Component.text("Améliorations Elytra", DARK_PURPLE)) {

    override fun placeButtons(viewer: Player) {
        val elytra = equipment.get(Elytra::class.java) ?: return
        val srpPlayer = playerService.getPlayer(playerID)

        EnumElytraUpgrade.entries.forEachIndexed { index, upgrade ->
            val currentLevel = upgradeService.getLevel(elytra, upgrade)
            val nextLevel = currentLevel + 1
            val maxLevel = upgradeService.getMaxLevel()
            val price = if (nextLevel < maxLevel) upgradeService.getPriceForLevel(nextLevel) else null

            val builder = ItemBuilder(upgrade.material)
                .name(Component.text(upgrade.displayName, AQUA))
                .lore()
                .append(Component.text(upgrade.description, GRAY, TextDecoration.ITALIC))
                .newline().newline()
                .append(Component.text("Niveau actuel : ", GREEN))
                .append("${currentLevel + 1}", YELLOW)
                .newline()

            builder.append(getUpgradeStatLine(upgrade, currentLevel, isCurrent = true))
            builder.newline().newline()

            if (nextLevel < maxLevel && price != null) {
                val canAfford = srpPlayer.illuminationPoints >= price
                builder.append(Component.text("Prochaine amélioration :", BLUE, TextDecoration.BOLD))
                    .newline()
                    .append(getUpgradeStatLine(upgrade, nextLevel, isCurrent = false))
                    .newline().newline()
                    .append(Component.text("Coût : ", GOLD))
                    .append(
                        Component.text(
                            "$price points d'illumination",
                            if (canAfford) GREEN else RED
                        )
                    )
            } else {
                builder.append(Component.text("Amélioration maximale atteinte", DARK_GREEN))
            }
            val item = builder.buildLore().build()
            addButton(index, item) {
                if (nextLevel >= maxLevel || price == null) return@addButton
                if (playerService.buyNextElytraUpgrade(playerID, elytra, upgrade)) {
                    viewer.playSound(viewer.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                    open(viewer)
                }
            }
        }
        placePlayerInfo(srpPlayer)
        addBackButton()
        addCloseButton()
    }

    private fun placePlayerInfo(player: SrpPlayer) {
        addButton(16, ItemBuilder(playerProfileService.getPlayerHead(playerID))
            .name(Component.text("Mes informations", GREEN))
            .lore()
            .newline()
            .addField("Argent", Component.text("${player.money} Lys", GOLD))
            .addField("Points d'illumination", Component.text("${player.illuminationPoints}", GOLD))
            .addField("Grade", Component.text("${player.rank}", GOLD))
            .buildLore()
            .build()
        )
    }

    private fun getUpgradeStatLine(upgrade: EnumElytraUpgrade, level: Int, isCurrent: Boolean): Component {
        val color = if (isCurrent) GRAY else YELLOW
        return when (upgrade) {
            EnumElytraUpgrade.SPEED -> {
                val bonus = (0.10 * (level + 1) * 100).toInt()
                Component.text("→ Vitesse : +$bonus%", color)
            }
            EnumElytraUpgrade.PROTECTION -> {
                Component.text("→ Armure : ${0.5 * level} points", color)
            }
            EnumElytraUpgrade.BOOST_CHARGE -> {
                val count = Elytra.getBoostCount(level)
                Component.text("→ Boosts max : $count", color)
            }
            EnumElytraUpgrade.RECOVERY -> {
                val seconds = Elytra.getRecoveryTime(level) / 1000
                Component.text("→ Recharge : 1 boost / ${seconds}s", color)
            }
        }
    }
}

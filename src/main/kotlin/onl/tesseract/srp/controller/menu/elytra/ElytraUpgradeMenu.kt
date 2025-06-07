package onl.tesseract.srp.controller.menu.elytra

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.lib.equipment.Equipment
import onl.tesseract.lib.event.equipment.invocable.Elytra
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.profile.PlayerProfileService
import onl.tesseract.lib.event.equipment.invocable.EnumElytraUpgrade
import onl.tesseract.srp.domain.player.SrpPlayer
import onl.tesseract.srp.service.elytra.ElytraService
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class ElytraUpgradeMenu(
    private val playerID: UUID,
    private val equipment: Equipment,
    private val playerService: SrpPlayerService,
    private val playerProfileService: PlayerProfileService,
    private val elytraService: ElytraService
) : Menu(MenuSize.Two, Component.text("Améliorations Elytra", NamedTextColor.DARK_PURPLE)) {

    override fun placeButtons(viewer: Player) {
        val elytra = equipment.get(Elytra::class.java)
        if (elytra == null) {
            viewer.closeInventory()
            viewer.sendMessage(Component.text("Tu ne possèdes pas d'élytra personnalisée.", NamedTextColor.RED))
            return
        }

        val srpPlayer = playerService.getPlayer(playerID)

        placeUpgradeButtons(viewer, srpPlayer, elytra)
        placePlayerInfo(srpPlayer)
        addBackButton()
        addCloseButton()
    }

    private fun placeUpgradeButtons(viewer: Player, srpPlayer: SrpPlayer, elytra: Elytra) {
        EnumElytraUpgrade.entries.forEachIndexed { index, upgrade ->
            val item = buildUpgradeItem(srpPlayer, upgrade)
            addButton(index, item) {
                val currentLevel = elytra.getLevel(upgrade)
                val nextLevel = currentLevel + 1
                val maxLevel = elytra.getMaxLevel()
                val price = if (nextLevel < maxLevel) elytra.getPriceForLevel(nextLevel) else null

                if (nextLevel >= maxLevel || price == null) return@addButton
                if (elytraService.buyNextElytraUpgrade(playerID, elytra, upgrade)) {
                    viewer.playSound(viewer.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                    open(viewer)
                }
            }
        }
    }

    private fun buildUpgradeItem(
        srpPlayer: SrpPlayer,
        upgrade: EnumElytraUpgrade
    ): ItemStack {
        val elytra = equipment.get(Elytra::class.java)!!
        val currentLevel = elytra.getLevel(upgrade)
        val nextLevel = currentLevel + 1
        val maxLevel = elytra.getMaxLevel()
        val price = if (nextLevel < maxLevel) elytra.getPriceForLevel(nextLevel) else null
        val canAfford = price != null && srpPlayer.illuminationPoints >= price

        val builder = ItemBuilder(upgrade.material)
            .name(Component.text(upgrade.displayName, NamedTextColor.AQUA))
            .lore()
            .append(upgrade.description, NamedTextColor.GRAY, TextDecoration.ITALIC)
            .newline().newline()
            .append("Niveau actuel : ", NamedTextColor.GREEN)
            .append("${currentLevel + 1}", NamedTextColor.YELLOW)
            .newline()
            .append(getUpgradeStatLine(upgrade, currentLevel), NamedTextColor.GRAY)
            .newline().newline()

        if (nextLevel < maxLevel && price != null) {
            builder.append("Prochaine amélioration :", NamedTextColor.BLUE, TextDecoration.BOLD)
                .newline()
                .append(getUpgradeStatLine(upgrade, nextLevel), NamedTextColor.YELLOW)
                .newline().newline()
                .append("Coût : ", NamedTextColor.GOLD)
                .append(Component.text("$price points d'illumination",
                    if (canAfford) NamedTextColor.GREEN else NamedTextColor.RED))
        } else {
            builder.append("Amélioration maximale atteinte", NamedTextColor.DARK_GREEN)
        }

        return builder.buildLore().build()
    }

    private fun placePlayerInfo(player: SrpPlayer) {
        addButton(16, ItemBuilder(playerProfileService.getPlayerHead(playerID))
            .name(Component.text("Mes informations", NamedTextColor.GREEN))
            .lore()
            .newline()
            .addField("Argent", Component.text("${player.money} Lys", NamedTextColor.GOLD))
            .addField("Points d'illumination",
                Component.text("${player.illuminationPoints}", NamedTextColor.GOLD))
            .addField("Grade", Component.text("${player.rank}", NamedTextColor.GOLD))
            .buildLore()
            .build()
        )
    }

    private fun getUpgradeStatLine(upgrade: EnumElytraUpgrade, level: Int): String {
        return when (upgrade) {
            EnumElytraUpgrade.SPEED -> {
                val bonus = (0.10 * (level + 1) * 100).toInt()
                "→ Vitesse : +$bonus%"
            }
            EnumElytraUpgrade.PROTECTION -> {
                "→ Armure : ${0.5 * level} points"
            }
            EnumElytraUpgrade.BOOST_NUMBER -> {
                val count = Elytra.getBoostCount(level)
                "→ Boosts max : $count"
            }
            EnumElytraUpgrade.RECOVERY -> {
                val seconds = Elytra.getRecoveryTime(level) / 1000
                "→ Recharge : 1 boost / ${seconds}s"
            }
        }
    }
}

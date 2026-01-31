package onl.tesseract.srp.controller.menu.elytra

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.lib.event.equipment.invocable.ElytraUpgrade
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.profile.PlayerProfileService
import onl.tesseract.lib.util.ChatFormats.ELYTRA
import onl.tesseract.lib.util.ChatFormats.ELYTRA_ERROR
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.service.equipment.elytra.ElytraService
import onl.tesseract.srp.domain.equipment.elytra.ElytraUpgradeEntry
import onl.tesseract.srp.domain.equipment.elytra.ElytraUpgradeResult
import onl.tesseract.srp.domain.equipment.elytra.ElytraUpgradeStats
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

private const val SLOT_UPGRADE_SPEED = 19
private const val SLOT_UPGRADE_PROTECTION = 21
private const val SLOT_UPGRADE_BOOST_NUMBER = 23
private const val SLOT_UPGRADE_RECOVERY = 25
private const val PLAYER_INFO_SLOT = 40

class ElytraUpgradeMenu(
    private val playerID: UUID,
    private val playerProfileService: PlayerProfileService,
    private val elytraService: ElytraService,
    private val srpPlayerService: SrpPlayerService,
    previous: Menu?
) : ElytraBaseMenu(
    MenuSize.Five,
    Component.text("Améliorations Ailes Célestes", NamedTextColor.DARK_PURPLE),
    previous
) {

    override fun placeButtons(viewer: Player) {
        val entries = elytraService.getUpgradeEntries(playerID)
        val player = srpPlayerService.getPlayer(playerID)
        placeDecorations()
        placeUpgradeButtons(viewer, entries)
        placePlayerInfo(
            money = player.money,
            illuminationPoints = player.illuminationPoints,
            rankLabel = player.rank.toString()
        )
        addBackButton()
        addCloseButton()
    }


    private fun placeUpgradeButtons(viewer: Player, entries: List<ElytraUpgradeEntry>) {
        val slots = listOf(
            SLOT_UPGRADE_SPEED,
            SLOT_UPGRADE_PROTECTION,
            SLOT_UPGRADE_BOOST_NUMBER,
            SLOT_UPGRADE_RECOVERY
        )
        entries.forEachIndexed { index, entry ->
            val slot = slots.getOrNull(index) ?: return@forEachIndexed
            val item = buildUpgradeItem(entry)
            addButton(slot, item) {
                val result = elytraService.tryBuyNextUpgrade(playerID, entry.upgrade)
                when (result) {
                    ElytraUpgradeResult.SUCCESS -> {
                        viewer.playSound(viewer.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                        open(viewer)
                    }
                    ElytraUpgradeResult.NOT_ENOUGH_POINTS -> {
                        viewer.sendMessage(ELYTRA_ERROR + "Tu n'as pas assez de points d'illumination.")
                    }
                    ElytraUpgradeResult.MAX_LEVEL_REACHED -> {
                        viewer.sendMessage(ELYTRA + "Cette amélioration est déjà au niveau maximal.")
                    }
                    ElytraUpgradeResult.NO_ELYTRA -> {
                        viewer.sendMessage(ELYTRA_ERROR +"Tu ne possèdes pas d'ailes célestes.")
                    }
                }
            }
        }
    }

    private fun buildUpgradeItem(
        entry: ElytraUpgradeEntry
    ): ItemStack {
        val stats = elytraService.getUpgradeStats(entry.upgrade, entry.currentLevel)
        val nextStats = stats.nextValue?.let { stats.copy(currentValue = it) }
        val builder = ItemBuilder(entry.upgrade.material)
            .name(entry.upgrade.displayName, NamedTextColor.AQUA)
            .lore()
            .append(entry.upgrade.description, NamedTextColor.GRAY, TextDecoration.ITALIC)
            .newline().newline()
            .append("Niveau actuel : ", NamedTextColor.GREEN)
            .append("${entry.currentLevel + 1}", NamedTextColor.YELLOW)
            .newline()
            .append(formatStat(stats), NamedTextColor.WHITE)
            .newline().newline()

        if (entry.nextLevel != null && entry.price != null && entry.currentLevel < entry.maxLevel) {
            builder.append("Prochaine amélioration :", NamedTextColor.BLUE, TextDecoration.BOLD)
                .newline()
                .append(nextStats?.let { formatStat(it) }, NamedTextColor.YELLOW)
                .newline().newline()
                .append("Coût : ", NamedTextColor.GOLD)
                .append("${entry.price} points d'illumination",
                    if (entry.canAfford) NamedTextColor.GREEN else NamedTextColor.RED)
                .append(" (Cliquez pour acheter)", NamedTextColor.GRAY, TextDecoration.ITALIC)
        } else {
            builder.append("Amélioration maximale atteinte", NamedTextColor.DARK_GREEN)
        }
        return builder.buildLore().build()
    }

    private fun placePlayerInfo(money: Int, illuminationPoints: Int, rankLabel: String) {
        addButton(
            PLAYER_INFO_SLOT,
            ItemBuilder(playerProfileService.getPlayerHead(playerID))
                .name("Mes informations", NamedTextColor.GREEN)
                .lore()
                .newline()
                .addField("Argent", "$money Lys", NamedTextColor.GOLD)
                .addField("Points d'illumination", "$illuminationPoints", NamedTextColor.GOLD)
                .addField("Grade", rankLabel, NamedTextColor.GOLD)
                .buildLore()
                .build()
        )
    }

    private fun formatStat(stats: ElytraUpgradeStats): String = when (stats.type) {
        ElytraUpgrade.SPEED -> "→ Vitesse : +${stats.currentValue.toInt()}%"
        ElytraUpgrade.PROTECTION -> "→ Armure : ${stats.currentValue} points"
        ElytraUpgrade.BOOST_NUMBER -> "→ Boosts max : ${stats.currentValue.toInt()}"
        ElytraUpgrade.RECOVERY -> "→ Recharge : 1 boost / ${stats.currentValue.toInt()}s"
    }
}

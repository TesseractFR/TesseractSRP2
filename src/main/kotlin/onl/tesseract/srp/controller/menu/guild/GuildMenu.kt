package onl.tesseract.srp.controller.menu.guild

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.lib.chat.ChatEntryService
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuService
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.lib.util.ItemLoreBuilder
import onl.tesseract.lib.util.plus
import onl.tesseract.lib.util.toComponent
import onl.tesseract.srp.domain.territory.guild.Guild
import onl.tesseract.srp.domain.territory.guild.enum.GuildUpgradeResult
import onl.tesseract.srp.mapper.toLocation
import onl.tesseract.srp.service.TeleportationService
import onl.tesseract.srp.service.territory.guild.GuildService
import onl.tesseract.srp.util.GuildChatError
import onl.tesseract.srp.util.GuildChatFormat
import onl.tesseract.srp.util.GuildChatSuccess
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

private const val BANK_BUTTON_INDEX = 4
private val GUILD_DATE_FORMATTER =
    DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault())

class GuildMenu(
    val playerID: UUID,
    private val guildService: GuildService,
    private val menuService: MenuService,
    private val teleportService: TeleportationService,
    private val chatService: ChatEntryService,
    previous: Menu? = null
) : Menu(MenuSize.Five, "Guilde".toComponent(), previous) {

    override fun placeButtons(viewer: Player) {
        val guild = guildService.getGuildByMember(viewer.uniqueId) ?: return close()

        addBankButton(guild, viewer)
        addMembersListButton(viewer)
        addStructuresStatsButton()
        addShopsButton()
        addMissionsTableButton()
        addGuildPermissionsButton()
        addTeleportationButton(viewer, guild)
        addUpgradeGuildButton(viewer, guild)
        addPendingRequestsButton(viewer)
        addGeneralInformationButton(guild)
        addBackButton()
        addCloseButton()
    }

    private fun addBankButton(guild: Guild, viewer: Player) {
        val role = guild.getMemberRole(playerID)
        val lore = ItemLoreBuilder()
            .newline()
            .addField("Compte", NamedTextColor.GREEN + "${guild.money} Lys")
            .newline()
            .append("Clic gauche : ", NamedTextColor.GOLD)
            .append("Déposer", NamedTextColor.GRAY)
        if (role.canWithdrawMoney()) {
            lore.newline()
                .append("Clic droit : ", NamedTextColor.GOLD)
                .append("Retirer", NamedTextColor.GRAY)
        }

        addButton(
            BANK_BUTTON_INDEX, ItemBuilder(Material.GOLD_INGOT)
                .name("Banque de guilde", NamedTextColor.GOLD)
                .lore(lore.get())
                .build()
        ) {
            when (it.click) {
                ClickType.LEFT -> promptPlayerForBankOperation(guild, viewer, BankOperation.Deposit)
                ClickType.RIGHT -> {
                    if (role.canWithdrawMoney())
                        promptPlayerForBankOperation(guild, viewer, BankOperation.Withdraw)
                }
                else -> { /* No action */ }
            }
        }
    }

    private fun addMembersListButton(viewer: Player) {
        val lore = ItemLoreBuilder()
            .append("Voir la liste des membres de la guilde", NamedTextColor.GRAY)
        addButton(
            10,
            ItemBuilder(Material.PLAYER_HEAD)
                .name("Membres", NamedTextColor.GOLD)
                .lore(lore.get())
                .build()
        ) {
            GuildMembersMenu(playerID, guildService, menuService, 0, this).open(viewer)
        }
    }

    private fun addStructuresStatsButton() {
        val lore = ItemLoreBuilder()
            .append("Voir les statistiques des structures d'artisanat de la guilde", NamedTextColor.GRAY)
        addButton(
            11,
            ItemBuilder(Material.PAPER)
                .name("Structures d'artisanat", NamedTextColor.GOLD)
                .lore(lore.get())
                .build()
        ) { //TODO Faire le menu des stats des structures d'artisanat
        }
    }

    private fun addShopsButton() {
        val lore = ItemLoreBuilder()
            .append("Voir les shops de la guilde", NamedTextColor.GRAY)
        addButton(
            12,
            ItemBuilder(Material.CHEST)
                .name("Shops", NamedTextColor.GOLD)
                .lore(lore.get())
                .build()
        ) { //TODO Faire le menu des shops
        }
    }

    private fun addMissionsTableButton() {
        val lore = ItemLoreBuilder()
            .append("Voir le tableau des missions de la guilde", NamedTextColor.GRAY)
        addButton(
            13,
            ItemBuilder(Material.OAK_SIGN)
                .name("Tableau des missions", NamedTextColor.GOLD)
                .lore(lore.get())
                .build()
        ) { //TODO Faire le menu du tableau des missions de guilde
        }
    }

    private fun addGuildPermissionsButton() {
        val lore = ItemLoreBuilder()
            .append("Gérer les permissions des membres de la guilde", NamedTextColor.GRAY)
        addButton(
            14,
            ItemBuilder(Material.BOOK)
                .name("Permissions", NamedTextColor.GOLD)
                .lore(lore.get())
                .build()
        ) { //TODO Faire le menu des permissions de guilde
        }
    }

    private fun addTeleportationButton(viewer: Player, guild: Guild) {
        val lore = ItemLoreBuilder()
            .append("Se téléporter au spawn privé de sa guilde", NamedTextColor.GRAY)
        addButton(
            15,
            ItemBuilder(Material.ENDER_PEARL)
                .name("Téléportation vers sa guilde", NamedTextColor.GOLD)
                .lore(lore.get())
                .build()
        ) {
            close()
            teleportService.teleport(viewer, guildService.getPrivateSpawn(guild.id)!!.toLocation())
        }
    }

    private fun addUpgradeGuildButton(viewer: Player, guild: Guild) {
        val currentRank = guild.rank
        val nextRank = currentRank.next()
        val lore = ItemLoreBuilder()
            .append("Rang actuel : ", NamedTextColor.GRAY)
            .append(currentRank.title, currentRank.color)
            .newline()
            .newline()
        if (nextRank == null) {
            lore.append("Votre guilde est déjà au rang maximum.", NamedTextColor.GOLD)
        } else {
            val hasRequiredLevel = guild.level >= nextRank.minLevel
            val hasEnoughMoney = guild.money >= nextRank.cost
            val costColor = if (hasEnoughMoney) NamedTextColor.GREEN else NamedTextColor.RED
            lore.append("Prochain rang : ", NamedTextColor.GRAY)
                .append(nextRank.title, nextRank.color)
                .newline()
                .append("Coût : ", NamedTextColor.GRAY)
                .append("${nextRank.cost} Lys", costColor)
                .newline()

            if (!hasRequiredLevel) {
                lore.newline()
                    .append("Niveau minimum requis : ${nextRank.minLevel}", NamedTextColor.RED)
                    .newline()
                    .append("(Niveau actuel : ${guild.level})", NamedTextColor.RED, TextDecoration.ITALIC)
            } else if (!hasEnoughMoney) {
                lore.newline()
                    .append("Argent insuffisant.", NamedTextColor.RED)
                    .newline()
                    .append("(Banque de guilde : ${guild.money} Lys)", NamedTextColor.RED, TextDecoration.ITALIC)
            } else {
                lore.newline()
                    .append("Cliquez pour améliorer la guilde !", NamedTextColor.GREEN)
            }
        }
        addButton(
            16,
            ItemBuilder(Material.DIAMOND_PICKAXE)
                .name("Améliorer le rang de guilde", NamedTextColor.GOLD)
                .lore(lore.get())
                .build()
        ) {
            val g = guildService.getGuildByMember(viewer.uniqueId) ?: return@addButton
            val next = g.rank.next()
            if (next == null) {
                viewer.sendMessage(GuildChatFormat + "Votre guilde est déjà au rang maximum.")
                return@addButton
            }
            when (guildService.upgradeRank(g.id, next)) {
                GuildUpgradeResult.SUCCESS -> {
                    close()
                    viewer.sendMessage(GuildChatSuccess
                        + "Félicitations, votre guilde est maintenant au rang de "
                        + Component.text(next.title, next.color)
                        + " !"
                    )
                }
                GuildUpgradeResult.NOT_ENOUGH_MONEY -> {
                    viewer.sendMessage(GuildChatError + "Argent insuffisant : il faut ${next.cost} Lys.")
                }
                GuildUpgradeResult.RANK_LOCKED -> {
                    viewer.sendMessage(
                        GuildChatError + "Niveau insuffisant : votre guilde doit être niveau ${next.minLevel} minimum."
                    )
                }
                GuildUpgradeResult.ALREADY_AT_OR_ABOVE -> {
                    viewer.sendMessage(GuildChatFormat + "Votre guilde est déjà à ce rang (ou supérieur).")
                    open(viewer)
                }
            }
        }
    }

    private fun addPendingRequestsButton(viewer: Player) {
        val lore = ItemLoreBuilder()
            .append("Voir les invitations de joueurs en attente", NamedTextColor.GRAY)
        addButton(
            17,
            ItemBuilder(Material.CLOCK)
                .name("Demandes pour rejoindre en attente", NamedTextColor.GOLD)
                .lore(lore.get())
                .build()
        ) {
            GuildJoinRequestsMenu(playerID, guildService, menuService, 0, this).open(viewer)
        }
    }

    private fun addGeneralInformationButton(guild: Guild) {
        val lore = ItemLoreBuilder()
            .append("Nom : ", NamedTextColor.GRAY)
            .append(guild.name, NamedTextColor.WHITE)
            .newline()
            .append("Niveau : ", NamedTextColor.GRAY)
            .append(guild.level.toString(), NamedTextColor.GREEN)
            .newline()
            .append("X : ", NamedTextColor.GRAY)
            .append(guild.level.toString(), NamedTextColor.GREEN)
            .newline()
            .append("Rang : ", NamedTextColor.GRAY)
            .append(guild.rank.title, guild.rank.color)
            .newline()
            .append("Chef : ", NamedTextColor.GRAY)
        val leaderName = Bukkit.getOfflinePlayer(guild.leaderId).name ?: "Inconnu"
        lore.append(leaderName, NamedTextColor.GOLD)
            .newline()
            .append("Date de création : ", NamedTextColor.GRAY)
            .append(GUILD_DATE_FORMATTER.format(guild.creationDate), NamedTextColor.WHITE)
            .newline()
            .append("Membres : ", NamedTextColor.GRAY)
            .append("${guild.members.size}", NamedTextColor.AQUA)
            .newline()
            .append("Nombre de chunks : ", NamedTextColor.GRAY)
            .append("${guild.getChunks().size}", NamedTextColor.AQUA)
            .append(" / ${guild.rank.maxChunksNumber}", NamedTextColor.GRAY)
        addButton(
            18,
            ItemBuilder(Material.BLUE_BANNER)
                .name("Informations générales", NamedTextColor.GOLD)
                .lore(lore.get())
                .build()
        ) {
            // TODO menu détaillé
        }
    }

    private fun promptPlayerForBankOperation(guild: Guild, viewer: Player, operation: BankOperation) {
        close()
        chatService.getChatEntry(viewer, NamedTextColor.GREEN + "Montant à $operation") {
            it.runCatching { it.toUInt() }
                .onFailure { viewer.sendMessage(ChatFormats.CHAT_ERROR + "Nombre invalide") }
                .recover { return@getChatEntry }
                .mapCatching { amount ->
                    when (operation) {
                        BankOperation.Deposit -> guildService.depositMoney(guild.id, playerID, amount)
                        BankOperation.Withdraw -> guildService.withdrawMoney(guild.id, playerID, amount)
                    }
                }
                .onFailure { viewer.sendMessage(ChatFormats.CHAT_ERROR + "Une erreur est survenue") }
                .onSuccess { success ->
                    if (success)
                        open(viewer)
                    else
                        viewer.sendMessage(ChatFormats.CHAT_ERROR + "Argent insuffisant")
                }
        }
    }

    enum class BankOperation(private val label: String) {
        Deposit("déposer"), Withdraw("retirer"),
        ;

        override fun toString(): String = label
    }
}

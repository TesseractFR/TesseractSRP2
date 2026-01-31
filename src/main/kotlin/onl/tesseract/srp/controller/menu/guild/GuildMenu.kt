package onl.tesseract.srp.controller.menu.guild

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.chat.ChatEntryService
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.lib.util.ItemLoreBuilder
import onl.tesseract.lib.util.plus
import onl.tesseract.lib.util.toComponent
import onl.tesseract.srp.domain.territory.guild.Guild
import onl.tesseract.srp.service.territory.guild.GuildService
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import java.util.*

private const val BANK_BUTTON_INDEX = 4

class GuildMenu(
    val playerID: UUID,
    private val guildService: GuildService,
    private val chatService: ChatEntryService,
    previous: Menu? = null
) : Menu(MenuSize.Five, "Guilde".toComponent(), previous) {

    override fun placeButtons(viewer: Player) {
        val guild = guildService.getGuildByMember(viewer.uniqueId) ?: return close()

        addBankButton(guild, viewer)

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

package onl.tesseract.srp.controller.event.guild.listener

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.chat.ChatEntryService
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.territory.guild.event.GuildInvitationEvent
import onl.tesseract.srp.domain.territory.guild.event.GuildLevelUpEvent
import onl.tesseract.srp.service.territory.guild.GuildService
import onl.tesseract.srp.util.GuildChatError
import onl.tesseract.srp.util.GuildChatFormat
import onl.tesseract.srp.util.GuildChatSuccess
import org.bukkit.Bukkit
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class GuildListener(
    private val guildService: GuildService,
    private val chatEntryService: ChatEntryService
) {

    @EventListener
    fun onLevelUp(event: GuildLevelUpEvent){
        val msg: Component =
            GuildChatSuccess +
                    "Ta guilde ${event.guild.name} est passée au niveau " +
                    Component.text(event.guild.level.toString(), NamedTextColor.GOLD) + " !"

        val recipients = (event.guild.members.map { it.playerID } + event.guild.leaderId).distinct()
        recipients
                .mapNotNull { Bukkit.getPlayer(it) }
                .forEach { player -> player.sendMessage(msg) }
    }

    @EventListener
    fun onInvitation(event: GuildInvitationEvent){
        val sender = Bukkit.getPlayer(event.sender)?:return
        val target = Bukkit.getPlayer(event.target)?:return
        target.sendMessage(GuildChatFormat + "${sender.name} vous invite dans la guilde ${event.guild}.")

        val acceptButton = Component.text("✔ Accepter")
                .color(NamedTextColor.GREEN)
                .clickEvent(chatEntryService.clickCommand(target) {
                    if (guildService.acceptInvitation(event.guild, target.uniqueId)) {
                        target.sendMessage(GuildChatSuccess + "Tu as rejoint la guilde ${event.guild}.")
                        sender.sendMessage(GuildChatSuccess + "${target.name} a rejoint la guilde.")
                    }
                })

        val denyButton = Component.text("✖ Refuser")
                .color(NamedTextColor.RED)
                .clickEvent(chatEntryService.clickCommand(target) {
                    if (guildService.declineInvitation(event.guild, target.uniqueId)) {
                        target.sendMessage(GuildChatError + "Invitation refusée.")
                        sender.sendMessage(GuildChatError + "${target.name} a refusé l'invitation.")
                    }
                })

        target.sendMessage(acceptButton.append(Component.text(" ")).append(denyButton))
    }
}
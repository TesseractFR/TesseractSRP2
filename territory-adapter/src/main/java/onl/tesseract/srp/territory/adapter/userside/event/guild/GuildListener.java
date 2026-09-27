package onl.tesseract.srp.territory.adapter.userside.event.guild;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.chat.ChatEntryService;
import onl.tesseract.srp.common.adapter.SrpChatFormats;
import onl.tesseract.srp.territory.domain.model.guild.event.GuildInvitationEvent;
import onl.tesseract.srp.territory.domain.model.guild.event.GuildLevelUpEvent;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.springframework.context.event.EventListener;

import java.util.LinkedHashSet;
import java.util.Set;

@org.springframework.stereotype.Component
public class GuildListener {
    private final GuildService guildService;
    private final ChatEntryService chatEntryService;

    public GuildListener(GuildService guildService, ChatEntryService chatEntryService) {
        this.guildService = guildService;
        this.chatEntryService = chatEntryService;
    }

    @EventListener
    public void onLevelUp(GuildLevelUpEvent event) {
        var guild = event.guild();
        Component message = SrpChatFormats.GUILD_CHAT_SUCCESS
                .append(Component.text("Ta guilde " + guild.getName() + " est passée au niveau "))
                .append(Component.text(Integer.toString(guild.getLevel()), NamedTextColor.GOLD))
                .append(Component.text(" !"));

        Set<java.util.UUID> recipients = new LinkedHashSet<>();
        guild.getMembers().forEach(member -> recipients.add(member.getPlayerID()));
        recipients.add(guild.getLeaderId());
        recipients.stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null)
                .forEach(player -> player.sendMessage(message));
    }

    @EventListener
    public void onInvitation(GuildInvitationEvent event) {
        Player sender = Bukkit.getPlayer(event.sender());
        Player target = Bukkit.getPlayer(event.target());
        if (sender == null || target == null) return;

        target.sendMessage(SrpChatFormats.GUILD_CHAT_FORMAT.append(Component.text(
                sender.getName() + " vous invite dans la guilde " + event.guild() + ".")));

        Component acceptButton = Component.text("✔ Accepter")
                .color(NamedTextColor.GREEN)
                .clickEvent(chatEntryService.clickCommand(target, () -> {
                    if (guildService.acceptInvitation(event.guild(), target.getUniqueId())) {
                        target.sendMessage(SrpChatFormats.GUILD_CHAT_SUCCESS.append(Component.text(
                                "Tu as rejoint la guilde " + event.guild() + ".")));
                        sender.sendMessage(SrpChatFormats.GUILD_CHAT_SUCCESS.append(Component.text(
                                target.getName() + " a rejoint la guilde.")));
                    }
                    return;
                }));

        Component denyButton = Component.text("✖ Refuser")
                .color(NamedTextColor.RED)
                .clickEvent(chatEntryService.clickCommand(target, () -> {
                    if (guildService.declineInvitation(event.guild(), target.getUniqueId())) {
                        target.sendMessage(SrpChatFormats.GUILD_CHAT_ERROR.append(Component.text("Invitation refusée.")));
                        sender.sendMessage(SrpChatFormats.GUILD_CHAT_ERROR.append(Component.text(
                                target.getName() + " a refusé l'invitation.")));
                    }
                    return;
                }));

        target.sendMessage(acceptButton.append(Component.text(" ")).append(denyButton));
    }
}

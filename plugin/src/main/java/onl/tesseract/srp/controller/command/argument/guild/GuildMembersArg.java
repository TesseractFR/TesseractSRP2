package onl.tesseract.srp.controller.command.argument.guild;

import onl.tesseract.commandBuilder.CommandArgument;
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps;
import onl.tesseract.lib.service.ServiceContainer;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class GuildMembersArg extends CommandArgument<String> {
    public GuildMembersArg(String name) {
        super(name);
    }

    @Override
    public void define(CommandArgumentBuilderSteps.Parser<String> builder) {
        builder
            .parser((input, _) -> input)
            .tabCompleter((prefix, env) -> {
                GuildService service = ServiceContainer.get(GuildService.class);
                Player player = env.getSenderAsPlayer();
                if (player == null) {
                    return List.of();
                }
                Guild guild = service.getGuildByMember(player.getUniqueId());
                if (guild == null) {
                    return List.of();
                }
                List<String> out = new ArrayList<>();
                out.add("<Membre_de_" + guild.getName() + ">");
                out.addAll(guild.getMembers().stream()
                        .map(member -> Bukkit.getOfflinePlayer(member.getPlayerID()).getName())
                        .distinct()
                        .sorted()
                        .toList());
                return out;
            });
    }
}


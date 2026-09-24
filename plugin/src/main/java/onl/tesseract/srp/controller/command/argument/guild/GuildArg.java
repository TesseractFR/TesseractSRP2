package onl.tesseract.srp.controller.command.argument.guild;

import onl.tesseract.commandBuilder.CommandArgument;
import onl.tesseract.commandBuilder.CommandArgumentBuilderSteps;
import onl.tesseract.commandBuilder.CommandArgumentException;
import onl.tesseract.lib.service.ServiceContainer;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;

public class GuildArg extends CommandArgument<Guild> {

    public GuildArg(String name) {
        super(name);
    }

    @Override
    public void define(CommandArgumentBuilderSteps.Parser<Guild> builder) {
        builder.parser((input, _) -> {
                GuildService guildService = guildService();

                Guild guild = guildService.getByName(input);
                if(guild == null){
                    throw new CommandArgumentException("Guilde $input introuvable");
                }
                return guild;
        })
            .tabCompleter((_, _) -> guildService().getAllGuilds().stream().map(Guild::getName).toList());
    }

    private GuildService guildService(){
        return ServiceContainer.get(GuildService.class);
    }

}

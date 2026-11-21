package onl.tesseract.srp.controller.command.staff;

import onl.tesseract.commandBuilder.CommandContext;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.commandBuilder.annotation.Perm;
import onl.tesseract.srp.common.adapter.SrpCommandInstanceProvider;
import onl.tesseract.srp.player.adapter.userside.controller.commands.staff.IlluminationPointStaffCommand;
import onl.tesseract.srp.player.adapter.userside.controller.commands.staff.MoneyStaffCommand;
import onl.tesseract.srp.player.adapter.userside.controller.commands.staff.PlayerRankStaffCommand;

@Command(name = "staffSrp", permission = @Perm("staff"), subCommands = {
    CustomItemStaffCommand.class,
    PlayerRankStaffCommand.class,
    CampStaffCommands.class,
    MoneyStaffCommand.class,
    GuildStaffCommand.class,
    IlluminationPointStaffCommand.class,
    ElytraStaffCommand.class,
    ContractStaffCommand.class
})
public class SrpStaffCommand extends CommandContext {
    public SrpStaffCommand(SrpCommandInstanceProvider commandInstanceProvider) {
        super(commandInstanceProvider);
    }
}


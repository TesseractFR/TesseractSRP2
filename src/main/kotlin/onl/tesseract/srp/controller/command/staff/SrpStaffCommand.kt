package onl.tesseract.srp.controller.command.staff

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.Perm
import onl.tesseract.srp.SrpCommandInstanceProvider

@Command(name = "staffSrp", permission = Perm("staff"), subCommands = [
    CustomItemStaffCommand::class,
    PlayerJobStaffCommand::class,
    PlayerRankStaffCommand::class,
    CampStaffCommands::class,
    MoneyStaffCommand::class,
    GuildStaffCommand::class,
])
class SrpStaffCommand(commandInstanceProvider: SrpCommandInstanceProvider) : CommandContext(commandInstanceProvider) {}

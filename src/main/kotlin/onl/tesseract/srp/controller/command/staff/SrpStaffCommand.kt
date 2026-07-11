package onl.tesseract.srp.controller.command.staff

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.Perm
import onl.tesseract.srp.SrpCommandInstanceProvider
import onl.tesseract.srp.customitem.adapter.userside.controller.command.CustomItemCommand

@Command(name = "staffSrp", permission = Perm("staff"), subCommands = [
    CustomItemStaffCommand::class,
    PlayerJobStaffCommand::class,
    PlayerRankStaffCommand::class,
    CampStaffCommands::class,
    MoneyStaffCommand::class,
    GuildStaffCommand::class,
    IlluminationPointStaffCommand::class,
    ElytraStaffCommand::class,
    CustomItemCommand::class
])
class SrpStaffCommand(commandInstanceProvider: SrpCommandInstanceProvider) : CommandContext(commandInstanceProvider) {}

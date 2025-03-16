package onl.tesseract.srp.controller.command.staff

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.Perm
import onl.tesseract.srp.SrpCommandInstanceProvider
import onl.tesseract.srp.controller.command.campement.CreateCampCommand

@Command(name = "campement", subCommands = [
    CreateCampCommand::class,
])
class CampementCommands(commandInstanceProvider: SrpCommandInstanceProvider) : CommandContext(commandInstanceProvider) {}
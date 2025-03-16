package onl.tesseract.srp.controller.command.campement

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.Perm
import onl.tesseract.srp.SrpCommandInstanceProvider
import onl.tesseract.srp.controller.command.campement.CreateCampCommand
import onl.tesseract.srp.controller.command.campement.DeleteCampCommand

@Command(name = "campement", subCommands = [
    CreateCampCommand::class,
    DeleteCampCommand::class,
])
class CampementCommands(commandInstanceProvider: SrpCommandInstanceProvider) : CommandContext(commandInstanceProvider) {}
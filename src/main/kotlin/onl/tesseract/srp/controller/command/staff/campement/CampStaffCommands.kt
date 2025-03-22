package onl.tesseract.srp.controller.command.staff.campement

import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.Perm

@Command(
    name = "camp",
    permission = Perm("staff"),
    subCommands = [
        CampCreateStaffCommand::class,
        CampDeleteStaffCommand::class,
    ]
)
class CampStaffCommands

package onl.tesseract.srp.controller.command.campement

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.srp.SrpCommandInstanceProvider

@Command(name = "campement", subCommands = [
    CreateCampCommand::class,
    DeleteCampCommand::class,
    SetCampSpawnCommand::class,
    CampSpawnCommand::class,
    ChunkClaimCommand::class,
    ChunkUnclaimCommand::class,
    CampTrustCommand::class,
    CampUntrustCommand::class
])
class CampementCommands(commandInstanceProvider: SrpCommandInstanceProvider) : CommandContext(commandInstanceProvider)
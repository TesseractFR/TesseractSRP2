package onl.tesseract.srp.controller.command.staff

import onl.tesseract.commandBuilder.annotation.Argument
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.lib.command.argument.IntegerCommandArgument
import onl.tesseract.srp.controller.command.argument.CustomMaterialArg
import onl.tesseract.srp.controller.command.argument.QualityArg
import onl.tesseract.srp.customitem.adapter.userside.ItemGateway
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "customItem", playerOnly = true)
class CustomItemStaffCommand(private val customItemService: ItemGateway) {

    @Command
    fun give(player: Player,
             @Argument("material") materialArg: CustomMaterialArg,
             @Argument("quality", optional = true, def = "POOR") quality: QualityArg,
             @Argument("amount", optional = true, def = "1") amount: IntegerCommandArgument
    ) {
        customItemService.addItem(player.uniqueId,materialArg.get().name, quality.get(),amount.get())
    }
}
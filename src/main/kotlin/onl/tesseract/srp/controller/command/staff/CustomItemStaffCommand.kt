package onl.tesseract.srp.controller.command.staff

import onl.tesseract.commandBuilder.annotation.Argument
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.lib.command.argument.IntegerCommandArgument
import onl.tesseract.srp.service.item.CustomItemService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "customItem", playerOnly = true)
class CustomItemStaffCommand(private val customItemService: CustomItemService) {

    @Command
    fun give(player: Player, @Argument("material") materialArg: CustomMaterialArg, @Argument("amount", optional = true, def = "1") amount: IntegerCommandArgument) {
        val item = customItemService.createCustomItem(materialArg.get(), amount.get())
        player.inventory.addItem(item)
    }
}
package onl.tesseract.srp.controller.command.staff

import onl.tesseract.commandBuilder.annotation.Argument
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.lib.command.argument.IntegerCommandArgument
import onl.tesseract.srp.controller.command.argument.CustomMaterialArg
import onl.tesseract.srp.domain.item.CustomItem
import onl.tesseract.srp.service.item.CustomItemService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "customItem", playerOnly = true)
class CustomItemStaffCommand(private val customItemService: CustomItemService) {

    @Command
    fun give(player: Player,
             @Argument("material") materialArg: CustomMaterialArg,
             @Argument("quality", optional = true, def = "100") quality: IntegerCommandArgument,
             @Argument("amount", optional = true, def = "1") amount: IntegerCommandArgument) {
        val item = customItemService.createCustomItem(CustomItem(materialArg.get(), quality.get()).toStack(amount.get()))
        player.inventory.addItem(item)
    }
}
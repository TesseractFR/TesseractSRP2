package onl.tesseract.srp.domain.shop

import onl.tesseract.srp.domain.guild.Guild
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

class GuildShop(val guildId: Int,signLocation: Location, item: ItemStack, price: Float, type: ShopType) :
        Shop(signLocation, item, price, type, OwnerType.GUILD) {

}
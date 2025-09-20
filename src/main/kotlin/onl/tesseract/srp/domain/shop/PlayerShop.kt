package onl.tesseract.srp.domain.shop

import onl.tesseract.srp.domain.player.SrpPlayer
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.util.UUID

class PlayerShop(val playerUuid: UUID,signLocation: Location, item: ItemStack, price: Float, type: ShopType) :
        Shop(signLocation, item, price, type, OwnerType.PLAYER) {

}
package onl.tesseract.srp.domain.shop

import onl.tesseract.lib.util.addItem
import onl.tesseract.lib.util.availableSpace
import onl.tesseract.lib.util.countItem
import onl.tesseract.lib.util.removeItem
import org.bukkit.Location
import org.bukkit.block.Chest
import org.bukkit.block.Container
import org.bukkit.block.Sign
import org.bukkit.block.data.Directional
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack


abstract class Shop(val signLocation: Location,
                    val item: ItemStack,
                    val price: Float,
                    var type : ShopType,
                    val ownerType: OwnerType
                    ) {

    val inventory: Inventory
    val sign: Sign
    var id: Int? = null
    init {
        val blockSign = signLocation.block
        require(blockSign.state is Sign) {
            throw IllegalArgumentException("La position donnée n'est pas un panneau !")
        }
        sign = blockSign.state as Sign
        val chestBlock = sign.block.getRelative((sign.blockData as Directional).facing.oppositeFace)
        require(chestBlock.state is Container) {
            throw IllegalArgumentException("Aucun coffre trouvé derrière le panneau !")
        }
        inventory = (chestBlock.state as Chest).blockInventory
    }


    fun getStock(): Int {
        return inventory.countItem(item)
    }
    fun getAvailableSpace(): Int {
        return inventory.availableSpace(item)
    }

    fun retreiveItem(number: Int){
        require(getStock()>=number)
        inventory.removeItem(item,number)
    }

    fun addItem(number: Int){
        require(getAvailableSpace()>=number)
        inventory.addItem(item,number)
    }



}
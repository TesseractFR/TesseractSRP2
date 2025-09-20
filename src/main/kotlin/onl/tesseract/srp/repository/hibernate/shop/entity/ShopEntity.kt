package onl.tesseract.srp.repository.hibernate.shop.entity

import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import onl.tesseract.srp.domain.shop.GuildShop
import onl.tesseract.srp.domain.shop.OwnerType
import onl.tesseract.srp.domain.shop.PlayerShop
import onl.tesseract.srp.domain.shop.Shop
import onl.tesseract.srp.domain.shop.ShopType
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import org.springframework.cache.annotation.Cacheable
import java.util.*
import onl.tesseract.srp.repository.hibernate.shop.converter.ItemStackConverter

@Entity
@Table(name = "t_shop")
@Cacheable(value = ["t_shop"], key = "#id")
data class ShopEntity(
    @Id
    var id: Long? = null,

    var ownerType: OwnerType,

    var playerUUID: UUID?,

    var guildId: Int?,

    @Convert(converter = ItemStackConverter::class)
    var item: ItemStack,

    val shopType: ShopType,

    val world: String,
    val x: Double,
    val y: Double,
    val z: Double,

    var price: Float,

    ){
    fun toDomain() : Shop{
        if (ownerType == OwnerType.GUILD){
            return GuildShop(guildId!!,Location(Bukkit.getWorld(world),x,y,z),
                item,price,shopType)
        }
        return PlayerShop(playerUUID!!,Location(Bukkit.getWorld(world), x, y, z),
            item, price, shopType)
    }
}

fun Shop.toEntity() : ShopEntity{
    return ShopEntity(
        id = null, // Will be generated
        ownerType = ownerType,
        playerUUID = if(this is PlayerShop) this.playerUuid else null, // Get player UUID based on shop type
        guildId = if (this is GuildShop) this.guildId else null, // Get guild ID based on shop type
        item = item,
        shopType = type,
        world = signLocation.world!!.name,
        x = signLocation.x,
        y = signLocation.y,
        z = signLocation.z,
        price = price
    )
}



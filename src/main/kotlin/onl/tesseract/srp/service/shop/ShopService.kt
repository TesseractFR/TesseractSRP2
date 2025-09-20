package onl.tesseract.srp.service.shop

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.shop.*
import onl.tesseract.srp.repository.generic.ShopRepository
import onl.tesseract.srp.service.campement.CampementService
import onl.tesseract.srp.service.guild.GuildService
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.block.data.Directional
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class ShopService(
    private val repository: ShopRepository,
    private val campementService: CampementService,
    private val guildService: GuildService,
    private val playerService: SrpPlayerService,
){
    
    // Temporary storage for players in shop creation process
    private val pendingShopCreations = ConcurrentHashMap<UUID, PendingShopCreation>()
    
    // ThreadLocal to store current player context for repository operations
    companion object {
        private val currentPlayerContext = ThreadLocal<UUID>()

        fun getCurrentPlayerUUID(): UUID? = currentPlayerContext.get()

        fun clearPlayerContext() = currentPlayerContext.remove()
    }
    
    data class PendingShopCreation(
        val chest: Block,
        val item: ItemStack,
        val timestamp: Long = System.currentTimeMillis(),
    )
    
    fun initiateShopCreation(player: Player, chest: Block, item: ItemStack) {
        // Store the pending shop creation
        pendingShopCreations[player.uniqueId] = PendingShopCreation(chest, item.clone())
        
        // Start a timer to clean up pending creation after 30 seconds
        // TODO: Implement proper cleanup mechanism
    }
    
    fun processPriceInput(player: Player, priceInput: String): Boolean {
        val pending = pendingShopCreations[player.uniqueId] ?: return false
        
        // Validate price input
        val price = try {
            priceInput.toFloat()
        } catch (e: NumberFormatException) {
            player.sendMessage(Component.text("Prix invalide! Utilise un nombre.", NamedTextColor.RED))
            return false
        }

        if (price <= 0) {
            player.sendMessage(Component.text("Le prix doit être supérieur à 0!", NamedTextColor.RED))
            return false
        }

        // Determine le type de propriété
        val shopCreationResult = determineShopOwner(player, pending.chest.location)
        pendingShopCreations.remove(player.uniqueId)

        if (!shopCreationResult.canCreate) {
            player.sendMessage(Component.text(shopCreationResult.reason, NamedTextColor.RED))
            return false
        }

            val success = createShop(
                player = player,
                chest = pending.chest,
                item = pending.item,
                price = price,
                shopType = shopCreationResult.shopType!!,
                ownerType = shopCreationResult.ownerType!!
            )

            if (success) {
                player.sendMessage(
                    Component.text("Shop créé avec succès pour ", NamedTextColor.GREEN) +
                            Component.text(pending.item.type.name, NamedTextColor.GOLD) +
                            Component.text(" au prix de ", NamedTextColor.GREEN) +
                            Component.text("$price", NamedTextColor.YELLOW)
                )
            } else {
                player.sendMessage(Component.text("Erreur lors de la création du shop!", NamedTextColor.RED))
            }
            return success

    }
    
    private data class ShopCreationResult(
        val canCreate: Boolean,
        val shopType: ShopType? = null,
        val ownerType: OwnerType? = null,
        val reason: String = "",
    )

    fun canCreateShop(player: Player,block: Block): Boolean{
        return determineShopOwner(player,block.location).canCreate
    }

    private fun determineShopOwner(player: Player, location: Location): ShopCreationResult {
        val chunk = location.chunk
        // Vérifie si le joueur est dans son campement
        val campement = campementService.getCampementByChunk(chunk.x, chunk.z)
        if (campement != null && campement.ownerID == player.uniqueId) {
            return ShopCreationResult(
                canCreate = true,
                shopType = ShopType.SELL, // Default to sell shop
                ownerType = OwnerType.PLAYER
            )
        }
        
        // Check if player is on guild territory with appropriate permissions
        // For now, we'll implement a basic version that allows guild shop creation
        // TODO: Implement proper guild territory and rank validation when guild domain is available
        try {
            val srpPlayer = playerService.getPlayer(player.uniqueId)
            if (srpPlayer != null) {
                // Simplified guild check - if player has sufficient rank, allow guild shop creation anywhere for now
                // This should be enhanced to check actual guild territory and rank
                return ShopCreationResult(
                    canCreate = false, // Disabled for now until proper guild integration
                    shopType = ShopType.SELL,
                    ownerType = OwnerType.GUILD,
                    reason = "Création de shops de guilde temporairement désactivée"
                )
            }
        } catch (e: Exception) {
            // If guild checking fails, continue with denial
        }
        
        // If not in camp or guild territory, deny creation
        return ShopCreationResult(
            canCreate = false,
            reason = "Tu ne peux créer un shop que dans ton campement ou sur le territoire de ta guilde (si tu es au moins adjoint)!"
        )
    }
    
    private fun createShop(
        player: Player,
        chest: Block,
        item: ItemStack,
        price: Float,
        shopType: ShopType,
        ownerType: OwnerType,
    ): Boolean {
        // Store player context for repository access
        currentPlayerContext.set(player.uniqueId)
        return try {
            // Get the sign loc
            val dir = (chest.getBlockData() as Directional).getFacing()
            val signLocation = chest.location.clone().add(dir.getDirection())
            // Create the appropriate shop type
            val shop = when (ownerType) {
                OwnerType.GUILD -> GuildShop(0,signLocation, item, price, shopType)
                OwnerType.PLAYER -> PlayerShop(player.uniqueId,signLocation, item, price, shopType)
            }
            
            // Save the shop to repository
            repository.save(shop)
            
            // Update the sign with shop information
            updateShopSign(signLocation, shop)
            
            true
        } catch (e: Exception) {
            player.sendMessage(Component.text("Erreur: ${e.message}", NamedTextColor.RED))
            false
        } finally {
            // Clean up player context
            clearPlayerContext()
        }
    }

    private fun updateShopSign(signLocation: Location, shop: Shop) {
        val sign = signLocation.block.state as Sign
        sign.line(0, Component.text("[SHOP]", NamedTextColor.DARK_BLUE))
        sign.line(1, Component.text(shop.type.name, NamedTextColor.BLUE))
        sign.line(2, Component.text(shop.item.type.name, NamedTextColor.GOLD))
        sign.line(3, Component.text("${shop.price} lys", NamedTextColor.GREEN))
        sign.update()
    }
}
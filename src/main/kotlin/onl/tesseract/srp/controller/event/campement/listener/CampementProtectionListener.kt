package onl.tesseract.srp.controller.event.campement.listener

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.controller.event.global.listener.ChunkProtectionListener
import onl.tesseract.srp.domain.territory.ChunkCoord
import onl.tesseract.srp.service.territory.campement.CampementService
import onl.tesseract.srp.util.InteractionAllowResult
import onl.tesseract.srp.util.CampementChatError
import onl.tesseract.srp.util.EntityUtils
import onl.tesseract.srp.util.PlayerUtils
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.*
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class CampementProtectionListener(
    val campementService: CampementService,
    playerUtils: PlayerUtils,
    entityUtils: EntityUtils
) : ChunkProtectionListener(playerUtils, entityUtils) {
    override fun hasProcessingResponsibility(chunk: Chunk): Boolean {
        return campementService.getByChunk(ChunkCoord(chunk.x,chunk.z,chunk.world.name)) != null
    }

    override fun getProtectionMessage(chunk : Chunk): Component {
        val camp = campementService.getByChunk(ChunkCoord(chunk.x,chunk.z,chunk.world.name))
        require(camp!=null){

        }
        val ownerName = Bukkit.getOfflinePlayer(camp.ownerID).name ?: "Inconnu"
        return CampementChatError + "Tu ne peux pas interagir ici ! Ce terrain appartient à " +
                    Component.text(ownerName, NamedTextColor.GOLD) + "."

    }

    override fun canPlaceBlock(player: Player, block: Block): Boolean {
        return (InteractionAllowResult.Deny != campementService.canInteractInChunk(player.uniqueId,block.chunk))
    }

    override fun canBreakBlock(player: Player, block: Block): Boolean {
        return (InteractionAllowResult.Deny != campementService.canInteractInChunk(player.uniqueId,block.chunk))
    }

    override fun canDamagePassiveEntity(player: Player, entity: LivingEntity): Boolean {
        return (InteractionAllowResult.Deny != campementService.canInteractInChunk(player.uniqueId,entity.chunk))
    }

    override fun canHostileDamagePlayer(player: Player, attacker: Entity): Boolean {
        return false
    }

    override fun canUseBucket(player: Player, block: Block): Boolean {
        return (InteractionAllowResult.Deny != campementService.canInteractInChunk(player.uniqueId,block.chunk))
    }

    override fun canPlayerIgnite(player: Player, block: Block): Boolean {
        return (InteractionAllowResult.Deny != campementService.canInteractInChunk(player.uniqueId,block.chunk))
    }

    override fun canNaturallyIgnite(block: Block, cause: BlockIgniteEvent.IgniteCause): Boolean {
        return false
    }

    override fun canUseRedstone(player: Player, block: Block): Boolean {
        return (InteractionAllowResult.Deny != campementService.canInteractInChunk(player.uniqueId,block.chunk))
    }

    override fun canFishEntity(player: Player, entity: Entity): Boolean {
        return (InteractionAllowResult.Deny !=
                campementService.canInteractInChunk(player.uniqueId,entity.location.chunk))
    }

    override fun canSaddleEntity(player: Player, entity: LivingEntity): Boolean {
        return (InteractionAllowResult.Deny !=
                campementService.canInteractInChunk(player.uniqueId,entity.location.chunk))
    }

    override fun canMountEntity(player: Player, mount: Entity): Boolean {
        return (InteractionAllowResult.Deny !=
                campementService.canInteractInChunk(player.uniqueId,mount.location.chunk))
    }

    override fun canEnterVehicle(player: Player, vehicle: Vehicle): Boolean {
        return (InteractionAllowResult.Deny !=
                campementService.canInteractInChunk(player.uniqueId,vehicle.location.chunk))
    }

    override fun canBreakVehicle(player: Player, vehicle: Vehicle): Boolean {
        return (InteractionAllowResult.Deny !=
                campementService.canInteractInChunk(player.uniqueId, vehicle.location.chunk))
    }

    override fun canBreakHanging(player: Player, hanging: Hanging): Boolean {
        return (InteractionAllowResult.Deny !=
                campementService.canInteractInChunk(player.uniqueId,hanging.location.chunk))
    }

    override fun canEditItemFrame(player: Player, frame: ItemFrame, action: ItemFrameAction): Boolean {
        return (InteractionAllowResult.Deny !=
                campementService.canInteractInChunk(player.uniqueId,frame.location.chunk))
    }

    override fun canExplosionAffect(chunk: Chunk, source: Entity?, cause: ExplosionCause): Boolean {
        return false
    }

    override fun canLeashEntity(player: Player, entity: LivingEntity, action: LeashAction): Boolean {
        return (InteractionAllowResult.Deny !=
                campementService.canInteractInChunk(player.uniqueId,entity.chunk))
    }

    override fun canShearEntity(player: Player, entity: LivingEntity): Boolean {
        return (InteractionAllowResult.Deny !=
                campementService.canInteractInChunk(player.uniqueId,entity.chunk))
    }

    override fun canBucketMob(player: Player, entity: LivingEntity): Boolean {
        return (InteractionAllowResult.Deny !=
                campementService.canInteractInChunk(player.uniqueId,entity.chunk))
    }

    override fun canNameEntity(player: Player, entity: LivingEntity, newName: Component): Boolean {
        return (InteractionAllowResult.Deny !=
                campementService.canInteractInChunk(player.uniqueId,entity.chunk))
    }

    override fun canEditArmorStand(
        player: Player,
        stand: ArmorStand,
        action: ArmorStandAction,
        slot: EquipmentSlot,
        playerItem: ItemStack,
        standItem: ItemStack
    ): Boolean {
        return (InteractionAllowResult.Deny !=
                campementService.canInteractInChunk(player.uniqueId,stand.chunk))
    }

    override fun canBreakArmorStand(player: Player, stand: ArmorStand): Boolean {
        return (InteractionAllowResult.Deny !=
                campementService.canInteractInChunk(player.uniqueId,stand.chunk))
    }

    override fun canOpenContainer(
        player: Player,
        container: Container,
    ): Boolean {
        if(Material.ENDER_CHEST == container.type) return true
        return (InteractionAllowResult.Deny != campementService.canInteractInChunk(player.uniqueId,container.chunk))
    }
}

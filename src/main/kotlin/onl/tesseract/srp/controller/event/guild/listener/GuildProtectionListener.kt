package onl.tesseract.srp.controller.event.guild.listener

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.controller.event.global.listener.ChunkProtectionListener
import onl.tesseract.srp.mapper.toChunkCoord
import onl.tesseract.srp.service.territory.guild.GuildService
import onl.tesseract.srp.util.*
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
class GuildProtectionListener(
    private val guildService: GuildService
) : ChunkProtectionListener() {
    override fun hasProcessingResponsibility(chunk: Chunk): Boolean {
        return guildService.getByChunk(chunk.toChunkCoord()) != null
    }

    override fun getProtectionMessage(chunk: Chunk): Component {
        val guild = guildService.getGuildByChunk(chunk.toChunkCoord())
        require(guild != null) {
        }
        val guildName = guild.name
        return GuildChatError + "Tu ne peux pas interagir ici ! Ce terrain appartient à la guilde " +
                Component.text(guildName, NamedTextColor.GOLD) + "."
    }

    override fun canPlaceBlock(player: Player, block: Block): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, block.chunk.toChunkCoord()))
    }

    override fun canBreakBlock(player: Player, block: Block): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, block.chunk.toChunkCoord()))
    }

    override fun canOpenContainer(player: Player, container: Container): Boolean {
        if(Material.ENDER_CHEST == container.type) return true
            return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId,container.chunk.toChunkCoord()))
        }

    override fun canDamagePassiveEntity(player: Player, entity: LivingEntity): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, entity.chunk.toChunkCoord()))
    }

    override fun canHostileDamagePlayer(player: Player, attacker: Entity): Boolean {
        return false
    }

    override fun canUseBucket(player: Player, block: Block): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, block.chunk.toChunkCoord()))
    }

    override fun canPlayerIgnite(player: Player, block: Block): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, block.chunk.toChunkCoord()))
    }

    override fun canNaturallyIgnite(block: Block, cause: BlockIgniteEvent.IgniteCause): Boolean {
        return false
    }

    override fun canUseRedstone(player: Player, block: Block): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, block.chunk.toChunkCoord()))
    }

    override fun canFishEntity(player: Player, entity: Entity): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, entity.location.chunk.toChunkCoord()))
    }

    override fun canSaddleEntity(player: Player, entity: LivingEntity): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, entity.location.chunk.toChunkCoord()))
    }

    override fun canMountEntity(player: Player, mount: Entity): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, mount.location.chunk.toChunkCoord()))
    }

    override fun canEnterVehicle(player: Player, vehicle: Vehicle): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, vehicle.location.chunk.toChunkCoord()))
    }

    override fun canBreakVehicle(player: Player, vehicle: Vehicle): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, vehicle.location.chunk.toChunkCoord()))
    }

    override fun canBreakHanging(player: Player, hanging: Hanging): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, hanging.location.chunk.toChunkCoord()))
    }

    override fun canEditItemFrame(player: Player, frame: ItemFrame, action: ItemFrameAction): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, frame.location.chunk.toChunkCoord()))
    }

    override fun canExplosionAffect(chunk: Chunk, source: Entity?, cause: ExplosionCause): Boolean {
        return false
    }

    override fun canLeashEntity(player: Player, entity: LivingEntity, action: LeashAction): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, entity.location.chunk.toChunkCoord()))
    }

    override fun canShearEntity(player: Player, entity: LivingEntity): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, entity.location.chunk.toChunkCoord()))
    }

    override fun canBucketMob(player: Player, entity: LivingEntity): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, entity.location.chunk.toChunkCoord()))
    }

    override fun canNameEntity(player: Player, entity: LivingEntity, newName: Component): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, entity.location.chunk.toChunkCoord()))
    }

    override fun canEditArmorStand(
        player: Player,
        stand: ArmorStand,
        action: ArmorStandAction,
        slot: EquipmentSlot,
        playerItem: ItemStack,
        standItem: ItemStack
    ): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, stand.chunk.toChunkCoord()))
    }

    override fun canBreakArmorStand(player: Player, stand: ArmorStand): Boolean {
        return (InteractionAllowResult.Deny != guildService.canInteractInChunk(player.uniqueId, stand.chunk.toChunkCoord()))
    }
}
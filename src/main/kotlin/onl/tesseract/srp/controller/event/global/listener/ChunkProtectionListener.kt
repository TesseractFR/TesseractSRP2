package onl.tesseract.srp.controller.event.global.listener

import net.kyori.adventure.text.Component
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.data.Openable
import org.bukkit.block.data.Powerable
import org.bukkit.block.data.type.Repeater
import org.bukkit.block.data.type.Switch
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerInteractEvent

abstract class ChunkProtectionListener : Listener {
    protected abstract fun hasProcessingResponsibility(chunk: Chunk) : Boolean

    protected abstract fun getProtectionMessage(chunk: Chunk): Component

    protected abstract fun canPlaceBlock(player: Player,block: Block) : Boolean

    protected abstract fun canBreakBlock(player: Player,block: Block) : Boolean

    protected abstract fun canOpenContainer(player: Player, container: Container) : Boolean

    protected abstract fun canDamagePassiveEntity(player: Player, entity: LivingEntity): Boolean

    protected abstract fun canUseBucket(player: Player, block: Block): Boolean

    protected abstract fun canPlayerIgnite(player: Player, block: Block): Boolean
    protected abstract fun canNaturallyIgnite(block: Block, cause: BlockIgniteEvent.IgniteCause): Boolean

    protected abstract fun canUseRedstone(player: Player,block: Block): Boolean


    @EventHandler
    fun onPlayerPlaceBlock(event: BlockPlaceEvent){
        if(!hasProcessingResponsibility(event.block.chunk))return
        if(!canPlaceBlock(event.player,event.block)){
            event.player.sendMessage { getProtectionMessage(event.block.chunk) }
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerOpenContainer(event: PlayerInteractEvent) {
        if(!event.hasBlock()
            || event.clickedBlock == null
            || event.clickedBlock!!.state !is Container)return
        if(!canOpenContainer(event.player,event.clickedBlock!!.state as Container)){
                event.player.sendMessage { getProtectionMessage(event.clickedBlock!!.chunk) }
                event.isCancelled = true
            }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if(!hasProcessingResponsibility(event.block.chunk))return
        if(!canBreakBlock(event.player,event.block)){
            event.player.sendMessage { getProtectionMessage(event.block.chunk) }
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onEntityDamagePassive(event: EntityDamageByEntityEvent) {
        if (event.entity is Player || event.entity is Monster) return
        if (!hasProcessingResponsibility(event.entity.chunk)) return
        if (!canDamagePassiveEntity(event.damager as Player, event.entity as LivingEntity)) {
            event.damager.sendMessage { getProtectionMessage(event.entity.chunk) }
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBucketEmpty(event: PlayerBucketEmptyEvent) {
        if (!hasProcessingResponsibility(event.block.chunk)) return
        if (!canUseBucket(event.player, event.block)) {
            event.player.sendMessage { getProtectionMessage(event.block.chunk) }
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBucketFill(event: PlayerBucketFillEvent) {
        if (!hasProcessingResponsibility(event.block.chunk)) return
        if (!canUseBucket(event.player, event.block)) {
            event.player.sendMessage { getProtectionMessage(event.block.chunk) }
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onFlintAndSteelIgnite(event: BlockIgniteEvent) {
        if (!hasProcessingResponsibility(event.block.chunk)) return
        val player: Player? = when (val e = event.ignitingEntity) {
            is Player -> e
            is Projectile -> e.shooter as? Player
            else -> null
        }
        val allowed = if (player != null) {
            canPlayerIgnite(player, event.block)
        } else {
            canNaturallyIgnite(event.block, event.cause)
        }
        if (!allowed) {
            player?.sendMessage { getProtectionMessage(event.block.chunk) }
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onRedstoneUse(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return

        val data = block.blockData
        val isRedstoneInteractive =
            data is Switch ||
                    data is Powerable ||
                    data is Openable ||
                    data is Repeater ||
                    data is Comparator<*>

        if (isRedstoneInteractive
            && hasProcessingResponsibility(block.chunk)
            && !canUseRedstone(event.player, block)
        ) {
            event.player.sendMessage { getProtectionMessage(block.chunk) }
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onRedstonePressurePlate(event: PlayerInteractEvent) {
        if (event.action != Action.PHYSICAL) return
        val block = event.clickedBlock ?: return
        val shouldCancel =
            hasProcessingResponsibility(block.chunk) &&
                    isPressurePlate(block.type) &&
                    !canUseRedstone(event.player, block)
        if (shouldCancel) {
            event.player.sendMessage { getProtectionMessage(block.chunk) }
            event.isCancelled = true
        }
    }
    private fun isPressurePlate(type: Material): Boolean = type.name.endsWith("_PRESSURE_PLATE")
}
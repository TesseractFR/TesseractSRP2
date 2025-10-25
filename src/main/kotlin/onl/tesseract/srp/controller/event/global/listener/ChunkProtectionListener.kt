package onl.tesseract.srp.controller.event.global.listener

import io.papermc.paper.event.player.PlayerItemFrameChangeEvent
import io.papermc.paper.event.player.PlayerNameEntityEvent
import net.kyori.adventure.text.Component
import onl.tesseract.srp.PLUGIN_INSTANCE
import onl.tesseract.srp.util.EntityUtils
import onl.tesseract.srp.util.PlayerUtils
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.*
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.*
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.player.*
import org.bukkit.event.vehicle.VehicleDamageEvent
import org.bukkit.event.vehicle.VehicleDestroyEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

private val nameTagMsgOnce = mutableSetOf<String>()
private val NEUTRAL_INTERACTABLES = setOf(
    Material.CRAFTING_TABLE,
    Material.CARTOGRAPHY_TABLE,
    Material.LOOM,
    Material.GRINDSTONE,
    Material.STONECUTTER,
    Material.FLETCHING_TABLE,
    Material.SMITHING_TABLE,
    Material.BELL,
    Material.ENCHANTING_TABLE
)

abstract class ChunkProtectionListener(
    private val playerUtils: PlayerUtils,
    private val entityUtils: EntityUtils
) : Listener {
    protected abstract fun hasProcessingResponsibility(chunk: Chunk) : Boolean
    protected abstract fun getProtectionMessage(chunk: Chunk): Component
    protected abstract fun canPlaceBlock(player: Player,block: Block) : Boolean
    protected abstract fun canBreakBlock(player: Player,block: Block) : Boolean
    protected abstract fun canOpenContainer(player: Player, container: Container) : Boolean
    protected abstract fun canDamagePassiveEntity(player: Player, entity: LivingEntity): Boolean
    protected abstract fun canHostileDamagePlayer(player: Player, attacker: Entity): Boolean
    protected abstract fun canUseBucket(player: Player, block: Block): Boolean
    protected abstract fun canPlayerIgnite(player: Player, block: Block): Boolean
    protected abstract fun canNaturallyIgnite(block: Block, cause: BlockIgniteEvent.IgniteCause): Boolean
    protected abstract fun canUseRedstone(player: Player,block: Block): Boolean
    protected abstract fun canFishEntity(player: Player, entity: Entity): Boolean
    protected abstract fun canSaddleEntity(player: Player, entity: LivingEntity): Boolean
    protected abstract fun canMountEntity(player: Player, mount: Entity): Boolean
    protected abstract fun canEnterVehicle(player: Player, vehicle: Vehicle): Boolean
    protected abstract fun canBreakVehicle(player: Player, vehicle: Vehicle): Boolean
    protected abstract fun canBreakHanging(player: Player, hanging: Hanging): Boolean
    protected abstract fun canEditItemFrame(player: Player, frame: ItemFrame, action: ItemFrameAction): Boolean
    protected abstract fun canExplosionAffect(chunk: Chunk, source: Entity?, cause: ExplosionCause): Boolean
    protected abstract fun canLeashEntity(player: Player, entity: LivingEntity, action: LeashAction): Boolean
    protected abstract fun canShearEntity(player: Player, entity: LivingEntity): Boolean
    protected abstract fun canBucketMob(player: Player, entity: LivingEntity): Boolean
    protected abstract fun canNameEntity(player: Player, entity: LivingEntity, newName: Component): Boolean
    protected abstract fun canEditArmorStand(player: Player, stand: ArmorStand, action: ArmorStandAction,
        slot: EquipmentSlot, playerItem: ItemStack, standItem: ItemStack): Boolean
    protected abstract fun canBreakArmorStand(player: Player, stand: ArmorStand): Boolean

    enum class LeashAction { ATTACH, DETACH }
    enum class ExplosionCause { ENTITY, BLOCK }
    enum class ItemFrameAction { PLACE_ITEM, ROTATE_ITEM, REMOVE_ITEM }
    enum class ArmorStandAction { EQUIP, UNEQUIP, SWAP}

    private fun deny(player: Player?, chunk: Chunk, event: Cancellable) {
        player?.sendMessage { getProtectionMessage(chunk) }
        event.isCancelled = true
    }

    protected open fun canInteractWithBlock(player: Player, block: Block): Boolean {
        if (block.type in NEUTRAL_INTERACTABLES) return true
        val state = block.state
        return if (state is Container) {
            canOpenContainer(player, state)
        } else {
            canUseRedstone(player, block)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerPlaceBlock(event: BlockPlaceEvent){
        if(!hasProcessingResponsibility(event.block.chunk))return
        if(!canPlaceBlock(event.player,event.block)){
            deny(event.player, event.block.chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerOpenContainer(event: PlayerInteractEvent) {
        if(!event.hasBlock()
            || event.clickedBlock == null
            || event.clickedBlock!!.state !is Container)return
        if(!hasProcessingResponsibility(event.clickedBlock!!.chunk))return
        if(!canOpenContainer(event.player,event.clickedBlock!!.state as Container)){
            deny(event.player, event.clickedBlock!!.chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        if(!hasProcessingResponsibility(event.block.chunk))return
        if(!canBreakBlock(event.player,event.block)){
            deny(event.player, event.block.chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityDamagePassive(event: EntityDamageByEntityEvent) {
        val victim = event.entity
        if (victim !is LivingEntity || victim is Player || victim is Monster) return
        if (!hasProcessingResponsibility(victim.chunk)) return
        val player = playerUtils.asPlayer(event.damager)
        if (player != null && !canDamagePassiveEntity(player, victim)) {
            deny(player, victim.chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onHostileDamagesPlayer(event: EntityDamageByEntityEvent) {
        val player = event.entity as? Player ?: return
        val attacker: Entity = when (val d = event.damager) {
            is Projectile -> d.shooter as? Entity ?: d
            else -> d
        }
        val chunk = player.location.chunk
        val isHostile = attacker is Monster || attacker is EnderDragon
        if (!hasProcessingResponsibility(chunk) || !isHostile) {
            return
        }
        if (!canHostileDamagePlayer(player, attacker)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBucketEmpty(event: PlayerBucketEmptyEvent) {
        if (!hasProcessingResponsibility(event.block.chunk)) return
        if (!canUseBucket(event.player, event.block)) {
            deny(event.player, event.block.chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBucketFill(event: PlayerBucketFillEvent) {
        if (!hasProcessingResponsibility(event.block.chunk)) return
        if (!canUseBucket(event.player, event.block)) {
            deny(event.player, event.block.chunk, event)
        }
    }

    private fun handleIgnitionDecision(
        block: Block,
        player: Player?,
        naturalCause: BlockIgniteEvent.IgniteCause?
    ): Boolean {
        return if (player != null) {
            canPlayerIgnite(player, block)
        } else {
            canNaturallyIgnite(block, naturalCause ?: BlockIgniteEvent.IgniteCause.EXPLOSION)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onIgnite(event: BlockIgniteEvent) {
        val chunk = event.block.chunk
        if (!hasProcessingResponsibility(chunk)) return
        val player = playerUtils.asPlayer(event.ignitingEntity)
        val allowed = handleIgnitionDecision(
            block = event.block,
            player = player,
            naturalCause = event.cause
        )
        if (!allowed) {
            deny(player, event.block.chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onTntPrime(event: TNTPrimeEvent) {
        val block = event.block
        val chunk = block.chunk
        if (!hasProcessingResponsibility(chunk)) return

        val player = playerUtils.asPlayer(event.primingEntity)
        val cause = when (event.cause) {
            TNTPrimeEvent.PrimeCause.FIRE        -> BlockIgniteEvent.IgniteCause.SPREAD
            TNTPrimeEvent.PrimeCause.REDSTONE    -> BlockIgniteEvent.IgniteCause.SPREAD
            TNTPrimeEvent.PrimeCause.PLAYER      -> BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL
            TNTPrimeEvent.PrimeCause.EXPLOSION   -> BlockIgniteEvent.IgniteCause.EXPLOSION
            TNTPrimeEvent.PrimeCause.PROJECTILE  -> BlockIgniteEvent.IgniteCause.FIREBALL
            TNTPrimeEvent.PrimeCause.BLOCK_BREAK -> BlockIgniteEvent.IgniteCause.SPREAD
            TNTPrimeEvent.PrimeCause.DISPENSER   -> BlockIgniteEvent.IgniteCause.SPREAD
        }
        val allowed = handleIgnitionDecision(
            block = block,
            player = playerUtils.asPlayer(event.primingEntity),
            naturalCause = cause
        )
        if (!allowed) {
            deny(player, event.block.chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onAnyExplosionDamageEntity(event: EntityDamageEvent) {
        val cause = event.cause
        if (cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION &&
            cause != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) return
        val victimChunk = event.entity.location.chunk
        if (!hasProcessingResponsibility(victimChunk)) return
        val sourceEntity: Entity? = (event as? EntityDamageByEntityEvent)?.damager
        val explosionCause = if (cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)
            ExplosionCause.ENTITY else ExplosionCause.BLOCK
        if (!canExplosionAffect(victimChunk, sourceEntity, explosionCause)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerBlockInteract(event: PlayerInteractEvent) {
        if (event.hand == EquipmentSlot.OFF_HAND || !event.hasBlock() || event.clickedBlock == null) return
        val block = event.clickedBlock!!
        val isRelevantAction =
            event.action == Action.RIGHT_CLICK_BLOCK || event.action == Action.PHYSICAL
        val isTurtleEgg = block.type == Material.TURTLE_EGG

        if (isRelevantAction && hasProcessingResponsibility(block.chunk)) {
            if (isTurtleEgg) {
                event.isCancelled = true
                return
            }
            if (!canInteractWithBlock(event.player, block)) {
                deny(event.player, block.chunk, event)
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerFishEntity(event: PlayerFishEvent) {
        if (event.state != PlayerFishEvent.State.CAUGHT_ENTITY || event.caught == null) return
        val chunk = event.caught!!.location.chunk
        if (!hasProcessingResponsibility(chunk)) return
        if (!canFishEntity(event.player, event.caught!!)) {
            deny(event.player, chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onTurtleEggTrampleInteract(event: EntityInteractEvent) {
        val block = event.block
        if (block.type == Material.TURTLE_EGG && hasProcessingResponsibility(block.chunk)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onTurtleEggTrampleChange(event: EntityChangeBlockEvent) {
        val block = event.block
        if (block.type == Material.TURTLE_EGG && hasProcessingResponsibility(block.chunk)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerSaddleEntity(event: PlayerInteractEntityEvent) {
        val entity = event.rightClicked
        if (!hasProcessingResponsibility(entity.location.chunk) || !entityUtils.isSaddlable(entity)) return

        val itemMain = event.player.inventory.itemInMainHand
        val itemOff  = event.player.inventory.itemInOffHand
        val holdingSaddle = itemMain.type == Material.SADDLE || itemOff.type == Material.SADDLE
        if (!holdingSaddle) return

        if (!canSaddleEntity(event.player, entity as LivingEntity)) {
            deny(event.player, entity.location.chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityMount(event: EntityMountEvent) {
        val passenger = event.entity
        val mount = event.mount
        if (passenger !is Player || !hasProcessingResponsibility(mount.location.chunk)
            || !entityUtils.isLivingMount(mount)) return
        if (!canMountEntity(passenger, mount)) {
            deny(passenger, mount.location.chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onVehicleEnter(event: org.bukkit.event.vehicle.VehicleEnterEvent) {
        val vehicle = event.vehicle
        val entered = event.entered
        if (entered !is Player) return
        if (!hasProcessingResponsibility(vehicle.location.chunk)) return
        if (!canEnterVehicle(entered, vehicle)) {
            deny(entered, vehicle.location.chunk, event)
        }
    }

    private fun handleVehicleBreak(attacker: Entity?, vehicle: Vehicle, event: Cancellable) {
        val player = playerUtils.asPlayer(attacker) ?: return
        if (!hasProcessingResponsibility(vehicle.location.chunk)) return
        if (!canBreakVehicle(player, vehicle)) {
            deny(player, vehicle.location.chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onVehicleDamage(event: VehicleDamageEvent) {
        handleVehicleBreak(event.attacker, event.vehicle, event)
    }

    @EventHandler(ignoreCancelled = true)
    fun onVehicleDestroy(event: VehicleDestroyEvent) {
        handleVehicleBreak(event.attacker, event.vehicle, event)
    }

    @EventHandler(ignoreCancelled = true)
    fun onHangingBreak(event: HangingBreakEvent) {
        val hanging = event.entity
        val chunk = hanging.location.chunk
        if (!hasProcessingResponsibility(chunk)) return
        val byEntity = event as? HangingBreakByEntityEvent
        val player = playerUtils.asPlayer(byEntity?.remover)
        if (byEntity == null || player == null) return
        if (!canBreakHanging(player, hanging)) {
            deny(player, chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onHitItemFrame(event: EntityDamageByEntityEvent) {
        val frame = event.entity as? ItemFrame ?: return
        val chunk = frame.location.chunk
        val player = playerUtils.asPlayer(event.damager)
        if (player == null || !hasProcessingResponsibility(chunk)) {
            return
        }
        if (!canEditItemFrame(player, frame, ItemFrameAction.REMOVE_ITEM)) {
            deny(player, chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onItemFrameChangePaper(event: PlayerItemFrameChangeEvent) {
        val frame = event.itemFrame
        val chunk = frame.location.chunk
        if (!hasProcessingResponsibility(chunk)) return

        val action = when (event.action) {
            PlayerItemFrameChangeEvent.ItemFrameChangeAction.PLACE -> ItemFrameAction.PLACE_ITEM
            PlayerItemFrameChangeEvent.ItemFrameChangeAction.ROTATE -> ItemFrameAction.ROTATE_ITEM
            PlayerItemFrameChangeEvent.ItemFrameChangeAction.REMOVE -> ItemFrameAction.REMOVE_ITEM
            null -> return
        }
        if (!canEditItemFrame(event.player, frame, action)) {
            deny(event.player, chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityExplode(event: EntityExplodeEvent) {
        val source = event.entity
        val originChunk = source.location.chunk
        if (!hasProcessingResponsibility(originChunk)) return
        if (!canExplosionAffect(originChunk, source, ExplosionCause.ENTITY)) {
            event.isCancelled = true
            return
        }
        event.blockList().removeIf { block ->
            hasProcessingResponsibility(block.chunk) && !canExplosionAffect(block.chunk, source, ExplosionCause.ENTITY)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockExplode(event: BlockExplodeEvent) {
        val originChunk = event.block.location.chunk
        if (!hasProcessingResponsibility(originChunk)) return
        if (!canExplosionAffect(originChunk, null, ExplosionCause.BLOCK)) {
            event.isCancelled = true
            return
        }
        event.blockList().removeIf { block ->
            hasProcessingResponsibility(block.chunk) && !canExplosionAffect(block.chunk, null, ExplosionCause.BLOCK)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerLeashEntity(event: PlayerLeashEntityEvent) {
        val entity = event.entity as? LivingEntity ?: return
        if (!hasProcessingResponsibility(entity.location.chunk)) return
        if (!canLeashEntity(event.player, entity, LeashAction.ATTACH)) {
            deny(event.player, entity.location.chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerTryUnleashEntity(event: PlayerInteractEntityEvent) {
        if (event.hand == EquipmentSlot.OFF_HAND) return
        val target : Entity = event.rightClicked
        if (!hasProcessingResponsibility(target.location.chunk)) return
        when (target) {
            is LivingEntity -> {
                if (target.isLeashed && !canLeashEntity(event.player, target, LeashAction.DETACH)) {
                    deny(event.player, target.location.chunk, event)
                }
            }
            is LeashHitch -> {
                if (!canLeashEntity(event.player, event.player, LeashAction.DETACH)) {
                    deny(event.player, target.location.chunk, event)
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerShear(event: PlayerShearEntityEvent) {
        val entity = event.entity as? LivingEntity ?: return
        if (!hasProcessingResponsibility(entity.location.chunk)) return
        if (!canShearEntity(event.player, entity)) {
            deny(event.player, entity.location.chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerBucketEntity(event: PlayerBucketEntityEvent) {
        val mob = event.entity as? LivingEntity ?: return
        if (!hasProcessingResponsibility(mob.location.chunk)) return
        if (!canBucketMob(event.player, mob)) {
            deny(event.player, mob.location.chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerNameEntity(event: PlayerNameEntityEvent) {
        val target = event.entity
        val newName = event.name ?: return
        val chunk = target.location.chunk
        if (!hasProcessingResponsibility(chunk)) return
        if (!canNameEntity(event.player, target, newName)) {
            event.name = null
            event.isPersistent = false
            deny(event.player, chunk, event)
            val key = "${event.player.uniqueId}:${target.entityId}:${newName}"
            if (nameTagMsgOnce.add(key)) {
                org.bukkit.Bukkit.getScheduler().runTaskLater(
                    PLUGIN_INSTANCE, Runnable { nameTagMsgOnce.remove(key) }, 1L
                )
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onArmorStandEdit(event: PlayerArmorStandManipulateEvent) {
        val stand = event.rightClicked
        val chunk = stand.location.chunk
        if (!hasProcessingResponsibility(chunk)) return
        val action = when {
            !event.playerItem.type.isAir && event.armorStandItem.type.isAir -> ArmorStandAction.EQUIP
            event.playerItem.type.isAir && !event.armorStandItem.type.isAir -> ArmorStandAction.UNEQUIP
            else -> ArmorStandAction.SWAP
        }
        if (!canEditArmorStand(event.player, stand, action, event.slot, event.playerItem, event.armorStandItem)) {
            deny(event.player, chunk, event)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onArmorStandDamage(event: EntityDamageByEntityEvent) {
        val stand = event.entity as? ArmorStand ?: return
        val chunk = stand.location.chunk
        if (!hasProcessingResponsibility(chunk)) return
        val player = playerUtils.asPlayer(event.damager)
        if (player == null || !canBreakArmorStand(player, stand)) {
            deny(player, chunk, event)
        }
    }

}

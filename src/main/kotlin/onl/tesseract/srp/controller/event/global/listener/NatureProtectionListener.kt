package onl.tesseract.srp.controller.event.global.listener

import net.kyori.adventure.text.Component
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.world.SrpWorld
import onl.tesseract.srp.service.campement.CampementService
import onl.tesseract.srp.service.guild.GuildService
import onl.tesseract.srp.service.world.WorldService
import onl.tesseract.srp.util.CampementChatError
import onl.tesseract.srp.util.EntityUtils
import onl.tesseract.srp.util.GuildChatError
import onl.tesseract.srp.util.PlayerUtils
import org.bukkit.Chunk
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.*
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class NatureProtectionListener(
    private val campementService: CampementService,
    private val guildService: GuildService,
    private val worldService: WorldService,
    playerUtils: PlayerUtils,
    entityUtils: EntityUtils
) : ChunkProtectionListener(playerUtils, entityUtils) {
    override fun hasProcessingResponsibility(chunk: Chunk): Boolean {
        val noCamp = campementService.getCampementByChunk(chunk.x,chunk.z) == null
        val noGuild = guildService.getGuildByChunk(chunk.x,chunk.z) == null
        return (noCamp && noGuild)
    }

    override fun getProtectionMessage(chunk: Chunk): Component {
        val world = worldService.getSrpWorld(chunk.world)
        val prefix = when (world) {
            SrpWorld.GuildWorld -> GuildChatError
            SrpWorld.Elysea -> CampementChatError
            else -> Component.text("")
        }
        return prefix + "Tu ne peux pas interagir dans la nature."
    }

    override fun canPlaceBlock(player: Player, block: Block): Boolean {
        return false
    }

    override fun canBreakBlock(player: Player, block: Block): Boolean {
        return false
    }

    override fun canOpenContainer(
        player: Player,
        container: Container,
    ): Boolean {
        return true
    }

    override fun canDamagePassiveEntity(player: Player, entity: LivingEntity): Boolean {
        return true
    }

    override fun canHostileDamagePlayer(player: Player, attacker: Entity): Boolean {
        return true
    }

    override fun canUseBucket(player: Player, block: Block): Boolean {
        return false
    }

    override fun canPlayerIgnite(player: Player, block: Block): Boolean {
        return false
    }

    override fun canNaturallyIgnite(block: Block, cause: BlockIgniteEvent.IgniteCause): Boolean {
        return false
    }

    override fun canUseRedstone(player: Player, block: Block): Boolean {
        return true
    }

    override fun canFishEntity(player: Player, entity: Entity): Boolean {
        return true
    }

    override fun canSaddleEntity(player: Player, entity: LivingEntity): Boolean {
        return true
    }

    override fun canMountEntity(player: Player, mount: Entity): Boolean {
        return true
    }

    override fun canEnterVehicle(player: Player, vehicle: Vehicle): Boolean {
        return true
    }

    override fun canBreakVehicle(player: Player, vehicle: Vehicle): Boolean {
        return true
    }

    override fun canBreakHanging(player: Player, hanging: Hanging): Boolean {
        return false
    }

    override fun canEditItemFrame(player: Player, frame: ItemFrame, action: ItemFrameAction): Boolean {
        return false
    }

    override fun canExplosionAffect(chunk: Chunk, source: Entity?, cause: ExplosionCause): Boolean {
        return true
    }

    override fun canLeashEntity(player: Player, entity: LivingEntity, action: LeashAction): Boolean {
        return true
    }

    override fun canShearEntity(player: Player, entity: LivingEntity): Boolean {
        return true
    }

    override fun canBucketMob(player: Player, entity: LivingEntity): Boolean {
        return true
    }

    override fun canNameEntity(player: Player, entity: LivingEntity, newName: Component): Boolean {
        return false
    }

    override fun canEditArmorStand(
        player: Player,
        stand: ArmorStand,
        action: ArmorStandAction,
        slot: EquipmentSlot,
        playerItem: ItemStack,
        standItem: ItemStack
    ): Boolean {
        return true
    }

    override fun canBreakArmorStand(player: Player, stand: ArmorStand): Boolean {
        return true
    }
}

package onl.tesseract.srp.territory.adapter.userside.event.global;

import net.kyori.adventure.text.Component;
import onl.tesseract.srp.common.adapter.SrpChatFormats;
import onl.tesseract.srp.common.adapter.mapper.ChunkCoordMapper;
import onl.tesseract.srp.common.domain.model.world.SrpWorld;
import onl.tesseract.srp.common.domain.model.world.WorldName;
import onl.tesseract.srp.common.domain.port.userside.world.WorldService;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementService;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

@org.springframework.stereotype.Component
public class NatureProtectionListener extends ChunkProtectionListener {
    private final CampementService campementService;
    private final GuildService guildService;
    private final WorldService worldService;

    public NatureProtectionListener(CampementService campementService, GuildService guildService,
                                    WorldService worldService, Plugin plugin) {
        super(plugin);
        this.campementService = campementService;
        this.guildService = guildService;
        this.worldService = worldService;
    }

    @Override
    public boolean hasProcessingResponsibility(Chunk chunk) {
        var chunkCoord = ChunkCoordMapper.toChunkCoord(chunk);
        return campementService.getByChunk(chunkCoord) == null && guildService.getByChunk(chunkCoord) == null;
    }

    @Override
    public Component getProtectionMessage(Chunk chunk) {
        SrpWorld world = worldService.getSrpWorld(new WorldName(chunk.getWorld().getName()));
        Component prefix = world == SrpWorld.GuildWorld ? SrpChatFormats.GUILD_CHAT_ERROR
                : world == SrpWorld.Elysea ? SrpChatFormats.CAMPEMENT_CHAT_ERROR : Component.text("");
        return prefix.append(Component.text("Tu ne peux pas interagir dans la nature."));
    }

    @Override public boolean canPlaceBlock(Player player, Block block) { return false; }
    @Override public boolean canBreakBlock(Player player, Block block) { return false; }
    @Override public boolean canOpenContainer(Player player, Container container) { return true; }
    @Override public boolean canDamagePassiveEntity(Player player, LivingEntity entity) { return true; }
    @Override public boolean canHostileDamagePlayer(Player player, Entity attacker) { return true; }
    @Override public boolean canUseBucket(Player player, Block block) { return false; }
    @Override public boolean canPlayerIgnite(Player player, Block block) { return false; }
    @Override public boolean canNaturallyIgnite(Block block, BlockIgniteEvent.IgniteCause cause) { return false; }
    @Override public boolean canUseRedstone(Player player, Block block) { return true; }
    @Override public boolean canFishEntity(Player player, Entity entity) { return true; }
    @Override public boolean canSaddleEntity(Player player, LivingEntity entity) { return true; }
    @Override public boolean canMountEntity(Player player, Entity mount) { return true; }
    @Override public boolean canEnterVehicle(Player player, Vehicle vehicle) { return true; }
    @Override public boolean canBreakVehicle(Player player, Vehicle vehicle) { return true; }
    @Override public boolean canBreakHanging(Player player, Hanging hanging) { return false; }
    @Override public boolean canEditItemFrame(Player player, ItemFrame frame, ItemFrameAction action) { return false; }
    @Override public boolean canExplosionAffect(Chunk chunk, Entity source, ExplosionCause cause) { return true; }
    @Override public boolean canLeashEntity(Player player, LivingEntity entity, LeashAction action) { return true; }
    @Override public boolean canShearEntity(Player player, LivingEntity entity) { return true; }
    @Override public boolean canBucketMob(Player player, LivingEntity entity) { return true; }
    @Override public boolean canNameEntity(Player player, LivingEntity entity, Component newName) { return false; }

    @Override
    public boolean canEditArmorStand(Player player, ArmorStand stand, ArmorStandAction action, EquipmentSlot slot,
                                     ItemStack playerItem, ItemStack standItem) {
        return true;
    }

    @Override public boolean canBreakArmorStand(Player player, ArmorStand stand) { return true; }
}




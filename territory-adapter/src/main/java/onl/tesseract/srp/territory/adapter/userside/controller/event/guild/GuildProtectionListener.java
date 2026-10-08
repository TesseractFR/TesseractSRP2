package onl.tesseract.srp.territory.adapter.userside.controller.event.guild;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.srp.common.adapter.SrpChatFormats;
import onl.tesseract.srp.common.adapter.mapper.ChunkCoordMapper;
import onl.tesseract.srp.common.domain.model.enums.InteractionAllowResult;
import onl.tesseract.srp.territory.adapter.userside.controller.event.global.ChunkProtectionListener;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

@org.springframework.stereotype.Component
public class GuildProtectionListener extends ChunkProtectionListener {
    private final GuildService guildService;

    public GuildProtectionListener(GuildService guildService, Plugin plugin) {
        super(plugin);
        this.guildService = guildService;
    }

    @Override
    public boolean hasProcessingResponsibility(Chunk chunk) {
        return guildService.getByChunk(ChunkCoordMapper.toChunkCoord(chunk)) != null;
    }

    @Override
    public Component getProtectionMessage(Chunk chunk) {
        var guild = Objects.requireNonNull(guildService.getByChunk(ChunkCoordMapper.toChunkCoord(chunk)));
        return SrpChatFormats.GUILD_CHAT_ERROR.append(Component.text(
                        "Tu ne peux pas interagir ici ! Ce terrain appartient à la guilde "))
                .append(Component.text(guild.getName(), NamedTextColor.GOLD))
                .append(Component.text("."));
    }

    @Override
    public boolean canPlaceBlock(Player player, Block block) {
        return canInteract(player, block.getChunk());
    }

    @Override
    public boolean canBreakBlock(Player player, Block block) {
        return canInteract(player, block.getChunk());
    }

    @Override
    public boolean canOpenContainer(Player player, Container container) {
        return container.getType() == Material.ENDER_CHEST || canInteract(player, container.getChunk());
    }

    @Override
    public boolean canDamagePassiveEntity(Player player, LivingEntity entity) {
        return canInteract(player, entity.getChunk());
    }

    @Override
    public boolean canHostileDamagePlayer(Player player, Entity attacker) {
        return false;
    }

    @Override
    public boolean canUseBucket(Player player, Block block) {
        return canInteract(player, block.getChunk());
    }

    @Override
    public boolean canPlayerIgnite(Player player, Block block) {
        return canInteract(player, block.getChunk());
    }

    @Override
    public boolean canNaturallyIgnite(Block block, BlockIgniteEvent.IgniteCause cause) {
        return false;
    }

    @Override
    public boolean canUseRedstone(Player player, Block block) {
        return canInteract(player, block.getChunk());
    }

    @Override
    public boolean canFishEntity(Player player, Entity entity) {
        return canInteract(player, entity.getLocation().getChunk());
    }

    @Override
    public boolean canSaddleEntity(Player player, LivingEntity entity) {
        return canInteract(player, entity.getLocation().getChunk());
    }

    @Override
    public boolean canMountEntity(Player player, Entity mount) {
        return canInteract(player, mount.getLocation().getChunk());
    }

    @Override
    public boolean canEnterVehicle(Player player, Vehicle vehicle) {
        return canInteract(player, vehicle.getLocation().getChunk());
    }

    @Override
    public boolean canBreakVehicle(Player player, Vehicle vehicle) {
        return canInteract(player, vehicle.getLocation().getChunk());
    }

    @Override
    public boolean canBreakHanging(Player player, Hanging hanging) {
        return canInteract(player, hanging.getLocation().getChunk());
    }

    @Override
    public boolean canEditItemFrame(Player player, ItemFrame frame, ItemFrameAction action) {
        return canInteract(player, frame.getLocation().getChunk());
    }

    @Override
    public boolean canExplosionAffect(Chunk chunk, Entity source, ExplosionCause cause) {
        return false;
    }

    @Override
    public boolean canLeashEntity(Player player, LivingEntity entity, LeashAction action) {
        return canInteract(player, entity.getLocation().getChunk());
    }

    @Override
    public boolean canShearEntity(Player player, LivingEntity entity) {
        return canInteract(player, entity.getLocation().getChunk());
    }

    @Override
    public boolean canBucketMob(Player player, LivingEntity entity) {
        return canInteract(player, entity.getLocation().getChunk());
    }

    @Override
    public boolean canNameEntity(Player player, LivingEntity entity, Component newName) {
        return canInteract(player, entity.getLocation().getChunk());
    }

    @Override
    public boolean canEditArmorStand(Player player, ArmorStand stand, ArmorStandAction action, EquipmentSlot slot,
                                     ItemStack playerItem, ItemStack standItem) {
        return canInteract(player, stand.getChunk());
    }

    @Override
    public boolean canBreakArmorStand(Player player, ArmorStand stand) {
        return canInteract(player, stand.getChunk());
    }

    private boolean canInteract(Player player, Chunk chunk) {
        return !InteractionAllowResult.Deny.equals(
                guildService.canInteractInChunk(player.getUniqueId(), ChunkCoordMapper.toChunkCoord(chunk)));
    }
}

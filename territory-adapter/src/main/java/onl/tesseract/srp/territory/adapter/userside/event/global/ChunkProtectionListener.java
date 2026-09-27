package onl.tesseract.srp.territory.adapter.userside.event.global;

import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
import io.papermc.paper.event.player.PlayerNameEntityEvent;
import net.kyori.adventure.text.Component;
import onl.tesseract.srp.common.adapter.utils.EntityUtils;
import onl.tesseract.srp.common.adapter.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public abstract class ChunkProtectionListener implements Listener {
    private static final Set<String> NAME_TAG_MSG_ONCE = ConcurrentHashMap.newKeySet();
    private static final Set<Material> NEUTRAL_INTERACTABLES = Set.of(
            Material.CRAFTING_TABLE, Material.CARTOGRAPHY_TABLE, Material.LOOM, Material.GRINDSTONE,
            Material.STONECUTTER, Material.FLETCHING_TABLE, Material.SMITHING_TABLE, Material.BELL,
            Material.ENCHANTING_TABLE);

    private final Plugin plugin;

    protected ChunkProtectionListener(Plugin pluginInstance) {
        plugin = pluginInstance;
    }

    protected abstract boolean hasProcessingResponsibility(Chunk chunk);
    protected abstract Component getProtectionMessage(Chunk chunk);
    protected abstract boolean canPlaceBlock(Player player, Block block);
    protected abstract boolean canBreakBlock(Player player, Block block);
    protected abstract boolean canOpenContainer(Player player, Container container);
    protected abstract boolean canDamagePassiveEntity(Player player, LivingEntity entity);
    protected abstract boolean canHostileDamagePlayer(Player player, Entity attacker);
    protected abstract boolean canUseBucket(Player player, Block block);
    protected abstract boolean canPlayerIgnite(Player player, Block block);
    protected abstract boolean canNaturallyIgnite(Block block, BlockIgniteEvent.IgniteCause cause);
    protected abstract boolean canUseRedstone(Player player, Block block);
    protected abstract boolean canFishEntity(Player player, Entity entity);
    protected abstract boolean canSaddleEntity(Player player, LivingEntity entity);
    protected abstract boolean canMountEntity(Player player, Entity mount);
    protected abstract boolean canEnterVehicle(Player player, Vehicle vehicle);
    protected abstract boolean canBreakVehicle(Player player, Vehicle vehicle);
    protected abstract boolean canBreakHanging(Player player, Hanging hanging);
    protected abstract boolean canEditItemFrame(Player player, ItemFrame frame, ItemFrameAction action);
    protected abstract boolean canExplosionAffect(Chunk chunk, Entity source, ExplosionCause cause);
    protected abstract boolean canLeashEntity(Player player, LivingEntity entity, LeashAction action);
    protected abstract boolean canShearEntity(Player player, LivingEntity entity);
    protected abstract boolean canBucketMob(Player player, LivingEntity entity);
    protected abstract boolean canNameEntity(Player player, LivingEntity entity, Component newName);
    protected abstract boolean canEditArmorStand(Player player, ArmorStand stand, ArmorStandAction action,
                                                  EquipmentSlot slot, ItemStack playerItem, ItemStack standItem);
    protected abstract boolean canBreakArmorStand(Player player, ArmorStand stand);

    public enum LeashAction { ATTACH, DETACH }
    public enum ExplosionCause { ENTITY, BLOCK }
    public enum ItemFrameAction { PLACE_ITEM, ROTATE_ITEM, REMOVE_ITEM }
    public enum ArmorStandAction { EQUIP, UNEQUIP, SWAP }

    private void deny(Player player, Chunk chunk, Cancellable event) {
        if (player != null) player.sendMessage(getProtectionMessage(chunk));
        event.setCancelled(true);
    }

    protected boolean canInteractWithBlock(Player player, Block block) {
        if (NEUTRAL_INTERACTABLES.contains(block.getType())) return true;
        return block.getState() instanceof Container container
                ? canOpenContainer(player, container) : canUseRedstone(player, block);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        if (hasProcessingResponsibility(event.getBlock().getChunk()) && !canPlaceBlock(event.getPlayer(), event.getBlock()))
            deny(event.getPlayer(), event.getBlock().getChunk(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerOpenContainer(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (!event.hasBlock() || block == null || !(block.getState() instanceof Container container)) return;
        if (hasProcessingResponsibility(block.getChunk()) && !canOpenContainer(event.getPlayer(), container))
            deny(event.getPlayer(), block.getChunk(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (hasProcessingResponsibility(event.getBlock().getChunk()) && !canBreakBlock(event.getPlayer(), event.getBlock()))
            deny(event.getPlayer(), event.getBlock().getChunk(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamagePassive(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        if (!(victim instanceof LivingEntity living) || victim instanceof Player || victim instanceof Monster) return;
        if (!hasProcessingResponsibility(victim.getChunk())) return;
        Player player = PlayerUtils.asPlayer(event.getDamager());
        if (player != null && !canDamagePassiveEntity(player, living)) deny(player, victim.getChunk(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHostileDamagesPlayer(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Entity damager = event.getDamager();
        Entity attacker = damager instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter
                ? shooter : damager;
        Chunk chunk = player.getLocation().getChunk();
        if ((attacker instanceof Monster || attacker instanceof EnderDragon) && hasProcessingResponsibility(chunk)
                && !canHostileDamagePlayer(player, attacker)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) { handleBucket(event.getPlayer(), event.getBlock(), event); }
    @EventHandler(ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) { handleBucket(event.getPlayer(), event.getBlock(), event); }
    private void handleBucket(Player player, Block block, Cancellable event) {
        if (hasProcessingResponsibility(block.getChunk()) && !canUseBucket(player, block)) deny(player, block.getChunk(), event);
    }

    private boolean handleIgnitionDecision(Block block, Player player, BlockIgniteEvent.IgniteCause cause) {
        return player != null ? canPlayerIgnite(player, block) : canNaturallyIgnite(block, cause);
    }

    @EventHandler(ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event) {
        Chunk chunk = event.getBlock().getChunk();
        if (!hasProcessingResponsibility(chunk)) return;
        Player player = PlayerUtils.asPlayer(event.getIgnitingEntity());
        if (!handleIgnitionDecision(event.getBlock(), player, event.getCause())) deny(player, chunk, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTntPrime(TNTPrimeEvent event) {
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();
        if (!hasProcessingResponsibility(chunk)) return;
        Player player = PlayerUtils.asPlayer(event.getPrimingEntity());
        BlockIgniteEvent.IgniteCause cause = switch (event.getCause()) {
            case FIRE, REDSTONE, BLOCK_BREAK, DISPENSER -> BlockIgniteEvent.IgniteCause.SPREAD;
            case PLAYER -> BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL;
            case EXPLOSION -> BlockIgniteEvent.IgniteCause.EXPLOSION;
            case PROJECTILE -> BlockIgniteEvent.IgniteCause.FIREBALL;
        };
        if (!handleIgnitionDecision(block, player, cause)) deny(player, chunk, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAnyExplosionDamageEntity(EntityDamageEvent event) {
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION && cause != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) return;
        Chunk chunk = event.getEntity().getLocation().getChunk();
        if (!hasProcessingResponsibility(chunk)) return;
        Entity source = event instanceof EntityDamageByEntityEvent byEntity ? byEntity.getDamager() : null;
        ExplosionCause explosionCause = cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ? ExplosionCause.ENTITY : ExplosionCause.BLOCK;
        if (!canExplosionAffect(chunk, source, explosionCause)) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBlockInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (event.getHand() == EquipmentSlot.OFF_HAND || !event.hasBlock() || block == null) return;
        boolean relevant = event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.PHYSICAL;
        if (!relevant || !hasProcessingResponsibility(block.getChunk())) return;
        if (block.getType() == Material.TURTLE_EGG) { event.setCancelled(true); return; }
        if (!canInteractWithBlock(event.getPlayer(), block)) deny(event.getPlayer(), block.getChunk(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFishEntity(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY || event.getCaught() == null) return;
        Entity caught = event.getCaught(); Chunk chunk = caught.getLocation().getChunk();
        if (hasProcessingResponsibility(chunk) && !canFishEntity(event.getPlayer(), caught)) deny(event.getPlayer(), chunk, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTurtleEggTrampleInteract(EntityInteractEvent event) { cancelTurtleEgg(event.getBlock(), event); }
    @EventHandler(ignoreCancelled = true)
    public void onTurtleEggTrampleChange(EntityChangeBlockEvent event) { cancelTurtleEgg(event.getBlock(), event); }
    private void cancelTurtleEgg(Block block, Cancellable event) {
        if (block.getType() == Material.TURTLE_EGG && hasProcessingResponsibility(block.getChunk())) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSaddleEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!hasProcessingResponsibility(entity.getLocation().getChunk()) || !EntityUtils.isSaddlable(entity)) return;
        if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.SADDLE
                && event.getPlayer().getInventory().getItemInOffHand().getType() != Material.SADDLE) return;
        if (!canSaddleEntity(event.getPlayer(), (LivingEntity) entity)) deny(event.getPlayer(), entity.getLocation().getChunk(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityMount(EntityMountEvent event) {
        Entity mount = event.getMount();
        if (!(event.getEntity() instanceof Player player) || !hasProcessingResponsibility(mount.getLocation().getChunk())
                || !EntityUtils.isLivingMount(mount)) return;
        if (!canMountEntity(player, mount)) deny(player, mount.getLocation().getChunk(), event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player player)) return;
        Vehicle vehicle = event.getVehicle(); Chunk chunk = vehicle.getLocation().getChunk();
        if (hasProcessingResponsibility(chunk) && !canEnterVehicle(player, vehicle)) deny(player, chunk, event);
    }

    private void handleVehicleBreak(Entity attacker, Vehicle vehicle, Cancellable event) {
        Player player = PlayerUtils.asPlayer(attacker);
        if (player == null) return;
        Chunk chunk = vehicle.getLocation().getChunk();
        if (hasProcessingResponsibility(chunk) && !canBreakVehicle(player, vehicle)) deny(player, chunk, event);
    }
    @EventHandler(ignoreCancelled = true)
    public void onVehicleDamage(VehicleDamageEvent event) { handleVehicleBreak(event.getAttacker(), event.getVehicle(), event); }
    @EventHandler(ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) { handleVehicleBreak(event.getAttacker(), event.getVehicle(), event); }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        Hanging hanging = event.getEntity(); Chunk chunk = hanging.getLocation().getChunk();
        if (!hasProcessingResponsibility(chunk)) return;
        if (!(event instanceof HangingBreakByEntityEvent byEntity)) return;
        Player player = PlayerUtils.asPlayer(byEntity.getRemover());
        if (player != null && !canBreakHanging(player, hanging)) deny(player, chunk, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHitItemFrame(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ItemFrame frame)) return;
        Chunk chunk = frame.getLocation().getChunk(); Player player = PlayerUtils.asPlayer(event.getDamager());
        if (player != null && hasProcessingResponsibility(chunk) && !canEditItemFrame(player, frame, ItemFrameAction.REMOVE_ITEM))
            deny(player, chunk, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemFrameChangePaper(PlayerItemFrameChangeEvent event) {
        ItemFrame frame = event.getItemFrame(); Chunk chunk = frame.getLocation().getChunk();
        if (!hasProcessingResponsibility(chunk)) return;
        PlayerItemFrameChangeEvent.ItemFrameChangeAction paperAction = event.getAction();
        if (paperAction == null) return;
        ItemFrameAction action = switch (paperAction) {
            case PLACE -> ItemFrameAction.PLACE_ITEM;
            case ROTATE -> ItemFrameAction.ROTATE_ITEM;
            case REMOVE -> ItemFrameAction.REMOVE_ITEM;
        };
        if (action != null && !canEditItemFrame(event.getPlayer(), frame, action)) deny(event.getPlayer(), chunk, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity source = event.getEntity(); Chunk origin = source.getLocation().getChunk();
        if (!hasProcessingResponsibility(origin)) return;
        if (!canExplosionAffect(origin, source, ExplosionCause.ENTITY)) { event.setCancelled(true); return; }
        event.blockList().removeIf(block -> hasProcessingResponsibility(block.getChunk())
                && !canExplosionAffect(block.getChunk(), source, ExplosionCause.ENTITY));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        Chunk origin = event.getBlock().getLocation().getChunk();
        if (!hasProcessingResponsibility(origin)) return;
        if (!canExplosionAffect(origin, null, ExplosionCause.BLOCK)) { event.setCancelled(true); return; }
        event.blockList().removeIf(block -> hasProcessingResponsibility(block.getChunk())
                && !canExplosionAffect(block.getChunk(), null, ExplosionCause.BLOCK));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        Chunk chunk = entity.getLocation().getChunk();
        if (hasProcessingResponsibility(chunk) && !canLeashEntity(event.getPlayer(), entity, LeashAction.ATTACH))
            deny(event.getPlayer(), chunk, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTryUnleashEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        Entity target = event.getRightClicked(); Chunk chunk = target.getLocation().getChunk();
        if (!hasProcessingResponsibility(chunk)) return;
        if (target instanceof LivingEntity living && living.isLeashed()
                && !canLeashEntity(event.getPlayer(), living, LeashAction.DETACH)) deny(event.getPlayer(), chunk, event);
        else if (target instanceof LeashHitch && !canLeashEntity(event.getPlayer(), event.getPlayer(), LeashAction.DETACH))
            deny(event.getPlayer(), chunk, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerShear(PlayerShearEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        Chunk chunk = entity.getLocation().getChunk();
        if (hasProcessingResponsibility(chunk) && !canShearEntity(event.getPlayer(), entity)) deny(event.getPlayer(), chunk, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBucketEntity(PlayerBucketEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        Chunk chunk = entity.getLocation().getChunk();
        if (hasProcessingResponsibility(chunk) && !canBucketMob(event.getPlayer(), entity)) deny(event.getPlayer(), chunk, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerNameEntity(PlayerNameEntityEvent event) {
        LivingEntity target = event.getEntity(); Component name = event.getName();
        if (name == null) return;
        Chunk chunk = target.getLocation().getChunk();
        if (!hasProcessingResponsibility(chunk) || canNameEntity(event.getPlayer(), target, name)) return;
        event.setName(null); event.setPersistent(false); deny(event.getPlayer(), chunk, event);
        String key = event.getPlayer().getUniqueId() + ":" + target.getEntityId() + ":" + name;
        if (NAME_TAG_MSG_ONCE.add(key)) Bukkit.getScheduler().runTaskLater(plugin, () -> NAME_TAG_MSG_ONCE.remove(key), 1L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandEdit(PlayerArmorStandManipulateEvent event) {
        ArmorStand stand = event.getRightClicked(); Chunk chunk = stand.getLocation().getChunk();
        if (!hasProcessingResponsibility(chunk)) return;
        boolean playerAir = event.getPlayerItem().getType().isAir();
        boolean standAir = event.getArmorStandItem().getType().isAir();
        ArmorStandAction action = !playerAir && standAir ? ArmorStandAction.EQUIP
                : playerAir && !standAir ? ArmorStandAction.UNEQUIP : ArmorStandAction.SWAP;
        if (!canEditArmorStand(event.getPlayer(), stand, action, event.getSlot(), event.getPlayerItem(), event.getArmorStandItem()))
            deny(event.getPlayer(), chunk, event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand)) return;
        Chunk chunk = stand.getLocation().getChunk();
        if (!hasProcessingResponsibility(chunk)) return;
        Player player = PlayerUtils.asPlayer(event.getDamager());
        if (player == null || !canBreakArmorStand(player, stand)) deny(player, chunk, event);
    }
}


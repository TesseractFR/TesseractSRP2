package onl.tesseract.srp.common.adapter.serverside;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.core.cosmetics.TeleportationAnimation;
import onl.tesseract.lib.task.TaskScheduler;
import onl.tesseract.srp.common.adapter.mapper.CoordinateMapper;
import onl.tesseract.srp.common.adapter.mapper.LocationMapper;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.port.serverside.TeleportationPort;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;

@org.springframework.stereotype.Component
public class TeleportationPortImpl implements TeleportationPort {

    private final Plugin plugin;
    private final TaskScheduler taskScheduler;

    public TeleportationPortImpl(Plugin plugin, TaskScheduler taskScheduler) {
        this.plugin = plugin;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public Coordinate getLocation(PlayerId playerId) {
        return getPlayer(playerId).map(player -> CoordinateMapper.toCoordinate(player.getLocation())).orElse(null);
    }

    @Override
    public boolean isOnline(PlayerId playerId) {
        return getPlayer(playerId).isPresent();
    }

    @Override
    public boolean isDestinationChunkLoaded(Coordinate destination) {
        return LocationMapper.toLocation(destination).getChunk().isLoaded();
    }

    @Override
    public void applyTeleportEffects(PlayerId playerId, int duration) {
        getPlayer(playerId).ifPresent(player -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration + 20, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, (int) (duration * 1.75f), 1));
        });
    }

    @Override
    public void playPortalSound(PlayerId playerId) {
        getPlayer(playerId).ifPresent(player -> player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 1f, 1f));
    }

    @Override
    public void animate(Coordinate location,String animationId, double duration) {
        TeleportationAnimation.valueOf(animationId).animate(
                plugin, LocationMapper.toLocation(location), (double) duration);
    }

    @Override
    public void teleport(PlayerId playerId, Coordinate destination) {
        getPlayer(playerId).ifPresent(player -> player.teleport(LocationMapper.toLocation(destination)));
    }

    @Override
    public void removeBlindness(PlayerId playerId) {
        getPlayer(playerId).ifPresent(player -> player.removePotionEffect(PotionEffectType.BLINDNESS));
    }

    @Override
    public void sendCancelledMessage(PlayerId playerId) {
        getPlayer(playerId).ifPresent(player -> player.sendMessage(Component.text("Téléportation annulée.", NamedTextColor.RED)));
    }

    @Override
    public Cancellable runTimer(long initialDelayTicks, long periodTicks, Runnable task) {
        return new Cancellable(){
            private final BukkitTask taskSchedule = taskScheduler.runTimer(initialDelayTicks, periodTicks, task);
            @Override
            public void cancel() {
                taskSchedule.cancel();
            }
        };
    }

    private Optional<Player> getPlayer(PlayerId playerId) {
        return Optional.ofNullable(Bukkit.getPlayer(playerId.value()));
    }

}

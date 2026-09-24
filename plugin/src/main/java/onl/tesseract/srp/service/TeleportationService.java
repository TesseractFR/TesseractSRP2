package onl.tesseract.srp.service;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.core.boutique.BoutiqueService;
import onl.tesseract.core.cosmetics.TeleportationAnimation;
import onl.tesseract.lib.logger.LoggerFactory;
import onl.tesseract.lib.task.TaskScheduler;
import onl.tesseract.srp.service.player.SrpPlayerService;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Teleport players to different locations, with a teleportation delay depending on their rank, and a teleportation
 * animation.
 */
@Service
public class TeleportationService {
    private static final Logger logger = LoggerFactory.getLogger(TeleportationService.class);

    private final SrpPlayerService playerService;
    private final TaskScheduler taskService;
    private final Plugin plugin;
    private final BoutiqueService boutiqueService;

    public TeleportationService(SrpPlayerService playerService, TaskScheduler taskService,
                                Plugin plugin, BoutiqueService boutiqueService) {
        this.playerService = playerService;
        this.taskService = taskService;
        this.plugin = plugin;
        this.boutiqueService = boutiqueService;
    }

    /**
     * Teleport the player to the given location, with a delay depending on his rank, and the active teleportation
     * animation.
     * @param callback Callback called once the teleportation happened
     */
    public void teleport(Player player, Location to, Runnable callback) {
        var srpPlayer = playerService.getPlayer(player.getUniqueId());
        var playerBoutiqueInfo = boutiqueService.getPlayerBoutiqueInfo(player.getUniqueId());
        teleport(player, to, srpPlayer.getRank().getTpDelay(), playerBoutiqueInfo.getActiveTpAnimation(), callback);
    }

    /**
     * Teleport a player with a duration and a teleportation animation.
     */
    public void teleport(Player player, Location to, int duration,
                         TeleportationAnimation animation, Runnable callback) {
        logger.debug("Teleporting player {} from {} to {}", player.getName(), player.getLocation(), to);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration + 20, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, (int) (duration * 1.75f), 1));
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 1f, 1f);
        animation.animate(plugin, player.getLocation(), (double) duration);
        if (to.getChunk().isLoaded()) {
            animation.animate(plugin, to, (double) duration);
        }

        Location startLocation = player.getLocation();
        AtomicInteger currentDuration = new AtomicInteger();
        taskService.runTimer(0L, 10L, 0L, task -> {
            currentDuration.addAndGet(10);
            if (currentDuration.get() >= duration) {
                task.cancel();
                player.teleport(to);
                if (callback != null) {
                    callback.run();
                }
            } else {
                if (!player.isOnline()) {
                    task.cancel();
                    return;
                }
                if (player.getLocation().getBlock() != startLocation.getBlock()) {
                    task.cancel();
                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                    player.sendMessage(Component.text("Téléportation annulée.", NamedTextColor.RED));
                }
            }
        });
    }
}


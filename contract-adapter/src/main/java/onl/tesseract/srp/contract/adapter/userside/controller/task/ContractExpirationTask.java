package onl.tesseract.srp.contract.adapter.userside.controller.task;

import onl.tesseract.srp.contract.domain.port.userside.ContractExpirationService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.springframework.stereotype.Component;

import java.util.logging.Level;

/**
 * Supprime périodiquement les contrats expirés. La tâche est démarrée par {@code ContractProvider}
 * après l'initialisation des beans et s'exécute toutes les dix minutes.
 */
@Component
public class ContractExpirationTask implements Runnable {
    private static final long PERIOD_TICKS = 20L * 60 * 10;
    private final ContractExpirationService contractExpirationService;
    private final Plugin plugin;

    public ContractExpirationTask(ContractExpirationService contractExpirationService, Plugin plugin) {
        this.contractExpirationService = contractExpirationService;
        this.plugin = plugin;
    }

    /** Programme la purge toutes les dix minutes, après un premier délai de dix minutes. */
    public void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, this, PERIOD_TICKS, PERIOD_TICKS);
    }

    @Override
    public void run() {
        int purgedCount = contractExpirationService.purgeExpired();
        if (purgedCount > 0) {
            plugin.getLogger().log(Level.INFO, "[contract] {0} contrat(s) expiré(s) supprimé(s).", purgedCount);
        }
    }
}

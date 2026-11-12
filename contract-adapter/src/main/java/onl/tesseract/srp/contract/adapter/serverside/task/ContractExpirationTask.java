package onl.tesseract.srp.contract.adapter.serverside.task;

import onl.tesseract.srp.contract.domain.port.userside.ContractService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.springframework.stereotype.Component;

import java.util.logging.Level;

/**
 * Supprime périodiquement du dépôt les contrats dont la date d'expiration est passée.
 *
 * <p>La tâche est démarrée par {@code ContractProvider} après l'initialisation des
 * beans. Elle s'exécute toutes les dix minutes, avec un premier passage après le
 * même délai. Une entrée de journal est écrite uniquement si au moins un contrat
 * a été supprimé.</p>
 */
@Component
public class ContractExpirationTask implements Runnable {
    private static final long PERIOD_TICKS = 20L * 60 * 10;
    private final ContractService contractService;
    private final Plugin plugin;

    /**
     * Crée la tâche à partir du service de contrats et du plugin Bukkit qui
     * fournit le planificateur et le journal.
     *
     * @param contractService service utilisé pour purger les contrats expirés
     * @param plugin plugin Bukkit propriétaire de la tâche
     */
    public ContractExpirationTask(ContractService contractService, Plugin plugin) {
        this.contractService = contractService;
        this.plugin = plugin;
    }

    /** Programme la purge toutes les dix minutes après un délai initial de dix minutes. */
    public void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, this, PERIOD_TICKS, PERIOD_TICKS);
    }

    /** Exécute une purge et journalise le nombre supprimé lorsqu'il est positif. */
    @Override
    public void run() {
        int purgedCount = contractService.purgeExpired();
        if (purgedCount > 0) {
            plugin.getLogger().log(Level.INFO, "[contract] {0} contrat(s) expiré(s) supprimé(s).", purgedCount);        }
    }
}

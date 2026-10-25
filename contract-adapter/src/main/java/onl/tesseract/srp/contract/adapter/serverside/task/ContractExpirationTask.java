package onl.tesseract.srp.contract.adapter.serverside.task;

import onl.tesseract.srp.contract.domain.port.userside.ContractService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.springframework.stereotype.Component;

import java.util.logging.Level;

@Component
public class ContractExpirationTask implements Runnable {
    private static final long PERIOD_TICKS = 20L * 60 * 10;
    private final ContractService contractService;
    private final Plugin plugin;

    public ContractExpirationTask(ContractService contractService, Plugin plugin) {
        this.contractService = contractService;
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, this, PERIOD_TICKS, PERIOD_TICKS);
    }

    @Override
    public void run() {
        int purged = contractService.purgeExpired();
        if (purged > 0) {
            plugin.getLogger().log(Level.INFO, "[contract] {0} contrat(s) expiré(s) supprimé(s).", purged);        }
    }
}

package onl.tesseract.srp.contract.adapter.serverside.messaging;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ContractNpcNames implements Listener {

    private final Map<String, String> names = new ConcurrentHashMap<>();

    public void remember(PlayerID player, String job, String npcName) {
        String name = npcName == null ? "" : npcName.replace('_', ' ').trim();
        if (name.isEmpty()) {
            names.remove(key(player, job));
            return;
        }
        names.put(key(player, job), name);
    }

    public Optional<String> nameFor(PlayerID player, String job) {
        return Optional.ofNullable(names.get(key(player, job)));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        String prefix = event.getPlayer().getUniqueId() + "|";
        names.keySet().removeIf(key -> key.startsWith(prefix));
    }

    private static String key(PlayerID player, String job) {
        return player.value() + "|" + job.toLowerCase(Locale.ROOT);
    }
}

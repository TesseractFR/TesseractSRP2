package onl.tesseract.srp.skill.adapter.serverside;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.srp.skill.domain.model.PlayerID;
import onl.tesseract.srp.skill.domain.model.recipe.Tier;
import onl.tesseract.srp.skill.domain.port.serverside.MessagingRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;

@org.springframework.stereotype.Component
public class BukkitMessagingRepository implements MessagingRepository {
    @Override
    public void sendInsuffisantTableTier(PlayerID player, Tier tier) {
        getPlayer(player).ifPresent(p -> p.sendMessage(Component
                .text("Le niveau de cette table est insuffisant pour cette recette ! (Tier requis : "
                        + tier.value() + ")", NamedTextColor.RED)));

    }

    @Override
    public void sendInsuffisantComponent(PlayerID player) {
        getPlayer(player).ifPresent(p ->
                p.sendMessage(Component.text("Vous n'avez pas assez de ressources !", NamedTextColor.RED)));
    }

    @Override
    public void sendCraftStarted(PlayerID player) {
        getPlayer(player).ifPresent(p ->
                p.sendMessage(Component.text("§aFabrication lancée !", NamedTextColor.GREEN)));
    }

    @Override
    public void sendCraftQueued(PlayerID player) {
        getPlayer(player).ifPresent(p ->
                p.sendMessage(Component.text("§aRecette ajoutée à la file d'attente !", NamedTextColor.GREEN)));
    }

    private Optional<Player> getPlayer(PlayerID player) {
        return Optional.ofNullable(Bukkit.getPlayer(player.value()));
    }
}

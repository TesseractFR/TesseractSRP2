package onl.tesseract.srp.contract.adapter.serverside.messaging;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import onl.tesseract.srp.contract.adapter.serverside.yaml.YamlContractConfigRepository;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.port.serverside.ContractMessagingRepository;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

import static onl.tesseract.srp.contract.adapter.utils.ContractChatFormats.CONTRACT_CHAT_ERROR;
import static onl.tesseract.srp.contract.adapter.utils.ContractChatFormats.CONTRACT_CHAT_FORMAT;
import static onl.tesseract.srp.contract.adapter.utils.ContractChatFormats.CONTRACT_CHAT_SUCCESS;
import static onl.tesseract.srp.common.adapter.SrpChatFormats.STAFF_CHAT_ERROR;
import static onl.tesseract.srp.common.adapter.SrpChatFormats.STAFF_CHAT_FORMAT;
import static onl.tesseract.srp.common.adapter.SrpChatFormats.STAFF_CHAT_SUCCESS;

/** Envoie aux joueurs les messages de contrat au moyen des composants Adventure et de Bukkit. */
@org.springframework.stereotype.Component
public class BukkitContractMessagingRepository implements ContractMessagingRepository {

    private final YamlContractConfigRepository yamlContractConfigRepository;

    public BukkitContractMessagingRepository(YamlContractConfigRepository yamlContractConfigRepository) {
        this.yamlContractConfigRepository = yamlContractConfigRepository;
    }

    private void send(PlayerId playerId, Component message) {
        Player player = Bukkit.getPlayer(playerId.value());
        if (player != null) player.sendMessage(message);
    }

    private void error(PlayerId playerId, String text) {
        send(playerId, CONTRACT_CHAT_ERROR.append(Component.text(text)));
    }

    private void success(PlayerId playerId, String text) {
        send(playerId, CONTRACT_CHAT_SUCCESS.append(Component.text(text)));
    }

    private void info(PlayerId playerId, String text) {
        send(playerId, CONTRACT_CHAT_FORMAT.append(Component.text(text)));
    }

    private void npcSays(PlayerId playerId, String npcJob, Component line) {
        String name = yamlContractConfigRepository.getNpcName(npcJob).orElseGet(() -> StringUtils.capitalize(npcJob));
        send(playerId, Component.empty()
                .append(Component.text(name, NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                .append(line));
    }

    private static Component text(String line) {
        return Component.text(line, NamedTextColor.WHITE);
    }

    private static Component pick(Component... lines) {
        return lines[ThreadLocalRandom.current().nextInt(lines.length)];
    }

    private String nameOf(PlayerId playerId) {
        String name = Bukkit.getOfflinePlayer(playerId.value()).getName();
        return name != null ? name : "?";
    }

    // achat
    @Override public void sendPurchaseSucceeded(PlayerId player, Contract contract, int ticketsLeft) {
        success(player, "Parchemin acheté : " + contract.quantity() + " × " + contract.material() + " (" + contract.job()
                + "), qualité minimale " + contract.minimalQuality().name() + ", récompense " + contract.reward()
                + " Lys"
                + ". Tickets restants : " + ticketsLeft + ".");
    }
    @Override public void sendPurchaseNoTicket(PlayerId player) { error(player, "Tu n'as plus de ticket de contrat aujourd'hui."); }
    @Override public void sendPurchaseInventoryFull(PlayerId player) { error(player, "Ton inventaire est plein."); }
    @Override public void sendPurchaseNoContractAvailable(PlayerId player) { error(player, "Aucun contrat n'est disponible pour le moment."); }
    // livraison : dialogues du PNJ, plusieurs répliques par cas, tirées au hasard
    @Override public void sendDeliverySucceeded(PlayerId player, String npcJob, int reward) {
        String lys = reward + " Lys";
        npcSays(player, npcJob, pick(
                Component.text("Parfait, c'est exactement ce qu'il me fallait ! Voilà tes ", NamedTextColor.WHITE)
                        .append(Component.text(lys, NamedTextColor.GOLD)).append(Component.text(".", NamedTextColor.WHITE)),
                Component.text("Excellent travail ! Tiens, ", NamedTextColor.WHITE)
                        .append(Component.text(lys, NamedTextColor.GOLD)).append(Component.text(" bien mérités.", NamedTextColor.WHITE)),
                Component.text("Livraison reçue ! ", NamedTextColor.WHITE)
                        .append(Component.text(lys, NamedTextColor.GOLD)).append(Component.text(" pour toi, dépense-les bien.", NamedTextColor.WHITE))));
    }
    @Override public void sendDeliveryWrongClient(PlayerId player, String npcJob) {
        npcSays(player, npcJob, pick(
                text("Ce parchemin n'est pas pour moi ! Va voir un collègue, je ne suis pas un bureau des objets trouvés."),
                text("Ce contrat n'est pas pour mon métier. Tu t'es trompé de PNJ, ça arrive aux meilleurs."),
                text("Je lis bien... non, ce n'est pas mon nom dessus. Essaie ailleurs.")));
    }
    @Override public void sendDeliveryInvalidScroll(PlayerId player, String npcJob) {
        npcSays(player, npcJob, pick(
                text("Ce parchemin est tout froissé, il ne vaut plus rien. Je ne peux rien en faire."),
                text("Ce papier n'a plus aucune valeur, même pour allumer mon feu.")));
    }
    @Override public void sendDeliveryExpired(PlayerId player, String npcJob) {
        npcSays(player, npcJob, pick(
                text("Trop tard ! Ce contrat a expiré, même le papier a l'air fatigué."),
                text("Tu arrives après la bataille : le délai est dépassé.")));
    }
    @Override public void sendDeliveryMissingItems(PlayerId player, String npcJob, int missing) {
        npcSays(player, npcJob, pick(
                text("Il m'en manque encore " + missing + ". Je sais compter, tu sais."),
                text("Encore " + missing + " à rapporter. Retourne en chercher, je t'attends.")));
    }

    @Override public void sendNoScrollInHand(PlayerId player, String npcJob) {
        npcSays(player, npcJob, pick(
                text("Je n'ai pas de contrat en cours avec toi. Reviens avec un parchemin, ça m'aiderait."),
                text("Tu veux me livrer quoi, de l'air ? Montre-moi un parchemin."),
                text("Les mains vides ? Même mon chat ramène plus de choses que toi.")));
    }

    // parchemin et tickets
    @Override public void sendScrollDestroyed(PlayerId player) { error(player, "Ce parchemin n'est plus valable : il a été détruit."); }
    @Override public void sendScrollsExpired(PlayerId player, int count) {
        error(player, count == 1
                ? "Un de tes parchemins de contrat a expiré : il a été supprimé de ton inventaire."
                : count + " de tes parchemins de contrat ont expiré : ils ont été supprimés de ton inventaire.");
    }
    @Override public void sendTicketsShown(PlayerId player, int tickets, int dailyQuota) {
        info(player, "Tickets de contrat : " + tickets + " (quota par jour : " + dailyQuota + ").");
    }
    @Override public void sendStaffInvalidAmount(PlayerId staff) {
        send(staff, STAFF_CHAT_ERROR.append(Component.text("Le montant doit être strictement positif.")));
    }
    @Override public void sendStaffTicketsGiven(PlayerId staff, PlayerId target, int amount) {
        send(staff, STAFF_CHAT_SUCCESS.append(Component.text("Opération effectuée - " + amount
                + " ticket(s) de contrat ajouté(s) à " + nameOf(target))));
    }
    @Override public void sendStaffTicketsShown(PlayerId staff, PlayerId target, int tickets, int dailyQuota) {
        send(staff, STAFF_CHAT_FORMAT.append(Component.text(nameOf(target) + " : " + tickets
                + " ticket(s) (quota par jour : " + dailyQuota + ")")));
    }
}

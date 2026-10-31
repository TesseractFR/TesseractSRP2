package onl.tesseract.srp.contract.adapter.serverside.messaging;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import onl.tesseract.srp.contract.adapter.serverside.yaml.YamlContractConfigRepository;
import onl.tesseract.srp.contract.adapter.utils.ContractDisplay;
import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.model.contract.DeliveryRefusal;
import onl.tesseract.srp.contract.domain.port.serverside.ContractMessagingRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

import static onl.tesseract.srp.contract.adapter.utils.ContractChatFormats.CONTRACT_CHAT_ERROR;
import static onl.tesseract.srp.contract.adapter.utils.ContractChatFormats.CONTRACT_CHAT_FORMAT;
import static onl.tesseract.srp.contract.adapter.utils.ContractChatFormats.CONTRACT_CHAT_SUCCESS;
import static onl.tesseract.srp.common.adapter.SrpChatFormats.STAFF_CHAT_ERROR;
import static onl.tesseract.srp.common.adapter.SrpChatFormats.STAFF_CHAT_FORMAT;
import static onl.tesseract.srp.common.adapter.SrpChatFormats.STAFF_CHAT_SUCCESS;

@org.springframework.stereotype.Component
public class BukkitContractMessagingRepository implements ContractMessagingRepository {

    private final YamlContractConfigRepository config;

    public BukkitContractMessagingRepository(YamlContractConfigRepository config) {
        this.config = config;
    }

    private void send(PlayerID id, Component msg) {
        Player p = Bukkit.getPlayer(id.value());
        if (p != null) p.sendMessage(msg);
    }

    private void error(PlayerID id, String text) {
        send(id, CONTRACT_CHAT_ERROR.append(Component.text(text)));
    }

    private void success(PlayerID id, String text) {
        send(id, CONTRACT_CHAT_SUCCESS.append(Component.text(text)));
    }

    private void info(PlayerID id, String text) {
        send(id, CONTRACT_CHAT_FORMAT.append(Component.text(text)));
    }

    private void npcSays(PlayerID id, String npc, Component line) {
        String name = config.getNpcName(npc).orElseGet(() -> ContractDisplay.capitalize(npc));
        send(id, Component.empty()
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

    private String nameOf(PlayerID id) {
        String name = Bukkit.getOfflinePlayer(id.value()).getName();
        return name != null ? name : "?";
    }

    // achat
    @Override public void sendPurchaseSucceeded(PlayerID p, Contract c, int ticketsLeft) {
        success(p, "Parchemin acheté : " + c.quantity() + " × " + c.material() + " (" + c.job()
                + "), qualité minimale " + c.minimalQuality().name() + ", récompense " + c.reward()
                + " Lys" + (c.type() == ContractType.HARVEST ? " + bonus métier" : "")
                + ". Tickets restants : " + ticketsLeft + ".");
    }
    @Override public void sendPurchaseNoTicket(PlayerID p) { error(p, "Tu n'as plus de ticket de contrat aujourd'hui."); }
    @Override public void sendPurchaseInventoryFull(PlayerID p) { error(p, "Ton inventaire est plein."); }
    @Override public void sendPurchaseNoContractAvailable(PlayerID p) { error(p, "Aucun contrat n'est disponible pour le moment."); }
    // livraison : dialogues du PNJ, plusieurs répliques par cas, tirées au hasard
    @Override public void sendDeliverySucceeded(PlayerID p, String npc, int reward) {
        String lys = reward + " Lys";
        npcSays(p, npc, pick(
                Component.text("Parfait, c'est exactement ce qu'il me fallait ! Voilà tes ", NamedTextColor.WHITE)
                        .append(Component.text(lys, NamedTextColor.GOLD)).append(Component.text(".", NamedTextColor.WHITE)),
                Component.text("Excellent travail ! Tiens, ", NamedTextColor.WHITE)
                        .append(Component.text(lys, NamedTextColor.GOLD)).append(Component.text(" bien mérités.", NamedTextColor.WHITE)),
                Component.text("Livraison reçue ! ", NamedTextColor.WHITE)
                        .append(Component.text(lys, NamedTextColor.GOLD)).append(Component.text(" pour toi, dépense-les bien.", NamedTextColor.WHITE))));
    }
    @Override public void sendDeliveryRefused(PlayerID p, String npc, DeliveryRefusal reason, int missing) {
        switch (reason) {
            case WRONG_CLIENT -> wrongClient(p, npc);
            case INVALID_SCROLL -> invalidScroll(p, npc);
            case EXPIRED -> expired(p, npc);
            case MISSING_ITEMS -> missingItems(p, npc, missing);
            case NOT_ENOUGH_QUALITY -> notEnoughQuality(p, npc, missing);
            case PAYMENT_FAILED -> paymentFailed(p, npc);
        }
    }
    private void wrongClient(PlayerID p, String npc) {
        npcSays(p, npc, pick(
                text("Ce parchemin n'est pas pour moi ! Va voir un collègue, je ne suis pas un bureau des objets trouvés."),
                text("Ce contrat n'est pas pour mon métier. Tu t'es trompé de PNJ, ça arrive aux meilleurs."),
                text("Je lis bien... non, ce n'est pas mon nom dessus. Essaie ailleurs.")));
    }
    private void invalidScroll(PlayerID p, String npc) {
        npcSays(p, npc, pick(
                text("Ce parchemin est tout froissé, il ne vaut plus rien. Je ne peux rien en faire."),
                text("Ce papier n'a plus aucune valeur, même pour allumer mon feu.")));
    }
    private void expired(PlayerID p, String npc) {
        npcSays(p, npc, pick(
                text("Trop tard ! Ce contrat a expiré, même le papier a l'air fatigué."),
                text("Tu arrives après la bataille : le délai est dépassé.")));
    }
    private void missingItems(PlayerID p, String npc, int missing) {
        npcSays(p, npc, pick(
                text("Il m'en manque encore " + missing + ". Je sais compter, tu sais."),
                text("Encore " + missing + " à rapporter. Retourne en chercher, je t'attends.")));
    }
    private void notEnoughQuality(PlayerID p, String npc, int missing) {
        npcSays(p, npc, pick(
                text("La qualité n'est pas au rendez-vous : il me manque " + missing + " pièce(s) du niveau demandé. Je ne suis pas si facile."),
                text("Tu as de quoi faire du bon travail, mais pas assez de qualité : encore " + missing + " du niveau demandé.")));
    }
    private void paymentFailed(PlayerID p, String npc) {
        npcSays(p, npc, pick(
                text("Mes caisses ont un petit souci, je te rends tes affaires. Reviens plus tard !"),
                text("Je n'arrive pas à te payer, reprends tes affaires et réessaie dans un moment.")));
    }
    @Override public void sendNoScrollInHand(PlayerID p, String npc) {
        npcSays(p, npc, pick(
                text("Je n'ai pas de contrat en cours avec toi. Reviens avec un parchemin, ça m'aiderait."),
                text("Tu veux me livrer quoi, de l'air ? Montre-moi un parchemin."),
                text("Les mains vides ? Même mon chat ramène plus de choses que toi.")));
    }

    // parchemin et tickets
    @Override public void sendScrollDestroyed(PlayerID p) { error(p, "Ce parchemin n'est plus valable : il a été détruit."); }
    @Override public void sendScrollsExpired(PlayerID p, int count) {
        error(p, count == 1
                ? "Un de tes parchemins de contrat a expiré : il a été supprimé de ton inventaire."
                : count + " de tes parchemins de contrat ont expiré : ils ont été supprimés de ton inventaire.");
    }
    @Override public void sendTicketsShown(PlayerID p, int tickets, int dailyQuota) {
        info(p, "Tickets de contrat : " + tickets + " (quota par jour : " + dailyQuota + ").");
    }
    @Override public void sendStaffInvalidAmount(PlayerID s) {
        send(s, STAFF_CHAT_ERROR.append(Component.text("Le montant doit être strictement positif.")));
    }
    @Override public void sendStaffTicketsGiven(PlayerID s, PlayerID t, int amount) {
        send(s, STAFF_CHAT_SUCCESS.append(Component.text("Opération effectuée - " + amount
                + " ticket(s) de contrat ajouté(s) à " + nameOf(t))));
    }
    @Override public void sendStaffTicketsShown(PlayerID s, PlayerID t, int tickets, int dailyQuota) {
        send(s, STAFF_CHAT_FORMAT.append(Component.text(nameOf(t) + " : " + tickets
                + " ticket(s) (quota par jour : " + dailyQuota + ")")));
    }
}
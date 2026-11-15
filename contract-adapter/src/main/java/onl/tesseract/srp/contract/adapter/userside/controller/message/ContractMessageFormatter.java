package onl.tesseract.srp.contract.adapter.userside.controller.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.contract.adapter.serverside.yaml.ContractNpcNameConfig;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;
import onl.tesseract.srp.contract.domain.model.exception.ContractException;
import onl.tesseract.srp.contract.domain.model.exception.ContractExpiredException;
import onl.tesseract.srp.contract.domain.model.exception.ContractNotFoundException;
import onl.tesseract.srp.contract.domain.model.exception.ContractScrollNotOwnedException;
import onl.tesseract.srp.contract.domain.model.exception.InvalidTicketAmountException;
import onl.tesseract.srp.contract.domain.model.exception.MissingContractItemsException;
import onl.tesseract.srp.contract.domain.model.exception.NoContractAvailableException;
import onl.tesseract.srp.contract.domain.model.exception.NoContractTicketLeftException;
import onl.tesseract.srp.contract.domain.model.exception.NotEnoughMaterialException;
import onl.tesseract.srp.contract.domain.model.exception.PaymentFailedException;
import onl.tesseract.srp.contract.domain.model.exception.ScrollInventoryFullException;
import onl.tesseract.srp.contract.domain.model.exception.WrongContractClientException;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ThreadLocalRandom;

import static onl.tesseract.srp.contract.adapter.userside.controller.message.ContractChatFormats.CONTRACT_CHAT_ERROR;
import static onl.tesseract.srp.contract.adapter.userside.controller.message.ContractChatFormats.CONTRACT_CHAT_FORMAT;
import static onl.tesseract.srp.contract.adapter.userside.controller.message.ContractChatFormats.CONTRACT_CHAT_SUCCESS;
import static onl.tesseract.srp.common.adapter.SrpChatFormats.STAFF_CHAT_ERROR;
import static onl.tesseract.srp.common.adapter.SrpChatFormats.STAFF_CHAT_FORMAT;
import static onl.tesseract.srp.common.adapter.SrpChatFormats.STAFF_CHAT_SUCCESS;

/**
 * Formate les messages de contrat affichés aux joueurs et au staff, dont la traduction des refus
 * métier. Lors d'une livraison, c'est le PNJ qui parle, avec plusieurs répliques tirées au hasard.
 */
@org.springframework.stereotype.Component
public class ContractMessageFormatter {

    private final ContractNpcNameConfig contractNpcNameConfig;

    public ContractMessageFormatter(ContractNpcNameConfig contractNpcNameConfig) {
        this.contractNpcNameConfig = contractNpcNameConfig;
    }

    // ---- Chat du module

    public Component formatPurchaseSucceeded(Contract contract, int ticketsLeft) {
        return success("Parchemin acheté : " + contract.quantity() + " × " + contract.material().value()
                + " (" + contract.client().name() + "), qualité minimale " + contract.minimalQuality().name()
                + ", récompense " + contract.reward().value() + " Lys. Tickets restants : " + ticketsLeft + ".");
    }

    public Component formatTickets(int tickets, int dailyQuota) {
        return CONTRACT_CHAT_FORMAT.append(Component.text(
                "Tickets de contrat : " + tickets + " (quota par jour : " + dailyQuota + ")."));
    }

    public Component formatScrollsExpired(int count) {
        return error(count == 1
                ? "Un de tes parchemins de contrat a expiré : il a été supprimé de ton inventaire."
                : count + " de tes parchemins de contrat ont expiré : ils ont été supprimés de ton inventaire.");
    }

    /** Traduit un refus métier en message de chat. Le switch est exhaustif grâce à la hiérarchie scellée. */
    public Component formatRefusal(ContractException exception) {
        return switch (exception) {
            case NoContractTicketLeftException ignored -> error("Tu n'as plus de ticket de contrat aujourd'hui.");
            case ScrollInventoryFullException ignored -> error("Ton inventaire est plein.");
            case NoContractAvailableException ignored -> error("Aucun contrat n'est disponible pour le moment.");
            case ContractNotFoundException ignored -> error("Ce parchemin ne correspond à aucun contrat.");
            case ContractExpiredException ignored -> error("Ce contrat a expiré.");
            case WrongContractClientException refusal -> error("Ce contrat est destiné à " + refusal.getExpectedClient().name() + ".");
            case ContractScrollNotOwnedException ignored -> error("Tu n'as pas le parchemin de ce contrat.");
            case MissingContractItemsException refusal -> error("Il te manque " + refusal.getMissingQuantity() + " matériaux.");
            case NotEnoughMaterialException ignored -> error("Tu n'as plus assez de matériaux pour ce contrat.");
            case PaymentFailedException ignored -> error("Le versement de ta récompense a échoué. Préviens le staff.");
            case InvalidTicketAmountException ignored ->
                    STAFF_CHAT_ERROR.append(Component.text("Le montant doit être strictement positif."));
        };
    }

    // ---- Dialogues du PNJ lors d'une livraison

    public Component formatNpcDeliverySucceeded(ContractClient npc, MoneyValue payment) {
        String lys = payment.value() + " Lys";
        return npcSays(npc, pickOne(
                Component.text("Parfait, c'est exactement ce qu'il me fallait ! Voilà tes ", NamedTextColor.WHITE)
                        .append(Component.text(lys, NamedTextColor.GOLD)).append(Component.text(".", NamedTextColor.WHITE)),
                Component.text("Excellent travail ! Tiens, ", NamedTextColor.WHITE)
                        .append(Component.text(lys, NamedTextColor.GOLD)).append(Component.text(" bien mérités.", NamedTextColor.WHITE)),
                Component.text("Livraison reçue ! ", NamedTextColor.WHITE)
                        .append(Component.text(lys, NamedTextColor.GOLD)).append(Component.text(" pour toi, dépense-les bien.", NamedTextColor.WHITE))));
    }

    public Component formatNpcNoScrollInHand(ContractClient npc) {
        return npcSays(npc, pickOne(
                white("Je n'ai pas de contrat en cours avec toi. Reviens avec un parchemin, ça m'aiderait."),
                white("Tu veux me livrer quoi, de l'air ? Montre-moi un parchemin."),
                white("Les mains vides ? Même mon chat ramène plus de choses que toi.")));
    }

    public Component formatNpcInvalidScroll(ContractClient npc) {
        return npcSays(npc, pickOne(
                white("Ce parchemin est tout froissé, il ne vaut plus rien. Je ne peux rien en faire."),
                white("Ce papier n'a plus aucune valeur, même pour allumer mon feu.")));
    }

    /** Les refus propres à la livraison sont dits par le PNJ ; les autres passent par le chat du module. */
    public Component formatNpcRefusal(ContractClient npc, ContractException exception) {
        return switch (exception) {
            case ContractNotFoundException ignored -> formatNpcInvalidScroll(npc);
            case WrongContractClientException ignored -> npcSays(npc, pickOne(
                    white("Ce parchemin n'est pas pour moi ! Va voir un collègue, je ne suis pas un bureau des objets trouvés."),
                    white("Ce contrat n'est pas pour mon métier. Tu t'es trompé de PNJ, ça arrive aux meilleurs."),
                    white("Je lis bien... non, ce n'est pas mon nom dessus. Essaie ailleurs.")));
            case ContractExpiredException ignored -> npcSays(npc, pickOne(
                    white("Trop tard ! Ce contrat a expiré, même le papier a l'air fatigué."),
                    white("Tu arrives après la bataille : le délai est dépassé.")));
            case ContractScrollNotOwnedException ignored -> formatNpcNoScrollInHand(npc);
            case MissingContractItemsException refusal -> npcSays(npc, pickOne(
                    white("Il m'en manque encore " + refusal.getMissingQuantity() + ". Je sais compter, tu sais."),
                    white("Encore " + refusal.getMissingQuantity() + " à rapporter. Retourne en chercher, je t'attends.")));
            default -> formatRefusal(exception);
        };
    }

    // ---- Staff

    public Component formatStaffTicketsGiven(String targetName, int amount) {
        return STAFF_CHAT_SUCCESS.append(Component.text("Opération effectuée - " + amount
                + " ticket(s) de contrat ajouté(s) à " + targetName));
    }

    public Component formatStaffTickets(String targetName, int tickets, int dailyQuota) {
        return STAFF_CHAT_FORMAT.append(Component.text(targetName + " : " + tickets
                + " ticket(s) (quota par jour : " + dailyQuota + ")"));
    }

    // ---- Mise en forme

    private Component npcSays(ContractClient npc, Component line) {
        String npcName = contractNpcNameConfig.findNpcName(npc).orElseGet(() -> StringUtils.capitalize(npc.name()));
        return Component.empty()
                .append(Component.text(npcName, NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                .append(line);
    }

    private static Component error(String text) {
        return CONTRACT_CHAT_ERROR.append(Component.text(text));
    }

    private static Component success(String text) {
        return CONTRACT_CHAT_SUCCESS.append(Component.text(text));
    }

    private static Component white(String line) {
        return Component.text(line, NamedTextColor.WHITE);
    }

    private static Component pickOne(Component... lines) {
        return lines[ThreadLocalRandom.current().nextInt(lines.length)];
    }
}

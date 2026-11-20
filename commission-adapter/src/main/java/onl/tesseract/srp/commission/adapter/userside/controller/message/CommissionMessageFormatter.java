package onl.tesseract.srp.commission.adapter.userside.controller.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.srp.commission.adapter.userside.controller.menu.CommissionDisplay;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.UpgradeType;
import onl.tesseract.srp.commission.domain.model.commission.Commission;
import onl.tesseract.srp.commission.domain.model.commission.Delivery;
import onl.tesseract.srp.commission.domain.model.exception.BoardFurnitureUnavailableException;
import onl.tesseract.srp.commission.domain.model.exception.BoardManagementNotAllowedException;
import onl.tesseract.srp.commission.domain.model.exception.BoardOutsideTerritoryException;
import onl.tesseract.srp.commission.domain.model.exception.BoardPartAccessDeniedException;
import onl.tesseract.srp.commission.domain.model.exception.BoardPlacementNotStartedException;
import onl.tesseract.srp.commission.domain.model.exception.BoardSpotNotFreeException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardNotFoundException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionNotAvailableException;
import onl.tesseract.srp.commission.domain.model.exception.GuildLevelTooLowException;
import onl.tesseract.srp.commission.domain.model.exception.InvalidUsageTaxRateException;
import onl.tesseract.srp.commission.domain.model.exception.ItemQualityTooLowException;
import onl.tesseract.srp.commission.domain.model.exception.NoItemToDeliverException;
import onl.tesseract.srp.commission.domain.model.exception.NotEnoughBankMoneyException;
import onl.tesseract.srp.commission.domain.model.exception.UpgradeMaxLevelReachedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionChatFormats.COMMISSION_CHAT_ERROR;
import static onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionChatFormats.COMMISSION_CHAT_FORMAT;
import static onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionChatFormats.COMMISSION_CHAT_SUCCESS;

/** Formate les messages de commission affichés aux joueurs, dont la traduction des refus métier. */
@org.springframework.stereotype.Component
public class CommissionMessageFormatter {

    // ---- Livraison

    /**
     * Le livreur ne voit que son gain, taxe déjà déduite : la taxe est détaillée dans le menu avant la
     * livraison, et ce que gagne la guilde est annoncé à ses membres par {@link #formatGuildDeliveryNotice}.
     *
     * @return le message de livraison, suivi d'un second message si la commission est terminée
     */
    public List<Component> formatDeliverySucceeded(Commission commission, Delivery delivery) {
        Component deliveryMessage = COMMISSION_CHAT_FORMAT.append(Component.text("Tu as livré "))
                .append(formatDeliveredItems(commission, delivery))
                .append(Component.text(" et gagné "))
                .append(Component.text(delivery.playerEarnings() + " lys", NamedTextColor.GREEN))
                .append(Component.text("."));
        List<Component> messages = new ArrayList<>();
        messages.add(deliveryMessage);
        if (delivery.completesCommission()) {
            messages.add(COMMISSION_CHAT_FORMAT.append(Component.text("Commission terminée : une nouvelle arrive sur le tableau.")));
        }
        return messages;
    }

    /**
     * Message envoyé aux membres de la guilde propriétaire, livreur compris. Il ne cite que ce que la
     * guilde gagne vraiment : la taxe d'un externe et l'XP.
     *
     * @return le message, ou {@link Optional#empty()} si la livraison ne rapporte rien à la guilde
     */
    public Optional<Component> formatGuildDeliveryNotice(String delivererName, Commission commission, Delivery delivery) {
        List<Component> guildGains = new ArrayList<>();
        if (delivery.guildTax() > 0) {
            guildGains.add(Component.text(delivery.guildTax() + " lys", NamedTextColor.GREEN));
        }
        if (delivery.guildXp() > 0) {
            guildGains.add(Component.text(delivery.guildXp() + " XP", NamedTextColor.AQUA));
        }
        if (guildGains.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(COMMISSION_CHAT_FORMAT.append(Component.text(delivererName, NamedTextColor.WHITE))
                .append(Component.text(" a livré "))
                .append(formatDeliveredItems(commission, delivery))
                .append(Component.text(" : la guilde gagne "))
                .append(Component.join(JoinConfiguration.separator(Component.text(" et ")), guildGains))
                .append(Component.text(".")));
    }

    private static Component formatDeliveredItems(Commission commission, Delivery delivery) {
        return Component.text(delivery.itemCount() + " × " + CommissionDisplay.formatMaterialName(commission.material()),
                NamedTextColor.WHITE);
    }

    // ---- Gestion du tableau

    public Component formatUpgradeSucceeded(UpgradeType upgradeType) {
        return success("Amélioration effectuée : " + CommissionDisplay.formatUpgradeName(upgradeType)
                + ". Les commissions en cours ne changent pas.");
    }

    public Component formatUsageTaxChanged(double usageTaxRate) {
        return success("La taxe des externes est maintenant de " + CommissionDisplay.formatPercent(usageTaxRate) + ".");
    }

    // ---- Pose du tableau

    public Component formatBoardPlacementStarted() {
        return success("Fais un clic droit sur le sol où poser le tableau. Accroupis-toi pour annuler.");
    }

    public Component formatBoardPlacementCountdown(int secondsLeft) {
        return Component.text("Clic droit au sol pour poser le tableau · " + secondsLeft + " s", NamedTextColor.YELLOW);
    }

    public Component formatBoardPlacementCancelled() {
        return error("Pose du tableau annulée.");
    }

    public Component formatBoardPlacementExpired() {
        return error("Temps écoulé : le tableau n'a pas été déplacé.");
    }

    public Component formatBoardPlaced() {
        return success("Le tableau de la guilde est posé.");
    }

    public Component formatBoardPlacementNotOnTopOfBlock() {
        return error("Clique sur le dessus d'un bloc.");
    }

    public Component formatBoardCannotBeBroken() {
        return error("Le tableau se déplace avec le bouton du menu de guilde.");
    }

    // ---- Refus métier

    public Component formatRefusal(CommissionException refusal) {
        return error(switch (refusal) {
            case CommissionNotAvailableException ignored -> "Cette commission n'est plus disponible.";
            case CommissionBoardNotFoundException ignored -> "Ce tableau n'existe plus.";
            case BoardPartAccessDeniedException ignored -> "Cette commission est réservée aux membres de la guilde.";
            case NoItemToDeliverException ignored -> "Tu n'as aucun item à livrer pour cette commission.";
            case ItemQualityTooLowException ignored -> "Tes items ne sont pas d'une qualité suffisante.";
            case BoardManagementNotAllowedException ignored -> "Seuls les adjoints et le chef peuvent gérer le tableau.";
            case UpgradeMaxLevelReachedException ignored -> "Cette amélioration est déjà au niveau maximal.";
            case GuildLevelTooLowException ignored -> "Le niveau de la guilde est trop bas pour cette amélioration.";
            case NotEnoughBankMoneyException ignored -> "La banque de la guilde n'a pas assez de lys.";
            case InvalidUsageTaxRateException ignored ->
                    "La taxe doit rester entre 0 % et " + CommissionDisplay.formatPercent(CommissionBoard.MAX_USAGE_TAX_RATE) + ".";
            case BoardOutsideTerritoryException ignored -> "Le tableau doit être entièrement sur le territoire de la guilde.";
            case BoardSpotNotFreeException ignored -> "Il faut 3 blocs de large et 2 de haut entièrement libres.";
            case BoardFurnitureUnavailableException ignored -> "Le tableau n'est pas disponible pour le moment.";
            case BoardPlacementNotStartedException ignored -> "Tu ne poses pas de tableau en ce moment.";
        });
    }

    private static Component success(String text) {
        return COMMISSION_CHAT_SUCCESS.append(Component.text(text));
    }

    private static Component error(String text) {
        return COMMISSION_CHAT_ERROR.append(Component.text(text));
    }
}

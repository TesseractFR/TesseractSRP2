package onl.tesseract.srp.commission.adapter.userside.controller.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeType;
import onl.tesseract.srp.commission.domain.model.board.UsageTaxRate;
import onl.tesseract.srp.commission.domain.model.commission.Commission;
import onl.tesseract.srp.commission.domain.model.commission.CommissionDelivery;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardManagementNotAllowedException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardPartAccessDeniedException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardTierTooLowException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardNotFoundException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionNotAvailableException;
import onl.tesseract.srp.commission.domain.model.exception.InvalidUsageTaxRateException;
import onl.tesseract.srp.commission.domain.model.exception.ItemQualityTooLowException;
import onl.tesseract.srp.commission.domain.model.exception.NoItemToDeliverException;
import onl.tesseract.srp.commission.domain.model.exception.NotEnoughBankMoneyException;
import onl.tesseract.srp.commission.domain.model.exception.NotEnoughDeliveredItemsException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardUpgradeMaxLevelReachedException;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionChatFormats.COMMISSION_CHAT_ERROR;
import static onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionChatFormats.COMMISSION_CHAT_FORMAT;
import static onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionChatFormats.COMMISSION_CHAT_SUCCESS;

/** Formate les messages de commission affichés aux joueurs, dont la traduction des refus métier. */
@org.springframework.stereotype.Component
public class CommissionMessageFormatter {

    private final CustomItemService customItemService;

    public CommissionMessageFormatter(CustomItemService customItemService) {
        this.customItemService = customItemService;
    }

    /** Un item custom affiche le nom de sa configuration ; un item vanilla, un nom tiré de son identifiant. */
    public String formatMaterialName(MaterialName material) {
        return Optional.ofNullable(customItemService.getCustomMaterial(material))
                .map(customMaterial -> customMaterial.displayName().value())
                .orElseGet(() -> formatVanillaMaterialName(material));
    }

    /**
     * Une seule ligne pour le livreur : ses lys, taxe déjà déduite (elle est détaillée dans le menu avant
     * la livraison), l'XP de sa guilde s'il en est membre, et la fin de la commission. Les autres membres
     * sont prévenus par {@link #formatGuildDeliveryNotice}.
     *
     * @return le message de livraison
     */
    public Component formatDeliverySucceeded(Commission commission, CommissionDelivery delivery) {
        List<Component> gains = new ArrayList<>();
        gains.add(Component.text("+" + delivery.playerEarnings() + " lys", NamedTextColor.GREEN));
        if (delivery.isGuildMember() && delivery.guildXp() > 0) {
            gains.add(formatGuildXp(delivery));
        }
        Component message = COMMISSION_CHAT_FORMAT.append(formatDeliveredItems(commission, delivery))
                .append(Component.text(" livrés : "))
                .append(Component.join(JoinConfiguration.commas(true), gains));
        if (delivery.completesCommission()) {
            message = message.append(Component.text(" · Commission terminée !", NamedTextColor.GOLD));
        }
        return message;
    }

    /**
     * Message envoyé aux autres membres connectés de la guilde propriétaire. Il ne cite que ce que la
     * guilde gagne vraiment : la taxe d'un externe et l'XP.
     *
     * @return le message, ou {@link Optional#empty()} si la livraison ne rapporte rien à la guilde
     */
    public Optional<Component> formatGuildDeliveryNotice(String delivererName, Commission commission, CommissionDelivery delivery) {
        List<Component> guildGains = new ArrayList<>();
        if (delivery.guildTax() > 0) {
            guildGains.add(Component.text("+" + delivery.guildTax() + " lys", NamedTextColor.GREEN));
        }
        if (delivery.guildXp() > 0) {
            guildGains.add(formatGuildXp(delivery));
        }
        if (guildGains.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(COMMISSION_CHAT_FORMAT.append(Component.text(delivererName, NamedTextColor.WHITE))
                .append(Component.text(" a livré "))
                .append(formatDeliveredItems(commission, delivery))
                .append(Component.text(" : "))
                .append(Component.join(JoinConfiguration.commas(true), guildGains)));
    }

    private static Component formatGuildXp(CommissionDelivery delivery) {
        return Component.text("+" + delivery.guildXp() + " XP de guilde", NamedTextColor.AQUA);
    }

    private Component formatDeliveredItems(Commission commission, CommissionDelivery delivery) {
        return Component.text(delivery.itemCount() + " × " + formatMaterialName(commission.material()),
                NamedTextColor.WHITE);
    }

    public Component formatUpgradeSucceeded(CommissionBoardUpgradeType upgradeType) {
        return success("Amélioration effectuée : " + CommissionLabels.formatUpgradeName(upgradeType)
                + ". Les commissions en cours ne changent pas.");
    }

    public Component formatUsageTaxChanged(UsageTaxRate usageTaxRate) {
        return success("La taxe des externes est maintenant de " + CommissionLabels.formatUsageTax(usageTaxRate) + ".");
    }

    public Component formatRefusal(CommissionException refusal) {
        return error(switch (refusal) {
            case CommissionNotAvailableException ignored -> "Cette commission n'est plus disponible.";
            case CommissionBoardNotFoundException ignored -> "Ce tableau n'existe plus.";
            case CommissionBoardPartAccessDeniedException ignored -> "Cette commission est réservée aux membres de la guilde.";
            case NoItemToDeliverException ignored -> "Tu n'as aucun item à livrer pour cette commission.";
            case ItemQualityTooLowException ignored -> "Tes items ne sont pas d'une qualité suffisante.";
            case CommissionBoardManagementNotAllowedException ignored -> "Seuls les adjoints et le chef peuvent gérer le tableau.";
            case CommissionBoardUpgradeMaxLevelReachedException ignored -> "Cette amélioration est déjà au niveau maximal.";
            case CommissionBoardTierTooLowException ignored -> "Le tier du tableau est trop bas pour cette amélioration.";
            case NotEnoughDeliveredItemsException ignored -> "Le tableau n'a pas encore reçu assez de livraisons pour monter de tier.";
            case NotEnoughBankMoneyException ignored -> "La banque de la guilde n'a pas assez de lys.";
            case InvalidUsageTaxRateException ignored ->
                    "La taxe doit rester entre 0 % et " + UsageTaxRate.MAX_PERCENT + " %.";
        });
    }

    private static String formatVanillaMaterialName(MaterialName material) {
        String lowerCaseMaterial = material.value().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lowerCaseMaterial.charAt(0)) + lowerCaseMaterial.substring(1);
    }

    private static Component success(String text) {
        return COMMISSION_CHAT_SUCCESS.append(Component.text(text));
    }

    private static Component error(String text) {
        return COMMISSION_CHAT_ERROR.append(Component.text(text));
    }
}

package onl.tesseract.srp.commission.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionLabels;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeType;
import onl.tesseract.srp.commission.domain.model.commission.Commission;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.commission.domain.model.commission.CommissionDelivery;
import onl.tesseract.srp.common.adapter.utils.QualityAdapter;

import java.util.ArrayList;
import java.util.List;

/** Textes et lignes de lore des menus de commissions. */
public final class CommissionDisplay {
    private static final String BULLET = "• ";
    private static final int PROGRESS_BAR_CELLS = 14;

    private CommissionDisplay() {
    }

    /** @return la première ligne du lore d'une commission : son type et le tier de son item */
    public static Component describeCommissionHeader(CommissionType commissionType, int commissionTier) {
        return coloredText(CommissionLabels.formatCommissionType(commissionType) + " · Tier " + commissionTier, NamedTextColor.DARK_GRAY);
    }

    /** @return la première ligne du lore d'une commission dont l'item a quitté le catalogue : son type seul */
    public static Component describeCommissionHeader(CommissionType commissionType) {
        return coloredText(CommissionLabels.formatCommissionType(commissionType), NamedTextColor.DARK_GRAY);
    }

    /**
     * @param totalGuildXp XP que la commission donne à la guilde une fois terminée, affichée aux membres seulement
     * @return le lore d'une commission sous son en-tête, avec ce que la livraison du joueur rapporterait
     */
    public static List<Component> describeCommission(Commission commission, int totalGuildXp, CommissionDelivery delivery,
                                                     boolean showsDeliveryHint) {
        QualityAdapter minimalQuality = QualityAdapter.from(commission.minimalQuality());
        List<Component> loreLines = new ArrayList<>();
        loreLines.add(Component.empty());
        loreLines.add(buildProgressBar(commission));
        loreLines.add(labeledValue("Qualité minimale : ", minimalQuality.label(), minimalQuality.color()));
        loreLines.add(labeledValue("Récompense totale : ", commission.reward().value() + " lys", NamedTextColor.GREEN));
        if (delivery.isGuildMember() && totalGuildXp > 0) {
            loreLines.add(labeledValue("XP de guilde totale : ", "+" + totalGuildXp, NamedTextColor.AQUA));
        }
        loreLines.add(Component.empty());
        if (delivery.isEmpty()) {
            loreLines.add(coloredText("Tu n'as aucun item à livrer", NamedTextColor.RED));
            return loreLines;
        }
        loreLines.add(coloredText("Ta livraison", NamedTextColor.GOLD));
        loreLines.add(labeledValue(BULLET + "Items : ", String.valueOf(delivery.itemCount()), NamedTextColor.WHITE));
        if (delivery.guildTax() > 0) {
            loreLines.add(labeledValue(BULLET + "Valeur : ", delivery.getItemsValue() + " lys", NamedTextColor.WHITE));
            loreLines.add(labeledValue(BULLET + "Taxe de la guilde : ", "-" + delivery.guildTax() + " lys", NamedTextColor.RED));
        }
        loreLines.add(labeledValue(BULLET + "Gain total : ", delivery.playerEarnings() + " lys", NamedTextColor.GREEN));
        if (delivery.isGuildMember() && delivery.guildXp() > 0) {
            loreLines.add(labeledValue(BULLET + "XP de guilde : ", "+" + delivery.guildXp(), NamedTextColor.AQUA));
        }
        if (showsDeliveryHint) {
            loreLines.add(Component.empty());
            loreLines.add(coloredText("» Clique pour livrer", NamedTextColor.YELLOW));
        }
        return loreLines;
    }

    /**
     * @param spawnRewardRate part de la valeur des items payée au spawn, affichée sur le tableau du spawn
     * @return le lore du bouton d'information du tableau
     */
    public static List<Component> describeBoard(CommissionBoard commissionBoard, double spawnRewardRate) {
        List<Component> loreLines = new ArrayList<>();
        loreLines.add(coloredText("Fonctionnement", NamedTextColor.GOLD));
        loreLines.add(coloredText(BULLET + "Les commissions sont communes", NamedTextColor.GRAY));
        loreLines.add(coloredText(BULLET + "Chacun livre ce qu'il peut", NamedTextColor.GRAY));
        loreLines.add(Component.empty());
        loreLines.add(coloredText("Paiement", NamedTextColor.GOLD));
        loreLines.add(coloredText(BULLET + "Tu touches la part de ce que tu livres", NamedTextColor.GRAY));
        loreLines.add(coloredText(BULLET + "Prix fixé par la qualité minimale", NamedTextColor.GRAY));
        loreLines.add(coloredText(BULLET + "Une meilleure qualité ne paie pas plus", NamedTextColor.GRAY));
        if (commissionBoard.isSpawnBoard()) {
            loreLines.add(coloredText(BULLET + "Au spawn, les montants valent " + CommissionLabels.formatPercent(spawnRewardRate)
                    + " de la valeur des items", NamedTextColor.GRAY));
        }
        loreLines.add(Component.empty());
        if (!commissionBoard.isSpawnBoard()) {
            loreLines.add(coloredText("Public et guilde", NamedTextColor.GOLD));
            loreLines.add(coloredText(BULLET + "Public : ouvert à tous", NamedTextColor.GRAY));
            loreLines.add(coloredText(BULLET + "Guilde : réservé aux membres", NamedTextColor.GRAY));
            loreLines.add(labeledValue(BULLET + "Taxe des externes : ", CommissionLabels.formatUsageTax(commissionBoard.getUsageTaxRate()),
                    NamedTextColor.WHITE));
            loreLines.add(Component.empty());
        }
        loreLines.add(coloredText("Nouvelles commissions", NamedTextColor.GOLD));
        loreLines.add(coloredText(BULLET + "Une commission terminée est remplacée aussitôt", NamedTextColor.GRAY));
        loreLines.add(coloredText(BULLET + "Sinon, elle reste jusqu'à ce qu'elle soit terminée", NamedTextColor.GRAY));
        return loreLines;
    }

    public static Component coloredText(String content, TextColor textColor) {
        return Component.text(content, textColor).decoration(TextDecoration.ITALIC, false);
    }

    public static Component labeledValue(String label, String value, TextColor valueColor) {
        return Component.text(label, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text(value, valueColor));
    }

    public static List<String> describeUpgrade(CommissionBoardUpgradeType upgradeType) {
        return switch (upgradeType) {
            case BOARD_TIER -> List.of("Débloque les paliers suivants", "des autres améliorations");
            case MAX_HARVEST_TIER -> List.of("Débloque des matériaux de récolte", "plus avancés pour plus de gains");
            case MAX_CRAFT_TIER -> List.of("Débloque des recettes plus avancées", "pour plus de gains");
            case HARVEST_SLOTS -> List.of("Ajoute une commission de récolte", "en public et en guilde");
            case CRAFT_SLOTS -> List.of("Ajoute une commission d'artisanat", "en public et en guilde");
            case MONEY_BONUS -> List.of("Augmente les gains des commissions");
        };
    }

    /** @param moneyBonusRate taux du bonus d'argent à ce niveau, utilisé seulement pour le bonus d'argent */
    public static String formatUpgradeLevel(CommissionBoardUpgradeType upgradeType, int upgradeLevel, double moneyBonusRate) {
        return switch (upgradeType) {
            case BOARD_TIER, MAX_HARVEST_TIER, MAX_CRAFT_TIER -> "Tier " + upgradeLevel;
            case HARVEST_SLOTS, CRAFT_SLOTS -> upgradeLevel + " en public et en guilde";
            case MONEY_BONUS -> "+" + CommissionLabels.formatPercent(moneyBonusRate);
        };
    }

    private static Component buildProgressBar(Commission commission) {
        int filledCells = PROGRESS_BAR_CELLS * commission.deliveredQuantity() / commission.quantity();
        return coloredText("■".repeat(filledCells), NamedTextColor.GREEN)
                .append(Component.text("■".repeat(PROGRESS_BAR_CELLS - filledCells), NamedTextColor.DARK_GRAY))
                .append(Component.text("  " + commission.deliveredQuantity() + " / " + commission.quantity(), NamedTextColor.YELLOW));
    }
}

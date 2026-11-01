package onl.tesseract.srp.contract.adapter.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import onl.tesseract.srp.common.adapter.utils.QualityAdapter;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ContractDisplay {

    private static final DateTimeFormatter EXPIRATION_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM HH:mm").withZone(ZoneId.systemDefault());

    private static final int UNKNOWN_BONUS = -1;

    private ContractDisplay() {
    }

    public static List<Component> scrollDetails(Contract contract, boolean hasJobBonus) {
        return details(contract, hasJobBonus ? UNKNOWN_BONUS : 0);
    }

    public static List<Component> details(Contract contract, int jobBonus) {
        boolean isCraft = contract.type() == ContractType.CRAFT;
        QualityAdapter minimalQuality = QualityAdapter.from(QualityConverter.toCommon(contract.minimalQuality()));
        List<Component> lines = new ArrayList<>();
        lines.add(line("Type : ", isCraft ? "Artisanat" : "Récolte", isCraft ? NamedTextColor.AQUA : NamedTextColor.GREEN));
        lines.add(line(isCraft ? "Compétence : " : "Métier : ", capitalize(contract.job()), NamedTextColor.GOLD));
        lines.add(line("Demande : ", contract.quantity() + " × " + capitalize(contract.material()), NamedTextColor.YELLOW));
        lines.add(line("Qualité minimale : ", minimalQuality.label(), minimalQuality.color()));
        lines.add(line("Récompense : ", rewardText(contract, isCraft, jobBonus), NamedTextColor.GREEN));
        lines.add(line("Expire le : ", EXPIRATION_DATE_FORMAT.format(contract.expiresAt()), NamedTextColor.RED));
        return lines;
    }

    private static String rewardText(Contract contract, boolean isCraft, int jobBonus) {
        String baseText = contract.reward() + " lys";
        if (isCraft) {
            return baseText;
        }
        if (jobBonus == UNKNOWN_BONUS) {
            return baseText + " + bonus métier";
        }
        return jobBonus > 0 ? baseText + " + " + jobBonus + " bonus métier" : baseText;
    }

    public static Component footer(Contract contract) {
        return footer(contract.type() == ContractType.CRAFT ? "PNJ de la compétence" : "PNJ du métier");
    }

    public static Component footer(String npcName) {
        return Component.text("Apporte-le à " + npcName + ".", NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, true);
    }

    private static Component line(String label, String value, TextColor color) {
        return Component.text(label, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false).append(Component.text(value, color));
    }

    public static String capitalize(String raw) {
        StringBuilder result = new StringBuilder();
        for (String word : raw.toLowerCase(Locale.ROOT).split("_")) {
            if (word.isEmpty()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
}
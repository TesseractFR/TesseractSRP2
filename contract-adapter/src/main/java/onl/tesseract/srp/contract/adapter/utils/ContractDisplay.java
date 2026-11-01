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
import java.util.OptionalInt;

public final class ContractDisplay {

    private static final DateTimeFormatter EXPIRATION_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM HH:mm").withZone(ZoneId.systemDefault());

    private static final int UNKNOWN_BONUS = -1;
    private static final String BULLET = "• ";

    private ContractDisplay() {
    }

    public static List<Component> scrollDetails(Contract contract, boolean hasJobBonus) {
        return describe(contract, hasJobBonus ? UNKNOWN_BONUS : 0, OptionalInt.empty());
    }

    public static List<Component> deliveryDetails(Contract contract, int jobBonus, int ownedItemCount) {
        return describe(contract, jobBonus, OptionalInt.of(ownedItemCount));
    }

    private static List<Component> describe(Contract contract, int jobBonus, OptionalInt ownedItemCount) {
        boolean isCraft = contract.type() == ContractType.CRAFT;
        QualityAdapter minimalQuality = QualityAdapter.from(QualityConverter.toCommon(contract.minimalQuality()));
        List<Component> lines = new ArrayList<>();
        lines.add(text((isCraft ? "Artisanat" : "Récolte") + " · " + capitalize(contract.job()), NamedTextColor.DARK_GRAY));
        lines.add(Component.empty());
        lines.add(text("À livrer", NamedTextColor.GOLD));
        lines.add(line(BULLET + "Quantité : ", String.valueOf(contract.quantity()), NamedTextColor.WHITE));
        lines.add(line(BULLET + "Qualité minimale : ", minimalQuality.label(), minimalQuality.color()));
        ownedItemCount.ifPresent(count -> lines.add(line(BULLET + "Dans ton inventaire : ", count + " / " + contract.quantity(),
                count >= contract.quantity() ? NamedTextColor.GREEN : NamedTextColor.RED)));
        lines.add(Component.empty());
        lines.add(text("Récompense", NamedTextColor.GOLD));
        lines.add(line(BULLET + "Gain : ", contract.reward() + " lys", NamedTextColor.GREEN));
        if (!isCraft && jobBonus == UNKNOWN_BONUS) {
            lines.add(text(BULLET + "+ bonus métier", NamedTextColor.GREEN));
        } else if (!isCraft && jobBonus > 0) {
            lines.add(line(BULLET + "Bonus métier : ", "+" + jobBonus + " lys", NamedTextColor.GREEN));
        }
        lines.add(Component.empty());
        lines.add(line("Expire le : ", EXPIRATION_DATE_FORMAT.format(contract.expiresAt()), NamedTextColor.RED));
        return lines;
    }

    public static Component title(Contract contract) {
        return text("Contrat · " + capitalize(contract.material()), NamedTextColor.GOLD);
    }

    public static Component footer(Contract contract) {
        return footer(contract.type() == ContractType.CRAFT ? "PNJ de la compétence" : "PNJ du métier");
    }

    public static Component footer(String npcName) {
        return text("» Apporte-le à " + npcName, NamedTextColor.YELLOW);
    }

    public static Component text(String content, TextColor color) {
        return Component.text(content, color).decoration(TextDecoration.ITALIC, false);
    }

    public static Component line(String label, String value, TextColor color) {
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
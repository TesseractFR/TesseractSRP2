package onl.tesseract.srp.controller.menu.elytra;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.lib.profile.PlayerProfileService;
import onl.tesseract.srp.domain.equipment.elytra.ElytraUpgradeEntry;
import onl.tesseract.srp.domain.equipment.elytra.ElytraUpgradeResult;
import onl.tesseract.srp.domain.equipment.elytra.ElytraUpgradeStats;
import onl.tesseract.srp.service.equipment.elytra.ElytraService;
import onl.tesseract.srp.service.player.SrpPlayerService;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

import static onl.tesseract.lib.chat.ChatFormats.ELYTRA;
import static onl.tesseract.lib.chat.ChatFormats.ELYTRA_ERROR;


public class ElytraUpgradeMenu extends ElytraBaseMenu {
    private static final int SLOT_UPGRADE_SPEED = 19;
    private static final int SLOT_UPGRADE_PROTECTION = 21;
    private static final int SLOT_UPGRADE_BOOST_NUMBER = 23;
    private static final int SLOT_UPGRADE_RECOVERY = 25;
    private static final int PLAYER_INFO_SLOT = 40;

    private final UUID playerID;
    private final PlayerProfileService playerProfileService;
    private final ElytraService elytraService;
    private final SrpPlayerService srpPlayerService;

    public ElytraUpgradeMenu(UUID playerID,
                             PlayerProfileService playerProfileService,
                             ElytraService elytraService,
                             SrpPlayerService srpPlayerService,
                             Menu previous) {
        super(MenuSize.Five, "Améliorations Ailes Célestes", previous, null);
        this.playerID = playerID;
        this.playerProfileService = playerProfileService;
        this.elytraService = elytraService;
        this.srpPlayerService = srpPlayerService;
    }

    @Override
    public void placeButtons(Player viewer) {
        List<ElytraUpgradeEntry> entries = elytraService.getUpgradeEntries(playerID);
        var playerData = srpPlayerService.getPlayer(playerID);

        placeUpgradeButtons(viewer, entries);
        placePlayerInfo(
                playerData.getMoney(),
                playerData.getIlluminationPoints(),
                playerData.getRank().toString()
        );
        addBackButton();
        addCloseButton();
    }

    private void placeUpgradeButtons(Player viewer, List<ElytraUpgradeEntry> entries) {
        int[] slots = {
                SLOT_UPGRADE_SPEED,
                SLOT_UPGRADE_PROTECTION,
                SLOT_UPGRADE_BOOST_NUMBER,
                SLOT_UPGRADE_RECOVERY
        };

        for (int index = 0; index < entries.size() && index < slots.length; index++) {
            ElytraUpgradeEntry entry = entries.get(index);
            addButton(slots[index], buildUpgradeItem(entry), event -> {
                ElytraUpgradeResult result = elytraService.tryBuyNextUpgrade(playerID, entry.upgrade());
                switch (result) {
                    case SUCCESS:
                        viewer.playSound(viewer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        open(viewer);
                        break;
                    case NOT_ENOUGH_POINTS:
                        viewer.sendMessage(ELYTRA_ERROR.append(Component.text(
                                "Tu n'as pas assez de points d'illumination."
                        )));
                        break;
                    case MAX_LEVEL_REACHED:
                        viewer.sendMessage(ELYTRA.append(Component.text(
                                "Cette amélioration est déjà au niveau maximal."
                        )));
                        break;
                    case NO_ELYTRA:
                        viewer.sendMessage(ELYTRA_ERROR.append(Component.text(
                                "Tu ne possèdes pas d'ailes célestes."
                        )));
                        break;
                    default:
                        throw new IllegalStateException("Unexpected upgrade result: " + result);
                }
            });
        }
    }

    private ItemStack buildUpgradeItem(ElytraUpgradeEntry entry) {
        ElytraUpgradeStats stats = elytraService.getUpgradeStats(entry.upgrade(), entry.currentLevel());
        var builder = new ItemBuilder(entry.upgrade().getMaterial())
                .name(entry.upgrade().getDisplayName(), NamedTextColor.AQUA)
                .lore()
                .append(entry.upgrade().getDescription(), NamedTextColor.GRAY, TextDecoration.ITALIC)
                .newline()
                .newline()
                .append("Niveau actuel : ", NamedTextColor.GREEN)
                .append(String.valueOf(entry.currentLevel() + 1), NamedTextColor.YELLOW)
                .newline()
                .append(formatStat(stats), NamedTextColor.WHITE)
                .newline()
                .newline();

        if (entry.nextLevel() != null
                && entry.price() != null
                && entry.currentLevel() < entry.maxLevel()
                && stats.nextValue() != null) {
            ElytraUpgradeStats nextStats = new ElytraUpgradeStats(
                    stats.nextValue(),
                    stats.nextValue(),
                    stats.type()
            );
            builder.append("Prochaine amélioration :", NamedTextColor.BLUE, TextDecoration.BOLD)
                    .newline()
                    .append(formatStat(nextStats), NamedTextColor.YELLOW)
                    .newline()
                    .newline()
                    .append("Coût : ", NamedTextColor.GOLD)
                    .append(entry.price() + " points d'illumination",
                            entry.canAfford() ? NamedTextColor.GREEN : NamedTextColor.RED)
                    .append(" (Cliquez pour acheter)", NamedTextColor.GRAY, TextDecoration.ITALIC);
        } else {
            builder.append("Amélioration maximale atteinte", NamedTextColor.DARK_GREEN);
        }

        return builder.buildLore().build();
    }

    private void placePlayerInfo(int money, int illuminationPoints, String rankLabel) {
        addButton(PLAYER_INFO_SLOT, new ItemBuilder(playerProfileService.getPlayerHead(playerID))
                .name("Mes informations", NamedTextColor.GREEN)
                .lore()
                .newline()
                .addField("Argent", money + " Lys", NamedTextColor.GOLD)
                .addField("Points d'illumination", String.valueOf(illuminationPoints), NamedTextColor.GOLD)
                .addField("Grade", rankLabel, NamedTextColor.GOLD)
                .buildLore()
                .build());
    }

    private String formatStat(ElytraUpgradeStats stats) {
        return switch (stats.type()) {
            case SPEED -> "→ Vitesse : +" + (int) stats.currentValue() + "%";
            case PROTECTION -> "→ Armure : " + stats.currentValue() + " points";
            case BOOST_NUMBER -> "→ Boosts max : " + (int) stats.currentValue();
            case RECOVERY -> "→ Recharge : 1 boost / " + (int) stats.currentValue() + "s";
        };
    }
}

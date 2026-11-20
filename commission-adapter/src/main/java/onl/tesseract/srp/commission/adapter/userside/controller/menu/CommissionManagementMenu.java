package onl.tesseract.srp.commission.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionMessageFormatter;
import onl.tesseract.srp.commission.domain.model.board.BoardLevels;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.model.board.UpgradeStep;
import onl.tesseract.srp.commission.domain.model.board.UpgradeType;
import onl.tesseract.srp.commission.domain.model.exception.CommissionException;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardManagementService;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardService;
import onl.tesseract.srp.common.adapter.userside.menu.CustomMenuButton;
import onl.tesseract.srp.common.adapter.userside.menu.ItemAdderMenu;
import onl.tesseract.srp.common.domain.model.PlayerId;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Menu de gestion d'un tableau de guilde : améliorations payées par la banque et taxe des externes. */
public class CommissionManagementMenu extends ItemAdderMenu {
    private static final String BACKGROUND_ID = "tesseract:commission_board_upgrade_menu";
    private static final int TEXTURE_OFFSET = -8;

    private static final int MAX_CRAFT_TIER_SLOT = 9;
    private static final int MAX_HARVEST_TIER_SLOT = 11;
    private static final int SLOTS_SLOT = 13;
    private static final int MAX_REQUIRED_QUALITY_SLOT = 15;
    private static final int MONEY_BONUS_SLOT = 17;
    private static final int USAGE_TAX_SLOT = 31;
    private static final int BACK_SLOT = 36;
    private static final int INFORMATION_SLOT = 44;
    private static final double USAGE_TAX_STEP = 0.05;

    private final CommissionBoard commissionBoard;
    private final GuildId guildId;
    private final CommissionBoardMenu commissionBoardMenu;
    private final CommissionBoardService commissionBoardService;
    private final CommissionBoardManagementService commissionBoardManagementService;
    private final CommissionMessageFormatter commissionMessageFormatter;

    public CommissionManagementMenu(CommissionBoard commissionBoard, GuildId guildId, CommissionBoardMenu commissionBoardMenu,
                                    CommissionBoardService commissionBoardService,
                                    CommissionBoardManagementService commissionBoardManagementService,
                                    CommissionMessageFormatter commissionMessageFormatter) {
        super(MenuSize.Five, BACKGROUND_ID, "", null, TEXTURE_OFFSET);
        this.commissionBoard = commissionBoard;
        this.guildId = guildId;
        this.commissionBoardMenu = commissionBoardMenu;
        this.commissionBoardService = commissionBoardService;
        this.commissionBoardManagementService = commissionBoardManagementService;
        this.commissionMessageFormatter = commissionMessageFormatter;
    }

    @Override
    public void placeButtons(Player viewer) {
        addUpgradeButton(viewer, MAX_CRAFT_TIER_SLOT, UpgradeType.MAX_CRAFT_TIER, Material.ANVIL);
        addUpgradeButton(viewer, MAX_HARVEST_TIER_SLOT, UpgradeType.MAX_HARVEST_TIER, Material.IRON_PICKAXE);
        addUpgradeButton(viewer, SLOTS_SLOT, UpgradeType.SLOTS, Material.CHEST);
        addUpgradeButton(viewer, MAX_REQUIRED_QUALITY_SLOT, UpgradeType.MAX_REQUIRED_QUALITY, Material.AMETHYST_SHARD);
        addUpgradeButton(viewer, MONEY_BONUS_SLOT, UpgradeType.MONEY_BONUS, Material.GOLD_INGOT);
        addUsageTaxButton(viewer);
        addButton(BACK_SLOT, getCustomButtonItemStack(CustomMenuButton.MENU_BACK_ARROW_BUTTON),
                () -> commissionBoardMenu.reopenWithUpToDateBoard(viewer));
        addInformationButton();
    }

    private void addUpgradeButton(Player viewer, int buttonSlot, UpgradeType upgradeType, Material iconMaterial) {
        List<Component> loreLines = new ArrayList<>();
        CommissionDisplay.describeUpgrade(upgradeType).forEach(line -> loreLines.add(CommissionDisplay.coloredText(line, NamedTextColor.GRAY)));
        loreLines.add(Component.empty());
        loreLines.add(CommissionDisplay.labeledValue("Actuel : ", formatLevel(commissionBoard.getLevels(), upgradeType), NamedTextColor.WHITE));
        Optional<UpgradeStep> nextStep = commissionBoardManagementService.findNextUpgradeStep(commissionBoard, upgradeType);
        nextStep.ifPresentOrElse(step -> {
            loreLines.add(CommissionDisplay.labeledValue("Prochain : ", formatLevel(commissionBoard.getLevels().upgrade(upgradeType), upgradeType),
                    NamedTextColor.GREEN));
            loreLines.add(Component.empty());
            loreLines.addAll(describeConditions(step));
        }, () -> {
            loreLines.add(Component.empty());
            loreLines.add(CommissionDisplay.coloredText("Niveau maximal atteint", NamedTextColor.GREEN));
        });
        addButton(buttonSlot, new ItemBuilder(iconMaterial).name(CommissionDisplay.formatUpgradeName(upgradeType), NamedTextColor.GOLD)
                .lore(loreLines).build(), () -> {
            upgradeBoard(viewer, upgradeType);
            reopenWithUpToDateBoard(viewer);
        });
    }

    private void upgradeBoard(Player viewer, UpgradeType upgradeType) {
        try {
            commissionBoardManagementService.upgradeBoard(new PlayerId(viewer.getUniqueId()), commissionBoard.getId(), upgradeType);
            viewer.sendMessage(commissionMessageFormatter.formatUpgradeSucceeded(upgradeType));
        } catch (CommissionException refusal) {
            viewer.sendMessage(commissionMessageFormatter.formatRefusal(refusal));
        }
    }

    /** Le libellé se calcule sur les niveaux décrits, pour afficher aussi bien le niveau actuel que le suivant. */
    private String formatLevel(BoardLevels levels, UpgradeType upgradeType) {
        return CommissionDisplay.formatUpgradeLevel(levels, upgradeType,
                commissionBoardManagementService.getMoneyBonusRate(levels.moneyBonusLevel()));
    }

    private List<Component> describeConditions(UpgradeStep nextStep) {
        boolean hasEnoughMoney = commissionBoardManagementService.getBankBalance(guildId) >= nextStep.cost();
        boolean hasGuildLevel = commissionBoardManagementService.getGuildLevel(guildId) >= nextStep.guildLevel();
        List<Component> loreLines = new ArrayList<>();
        loreLines.add(CommissionDisplay.coloredText("Conditions", NamedTextColor.GOLD));
        loreLines.add(CommissionDisplay.labeledValue("• Coût : ", nextStep.cost() + " lys", toConditionColor(hasEnoughMoney)));
        loreLines.add(CommissionDisplay.labeledValue("• Niveau de guilde : ", String.valueOf(nextStep.guildLevel()), toConditionColor(hasGuildLevel)));
        loreLines.add(Component.empty());
        loreLines.add(hasEnoughMoney && hasGuildLevel
                ? CommissionDisplay.coloredText("» Clique pour améliorer", NamedTextColor.YELLOW)
                : CommissionDisplay.coloredText("Conditions non remplies", NamedTextColor.RED));
        return loreLines;
    }

    private static NamedTextColor toConditionColor(boolean isMet) {
        return isMet ? NamedTextColor.GREEN : NamedTextColor.RED;
    }

    private void addUsageTaxButton(Player viewer) {
        List<Component> loreLines = List.of(
                CommissionDisplay.coloredText("Payée par les joueurs hors de la guilde", NamedTextColor.GRAY),
                CommissionDisplay.coloredText("et versée à la banque", NamedTextColor.GRAY),
                Component.empty(),
                CommissionDisplay.labeledValue("Actuelle : ", CommissionDisplay.formatPercent(commissionBoard.getUsageTaxRate()), NamedTextColor.WHITE),
                CommissionDisplay.labeledValue("Maximum : ", CommissionDisplay.formatPercent(CommissionBoard.MAX_USAGE_TAX_RATE), NamedTextColor.WHITE),
                Component.empty(),
                CommissionDisplay.coloredText("» Clic gauche : +" + CommissionDisplay.formatPercent(USAGE_TAX_STEP), NamedTextColor.YELLOW),
                CommissionDisplay.coloredText("» Clic droit : -" + CommissionDisplay.formatPercent(USAGE_TAX_STEP), NamedTextColor.YELLOW));
        addButton(USAGE_TAX_SLOT, new ItemBuilder(Material.EMERALD).name("Taxe des externes", NamedTextColor.GOLD).lore(loreLines).build(), event -> {
            double taxChange = event.isLeftClick() ? USAGE_TAX_STEP : -USAGE_TAX_STEP;
            changeUsageTax(viewer, roundToPercent(commissionBoard.getUsageTaxRate() + taxChange));
            reopenWithUpToDateBoard(viewer);
        });
    }

    /** Les additions de {@code double} accumulent des erreurs (0,25 + 0,05 donne 0,30000000000000004) : on arrondit au pourcent. */
    private static double roundToPercent(double rate) {
        return Math.round(rate * 100) / 100.0;
    }

    private void changeUsageTax(Player viewer, double usageTaxRate) {
        try {
            commissionBoardManagementService.changeUsageTax(new PlayerId(viewer.getUniqueId()), commissionBoard.getId(), usageTaxRate);
            viewer.sendMessage(commissionMessageFormatter.formatUsageTaxChanged(usageTaxRate));
        } catch (CommissionException refusal) {
            viewer.sendMessage(commissionMessageFormatter.formatRefusal(refusal));
        }
    }

    private void addInformationButton() {
        ItemStack informationIcon = getCustomButtonItemStack(CustomMenuButton.MENU_INFORMATION_BUTTON);
        informationIcon.editMeta(meta -> {
            meta.displayName(CommissionDisplay.coloredText("Informations", NamedTextColor.GOLD));
            meta.lore(List.of(
                    CommissionDisplay.labeledValue("Banque de la guilde : ", commissionBoardManagementService.getBankBalance(guildId) + " lys", NamedTextColor.GREEN),
                    CommissionDisplay.labeledValue("Niveau de la guilde : ", String.valueOf(commissionBoardManagementService.getGuildLevel(guildId)), NamedTextColor.WHITE),
                    Component.empty(),
                    CommissionDisplay.coloredText("• Les améliorations sont payées par la banque", NamedTextColor.GRAY),
                    CommissionDisplay.coloredText("• Les commissions en cours ne changent pas", NamedTextColor.GRAY)));
        });
        addButton(INFORMATION_SLOT, informationIcon);
    }

    private void reopenWithUpToDateBoard(Player viewer) {
        new CommissionManagementMenu(commissionBoardService.getGuildBoard(guildId), guildId, commissionBoardMenu, commissionBoardService,
                commissionBoardManagementService, commissionMessageFormatter).open(viewer);
    }
}

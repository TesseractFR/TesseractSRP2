package onl.tesseract.srp.commission.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionMessageFormatter;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardDeliveryRequirement;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeStep;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeType;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.commission.domain.model.exception.CommissionException;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardManagementService;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardService;
import onl.tesseract.srp.common.adapter.userside.menu.CustomMenuButton;
import onl.tesseract.srp.common.adapter.userside.menu.ItemAdderMenu;
import onl.tesseract.srp.common.adapter.utils.QualityAdapter;
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

    private static final int BOARD_TIER_SLOT = 30;
    private static final int MAX_HARVEST_TIER_SLOT = 9;
    private static final int HARVEST_SLOTS_SLOT = 11;
    private static final int MONEY_BONUS_SLOT = 13;
    private static final int CRAFT_SLOTS_SLOT = 15;
    private static final int MAX_CRAFT_TIER_SLOT = 17;
    private static final int USAGE_TAX_SLOT = 32;
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
        addUpgradeButton(viewer, BOARD_TIER_SLOT, CommissionBoardUpgradeType.BOARD_TIER, Material.NETHER_STAR);
        addUpgradeButton(viewer, MAX_HARVEST_TIER_SLOT, CommissionBoardUpgradeType.MAX_HARVEST_TIER, Material.IRON_PICKAXE);
        addUpgradeButton(viewer, HARVEST_SLOTS_SLOT, CommissionBoardUpgradeType.HARVEST_SLOTS, Material.CHEST);
        addUpgradeButton(viewer, MONEY_BONUS_SLOT, CommissionBoardUpgradeType.MONEY_BONUS, Material.GOLD_INGOT);
        addUpgradeButton(viewer, CRAFT_SLOTS_SLOT, CommissionBoardUpgradeType.CRAFT_SLOTS, Material.BARREL);
        addUpgradeButton(viewer, MAX_CRAFT_TIER_SLOT, CommissionBoardUpgradeType.MAX_CRAFT_TIER, Material.ANVIL);
        addUsageTaxButton(viewer);
        addButton(BACK_SLOT, getCustomButtonItemStack(CustomMenuButton.MENU_BACK_ARROW_BUTTON),
                () -> commissionBoardMenu.reopenWithUpToDateBoard(viewer));
        addInformationButton();
    }

    private void addUpgradeButton(Player viewer, int buttonSlot, CommissionBoardUpgradeType upgradeType, Material iconMaterial) {
        int currentLevel = commissionBoard.getLevels().getLevel(upgradeType);
        List<Component> loreLines = new ArrayList<>();
        CommissionDisplay.describeUpgrade(upgradeType).forEach(line -> loreLines.add(CommissionDisplay.coloredText(line, NamedTextColor.GRAY)));
        loreLines.add(Component.empty());
        loreLines.add(CommissionDisplay.labeledValue("Actuel : ", formatLevel(upgradeType, currentLevel), NamedTextColor.WHITE));
        Optional<CommissionBoardUpgradeStep> nextStep = commissionBoardManagementService.findNextUpgradeStep(commissionBoard, upgradeType);
        nextStep.ifPresentOrElse(step -> {
            loreLines.add(CommissionDisplay.labeledValue("Prochain : ", formatLevel(upgradeType, currentLevel + 1), NamedTextColor.GREEN));
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

    private void upgradeBoard(Player viewer, CommissionBoardUpgradeType upgradeType) {
        try {
            commissionBoardManagementService.upgradeBoard(new PlayerId(viewer.getUniqueId()), commissionBoard.getId(), upgradeType);
            viewer.sendMessage(commissionMessageFormatter.formatUpgradeSucceeded(upgradeType));
        } catch (CommissionException refusal) {
            viewer.sendMessage(commissionMessageFormatter.formatRefusal(refusal));
        }
    }

    private String formatLevel(CommissionBoardUpgradeType upgradeType, int upgradeLevel) {
        return CommissionDisplay.formatUpgradeLevel(upgradeType, upgradeLevel, commissionBoardManagementService.getMoneyBonusRate(upgradeLevel));
    }

    private List<Component> describeConditions(CommissionBoardUpgradeStep nextStep) {
        boolean hasEnoughMoney = commissionBoardManagementService.getBankBalance(guildId) >= nextStep.cost();
        boolean hasBoardTier = commissionBoard.getLevels().boardTier() >= nextStep.requiredBoardTier();
        List<Component> loreLines = new ArrayList<>();
        loreLines.add(CommissionDisplay.coloredText("Conditions", NamedTextColor.GOLD));
        loreLines.add(CommissionDisplay.labeledValue("• Coût : ", nextStep.cost() + " lys", toConditionColor(hasEnoughMoney)));
        if (nextStep.requiredBoardTier() > 1) {
            loreLines.add(CommissionDisplay.labeledValue("• Tier du tableau : ", String.valueOf(nextStep.requiredBoardTier()),
                    toConditionColor(hasBoardTier)));
        }
        boolean hasDeliveries = true;
        for (CommissionBoardDeliveryRequirement requirement : nextStep.deliveryRequirements()) {
            int deliveredItemCount = commissionBoardManagementService.countDeliveredItems(commissionBoard, requirement);
            boolean isMet = deliveredItemCount >= requirement.itemCount();
            hasDeliveries &= isMet;
            String itemKind = requirement.type() == CommissionType.HARVEST ? "• Items de récolte livrés : " : "• Items d'artisanat livrés : ";
            loreLines.add(CommissionDisplay.labeledValue(itemKind,
                    Math.min(deliveredItemCount, requirement.itemCount()) + " / " + requirement.itemCount(), toConditionColor(isMet)));
            QualityAdapter minimalQuality = QualityAdapter.from(requirement.minimalQuality());
            loreLines.add(CommissionDisplay.coloredText("   sur des commissions de qualité ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(minimalQuality.label(), minimalQuality.color()))
                    .append(Component.text(" ou plus", NamedTextColor.DARK_GRAY)));
        }
        loreLines.add(Component.empty());
        loreLines.add(hasEnoughMoney && hasBoardTier && hasDeliveries
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
        int boardTier = commissionBoard.getLevels().boardTier();
        List<Component> loreLines = new ArrayList<>();
        loreLines.add(CommissionDisplay.labeledValue("Banque de la guilde : ", commissionBoardManagementService.getBankBalance(guildId) + " lys",
                NamedTextColor.GREEN));
        loreLines.add(CommissionDisplay.labeledValue("Tier du tableau : ", String.valueOf(boardTier), NamedTextColor.WHITE));
        loreLines.add(Component.empty());
        loreLines.add(CommissionDisplay.coloredText("Fonctionnement", NamedTextColor.GOLD));
        loreLines.add(CommissionDisplay.coloredText("• Le tier du tableau fixe jusqu'où", NamedTextColor.GRAY));
        loreLines.add(CommissionDisplay.coloredText("  vont les autres améliorations", NamedTextColor.GRAY));
        loreLines.add(CommissionDisplay.coloredText("• Il monte quand assez d'items ont été", NamedTextColor.GRAY));
        loreLines.add(CommissionDisplay.coloredText("  livrés sur ce tableau, de bonne qualité", NamedTextColor.GRAY));
        loreLines.add(CommissionDisplay.coloredText("• Tout est payé par la banque de la guilde", NamedTextColor.GRAY));
        loreLines.add(CommissionDisplay.coloredText("• Les commissions en cours ne changent pas", NamedTextColor.GRAY));
        loreLines.add(Component.empty());
        loreLines.add(CommissionDisplay.coloredText("Maximum au tier " + boardTier, NamedTextColor.GOLD));
        for (CommissionBoardUpgradeType upgradeType : CommissionBoardUpgradeType.values()) {
            if (upgradeType != CommissionBoardUpgradeType.BOARD_TIER) {
                loreLines.add(CommissionDisplay.labeledValue("• " + CommissionDisplay.formatUpgradeName(upgradeType) + " : ",
                        formatLevel(upgradeType, commissionBoardManagementService.getMaxLevelAtBoardTier(commissionBoard, upgradeType)),
                        NamedTextColor.WHITE));
            }
        }
        ItemStack informationIcon = getCustomButtonItemStack(CustomMenuButton.MENU_INFORMATION_BUTTON);
        informationIcon.editMeta(meta -> {
            meta.displayName(CommissionDisplay.coloredText("Informations", NamedTextColor.GOLD));
            meta.lore(loreLines);
        });
        addButton(INFORMATION_SLOT, informationIcon);
    }


    private void reopenWithUpToDateBoard(Player viewer) {
        new CommissionManagementMenu(commissionBoardService.getGuildBoard(guildId), guildId, commissionBoardMenu, commissionBoardService,
                commissionBoardManagementService, commissionMessageFormatter).open(viewer);
    }
}

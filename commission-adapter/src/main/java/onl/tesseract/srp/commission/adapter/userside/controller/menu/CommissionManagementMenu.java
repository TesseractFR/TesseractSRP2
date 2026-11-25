package onl.tesseract.srp.commission.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionLabels;
import onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionMessageFormatter;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardDeliveryProgress;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardDeliveryRequirement;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeConditions;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeType;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.model.board.UsageTaxRate;
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
    private static final int USAGE_TAX_STEP_PERCENT = 5;

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
        CommissionDisplay.describeUpgrade(upgradeType)
                .forEach(line -> loreLines.add(CommissionDisplay.coloredText(line, NamedTextColor.GRAY)));
        loreLines.add(Component.empty());
        loreLines.add(CommissionDisplay.labeledValue("Actuel : ", formatLevel(upgradeType, currentLevel), NamedTextColor.WHITE));
        commissionBoardManagementService.findNextUpgradeConditions(commissionBoard, upgradeType).ifPresentOrElse(conditions -> {
            loreLines.add(CommissionDisplay.labeledValue("Prochain : ", formatLevel(upgradeType, currentLevel + 1), NamedTextColor.GREEN));
            loreLines.add(Component.empty());
            loreLines.addAll(describeConditions(conditions));
        }, () -> {
            loreLines.add(Component.empty());
            loreLines.add(CommissionDisplay.coloredText("Niveau maximal atteint", NamedTextColor.GREEN));
        });
        ItemStack upgradeIcon = new ItemBuilder(iconMaterial).name(CommissionLabels.formatUpgradeName(upgradeType), NamedTextColor.GOLD)
                .lore(loreLines).build();
        addButton(buttonSlot, upgradeIcon, () -> {
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
        double moneyBonusRate = commissionBoardManagementService.getMoneyBonusRate(upgradeLevel);
        return CommissionDisplay.formatUpgradeLevel(upgradeType, upgradeLevel, moneyBonusRate);
    }

    /** Affiche chaque condition évaluée par le domaine, en vert si elle est remplie et en rouge sinon. */
    private static List<Component> describeConditions(CommissionBoardUpgradeConditions conditions) {
        List<Component> loreLines = new ArrayList<>();
        loreLines.add(CommissionDisplay.coloredText("Conditions", NamedTextColor.GOLD));
        loreLines.add(CommissionDisplay.labeledValue("• Coût : ", conditions.step().cost() + " lys",
                toConditionColor(conditions.hasEnoughMoney())));
        if (conditions.step().requiredBoardTier() > 1) {
            loreLines.add(CommissionDisplay.labeledValue("• Tier du tableau : ", String.valueOf(conditions.step().requiredBoardTier()),
                    toConditionColor(conditions.hasBoardTier())));
        }
        conditions.deliveryProgress().forEach(progress -> loreLines.addAll(describeDeliveryProgress(progress)));
        loreLines.add(Component.empty());
        loreLines.add(conditions.areAllMet()
                ? CommissionDisplay.coloredText("» Clique pour améliorer", NamedTextColor.YELLOW)
                : CommissionDisplay.coloredText("Conditions non remplies", NamedTextColor.RED));
        return loreLines;
    }

    private static List<Component> describeDeliveryProgress(CommissionBoardDeliveryProgress progress) {
        CommissionBoardDeliveryRequirement requirement = progress.requirement();
        String itemKind = requirement.type() == CommissionType.HARVEST
                ? "• Items de récolte livrés : "
                : "• Items d'artisanat livrés : ";
        String count = Math.min(progress.deliveredItemCount(), requirement.itemCount()) + " / " + requirement.itemCount();
        QualityAdapter minimalQuality = QualityAdapter.from(requirement.minimalQuality());
        return List.of(
                CommissionDisplay.labeledValue(itemKind, count, toConditionColor(progress.isMet())),
                CommissionDisplay.coloredText("   sur des commissions de qualité ", NamedTextColor.DARK_GRAY)
                        .append(Component.text(minimalQuality.label(), minimalQuality.color()))
                        .append(Component.text(" ou plus", NamedTextColor.DARK_GRAY)));
    }

    private static NamedTextColor toConditionColor(boolean isMet) {
        return isMet ? NamedTextColor.GREEN : NamedTextColor.RED;
    }

    private void addUsageTaxButton(Player viewer) {
        String stepLabel = USAGE_TAX_STEP_PERCENT + " %";
        List<Component> loreLines = List.of(
                CommissionDisplay.coloredText("Payée par les joueurs hors de la guilde", NamedTextColor.GRAY),
                CommissionDisplay.coloredText("et versée à la banque", NamedTextColor.GRAY),
                Component.empty(),
                CommissionDisplay.labeledValue("Actuelle : ", CommissionLabels.formatUsageTax(commissionBoard.getUsageTaxRate()),
                        NamedTextColor.WHITE),
                CommissionDisplay.labeledValue("Maximum : ", UsageTaxRate.MAX_PERCENT + " %", NamedTextColor.WHITE),
                Component.empty(),
                CommissionDisplay.coloredText("» Clic gauche : +" + stepLabel, NamedTextColor.YELLOW),
                CommissionDisplay.coloredText("» Clic droit : -" + stepLabel, NamedTextColor.YELLOW));
        ItemStack usageTaxIcon = new ItemBuilder(Material.EMERALD).name("Taxe des externes", NamedTextColor.GOLD).lore(loreLines).build();
        addButton(USAGE_TAX_SLOT, usageTaxIcon, event -> {
            int taxChange = event.isLeftClick() ? USAGE_TAX_STEP_PERCENT : -USAGE_TAX_STEP_PERCENT;
            changeUsageTax(viewer, commissionBoard.getUsageTaxRate().percent() + taxChange);
            reopenWithUpToDateBoard(viewer);
        });
    }

    private void changeUsageTax(Player viewer, int usageTaxPercent) {
        try {
            commissionBoardManagementService.changeUsageTax(new PlayerId(viewer.getUniqueId()), commissionBoard.getId(), usageTaxPercent);
            viewer.sendMessage(commissionMessageFormatter.formatUsageTaxChanged(new UsageTaxRate(usageTaxPercent)));
        } catch (CommissionException refusal) {
            viewer.sendMessage(commissionMessageFormatter.formatRefusal(refusal));
        }
    }

    private void addInformationButton() {
        int boardTier = commissionBoard.getLevels().boardTier();
        List<Component> loreLines = new ArrayList<>();
        int bankBalance = commissionBoardManagementService.getBankBalance(guildId);
        loreLines.add(CommissionDisplay.labeledValue("Banque de la guilde : ", bankBalance + " lys", NamedTextColor.GREEN));
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
                int maxLevel = commissionBoardManagementService.getMaxLevelAtBoardTier(commissionBoard, upgradeType);
                loreLines.add(CommissionDisplay.labeledValue("• " + CommissionLabels.formatUpgradeName(upgradeType) + " : ",
                        formatLevel(upgradeType, maxLevel), NamedTextColor.WHITE));
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
        new CommissionManagementMenu(commissionBoardService.openGuildBoard(guildId), guildId, commissionBoardMenu, commissionBoardService,
                commissionBoardManagementService, commissionMessageFormatter).open(viewer);
    }
}

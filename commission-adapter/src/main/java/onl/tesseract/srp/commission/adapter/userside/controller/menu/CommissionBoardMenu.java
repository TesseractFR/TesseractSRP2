package onl.tesseract.srp.commission.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionLabels;
import onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionMessageFormatter;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardPart;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.commission.Commission;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.commission.domain.model.commission.CommissionDelivery;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardAccessService;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardManagementService;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardService;
import onl.tesseract.srp.commission.domain.port.userside.CommissionDeliveryService;
import onl.tesseract.srp.common.adapter.userside.menu.CustomMenuButton;
import onl.tesseract.srp.common.adapter.userside.menu.ItemAdderMenu;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Menu d'un tableau : ses commissions par partie et par type, et l'accès à la gestion pour les adjoints et le chef. */
public class CommissionBoardMenu extends ItemAdderMenu {
    private static final String BACKGROUND_ID = "tesseract:commission_board_menu";
    private static final int TEXTURE_OFFSET = -8;

    private static final int PUBLIC_HARVEST_ICON_SLOT = 0;
    private static final int GUILD_HARVEST_ICON_SLOT = 8;
    private static final int PUBLIC_CRAFT_ICON_SLOT = 27;
    private static final int GUILD_CRAFT_ICON_SLOT = 35;
    private static final int[] PUBLIC_HARVEST_SLOTS = {1, 2, 3, 10, 11, 12};
    private static final int[] GUILD_HARVEST_SLOTS = {7, 6, 5, 16, 15, 14};
    private static final int[] PUBLIC_CRAFT_SLOTS = {28, 29, 30, 37, 38, 39};
    private static final int[] GUILD_CRAFT_SLOTS = {34, 33, 32, 43, 42, 41};
    private static final int INFORMATION_SLOT = 45;
    private static final int MANAGEMENT_SLOT = 53;

    private final CommissionBoard commissionBoard;
    private final CommissionBoardService commissionBoardService;
    private final CommissionBoardAccessService commissionBoardAccessService;
    private final CommissionBoardManagementService commissionBoardManagementService;
    private final CommissionDeliveryService commissionDeliveryService;
    private final CustomItemService customItemService;
    private final CommissionMessageFormatter commissionMessageFormatter;

    public CommissionBoardMenu(CommissionBoard commissionBoard, CommissionBoardService commissionBoardService,
                               CommissionBoardAccessService commissionBoardAccessService,
                               CommissionBoardManagementService commissionBoardManagementService,
                               CommissionDeliveryService commissionDeliveryService, CustomItemService customItemService,
                               CommissionMessageFormatter commissionMessageFormatter) {
        super(MenuSize.Six, BACKGROUND_ID, "", null, TEXTURE_OFFSET);
        this.commissionBoard = commissionBoard;
        this.commissionBoardService = commissionBoardService;
        this.commissionBoardAccessService = commissionBoardAccessService;
        this.commissionBoardManagementService = commissionBoardManagementService;
        this.commissionDeliveryService = commissionDeliveryService;
        this.customItemService = customItemService;
        this.commissionMessageFormatter = commissionMessageFormatter;
    }

    @Override
    public void placeButtons(Player viewer) {
        PlayerId player = new PlayerId(viewer.getUniqueId());
        List<Commission> visibleCommissions = commissionBoardService.findAllVisibleCommissions(player, commissionBoard);
        placeBoardPart(viewer, CommissionBoardPart.PUBLIC, visibleCommissions);
        placeGuildPart(viewer, visibleCommissions);
        addInformationButton();
        if (commissionBoardManagementService.canManageBoard(player, commissionBoard)) {
            addManagementButton(viewer);
        }
    }

    /** Rouvre le menu avec le tableau relu, pour afficher la progression et les commissions arrivées entre-temps. */
    public void reopenWithUpToDateBoard(Player viewer) {
        CommissionBoard upToDateBoard = commissionBoard.findGuildId()
                .map(commissionBoardService::openGuildBoard)
                .orElseGet(commissionBoardService::openSpawnBoard);
        new CommissionBoardMenu(upToDateBoard, commissionBoardService, commissionBoardAccessService, commissionBoardManagementService,
                commissionDeliveryService, customItemService, commissionMessageFormatter).open(viewer);
    }

    private void addManagementButton(Player viewer) {
        addButton(MANAGEMENT_SLOT, new ItemBuilder(Material.COMPARATOR).name("Gérer le tableau", NamedTextColor.GOLD)
                        .lore(List.of(CommissionDisplay.coloredText("Améliorations et taxe", NamedTextColor.GRAY),
                                Component.empty(),
                                CommissionDisplay.coloredText("» Clique pour ouvrir", NamedTextColor.YELLOW))).build(),
                () -> commissionBoard.findGuildId().ifPresent(guildId -> new CommissionManagementMenu(commissionBoard, guildId, this,
                        commissionBoardService, commissionBoardManagementService, commissionMessageFormatter).open(viewer)));
    }

    private void placeGuildPart(Player viewer, List<Commission> visibleCommissions) {
        if (commissionBoardAccessService.canAccessPart(new PlayerId(viewer.getUniqueId()), commissionBoard, CommissionBoardPart.GUILD)) {
            placeBoardPart(viewer, CommissionBoardPart.GUILD, visibleCommissions);
            return;
        }
        String unavailableReason = commissionBoard.isSpawnBoard()
                ? "Pas de commissions de guilde au spawn"
                : "Réservé aux membres de la guilde";
        addUnavailableGuildSlots(GUILD_HARVEST_SLOTS, unavailableReason);
        addUnavailableGuildSlots(GUILD_CRAFT_SLOTS, unavailableReason);
    }

    private void addUnavailableGuildSlots(int[] menuSlots, String unavailableReason) {
        for (int menuSlot : menuSlots) {
            addButton(menuSlot, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(unavailableReason, NamedTextColor.GRAY).build());
        }
    }

    private void placeBoardPart(Player viewer, CommissionBoardPart boardPart, List<Commission> visibleCommissions) {
        boolean isPublic = boardPart == CommissionBoardPart.PUBLIC;
        addTypeIcon(isPublic ? PUBLIC_HARVEST_ICON_SLOT : GUILD_HARVEST_ICON_SLOT, CommissionType.HARVEST, Material.IRON_PICKAXE);
        addTypeIcon(isPublic ? PUBLIC_CRAFT_ICON_SLOT : GUILD_CRAFT_ICON_SLOT, CommissionType.CRAFT, Material.ANVIL);
        int[] harvestSlots = isPublic ? PUBLIC_HARVEST_SLOTS : GUILD_HARVEST_SLOTS;
        int[] craftSlots = isPublic ? PUBLIC_CRAFT_SLOTS : GUILD_CRAFT_SLOTS;
        placeCommissionSlots(viewer, boardPart, CommissionType.HARVEST, harvestSlots, visibleCommissions);
        placeCommissionSlots(viewer, boardPart, CommissionType.CRAFT, craftSlots, visibleCommissions);
    }

    private void addTypeIcon(int iconSlot, CommissionType commissionType, Material iconMaterial) {
        String description = commissionType == CommissionType.HARVEST
                ? "Items récoltés avec les métiers"
                : "Items fabriqués en artisanat";
        addButton(iconSlot, new ItemBuilder(iconMaterial).name(CommissionLabels.formatCommissionType(commissionType), NamedTextColor.GOLD)
                .lore(List.of(CommissionDisplay.coloredText(description, NamedTextColor.GRAY))).build());
    }

    private void placeCommissionSlots(Player viewer, CommissionBoardPart boardPart, CommissionType commissionType, int[] menuSlots,
                                      List<Commission> visibleCommissions) {
        List<Commission> listCommissions = visibleCommissions.stream()
                .filter(commission -> commission.belongsTo(boardPart, commissionType))
                .toList();
        for (int listPosition = 0; listPosition < menuSlots.length; listPosition++) {
            boolean isUnlocked = listPosition < commissionBoard.getLevels().getSlotsFor(commissionType);
            if (isUnlocked && listPosition < listCommissions.size()) {
                addCommissionButton(viewer, menuSlots[listPosition], listCommissions.get(listPosition));
            } else if (!isUnlocked && !commissionBoard.isSpawnBoard()) {
                // Le tableau du spawn ne s'améliore pas : ses slots au-delà de la configuration restent vides.
                addLockedSlot(menuSlots[listPosition]);
            }
        }
    }

    private void addLockedSlot(int menuSlot) {
        addButton(menuSlot, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name("À débloquer", NamedTextColor.GRAY)
                .lore(List.of(CommissionDisplay.coloredText("S'obtient en améliorant le tableau", NamedTextColor.DARK_GRAY))).build());
    }

    private void addCommissionButton(Player viewer, int menuSlot, Commission commission) {
        addButton(menuSlot, buildCommissionIcon(viewer, commission, true), () -> new CommissionDeliveryMenu(commissionBoard, commission,
                this, commissionDeliveryService, commissionBoardAccessService, commissionMessageFormatter,
                buildCommissionIcon(viewer, commission, false)).open(viewer));
    }

    private ItemStack buildCommissionIcon(Player viewer, Commission commission, boolean showsDeliveryHint) {
        PlayerId player = new PlayerId(viewer.getUniqueId());
        CommissionDelivery delivery = commissionDeliveryService.computeDelivery(player, commissionBoard, commission);
        ItemStack commissionIcon = customItemService.getItemStack(commission.material());
        commissionIcon.editMeta(meta -> {
            meta.displayName(CommissionDisplay.coloredText(commissionMessageFormatter.formatMaterialName(commission.material()),
                    NamedTextColor.GOLD));
            List<Component> loreLines = new ArrayList<>();
            loreLines.add(commissionBoardService.findCommissionTier(commission)
                    .map(tier -> CommissionDisplay.describeCommissionHeader(commission.type(), tier))
                    .orElseGet(() -> CommissionDisplay.describeCommissionHeader(commission.type())));
            loreLines.addAll(CommissionDisplay.describeCommission(commission,
                    commissionDeliveryService.computeTotalGuildXp(commissionBoard, commission), delivery, showsDeliveryHint));
            meta.lore(loreLines);
        });
        return commissionIcon;
    }

    private void addInformationButton() {
        ItemStack informationIcon = getCustomButtonItemStack(CustomMenuButton.MENU_INFORMATION_BUTTON);
        informationIcon.editMeta(meta -> {
            meta.displayName(CommissionDisplay.coloredText("Informations", NamedTextColor.GOLD));
            meta.lore(CommissionDisplay.describeBoard(commissionBoard, commissionBoardService.getSpawnRewardRate()));
        });
        addButton(INFORMATION_SLOT, informationIcon);
    }
}

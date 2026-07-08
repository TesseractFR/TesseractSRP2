package onl.tesseract.srp.skill.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.controller.menu.ItemAdderMenu;
import onl.tesseract.srp.customitem.adapter.userside.ItemGateway;
import onl.tesseract.srp.customitem.domain.model.MaterialName;
import onl.tesseract.srp.domain.item.CustomItemIds;
import onl.tesseract.srp.domain.port.PlayerInventoryPort;
import onl.tesseract.srp.service.item.CustomItemService;
import onl.tesseract.srp.service.territory.guild.GuildService;
import onl.tesseract.srp.skill.domain.model.PlayerID;
import onl.tesseract.srp.skill.domain.model.recipe.IngredientSlot;
import onl.tesseract.srp.skill.domain.model.recipe.Material;
import onl.tesseract.srp.skill.domain.model.recipe.Recipe;
import onl.tesseract.srp.skill.domain.model.recipe.RecipeComponent;
import onl.tesseract.srp.skill.domain.model.skill.Skill;
import onl.tesseract.srp.skill.domain.model.station.Station;
import onl.tesseract.srp.skill.domain.port.userside.CraftingService;
import onl.tesseract.srp.skill.domain.port.userside.StationService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.event.inventory.InventoryClickEvent;


public class CraftingMenu extends ItemAdderMenu {

    private static final int COMPONENTS_OFFSET = -1;
    private static final int RESULT_INDEX = 8;

    private static final int INFO_BUTTON_INDEX = 18;

    private static final int QUANTITY_PLUS_FIRST_INDEX = 21;
    private static final int QUANTITY_PLUS_SECOND_INDEX = 22;
    private static final int QUANTITY_MAX_INDEX = 23;

    private static final int QUANTITY_DISPLAY_INDEX = 31;
    private static final int LAUNCH_BUTTON_INDEX = 34;

    private static final int RECIPE_BOOK_BUTTON_INDEX = 36;
    private static final int QUANTITY_MINUS_FIRST_INDEX = 39;
    private static final int QUANTITY_MINUS_SECOND_INDEX = 40;
    private static final int QUANTITY_MIN_INDEX = 41;

    private static final int FIRST_OFFSET = 1;
    private static final int SECOND_OFFSET = 5;

    private final Skill skill;
    private final CustomItemService customItemService;
    private final ItemGateway itemGateway;
    private final CraftingService craftingService;
    private final GuildService guildService;
    private final StationService stationService;
    private final PlayerInventoryPort playerInventoryPort;
    private final Recipe activeRecipe;
    private final Station station;
    private int quantityToCraft = 1;

    public CraftingMenu(Skill skill,
                        CustomItemService customItemService,
                        ItemGateway itemGateway,
                        CraftingService craftingService,
                        GuildService guildService,
                        StationService stationService,
                        PlayerInventoryPort playerInventoryPort,
                        Recipe activeRecipe,
                        Station station,
                        Menu previous) {
        super(MenuSize.Six, "tesseract:recipe_launch", "", previous,-8);
        this.skill = skill;
        this.customItemService = customItemService;
        this.itemGateway = itemGateway;
        this.craftingService = craftingService;
        this.guildService = guildService;
        this.stationService = stationService;
        this.playerInventoryPort = playerInventoryPort;
        this.activeRecipe = activeRecipe;
        this.station = station;
    }

    @Override
    public void placeButtons(Player viewer) {
        addRecipeButton();
        addActiveRecipe();
        addInfoButton();
        addLaunchButton(viewer);
        addQuantityButtons(viewer);
    }

    private void addQuantityButtons(Player viewer) {
        int maxCraft = getMaxCraft(viewer);
        addMinButton(viewer);
        addMinusButton(viewer, SECOND_OFFSET, QUANTITY_MINUS_SECOND_INDEX, CustomItemIds.MENU_MINUS_5_BUTTON);
        addMinusButton(viewer, FIRST_OFFSET, QUANTITY_MINUS_FIRST_INDEX, CustomItemIds.MENU_MINUS_1_BUTTON);
        addCurrentQuantityButton();
        addPlusButton(viewer, FIRST_OFFSET, QUANTITY_PLUS_FIRST_INDEX, CustomItemIds.MENU_PLUS_1_BUTTON, maxCraft);
        addPlusButton(viewer, SECOND_OFFSET, QUANTITY_PLUS_SECOND_INDEX, CustomItemIds.MENU_PLUS_5_BUTTON, maxCraft);
        addMaxButton(viewer, maxCraft);
    }

    private void addCurrentQuantityButton() {
        ItemStack item = customItemService.getCustomItem(CustomItemIds.MENU_QUANTITY_BUTTON);
        item.editMeta(m -> {
            m.displayName(Component.text("Nombre actuel d'exécutions : x" + quantityToCraft, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            m.lore(Arrays.asList(
                    Component.text("La recette sera lancée ", NamedTextColor.GRAY)
                            .append(Component.text(quantityToCraft + " fois", NamedTextColor.WHITE))
                            .append(Component.text(".", NamedTextColor.GRAY))
            ));
        });
        addButton(QUANTITY_DISPLAY_INDEX, item.asQuantity(quantityToCraft));
    }

    private void addMinButton(Player viewer) {
        boolean canMin = quantityToCraft > 1;
        ItemStack item = customItemService.getCustomItem(CustomItemIds.MENU_MIN_BUTTON);
        item.editMeta(m -> {
            m.displayName(Component.text("MIN", canMin ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            m.lore(Arrays.asList(
                    canMin ?
                            Component.text("Retire ", NamedTextColor.GRAY)
                                    .append(Component.text((quantityToCraft - 1) + " fois", NamedTextColor.GREEN))
                                    .append(Component.text(" la recette.", NamedTextColor.GRAY))
                            : Component.text("Nombre d'exécutions déjà au minimum.", NamedTextColor.GRAY),
                    canMin ?
                            Component.text("Exécution(s) après retrait : ", NamedTextColor.GRAY).append(Component.text("x1", NamedTextColor.GREEN))
                            : Component.text("Exécution(s) minimum : ", NamedTextColor.GRAY).append(Component.text("x1", NamedTextColor.RED))
            ));
        });
        addButton(QUANTITY_MIN_INDEX, item, (InventoryClickEvent event) -> {
            quantityToCraft = 1;
            this.placeButtons(viewer);
        });
    }

    private void addMinusButton(Player viewer, int offset, int index, String itemId) {
        int newQuantity = Math.max(1, quantityToCraft - offset);
        boolean canMinus = quantityToCraft - offset >= 1;
        ItemStack item = customItemService.getCustomItem(itemId);
        item.editMeta(m -> {
            m.displayName(Component.text("-" + offset, canMinus ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            m.lore(Arrays.asList(
                    canMinus ?
                            Component.text("Retire ", NamedTextColor.GRAY)
                                    .append(Component.text(offset + " fois", NamedTextColor.GREEN))
                                    .append(Component.text(" la recette.", NamedTextColor.GRAY))
                            : Component.text("Impossible de retirer " + offset + " fois.", NamedTextColor.RED),
                    canMinus ?
                            Component.text("Exécution(s) après retrait : ", NamedTextColor.GRAY).append(Component.text("x" + newQuantity, NamedTextColor.GREEN))
                            : Component.text("Exécution(s) minimum : ", NamedTextColor.GRAY).append(Component.text("x1", NamedTextColor.RED))
            ));
        });
        addButton(index, item, (InventoryClickEvent event) -> {
            if (canMinus) {
                quantityToCraft = newQuantity;
                this.placeButtons(viewer);
            }
        });
    }

    private List<Component> buildPlusButtonLore(int offset, int maxCraft, boolean canPlus, boolean alreadyMax, boolean noMaterials, int newQuantity) {
        Component first;
        if (noMaterials) first = Component.text("Pas assez de matériaux.", NamedTextColor.RED);
        else if (alreadyMax) first = Component.text("Nombre d'exécutions déjà au maximum.", NamedTextColor.GRAY);
        else if (!canPlus) first = Component.text("Impossible d'ajouter " + offset + " fois, maximum dépassé.", NamedTextColor.RED);
        else first = Component.text("Ajoute ", NamedTextColor.GRAY).append(Component.text(offset + " fois", NamedTextColor.GREEN)).append(Component.text(" la recette.", NamedTextColor.GRAY));

        Component second = canPlus ?
                Component.text("Exécution(s) après ajout : ", NamedTextColor.GRAY).append(Component.text("x" + newQuantity, NamedTextColor.GREEN))
                : Component.text("Exécution(s) maximum : ", NamedTextColor.GRAY).append(Component.text("x" + maxCraft, NamedTextColor.RED));

        return Arrays.asList(first, second);
    }

    private void addPlusButton(Player viewer, int offset, int index, String itemId, int maxCraft) {
        boolean noMaterials = maxCraft == 0;
        boolean canPlus = !noMaterials && quantityToCraft + offset <= maxCraft;
        boolean alreadyMax = !noMaterials && quantityToCraft >= maxCraft;
        int newQuantity = noMaterials ? 1 : Math.min(maxCraft, quantityToCraft + offset);
        ItemStack item = customItemService.getCustomItem(itemId);
        item.editMeta(m -> {
            m.displayName(Component.text("+" + offset, canPlus ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            m.lore(buildPlusButtonLore(offset, maxCraft, canPlus, alreadyMax, noMaterials, newQuantity));
        });
        addButton(index, item, (InventoryClickEvent event) -> {
            if (canPlus) {
                quantityToCraft = newQuantity;
                this.placeButtons(viewer);
            }
        });
    }

    private void addMaxButton(Player viewer, int maxCraft) {
        boolean noMaterials = maxCraft == 0;
        boolean canMax = !noMaterials && maxCraft > quantityToCraft;
        boolean alreadyMax = !noMaterials && quantityToCraft >= maxCraft;
        ItemStack item = customItemService.getCustomItem(CustomItemIds.MENU_MAX_BUTTON);
        item.editMeta(m -> {
            m.displayName(Component.text("MAX", canMax ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            m.lore(Arrays.asList(
                    noMaterials ? Component.text("Pas assez de matériaux.", NamedTextColor.RED)
                            : alreadyMax ? Component.text("Nombre d'exécutions déjà au maximum.", NamedTextColor.GRAY)
                            : Component.text("Ajoute ", NamedTextColor.GRAY).append(Component.text((maxCraft - quantityToCraft) + " fois", NamedTextColor.GREEN)).append(Component.text(" la recette.", NamedTextColor.GRAY)),
                    canMax ? Component.text("Exécution(s) après ajout : ", NamedTextColor.GRAY).append(Component.text("x" + maxCraft, NamedTextColor.GREEN))
                            : Component.text("Exécution(s) maximum : ", NamedTextColor.GRAY).append(Component.text("x" + maxCraft, NamedTextColor.RED))
            ));
        });
        addButton(QUANTITY_MAX_INDEX, item, (InventoryClickEvent event) -> {
            if (canMax) {
                quantityToCraft = maxCraft;
                this.placeButtons(viewer);
            }
        });
    }

    private void addActiveRecipe() {
        // Assuming activeRecipe.getComponents() returns a Map-like structure
        for (Map.Entry<IngredientSlot, RecipeComponent> entry : activeRecipe.components().entrySet()) {
            IngredientSlot key = entry.getKey();
            RecipeComponent component = entry.getValue();
            // The original Kotlin used component.material and component.quantity — adapt as needed
            addButton(COMPONENTS_OFFSET + key.value(), customItemService.toItemstack(component.material()).asQuantity(component.quantity() * quantityToCraft));
        }
        addButton(RESULT_INDEX, customItemService.toItemstack(activeRecipe.result().material()).asQuantity(activeRecipe.result().quantity() * quantityToCraft));
    }

    private void addInfoButton() {
        ItemStack item = customItemService.getCustomItem(CustomItemIds.MENU_INFORMATION_BUTTON);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Informations", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            meta.lore(Arrays.asList(
                    Component.empty(),
                    Component.text("Recette sélectionnée", NamedTextColor.YELLOW),
                    Component.text("La ligne du haut affiche les ", NamedTextColor.GRAY).append(Component.text("matériaux ", NamedTextColor.WHITE)),
                    Component.text("nécessaires ", NamedTextColor.WHITE).append(Component.text("et le ", NamedTextColor.GRAY)).append(Component.text("résultat", NamedTextColor.WHITE)).append(Component.text(" de la recette.", NamedTextColor.GRAY)),
                    Component.empty(),
                    Component.text("Quantité", NamedTextColor.YELLOW),
                    Component.text("Utilisez ", NamedTextColor.GRAY).append(Component.text("+1, +5, MAX", NamedTextColor.GREEN)).append(Component.text(" pour augmenter,", NamedTextColor.GRAY)),
                    Component.text("et ", NamedTextColor.GRAY).append(Component.text("-1, -5, MIN", NamedTextColor.RED)).append(Component.text(" pour diminuer le nombre de fois", NamedTextColor.GRAY)),
                    Component.text("que la recette sera effectuée.", NamedTextColor.GRAY),
                    Component.text("La ", NamedTextColor.GRAY).append(Component.text("balance", NamedTextColor.WHITE)).append(Component.text(" affiche le nombre actuel d'exécutions.", NamedTextColor.GRAY)),
                    Component.empty(),
                    Component.text("Le maximum est atteint quand un matériau", NamedTextColor.GRAY),
                    Component.text("dépasse ", NamedTextColor.GRAY).append(Component.text("1 stack (64 unités)", NamedTextColor.WHITE)).append(Component.text(".", NamedTextColor.GRAY)),
                    Component.empty(),
                    Component.text("Lancement", NamedTextColor.YELLOW),
                    Component.text("Cliquez sur ", NamedTextColor.GRAY).append(Component.text("l'enclume", NamedTextColor.WHITE).decorate(TextDecoration.BOLD)).append(Component.text(" pour ajouter la recette ", NamedTextColor.GRAY)),
                    Component.text("dans la file d'attente de la ", NamedTextColor.GRAY).append(Component.text("table d'artisanat", NamedTextColor.WHITE)).append(Component.text(".", NamedTextColor.GRAY))
            ));
        });
        addButton(INFO_BUTTON_INDEX, item, (InventoryClickEvent ev) -> {});
    }

    private void addRecipeButton() {
        ItemStack item = customItemService.getCustomItem(CustomItemIds.MENU_RECIPE_BOOK_BUTTON);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Livre de recettes").decoration(TextDecoration.ITALIC, false));
            meta.lore(Arrays.asList(Component.text("Retour vers la sélection de recettes.", NamedTextColor.GRAY)));
        });
        addButton(RECIPE_BOOK_BUTTON_INDEX, item, (InventoryClickEvent event) -> {
            RecipeMenu menu = new RecipeMenu(skill, customItemService, itemGateway, playerInventoryPort, craftingService, guildService, stationService, station, this);
            menu.open((Player) event.getWhoClicked());
        });
    }

    private void addLaunchButton(Player viewer) {
        int maxCraft = getMaxCraft(viewer);
        boolean canLaunch = quantityToCraft <= maxCraft && maxCraft > 0;
        NamedTextColor resultColor = canLaunch ? NamedTextColor.GREEN : NamedTextColor.RED;
        ItemStack item = new ItemStack(org.bukkit.Material.ANVIL);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Lancer la fabrication", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            meta.lore(Arrays.asList(
                    Component.text("x" + quantityToCraft + " ", NamedTextColor.WHITE).append(Component.text("la recette → ", NamedTextColor.GRAY)).append(Component.text((activeRecipe.result().quantity() * quantityToCraft) + "x " + getComponentName(activeRecipe.result().material()), resultColor)),
                    Component.empty(),
                    canLaunch ? Component.text("Ajoute à la file d'attente.", NamedTextColor.GRAY) : Component.text("Pas assez de matériaux.", NamedTextColor.RED)
            ));
        });
        addButton(LAUNCH_BUTTON_INDEX, item, (InventoryClickEvent event) -> {
            if (!canLaunch) return;
            Player player = (Player) event.getWhoClicked();
            craftingService.startCraft(new PlayerID(player.getUniqueId()), skill.name(), activeRecipe, quantityToCraft, station);
            new SkillMainMenu(skill, customItemService,itemGateway, craftingService, guildService, stationService, playerInventoryPort, station ,this).open(viewer);
        });
    }

    private String getComponentName(Material material) {
        return itemGateway.getItemStack(new MaterialName(material.value())).getI18NDisplayName();
    }

    private int getMaxCraft(Player viewer) {
        int max = Integer.MAX_VALUE;
        for (Map.Entry<IngredientSlot, RecipeComponent> e : activeRecipe.components().entrySet()) {
            RecipeComponent component = e.getValue();
            int owned = playerInventoryPort.getItemNumber(viewer.getUniqueId(), itemGateway.getItemStack(new MaterialName(component.material().value())));
            int craftable = owned / component.quantity();
            max = Math.min(max, craftable);
        }
        if (max == Integer.MAX_VALUE) return 0;
        return Math.min(max, craftingService.getMaxStackSize(activeRecipe));
    }
}

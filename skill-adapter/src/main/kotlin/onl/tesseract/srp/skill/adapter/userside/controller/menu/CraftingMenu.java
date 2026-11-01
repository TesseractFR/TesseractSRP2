package onl.tesseract.srp.skill.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.common.adapter.userside.menu.CustomMenuButton;
import onl.tesseract.srp.common.adapter.userside.menu.ItemAdderMenu;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.customitem.domain.model.MaterialName;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.skill.domain.model.recipe.IngredientSlot;
import onl.tesseract.srp.skill.domain.model.recipe.Material;
import onl.tesseract.srp.skill.domain.model.recipe.Recipe;
import onl.tesseract.srp.skill.domain.model.recipe.RecipeComponent;
import onl.tesseract.srp.skill.domain.model.skill.Skill;
import onl.tesseract.srp.skill.domain.model.station.Station;
import onl.tesseract.srp.skill.domain.port.userside.CraftingService;
import onl.tesseract.srp.skill.domain.port.userside.StationService;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
    private static final int QUALITY_SELECT_INDEX = 33;
    private static final int LAUNCH_BUTTON_INDEX = 34;

    private static final int RECIPE_BOOK_BUTTON_INDEX = 36;
    private static final int QUANTITY_MINUS_FIRST_INDEX = 39;
    private static final int QUANTITY_MINUS_SECOND_INDEX = 40;
    private static final int QUANTITY_MIN_INDEX = 41;

    private static final int FIRST_OFFSET = 1;
    private static final int SECOND_OFFSET = 5;

    private final Skill skill;
    private final CraftingService craftingService;
    private final GuildService guildService;
    private final StationService stationService;
    private final CustomItemService customItemService;
    private final Recipe activeRecipe;
    private final Station station;
    private int quantityToCraft = 1;
    private Quality selectedQuality = Quality.POOR;

    public CraftingMenu(Skill skill,
                        Recipe activeRecipe,
                        Station station,
                        CraftingService craftingService,
                        GuildService guildService,
                        StationService stationService,
                        CustomItemService customItemService,
                        Menu previous) {
        super(MenuSize.Six, "tesseract:recipe_launch", "", previous,-8);
        this.skill = skill;
        this.craftingService = craftingService;
        this.guildService = guildService;
        this.stationService = stationService;
        this.customItemService = customItemService;
        this.activeRecipe = activeRecipe;
        this.station = station;
    }

    @Override
    public void placeButtons(Player viewer) {
        addRecipeButton();
        addActiveRecipe();
        addInfoButton();
        if (hasCustomIngredients()) {
            addQualitySelector(viewer);
        }
        addLaunchButton(viewer);
        addQuantityButtons(viewer);
    }

    private void addQualitySelector(Player viewer) {
        Quality maximum = Quality.valueOf(craftingService.getMaximumIngredientQuality(new PlayerId(viewer.getUniqueId()), skill.name()).name());
        Quality[] qualities = Arrays.stream(Quality.values()).filter(q -> q.ordinal() <= maximum.ordinal()).toArray(Quality[]::new);
        ItemStack item = new ItemStack(org.bukkit.Material.AMETHYST_SHARD);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Qualité des ingrédients : " + selectedQuality.name(), NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
            meta.lore(Arrays.asList(Component.text("Cliquez pour choisir une qualité débloquée.", NamedTextColor.GRAY)));
        });
        addButton(QUALITY_SELECT_INDEX, item, (InventoryClickEvent event) -> {
            for (int i = 0; i < qualities.length; i++) {
                if (qualities[i] == selectedQuality) {
                    selectedQuality = qualities[(i + 1) % qualities.length];
                    break;
                }
            }
            quantityToCraft = Math.min(quantityToCraft, Math.max(1, getMaxCraft(viewer)));
            placeButtons(viewer);
        });
    }

    private boolean hasCustomIngredients() {
        return activeRecipe.components().values().stream()
                .anyMatch(component -> customItemService.isCustomMaterial(
                        new MaterialName(component.material().value())));
    }

    private void addQuantityButtons(Player viewer) {
        int maxCraft = getMaxCraft(viewer);
        addMinButton(viewer);
        addMinusButton(viewer, SECOND_OFFSET, QUANTITY_MINUS_SECOND_INDEX, CustomMenuButton.MENU_MINUS_5_BUTTON);
        addMinusButton(viewer, FIRST_OFFSET, QUANTITY_MINUS_FIRST_INDEX, CustomMenuButton.MENU_MINUS_1_BUTTON);
        addCurrentQuantityButton();
        addPlusButton(viewer, FIRST_OFFSET, QUANTITY_PLUS_FIRST_INDEX, CustomMenuButton.MENU_PLUS_1_BUTTON, maxCraft);
        addPlusButton(viewer, SECOND_OFFSET, QUANTITY_PLUS_SECOND_INDEX, CustomMenuButton.MENU_PLUS_5_BUTTON, maxCraft);
        addMaxButton(viewer, maxCraft);
    }

    private void addCurrentQuantityButton() {
        int totalProduced = activeRecipe.result().quantity() * quantityToCraft;
        ItemStack item = customItemService.getItemStack(new MaterialName(activeRecipe.result().material().value()));
        item.editMeta(m -> {
            List<Component> lore = new ArrayList<>(Arrays.asList(
                    Component.text("Production totale : ", NamedTextColor.GRAY)
                            .append(Component.text(totalProduced + "x " + getComponentName(activeRecipe.result().material()), NamedTextColor.WHITE)),
                    Component.text("Ex\u00e9cutions : x" + quantityToCraft, NamedTextColor.GRAY)));
            if (hasCustomIngredients()) {
                lore.add(Component.text("Qualit\u00e9 s\u00e9lectionn\u00e9e : " + selectedQuality.name(), NamedTextColor.LIGHT_PURPLE));
            }
            m.lore(lore);
        });
        addButton(QUANTITY_DISPLAY_INDEX, item.asQuantity(totalProduced));
    }

    private void addMinButton(Player viewer) {
        boolean canMin = quantityToCraft > 1;
        ItemStack item = getCustomButtonItemStack(CustomMenuButton.MENU_MIN_BUTTON);
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

    private void addMinusButton(Player viewer, int offset, int index, CustomMenuButton customMenuButton) {
        int newQuantity = Math.max(1, quantityToCraft - offset);
        boolean canMinus = quantityToCraft - offset >= 1;
        ItemStack item = getCustomButtonItemStack(customMenuButton);
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

    private void addPlusButton(Player viewer, int offset, int index, CustomMenuButton customMenuButton, int maxCraft) {
        boolean noMaterials = maxCraft == 0;
        boolean canPlus = !noMaterials && quantityToCraft + offset <= maxCraft;
        boolean alreadyMax = !noMaterials && quantityToCraft >= maxCraft;
        int newQuantity = noMaterials ? 1 : Math.min(maxCraft, quantityToCraft + offset);
        ItemStack item = getCustomButtonItemStack(customMenuButton);
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
        ItemStack item = getCustomButtonItemStack(CustomMenuButton.MENU_MAX_BUTTON);
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
            addButton(COMPONENTS_OFFSET + key.value(), customItemService.getItemStack(new MaterialName(component.material().value())).asQuantity(component.quantity() * quantityToCraft));
        }
        ItemStack result = customItemService.getItemStack(new MaterialName(activeRecipe.result().material().value()));
        result.editMeta(meta -> {
            List<Component> lore = meta.lore() == null ? new ArrayList<>() : new ArrayList<>(meta.lore());
            lore.add(Component.empty());

            double successRate = craftingService.getSuccessRate(station.getBonus());
            double failureRate = Math.max(0, 1 - successRate);
            double successBonus = station.getBonus().successBonus().value();
            lore.add(Component.text("Taux de r\u00e9ussite : " + formatPercentage(successRate)
                    + " (+" + formatPercentage(successBonus) + ")", NamedTextColor.GREEN));
            lore.add(Component.text("Taux d'\u00e9chec : " + formatPercentage(failureRate), NamedTextColor.RED));
            lore.add(Component.text("Dur\u00e9e : " + formatDuration(craftingService.getAdjustedDuration(activeRecipe, station.getBonus())), NamedTextColor.GRAY));
            lore.add(Component.text("Chance d'am\u00e9lioration de qualit\u00e9 : "
                    + formatPercentage(station.getBonus().qualityBonus().value()), NamedTextColor.LIGHT_PURPLE));
            meta.lore(lore);
        });
        addButton(RESULT_INDEX, result.asQuantity(activeRecipe.result().quantity() * quantityToCraft));
    }

    private String formatPercentage(double value) {
        return String.format(Locale.FRANCE, "%.1f%%", value * 100);
    }

    private String formatDuration(Duration duration) {
        long totalSeconds = duration.toSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return minutes > 0 ? minutes + " min " + seconds + " s" : seconds + " s";
    }

    private void addInfoButton() {
        ItemStack item = getCustomButtonItemStack(CustomMenuButton.MENU_INFORMATION_BUTTON);
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
        ItemStack item = getCustomButtonItemStack(CustomMenuButton.MENU_RECIPE_BOOK_BUTTON);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Livre de recettes").decoration(TextDecoration.ITALIC, false));
            meta.lore(Arrays.asList(Component.text("Retour vers la sélection de recettes.", NamedTextColor.GRAY)));
        });
        addButton(RECIPE_BOOK_BUTTON_INDEX, item, (InventoryClickEvent event) -> {
            RecipeMenu menu = new RecipeMenu(skill, station, craftingService, customItemService, guildService, stationService,this);
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
            craftingService.startCraft(new PlayerId(player.getUniqueId()), skill.name(), activeRecipe, quantityToCraft,
                    onl.tesseract.srp.common.domain.model.enums.Quality.valueOf(selectedQuality.name()), station);
            new SkillMainMenu(skill, station , craftingService, customItemService, guildService, stationService, this).open(viewer);
        });
    }

    private String getComponentName(Material material) {
        return customItemService.getItemStack(new MaterialName(material.value())).getI18NDisplayName();
    }

    private int getMaxCraft(Player viewer) {
        return craftingService.getMaxCraft(activeRecipe, new PlayerId(viewer.getUniqueId()),
                onl.tesseract.srp.common.domain.model.enums.Quality.valueOf(selectedQuality.name()));
    }
}

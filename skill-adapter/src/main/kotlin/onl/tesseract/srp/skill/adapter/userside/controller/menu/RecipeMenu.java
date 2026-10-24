package onl.tesseract.srp.skill.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.common.adapter.userside.menu.CustomMenuButton;
import onl.tesseract.srp.common.adapter.userside.menu.ItemAdderBiMenu;
import onl.tesseract.srp.customitem.domain.model.MaterialName;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import onl.tesseract.srp.skill.domain.model.recipe.IngredientSlot;
import onl.tesseract.srp.skill.domain.model.recipe.Recipe;
import onl.tesseract.srp.skill.domain.model.recipe.RecipeComponent;
import onl.tesseract.srp.skill.domain.model.recipe.Tier;
import onl.tesseract.srp.skill.domain.model.skill.Skill;
import onl.tesseract.srp.skill.domain.model.skill.SkillTier;
import onl.tesseract.srp.skill.domain.model.station.Station;
import onl.tesseract.srp.skill.domain.port.userside.CraftingService;
import onl.tesseract.srp.skill.domain.port.userside.StationService;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecipeMenu extends ItemAdderBiMenu {

    private final Skill skill;
    private final CraftingService craftingService;
    private final Station station;
    private final Menu previous;
    private final GuildService guildService;
    private final StationService stationService;
    private final CustomItemService customItemService;

    private int tier = 1;
    private int startingRecipe = 0;

    public RecipeMenu(Skill skill,
                      Station station,
                      CraftingService craftingService,
                      CustomItemService customItemService,
                      GuildService guildService,
                      StationService stationService,
                      Menu previous
                      ) {
        super(MenuSize.Six, "tesseract:recipe_book", "Recettes " + skill.name(), previous, 100);
        this.skill = skill;
        this.craftingService = craftingService;
        this.guildService = guildService;
        this.stationService = stationService;
        this.station = station;
        this.previous = previous;
        this.customItemService = customItemService;
    }

    @Override
    public void placeButtons(Player viewer) {
        addButton(0, getCustomButtonItemStack(CustomMenuButton.MENU_BACK_ARROW_BUTTON), p -> {
            if (previous == null) {
                this.close();
                return;
            }
            previous.open(viewer);
        });
        placeRecipes(viewer);
        addBottomCloseButton(8);
    }

    private void placeRecipes(Player viewer) {
        Map<Tier, SkillTier> tiers = skill.tiers();
        Map<Integer, Recipe> recipes = tiers.get(new Tier(tier)).recipes();
        if (recipes == null) {
            tier = 1;
            placeRecipes(viewer);
            return;
        }
        for (int i = 1; i <= 7; i++) {
            Recipe recipe = (startingRecipe + i <= recipes.size()) ? recipes.get(i + startingRecipe) : null;
            if (recipe == null) {
                clearLigne(i);
                return;
            }
            placeRecipe(i, recipe, viewer);
        }

        addButton(8, getCustomButtonItemStack(CustomMenuButton.MENU_UP_ARROW_BUTTON).asQuantity(startingRecipe > 0 ? 1 : 0), p -> {
            startingRecipe--;
            placeRecipes(viewer);
        });

        addBottomButton(35, getCustomButtonItemStack(CustomMenuButton.MENU_DOWN_ARROW_BUTTON).asQuantity(recipes.size() > startingRecipe + 7 ? 1 : 0), p -> {
            startingRecipe++;
            placeRecipes(viewer);
        });
    }

    private void clearLigne(int lign) {
        for (int i = 0; i <= 8; i++) {
            addButton(lign * 9 + i, new ItemStack(Material.STONE).asQuantity(0), p -> {});
        }
    }

    private void placeRecipe(int ligne, Recipe recipe, Player viewer) {
        Map<IngredientSlot, RecipeComponent> comps = recipe.components();
        for (var e : comps.entrySet()) {
            RecipeComponent rc = e.getValue();
            IngredientSlot is = e.getKey();
            ItemStack item = customItemService.getItemStack(new MaterialName(rc.material().value()));
            item.setAmount(rc.quantity());
            addButton(9 * (ligne) + (is.value() - 1), item, p -> {});
        }
        ItemStack item = customItemService.getItemStack(new MaterialName(recipe.result().material().value()));
        item.setAmount(recipe.result().quantity());
        item.editMeta(meta -> {
            List<Component> lore = meta.lore() == null ? new ArrayList<>() : new ArrayList<>(meta.lore());
            lore.add(Component.empty());

            double successRate = craftingService.getSuccessRate(station.getBonus());
            double failureRate = Math.max(0, 1 - successRate);
            double successBonus = station.getBonus().successBonus().value();
            lore.add(Component.text("Taux de r\u00e9ussite : " + formatPercentage(successRate)
                    + " (+" + formatPercentage(successBonus) + ")", NamedTextColor.GREEN));
            lore.add(Component.text("Taux d'\u00e9chec : " + formatPercentage(failureRate), NamedTextColor.RED));
            lore.add(Component.text("Dur\u00e9e : " + formatDuration(craftingService.getAdjustedDuration(recipe, station.getBonus())), NamedTextColor.GRAY));
            lore.add(Component.text("Chance d'am\u00e9lioration de qualit\u00e9 : "
                    + formatPercentage(station.getBonus().qualityBonus().value()), NamedTextColor.LIGHT_PURPLE));
            meta.lore(lore);
        });
        addButton(9 * (ligne) + (8), item, p ->
                new CraftingMenu(skill,recipe,station, craftingService,guildService,stationService,customItemService,this).open(viewer));
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

    private Object getField(Object obj, String fieldName) {
        try {
            return obj.getClass().getField(fieldName).get(obj);
        } catch (Exception e) {
            return null;
        }
    }
}

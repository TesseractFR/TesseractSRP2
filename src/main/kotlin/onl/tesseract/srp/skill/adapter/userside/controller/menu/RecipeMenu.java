package onl.tesseract.srp.skill.adapter.userside.controller.menu;

import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.controller.menu.ItemAdderBiMenu;
import onl.tesseract.srp.customitem.adapter.userside.ItemGateway;
import onl.tesseract.srp.domain.item.CustomItemIds;
import onl.tesseract.srp.domain.port.PlayerInventoryPort;
import onl.tesseract.srp.service.item.CustomItemService;
import onl.tesseract.srp.service.territory.guild.GuildService;
import onl.tesseract.srp.skill.domain.model.recipe.IngredientSlot;
import onl.tesseract.srp.skill.domain.model.recipe.Recipe;
import onl.tesseract.srp.skill.domain.model.recipe.RecipeComponent;
import onl.tesseract.srp.skill.domain.model.recipe.Tier;
import onl.tesseract.srp.skill.domain.model.skill.Skill;
import onl.tesseract.srp.skill.domain.model.skill.SkillTier;
import onl.tesseract.srp.skill.domain.model.station.Station;
import onl.tesseract.srp.skill.domain.port.userside.CraftingService;
import onl.tesseract.srp.skill.domain.port.userside.StationService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class RecipeMenu extends ItemAdderBiMenu {

    private final Skill skill;
    private final CustomItemService customItemService;
    private final ItemGateway itemGateway;
    private final PlayerInventoryPort playerInventoryPort;
    private final CraftingService craftingService;
    private final Station station;
    private final Menu previous;
    private final GuildService guildService;
    private final StationService stationService;

    private int tier = 1;
    private int startingRecipe = 0;

    public RecipeMenu(Skill skill,
                      CustomItemService customItemService,
                      ItemGateway itemGateway,
                      PlayerInventoryPort playerInventoryPort,
                      CraftingService craftingService,
                      GuildService guildService,
                      StationService stationService,
                      Station station,
                      Menu previous) {
        super(MenuSize.Six, "tesseract:recipe_book", "Recettes " + skill.name(), previous, 100);
        this.skill = skill;
        this.customItemService = customItemService;
        this.itemGateway = itemGateway;
        this.playerInventoryPort = playerInventoryPort;
        this.craftingService = craftingService;
        this.guildService = guildService;
        this.stationService = stationService;
        this.station = station;
        this.previous = previous;
    }

    @Override
    public void placeButtons(Player viewer) {
        addButton(0, customItemService.getCustomItem(CustomItemIds.MENU_BACK_ARROW_BUTTON), p -> {
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
            Recipe recipe = (startingRecipe + i < recipes.size()) ? recipes.get(i + startingRecipe) : null;
            if (recipe == null) {
                clearLigne(i);
                return;
            }
            placeRecipe(i, recipe, viewer);
        }

        addButton(8, customItemService.getCustomItem(CustomItemIds.MENU_UP_ARROW_BUTTON).asQuantity(startingRecipe > 0 ? 1 : 0), p -> {
            startingRecipe--;
            placeRecipes(viewer);
        });

        addBottomButton(35, customItemService.getCustomItem(CustomItemIds.MENU_DOWN_ARROW_BUTTON).asQuantity(recipes.size() > startingRecipe + 7 ? 1 : 0), p -> {
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
            ItemStack item = customItemService.toItemstack(rc.material());
            item.setAmount(rc.quantity());
            addButton(9 * (ligne) + (is.value() - 1), item, p -> {});
        }
        ItemStack item = customItemService.toItemstack(recipe.result().material());
        item.setAmount(recipe.result().quantity());
        addButton(9 * (ligne) + (8), item, p ->
                new CraftingMenu(skill, customItemService, itemGateway, craftingService,guildService,stationService,playerInventoryPort, recipe, station, this).open(viewer));
    }

    private Object getField(Object obj, String fieldName) {
        try {
            return obj.getClass().getField(fieldName).get(obj);
        } catch (Exception e) {
            return null;
        }
    }
}

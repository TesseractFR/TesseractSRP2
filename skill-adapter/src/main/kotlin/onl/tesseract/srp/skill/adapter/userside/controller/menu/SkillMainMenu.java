package onl.tesseract.srp.skill.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.lib.service.ServiceContainer;
import onl.tesseract.srp.common.adapter.menu.CustomMenuButton;
import onl.tesseract.srp.common.adapter.menu.ItemAdderMenu;
import onl.tesseract.srp.customitem.domain.model.MaterialName;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import onl.tesseract.srp.skill.domain.model.PlayerID;
import onl.tesseract.srp.skill.domain.model.crafting.CraftTask;
import onl.tesseract.srp.skill.domain.model.crafting.QueuedRecipe;
import onl.tesseract.srp.skill.domain.model.recipe.Recipe;
import onl.tesseract.srp.skill.domain.model.recipe.RecipeComponent;
import onl.tesseract.srp.skill.domain.model.skill.Skill;
import onl.tesseract.srp.skill.domain.model.station.Station;
import onl.tesseract.srp.skill.domain.port.userside.CraftingService;
import onl.tesseract.srp.skill.domain.port.userside.StationService;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkillMainMenu extends ItemAdderMenu {

    private static final int RECIPE_BOOK_BUTTON_INDEX = 0;
    private static final int SKILL_TREE_BUTTON_INDEX = 4;
    private static final int INFO_BUTTON_INDEX = 8;

    private static final int PENDING_RECIPE_1_INDEX = 19;
    private static final int PENDING_RECIPE_2_INDEX = 20;
    private static final int PENDING_RECIPE_3_INDEX = 21;
    private static final int PENDING_RECIPE_4_INDEX = 22;
    private static final int PENDING_RECIPE_5_INDEX = 23;
    private static final int PENDING_RECIPE_6_INDEX = 24;
    private static final int PENDING_RECIPE_7_INDEX = 25;
    private static final int PENDING_RECIPE_8_INDEX = 26;

    private static final int CURRENT_RECIPE_INDEX = 39;
    private static final int ITEM_TO_COLLECT_INDEX = 41;

    private final Skill skill;
    private final CustomItemService itemGateway;
    private final CraftingService craftingService;
    private final Station station;
    private BukkitTask refreshTask;
    private final GuildService guildService;
    private final StationService stationService;

    public SkillMainMenu(Skill skill,
                         Station station,
                         CraftingService craftingService,
                         CustomItemService itemGateway,
                         GuildService guildService,
                         StationService stationService,
                         Menu previous) {
        super(MenuSize.Six, "tesseract:recipe_advancement", skill.name().value(), previous,-8);
        this.skill = skill;
        this.itemGateway = itemGateway;
        this.craftingService = craftingService;
        this.station = station;
        this.guildService = guildService;
        this.stationService = stationService;
    }

    @Override
    public void open(Player viewer) {
        super.open(viewer);
        refreshTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!hasViewer()) {
                    cancel();
                    return;
                }
                refresh(viewer);
            }
        }.runTaskTimer(ServiceContainer.get(Plugin.class), 20L, 20L);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        super.onClose(event);
        if (refreshTask != null) refreshTask.cancel();
    }

    @Override
    public void placeButtons(Player viewer) {
        addRecipeButton(viewer);
        addSkillTreeButton(viewer);
        addInfoButton();
        addPendingRecipeButtons(viewer);
        addCurrentRecipeButton(viewer);
        addItemToCollectButton(viewer);
        addBackButton();
        super.placeButtons(viewer);
    }

    private void addRecipeButton(Player viewer) {
        ItemStack item = getCustomButtonItemStack(CustomMenuButton.MENU_RECIPE_BOOK_BUTTON);
        ItemMetaUtil.displayName(item, Component.text("Recettes"));
        addButton(RECIPE_BOOK_BUTTON_INDEX, item, p ->
                new RecipeMenu(skill, station,craftingService,itemGateway,guildService,
                        stationService, this).open(viewer));
    }

    private void addSkillTreeButton(Player viewer) {
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMetaUtil.displayName(item, Component.text("Améliorations tables/joueur"));
        addButton(SKILL_TREE_BUTTON_INDEX, item, p -> {
            new SkillUpgradeMenu(skill,itemGateway,stationService, guildService, station, this).open(viewer);
        });
    }

    private void addInfoButton() {
        ItemStack item = getCustomButtonItemStack(CustomMenuButton.MENU_INFORMATION_BUTTON);
        ItemMetaUtil.displayName(item, Component.text("Informations"));
        addButton(INFO_BUTTON_INDEX, item, p -> {
        });
    }

    private void addPendingRecipeButtons(Player viewer) {
        List<Integer> pendingSlots = Arrays.asList(
                PENDING_RECIPE_1_INDEX,
                PENDING_RECIPE_2_INDEX,
                PENDING_RECIPE_3_INDEX,
                PENDING_RECIPE_4_INDEX,
                PENDING_RECIPE_5_INDEX,
                PENDING_RECIPE_6_INDEX,
                PENDING_RECIPE_7_INDEX,
                PENDING_RECIPE_8_INDEX
        );
        List<QueuedRecipe> queue = craftingService.getQueue(new PlayerID(viewer.getUniqueId()), skill.name()).stream().toList();

        for (int i = 0; i < pendingSlots.size(); i++) {
            int slot = pendingSlots.get(i);
            QueuedRecipe queuedRecipe = i < queue.size() ? queue.get(i) : null;
            ItemStack item;
            if (queuedRecipe != null) {
                try {
                    Recipe recipe = queuedRecipe.getRecipe();
                    RecipeComponent result = recipe.result();
                    ItemStack resultItem = itemGateway.getItemStack(new MaterialName(result.material().value()));
                    resultItem.editMeta( meta ->
                    {
                        meta.displayName(Component.text("§eRecette en attente : "
                                +queuedRecipe.getRecipe().result().quantity()+" x ..."));
                        List<Component> lore = new ArrayList<>();
                        lore.add(Component.text("§7Quantité : §f" + queuedRecipe.getQuantity()));
                        meta.lore(lore);
                    });
                    // quantity handling best-effort
                    resultItem.setAmount(Math.max(1, queuedRecipe.getQuantity()));
                    ItemMetaUtil.displayName(resultItem, Component.text("§eRecette en attente : ..."));
                    item = resultItem;
                } catch (Exception e) {
                    item = new ItemStack(Material.PAPER);
                    ItemMetaUtil.displayName(item, Component.text("Slot de file d'attente vide"));
                    item.setAmount(0);
                }
            } else {
                item = new ItemStack(Material.PAPER);
                ItemMetaUtil.displayName(item, Component.text("Slot de file d'attente vide"));
                item.setAmount(0);
            }
            addButton(slot, item, p -> {
            });
        }
    }

    private void addCurrentRecipeButton(Player viewer) {
        CraftTask task = craftingService.getActiveTask(new PlayerID(viewer.getUniqueId()), skill.name());
        ItemStack item;
        if (task != null) {
            try {
                QueuedRecipe queuedRecipe = task.queuedRecipe();
                Recipe recipe = queuedRecipe.getRecipe();
                RecipeComponent result = recipe.result();
                ItemStack resultItem = itemGateway.getItemStack(new MaterialName(result.material().value()));
                resultItem.setAmount(Math.max(1, queuedRecipe.getQuantity()));
                ItemMetaUtil.displayName(resultItem, Component.text("§aFabrication en cours..."));
                List<Component> lore = resultItem.lore();
                lore.add(Component.text("§7Time: " + task.timeLeft()));
                ItemMetaUtil.lore(resultItem, lore);
                item = resultItem;
            } catch (Exception e) {
                item = new ItemStack(Material.BARRIER);
                ItemMetaUtil.displayName(item, Component.text("Aucune fabrication en cours"));
            }
        } else {
            item = new ItemStack(Material.BARRIER);
            ItemMetaUtil.displayName(item, Component.text("Aucune fabrication en cours"));
        }
        addButton(CURRENT_RECIPE_INDEX, item, p -> {
        });
    }

    private void addItemToCollectButton(Player viewer) {
        PlayerID playerID = new PlayerID(viewer.getUniqueId());
        boolean hasItems = craftingService.hasLootCache(playerID, skill.name());
        ItemStack item;
        if (hasItems) {
            Object lootCache = craftingService.getLootCache(playerID, skill.name());
            int doneCount = 0;
            int garbageCount = 0;
            try {
                List<?> done = (List<?>) lootCache.getClass().getMethod("done").invoke(lootCache);
                for (Object d : done) doneCount += (int) d.getClass().getField("quantity").get(d);
                List<?> garbage = (List<?>) lootCache.getClass().getMethod("garbage").invoke(lootCache);
                for (Object g : garbage) garbageCount += (int) g.getClass().getField("quantity").get(g);
            } catch (Exception e) {
                // ignore
            }
            item = new ItemStack(Material.CHEST);
            ItemMetaUtil.displayName(item, Component.text("§aObjets à récupérer"));
            List<Component> lore = new java.util.ArrayList<>();
            if (doneCount > 0) lore.add(Component.text("§7- " + doneCount + " objets fabriqués"));
            if (garbageCount > 0) lore.add(Component.text("§7- " + garbageCount + " résidus"));
            ItemMetaUtil.lore(item, lore);
        } else {
            item = new ItemStack(Material.MINECART);
            ItemMetaUtil.displayName(item, Component.text("§cRien à récupérer"));
        }
        addButton(ITEM_TO_COLLECT_INDEX, item, p -> {
            craftingService.collectCraftResults(playerID, skill.name());
            this.placeButtons(viewer);
        });
    }

}

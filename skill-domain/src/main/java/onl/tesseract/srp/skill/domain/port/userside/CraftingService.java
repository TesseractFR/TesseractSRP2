package onl.tesseract.srp.skill.domain.port.userside;

import onl.tesseract.srp.skill.domain.model.PlayerID;
import onl.tesseract.srp.skill.domain.model.Quality;
import onl.tesseract.srp.skill.domain.model.bonus.Bonus;
import onl.tesseract.srp.skill.domain.model.bonus.CraftingBonus;
import onl.tesseract.srp.skill.domain.model.bonus.StationBonus;
import onl.tesseract.srp.skill.domain.model.crafting.*;
import onl.tesseract.srp.skill.domain.model.recipe.Recipe;
import onl.tesseract.srp.skill.domain.model.recipe.RecipeComponent;
import onl.tesseract.srp.skill.domain.model.skill.SkillName;
import onl.tesseract.srp.skill.domain.model.station.StatType;
import onl.tesseract.srp.skill.domain.model.station.Station;
import onl.tesseract.srp.skill.domain.port.serverside.*;
import lombok.NonNull;

import java.time.Duration;
import java.util.*;

public class CraftingService {
    private static final float DEFAULT_SUCCESS_RATE = 0.75f;
    private final SkillResultCacheRepository skillResultCacheRepository;
    private final CraftingRepository craftingRepository;
    private final ItemRepository itemRepository;
    private final CraftTaskScheduler craftTaskScheduler;
    private final MessagingRepository messagingRepository;
    private final PlayerSkillUpgradeRepository playerSkillUpgradeRepository;
    private final Map<PlayerID, PlayerLootCache> playersLootCache = new HashMap<>();
    private final Map<PlayerID, PerSkillTask> activeTasks = new HashMap<>();
    private final Map<PlayerID, PerSkillQueue> queues = new HashMap<>();

    public CraftingService(SkillResultCacheRepository skillResultCacheRepository, CraftingRepository craftingRepository, ItemRepository itemRepository, CraftTaskScheduler craftTaskScheduler, MessagingRepository messagingRepository, PlayerSkillUpgradeRepository playerSkillUpgradeRepository) {
        this.skillResultCacheRepository = skillResultCacheRepository;
        this.craftingRepository = craftingRepository;
        this.itemRepository = itemRepository;
        this.craftTaskScheduler = craftTaskScheduler;
        this.messagingRepository = messagingRepository;
        this.playerSkillUpgradeRepository = playerSkillUpgradeRepository;

        loadAllCollectionCaches();
    }


    private void loadAllCollectionCaches() {
        playersLootCache.putAll(skillResultCacheRepository.findAll());
    }

    public LootCache getLootCache(@NonNull PlayerID playerID,@NonNull SkillName skillName) {
        playersLootCache.putIfAbsent(playerID, new PlayerLootCache(new HashMap<>()));
        PlayerLootCache plc = playersLootCache.get(playerID);
        plc.lootCaches().putIfAbsent(skillName, new LootCache());
        return plc.lootCaches().get(skillName);
    }

    public Queue<QueuedRecipe> getQueue(@NonNull PlayerID playerID, @NonNull SkillName skillName) {
        queues.putIfAbsent(playerID, new PerSkillQueue(new HashMap<>()));
        PerSkillQueue perSkillQueue = queues.get(playerID);
        perSkillQueue.queuedRecipes().putIfAbsent(skillName, new ArrayDeque<>());
        return perSkillQueue.queuedRecipes().get(skillName);
    }

    public void collectCraftResults(@NonNull PlayerID playerID, @NonNull SkillName skillName) {
        LootCache lootCache = getLootCache(playerID, skillName);
        if(lootCache.done().isEmpty() && lootCache.garbage().isEmpty()) {
            return;
        }
        lootCache.done().forEach(item->itemRepository.giveItem(playerID, item.material(), item.quantity(),item.quality()));
        lootCache.garbage().forEach(item->itemRepository.giveItem(playerID, item.material(), item.quantity(),item.quality()));
        lootCache.done().clear();
        lootCache.garbage().clear();
        skillResultCacheRepository.delete(playerID, skillName);
    }

    public int getMaxStackSize(Recipe recipe) {
        int maxCompo = 64;
        for (RecipeComponent comp : recipe.components().values()) {
            maxCompo = Math.min(maxCompo, itemRepository.getItemMaxSize(comp.material()) / comp.quantity());
        }
        return Math.min(itemRepository.getItemMaxSize(recipe.result().material()) / recipe.result().quantity(), maxCompo);
    }


    public void processCraftStep(PlayerID player, SkillName skill, CraftTask task) {
        if (task.getQueuedRecipe().getQuantity() <= 0) {
            finishTask(player, skill, task);
        }
        CraftingBonus bonus = task.getQueuedRecipe().getBonus();
        // Appliquer le taux de réussite
        double effectiveSuccessRate = getSuccessRate(bonus.craftingStationBonus());
        boolean success = new Random().nextDouble() <= effectiveSuccessRate;
        if(success){
            var result = task.getQueuedRecipe().getRecipe().result();

            var quality = task.getQueuedRecipe().getCompoQuality();
            if(new Random().nextDouble() <= bonus.craftingStationBonus().qualityBonus().value()){
                quality= quality.next();
            }
            LootCache lootCache = getLootCache(player, skill);
            lootCache.done().add(new CraftElement(result.material(), result.quantity(), quality));
        }
        task.decrementQuantity();
        saveTasks(player, skill);
        savePlayerCache(player, skill);
        if(task.getQueuedRecipe().getQuantity() <= 0) {
            finishTask(player, skill, task);
        }
    }

    private void savePlayerCache(PlayerID player, SkillName skill) {
        skillResultCacheRepository.save(player,skill, getLootCache(player, skill));
    }

    private void finishTask(PlayerID player, SkillName skill, CraftTask task) {
        craftTaskScheduler.cancel(player, skill);
        craftingRepository.removeActiveTask(player, skill);
        //On retire la task de la liste active
        activeTasks.computeIfPresent(player, (playerId, skillMap) -> {
            skillMap.tasks().remove(skill);
            return skillMap;
        });

        // Lancer la recette suivante si disponible
        var queue = queues.get(player).queuedRecipes().get(skill);
        if (queue != null && !queue.isEmpty()) {
            var nextRecipe = queue.remove();
            startTask(player, skill, nextRecipe);
        }
        saveTasks(player, skill);
    }

    private void saveTasks(PlayerID player, SkillName skill) {
        activeTasks.computeIfPresent(player, (playerId, skillMap) -> {
            skillMap.tasks().computeIfPresent(skill, (skillName, task) -> {
                craftingRepository.saveActiveTask(player, skill, task);
                return task;
            });
            return skillMap;
        });

        queues.computeIfPresent(player, (playerId, skillQueue) -> {
            skillQueue.queuedRecipes().computeIfPresent(skill, (skillName, queuedRecipes) -> {
                craftingRepository.saveQueuedRecipe(player, skill, queuedRecipes);
                return queuedRecipes;
            });
            return skillQueue;
        });
    }

    private void startTask(PlayerID player,SkillName skill,QueuedRecipe queuedRecipe){
        Duration reducedDuration = getAdjustedDuration(queuedRecipe.getRecipe(), queuedRecipe.getBonus().craftingStationBonus());
        CraftTask craftTask = new CraftTask(queuedRecipe,reducedDuration);
        activeTasks.get(player).tasks().put(skill, craftTask);
        craftTaskScheduler.schedule(player, skill, craftTask, this);
    }

    public double getSuccessRate(StationBonus stationBonus) {
        return DEFAULT_SUCCESS_RATE + stationBonus.successBonus().value();
    }

    public Duration getAdjustedDuration(Recipe recipe, StationBonus stationBonus) {
        return Duration.ofMillis(Math.round(
                recipe.duration().toMillis() * (1 - stationBonus.timeReduction().value())));
    }

    public Quality getMaximumIngredientQuality(PlayerID player, SkillName skill) {
        return playerSkillUpgradeRepository.getMaximumIngredientQuality(player, skill);
    }

    public Quality upgradeMaximumIngredientQuality(PlayerID player, SkillName skill) {
        Quality current = getMaximumIngredientQuality(player, skill);
        if (current == Quality.EXCEPTIONAL) return current;
        Quality next = current.next();
        playerSkillUpgradeRepository.saveMaximumIngredientQuality(player, skill, next);
        return next;
    }

    public int getMaxCraft(Recipe recipe, PlayerID player, Quality quality) {
        int maxCraft = Integer.MAX_VALUE;
        for (RecipeComponent component : recipe.components().values()) {
            int owned = itemRepository.getItemNumber(player, component.material(), quality);
            maxCraft = Math.min(maxCraft, owned / component.quantity());
        }
        if (maxCraft == Integer.MAX_VALUE) return 0;
        return Math.min(maxCraft, getMaxStackSize(recipe));
    }

    public void startCraft(PlayerID player, SkillName skill, Recipe recipe, int quantity, Quality quality, Station station) {
        if (!quality.isAtMost(getMaximumIngredientQuality(player, skill))) return;
        // Tier check
        if (recipe.tier().value() > station.getStatLevel(StatType.TIER)) {
            messagingRepository.sendInsuffisantTableTier(player,recipe.tier());
            return;
        }

        // Check and delete resources
        for (var component : recipe.components().values()) {
            int available = itemRepository.getItemNumber(player, component.material(),quality);
            int totalNeeded = component.quantity() * quantity;
            if (available < totalNeeded) {
                messagingRepository.sendInsuffisantComponent(player);
                return;
            }
        }

        for (var component : recipe.components().values()) {
            int totalNeeded = component.quantity() * quantity;
            itemRepository.removeItems(player, component.material(), totalNeeded,quality);
        }

        // Create bonus from station upgrades
        var bonus = new CraftingBonus(station.getBonus(), new Bonus(0.0), new Bonus(0.0), new Bonus(0.0));

        var queuedRecipe = new QueuedRecipe(recipe, quality,bonus, quantity);
        var skillQueue = getQueue(player,skill);


        activeTasks.putIfAbsent(player,new PerSkillTask(new HashMap<>()));
        //Si il a pas de craft en cours
        if (activeTasks.get(player).tasks().get(skill) == null ) {
            startTask(player,skill,queuedRecipe);
            messagingRepository.sendCraftStarted(player);
        } else {
            skillQueue.add(queuedRecipe);
            messagingRepository.sendCraftQueued(player);
        }
        saveTasks(player, skill);
    }

    public boolean hasLootCache(PlayerID player, SkillName name) {
        LootCache lootCache = getLootCache(player, name);
        return !lootCache.garbage().isEmpty() || !lootCache.done().isEmpty();
    }

    public CraftTask getActiveTask(@NonNull PlayerID playerID, SkillName name) {
        activeTasks.putIfAbsent(playerID, new PerSkillTask(new HashMap<>()));
        return activeTasks.get(playerID).tasks().get(name);
    }
}

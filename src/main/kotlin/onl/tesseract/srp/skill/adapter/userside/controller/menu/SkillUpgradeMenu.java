package onl.tesseract.srp.skill.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.controller.menu.ItemAdderMenu;
import onl.tesseract.srp.customitem.adapter.userside.CustomItemTags;
import onl.tesseract.srp.customitem.adapter.userside.ItemGateway;
import onl.tesseract.srp.domain.item.CustomItemIds;
import onl.tesseract.srp.domain.skill.station.StatType;
import onl.tesseract.srp.service.item.CustomItemService;
import onl.tesseract.srp.service.territory.guild.GuildService;
import onl.tesseract.srp.skill.domain.model.skill.Skill;
import onl.tesseract.srp.skill.domain.model.station.Station;
import onl.tesseract.srp.skill.domain.port.userside.StationService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class SkillUpgradeMenu extends ItemAdderMenu {

    private static final int TIER_BUTTON_INDEX = 10;
    private static final int QUALITY_BUTTON_INDEX = 12;
    private static final int SUCCESS_BUTTON_INDEX = 14;
    private static final int TIME_BUTTON_INDEX = 16;

    private static final int RECOVERY_SUCCESS_INDEX = 29;
    private static final int RECOVERY_FAILURE_INDEX = 31;
    private static final int MULTI_CRAFT_INDEX = 33;

    private static final int BACK_BUTTON_INDEX = 36;
    private static final int INFO_BUTTON_INDEX = 44;

    private final Skill skill;
    private final ItemGateway itemGateway;
    private final StationService stationService;
    private final GuildService guildService;
    private final Station station;
    private final Menu previous;

    public SkillUpgradeMenu(Skill skill,
                            ItemGateway itemGateway,
                            StationService stationService,
                            GuildService guildService,
                            Station station,
                            Menu previous) {
        super(MenuSize.Five, "tesseract:skill_upgrade", "", previous,-8);
        this.skill = skill;
        this.itemGateway = itemGateway;
        this.stationService = stationService;
        this.guildService = guildService;
        this.station = station;
        this.previous = previous;
    }

    @Override
    public void placeButtons(Player viewer) {
        addTierButton(viewer);
        addQualityButton(viewer);
        addSuccessButton(viewer);
        addTimeButton(viewer);

        addRecoverySuccessButton(viewer);
        addRecoveryFailureButton(viewer);
        addMultiCraftButton(viewer);

        addButton(BACK_BUTTON_INDEX, itemGateway.getItemStack(CustomItemTags.MENU_BACK_ARROW_BUTTON), p -> {
            if (previous == null) {
                this.close();
                return;
            }
            previous.open(viewer);
        });
        addInfoButton(viewer);
    }

    private void addTierButton(Player viewer) {
        int level = station.getStatLevel(StatType.TIER);
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("+1 Tier de recettes", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            meta.lore(Arrays.asList(Component.text("Tier disponible: ", NamedTextColor.GRAY).append(Component.text(String.valueOf(level), NamedTextColor.WHITE))));
            item.setItemMeta(meta);
        }
        addButton(TIER_BUTTON_INDEX, item, p -> upgradeStat(viewer, StatType.TIER));
    }

    private void addQualityButton(Player viewer) {
        int level = station.getStatLevel(StatType.QUALITY);
        int power = (int) (station.getBonus().qualityBonus().value() * 100);
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("+" + power + "% Qualité des items craftés", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
            meta.lore(Arrays.asList(
                    Component.text("Niveau: ", NamedTextColor.GRAY).append(Component.text(String.valueOf(level), NamedTextColor.WHITE)),
                    Component.text("Bonus: ", NamedTextColor.GRAY).append(Component.text(power + "%", NamedTextColor.LIGHT_PURPLE)),
                    Component.text("Augmente la chance d'obtenir une ", NamedTextColor.GRAY)
                            .append(Component.text("qualité plus élevée", NamedTextColor.WHITE)).append(Component.text(".", NamedTextColor.GRAY))
            ));
            item.setItemMeta(meta);
        }
        addButton(QUALITY_BUTTON_INDEX, item, p -> upgradeStat(viewer, StatType.QUALITY));
    }

    private void addSuccessButton(Player viewer) {
        int level = station.getStatLevel(StatType.SUCCESS);
        int power = (int) (station.getBonus().successBonus().value() * 100);
        ItemStack item = new ItemStack(Material.RABBIT_FOOT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("+" + power + "% Chance de succès", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            meta.lore(Arrays.asList(
                    Component.text("Niveau: ", NamedTextColor.GRAY).append(Component.text(String.valueOf(level), NamedTextColor.WHITE)),
                    Component.text("Bonus: ", NamedTextColor.GRAY).append(Component.text(power + "%", NamedTextColor.GREEN)),
                    Component.text("Augmente la ", NamedTextColor.GRAY).append(Component.text("probabilité de réussite", NamedTextColor.WHITE)).append(Component.text(" du craft.", NamedTextColor.GRAY))
            ));
            item.setItemMeta(meta);
        }
        addButton(SUCCESS_BUTTON_INDEX, item, p -> upgradeStat(viewer, StatType.SUCCESS));
    }

    private void addTimeButton(Player viewer) {
        int level = station.getStatLevel(StatType.TIME_REDUCTION);
        int power = (int) (station.getBonus().timeReduction().value() * 100);
        ItemStack item = new ItemStack(Material.CLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("-" + power + "% Temps de craft", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            meta.lore(Arrays.asList(
                    Component.text("Niveau: ", NamedTextColor.GRAY).append(Component.text(String.valueOf(level), NamedTextColor.WHITE)),
                    Component.text("Réduction: ", NamedTextColor.GRAY).append(Component.text(power + "%", NamedTextColor.YELLOW)),
                    Component.text("Réduit le ", NamedTextColor.GRAY).append(Component.text("temps de fabrication", NamedTextColor.WHITE)).append(Component.text(" par recette.", NamedTextColor.GRAY))
            ));
            item.setItemMeta(meta);
        }
        addButton(TIME_BUTTON_INDEX, item, p -> upgradeStat(viewer, StatType.TIME_REDUCTION));
    }

    private void addRecoverySuccessButton(Player viewer) {
        //TODO Get player LVL
        int level = 0;
        int power = 0;
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("+" + power + "% Récupération (succès)", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            meta.lore(Arrays.asList(
                    Component.text("Niveau: ", NamedTextColor.GRAY).append(Component.text(String.valueOf(level), NamedTextColor.WHITE)),
                    Component.text("Récupération: ", NamedTextColor.GRAY).append(Component.text(power + "%", NamedTextColor.GREEN)),
                    Component.text("Récupère une partie des ", NamedTextColor.GRAY).append(Component.text("composants utilisés", NamedTextColor.WHITE)).append(Component.text(" en cas de ", NamedTextColor.GRAY)).append(Component.text("réussite", NamedTextColor.GREEN)).append(Component.text(".", NamedTextColor.GRAY))
            ));
            item.setItemMeta(meta);
        }
        addButton(RECOVERY_SUCCESS_INDEX, item, p -> upgradeStat(viewer, StatType.RECOVERY_SUCCESS));
    }

    private void addRecoveryFailureButton(Player viewer) {
        //TODO Recup player stats
        int level = 0;
        int power = 0;
        ItemStack item = new ItemStack(Material.FLINT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("+" + power + "% Récupération (échec)", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            meta.lore(Arrays.asList(
                    Component.text("Niveau: ", NamedTextColor.GRAY).append(Component.text(String.valueOf(level), NamedTextColor.WHITE)),
                    Component.text("Récupération: ", NamedTextColor.GRAY).append(Component.text(power + "%", NamedTextColor.RED)),
                    Component.text("Récupère une partie des ", NamedTextColor.GRAY).append(Component.text("composants utilisés", NamedTextColor.WHITE)).append(Component.text(" en cas d'", NamedTextColor.GRAY)).append(Component.text("échec", NamedTextColor.RED)).append(Component.text(".", NamedTextColor.GRAY))
            ));
            item.setItemMeta(meta);
        }
        addButton(RECOVERY_FAILURE_INDEX, item, p -> upgradeStat(viewer, StatType.RECOVERY_FAILURE));
    }

    private void addMultiCraftButton(Player viewer) {
        //TODO get player Stat
        int level = 0;
        int power = 0;
        ItemStack item = new ItemStack(Material.BUNDLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("+" + power + "% Craft multiple", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            meta.lore(Arrays.asList(
                    Component.text("Niveau: ", NamedTextColor.GRAY).append(Component.text(String.valueOf(level), NamedTextColor.WHITE)),
                    Component.text("Bonus: ", NamedTextColor.GRAY).append(Component.text(power + "%", NamedTextColor.GOLD)),
                    Component.text("Chance de produire ", NamedTextColor.GRAY).append(Component.text("plusieurs exécutions", NamedTextColor.WHITE)).append(Component.text(" d'une même recette simultanément.", NamedTextColor.GRAY))
            ));
            item.setItemMeta(meta);
        }
        addButton(MULTI_CRAFT_INDEX, item, p -> upgradeStat(viewer, StatType.MULTI_CRAFT));
    }

    private void addInfoButton(Player viewer) {
        ItemStack item = itemGateway.getItemStack(CustomItemTags.MENU_INFORMATION_BUTTON);
        Object guild = guildService.getById(station.key().territoryId());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Informations"));
            if (guild != null) {
                // best-effort: assume guild has a name() method
                try {
                    String name = guild.getClass().getMethod("name").invoke(guild).toString();
                    meta.lore(Arrays.asList(
                            Component.text("Territoire: ", NamedTextColor.GRAY).append(Component.text(name, NamedTextColor.WHITE)),
                            Component.text("Compétence: ", NamedTextColor.GRAY).append(Component.text(station.key().skillName().value(), NamedTextColor.WHITE))
                    ));
                } catch (Exception e) {
                    meta.lore(Arrays.asList(
                            Component.text("Territoire: ", NamedTextColor.GRAY).append(Component.text("?", NamedTextColor.WHITE)),
                            Component.text("Compétence: ", NamedTextColor.GRAY).append(Component.text(station.key().skillName().value(), NamedTextColor.WHITE))
                    ));
                }
            } else {
                meta.lore(Arrays.asList(
                        Component.text("Compétence: ", NamedTextColor.GRAY).append(Component.text(station.key().skillName().value(), NamedTextColor.WHITE))
                ));
            }
            item.setItemMeta(meta);
        }
        addButton(INFO_BUTTON_INDEX, item, p -> {});
    }

    private void upgradeStat(Player viewer, StatType statType) {
        Object upgraded = stationService.upgradeStat(station, statType);
        if (upgraded != null) {
            viewer.sendMessage(Component.text("✓ Amélioration effectuée!", NamedTextColor.GREEN));
            close();
            new SkillUpgradeMenu(skill, itemGateway, stationService, guildService, station, null).open(viewer);
        } else {
            viewer.sendMessage(Component.text("✗ Erreur lors de l'amélioration", NamedTextColor.RED));
        }
    }
}

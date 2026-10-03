package onl.tesseract.srp.controller.menu.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.menu.Button;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.menu.InventoryHeadIcons;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.lib.profile.PlayerProfileService;
import onl.tesseract.lib.task.TaskScheduler;
import onl.tesseract.lib.itembuilder.ItemLoreBuilder;
import onl.tesseract.srp.common.adapter.userside.menu.BiMenu;
import onl.tesseract.srp.common.domain.model.enums.PlayerRank;
import onl.tesseract.srp.common.domain.model.SrpPlayer;
import onl.tesseract.srp.service.player.SrpPlayerService;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerRankProgressMenu extends BiMenu {
    private static final int SCROLL_SPREAD = 4;

    private final UUID playerID;
    private final SrpPlayerService playerService;
    private final PlayerProfileService profileService;
    private final TaskScheduler scheduler;

    public PlayerRankProgressMenu(UUID playerID,
                                  SrpPlayerService playerService,
                                  PlayerProfileService profileService,
                                  TaskScheduler scheduler,
                                  Menu previous) {
        super(MenuSize.One, Component.text("Grades"), previous);
        this.playerID = playerID;
        this.playerService = playerService;
        this.profileService = profileService;
        this.scheduler = scheduler;
    }

    @Override
    public void placeButtons(Player viewer) {
        SrpPlayer player = playerService.getPlayer(playerID);
        int scroll = player.getRank().ordinal() * SCROLL_SPREAD;
        placeButtons(viewer, scroll);
    }

    private void placeButtons(Player viewer, int scroll) {
        if (scroll < 0) {
            placeButtons(viewer, 0);
            return;
        }

        SrpPlayer player = playerService.getPlayer(playerID);
        List<Button> buttonLine = computeButtonLine(player, viewer);
        if (scroll > buttonLine.size() - 9) {
            placeButtons(viewer, buttonLine.size() - 9);
            return;
        }

        for (int inventoryIndex = 0; inventoryIndex < 9; inventoryIndex++) {
            int lineIndex = inventoryIndex + scroll;
            addButton(inventoryIndex, buttonLine.get(lineIndex));
        }

        addScrollButtons(scroll, viewer);

        addBottomButton(SCROLL_SPREAD, new ItemBuilder(Material.PISTON)
                .name("Affichage compacte")
                .lore()
                .newline()
                .append("Clique pour voir tous les grades", NamedTextColor.GRAY)
                .buildLore()
                .build(), event -> {
        });

        placePlayerInfo(player);
        addBottomBackButton();
        addBottomCloseButton();
        addMenuUsage();
    }

    private void addScrollButtons(int scroll, Player viewer) {
        addBottomButton(21, new ItemBuilder(Material.PLAYER_HEAD)
                .customHead(    InventoryHeadIcons.LEFT_ARROW.getData(), InventoryHeadIcons.LEFT_ARROW.getSignature())
                .name("Gauche")
                .build(), event -> placeButtons(viewer, scroll - 1));

        addBottomButton(23, new ItemBuilder(Material.PLAYER_HEAD)
                .customHead(InventoryHeadIcons.RIGHT_ARROW.getData(), InventoryHeadIcons.LEFT_ARROW.getSignature())
                .name("Droite")
                .build(), event -> placeButtons(viewer, scroll + 1));
    }

    private List<Button> computeButtonLine(SrpPlayer player, Player viewer) {
        List<Button> buttons = new ArrayList<>();
        for (PlayerRank rank : PlayerRank.values()) {
            buttons.add(getRankButton(player, rank, viewer));

            PlayerRank nextRank = rank.next();
            if (nextRank == null) {
                continue;
            }

            int ratio;
            if (rank.compareTo(player.getRank()) < 0) {
                ratio = 3;
            } else if (rank.compareTo(player.getRank()) > 0) {
                ratio = 0;
            } else {
                ratio = (int) (3f * player.getMoney() / nextRank.getCost());
            }

            for (int i = 1; i <= 3; i++) {
                Material material = ratio < i
                        ? Material.RED_STAINED_GLASS_PANE
                        : Material.GREEN_STAINED_GLASS_PANE;
                buttons.add(new Button(new ItemBuilder(material).name("").build()));
            }
        }
        return buttons;
    }

    private Button getRankButton(SrpPlayer player, PlayerRank rank, Player viewer) {
        ItemLoreBuilder lore = new ItemLoreBuilder().newline();
        if (rank == player.getRank()) {
            lore.append("Grade actuel", NamedTextColor.GREEN);
        } else if (rank == player.getRank().next()) {
            lore.append("Prochain grade", NamedTextColor.GREEN)
                    .newline()
                    .append("Coût : " + rank.getCost() + " lys", NamedTextColor.GRAY);
        } else if (rank.compareTo(player.getRank()) < 0) {
            lore.append("Obtenu", NamedTextColor.GRAY);
        } else {
            lore.append("Bloqué", NamedTextColor.RED);
        }

        return new Button(
                new ItemBuilder(Material.NAME_TAG)
                        .name(rank.name())
                        .enchanted(rank.compareTo(player.getRank()) <= 0)
                        .lore(lore.get())
                        .build(),
                event -> {
                    if (playerService.getPlayer(playerID).getRank().next() != rank) {
                        return;
                    }
                    if (playerService.buyNextRank(playerID)) {
                        viewer.playSound(viewer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        int scroll = (rank.ordinal() - 1) * SCROLL_SPREAD;
                        for (int i = 0; i < SCROLL_SPREAD; i++) {
                            int index = i;
                            scheduler.runTimer(10L * (index + 1), 0L, 0L,
                                    task -> placeButtons(viewer, scroll + index + 1));
                        }
                    }
                });
    }

    private void placePlayerInfo(SrpPlayer player) {
        addBottomButton(9,
                () -> new ItemBuilder(profileService.getPlayerHead(playerID))
                        .name("Mes informations", NamedTextColor.GREEN)
                        .lore()
                        .newline()
                        .addField("Argent", Component.text(player.getMoney() + " Lys",NamedTextColor.GOLD))
                        .addField("Grade", Component.text(player.getRank().toString(),NamedTextColor.GOLD))
                        .buildLore()
                        .build(),
                null);
    }

    private void addMenuUsage() {
        addBottomButton(17, new ItemBuilder(Material.OAK_SIGN)
                .name("Comment utiliser ce menu ?")
                .lore()
                .newline()
                .append(
                        "La vue du haut montre les différents grades obtenables. Utilise les flèches pour te déplacer de gauche à droite"
                                + " et voir les grades suivants. Tu es par défaut centré sur le prochain grade disponible.",
                        NamedTextColor.GRAY)
                .buildLore()
                .build(), event -> {
        });
    }
}

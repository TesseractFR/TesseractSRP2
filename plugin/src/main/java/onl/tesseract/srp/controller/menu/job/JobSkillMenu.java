package onl.tesseract.srp.controller.menu.job;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.lib.menu.ItemBuilder;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.lib.util.ItemLoreBuilder;
import onl.tesseract.lib.util.menu.InventoryHeadIcons;
import onl.tesseract.srp.common.adapter.userside.menu.BiMenu;
import onl.tesseract.srp.job.domain.model.Job;
import onl.tesseract.srp.job.domain.model.PlayerID;
import onl.tesseract.srp.job.domain.model.talent.Talent;
import onl.tesseract.srp.job.domain.model.talenttree.Arrow;
import onl.tesseract.srp.job.domain.model.talenttree.CellType;
import onl.tesseract.srp.job.domain.model.talenttree.RootCell;
import onl.tesseract.srp.job.domain.model.talenttree.SkillCell;
import onl.tesseract.srp.job.domain.model.talenttree.TalentTree;
import onl.tesseract.srp.job.domain.port.userside.JobPlayerProgressionService;
import onl.tesseract.srp.job.domain.port.userside.JobTalentTreeService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobSkillMenu extends BiMenu {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobSkillMenu.class);

    private final PlayerID playerID;
    private final Job job;
    private final JobTalentTreeService jobTalentTreeService;
    private final JobPlayerProgressionService jobPlayerProgressionService;

    private TalentTree menuConfig;
    private int scroll;

    public JobSkillMenu(PlayerID playerID,
                        Job job,
                        JobTalentTreeService jobTalentTreeService,
                        JobPlayerProgressionService jobPlayerProgressionService) {
        super(MenuSize.Six, Component.text("Compétences"), null);
        this.playerID = playerID;
        this.job = job;
        this.jobTalentTreeService = jobTalentTreeService;
        this.jobPlayerProgressionService = jobPlayerProgressionService;
    }

    @Override
    public void placeButtons(Player viewer) {
        try {
            menuConfig = jobTalentTreeService.getTalentTree(job.jobName());
        } catch (Exception exception) {
            LOGGER.error("Failed to open skill menu for job {}", job, exception);
            viewer.sendMessage(Component.text(
                    "Une erreur est survenue lors de l'ouverture du menu. Veuillez contacter un administrateur.",
                    NamedTextColor.RED
            ));
            close();
            return;
        }

        openScroll(0);
    }

    private void openScroll(int scroll) {
        clearTop();
        this.scroll = scroll;

        CellType[][] matrix = menuConfig.matrix();
        int maxHeight = Math.min(matrix.length, 6 + scroll);
        for (int row = scroll; row < maxHeight; row++) {
            for (int column = 0; column < matrix[row].length; column++) {
                int index = column + ((5 - (row - scroll)) * 9);
                placeCell(matrix[row][column], index);
            }
        }

        addBottomButton(13, new ItemBuilder(Material.PLAYER_HEAD)
                .customHead(InventoryHeadIcons.UP_ARROW.getData(), null)
                .name("Monter", NamedTextColor.GRAY)
                .build(), event -> {
            if (this.scroll < menuConfig.matrix().length - 1) {
                openScroll(this.scroll + 1);
            }
        });

        addBottomButton(21, new ItemBuilder(Material.PLAYER_HEAD)
                .customHead(InventoryHeadIcons.LEFT_ARROW_LOG.getData(), null)
                .name("Gauche", NamedTextColor.GRAY)
                .build(), null);

        addBottomButton(23, new ItemBuilder(Material.PLAYER_HEAD)
                .customHead(InventoryHeadIcons.RIGHT_ARROW_LOG.getData(), null)
                .name("Droite", NamedTextColor.GRAY)
                .build(), null);

        addBottomButton(31, new ItemBuilder(Material.PLAYER_HEAD)
                .customHead(InventoryHeadIcons.DOWN_ARROW.getData(), null)
                .name("Descendre", NamedTextColor.GRAY)
                .build(), event -> {
            if (this.scroll > 0) {
                openScroll(this.scroll - 1);
            }
        });
    }

    private void placeCell(CellType cellType, int index) {
        if (cellType instanceof Arrow arrow) {
            addButton(index, new ItemBuilder(Material.STONE_BUTTON)
                    .name(" ")
                    .customModelData(arrow.type().getCustomModelData())
                    .build());
        } else if (cellType instanceof RootCell) {
            addButton(index, new ItemBuilder(Material.DIAMOND_PICKAXE)
                    .name(job.jobDisplayName().value())
                    .build());
        } else if (cellType instanceof SkillCell skillCell) {
            Talent skill = job.talents().get(skillCell.talent());
            ItemLoreBuilder lore = new ItemLoreBuilder()
                    .newline()
                    .append(skill.bonus().getDescription())
                    .newline();

            if (jobPlayerProgressionService.getTalentLevel(
                    playerID, job.jobName(), skill.name()) > 0) {
                lore.append("Acquis", NamedTextColor.GREEN);
            } else {
                NamedTextColor color = jobPlayerProgressionService.canBuyUpgrade(
                        playerID, job.jobName(), skill
                ) ? NamedTextColor.BLUE : NamedTextColor.RED;
                lore.append("Coût", color)
                        .append(" : " + jobPlayerProgressionService.getTalentCost(
                                playerID, job.jobName(), skill
                        ), NamedTextColor.GRAY);
                if (!jobPlayerProgressionService.isAvailable(
                        playerID, job.jobName(), skill
                )) {
                    lore.newline().append("Bloqué", NamedTextColor.RED);
                }
            }

            addButton(index, new ItemBuilder(Material.RABBIT_FOOT)
                    .name(skill.name().value())
                    .lore(lore.get())
                    .build(), event -> {
                if (jobPlayerProgressionService.upgradeSkill(playerID, skill)) {
                    openScroll(this.scroll);
                }
            });
        }
    }
}

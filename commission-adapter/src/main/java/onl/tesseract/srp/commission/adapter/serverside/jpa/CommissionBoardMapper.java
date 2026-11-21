package onl.tesseract.srp.commission.adapter.serverside.jpa;

import onl.tesseract.srp.commission.adapter.serverside.jpa.entity.CommissionBoardEntity;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardLevels;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Convertit les tableaux du domaine en entités JPA et inversement. */
@Component
public class CommissionBoardMapper {

    /** @return l'entité du tableau du spawn, sans identifiant, que la base complète à l'insertion */
    public CommissionBoardEntity toNewSpawnBoardEntity(CommissionBoardLevels levels) {
        return toNewEntity(levels, 0);
    }

    /** @return l'entité d'un tableau de guilde neuf, sans identifiant, que la base complète à l'insertion */
    public CommissionBoardEntity toNewGuildBoardEntity(GuildId guildId, double usageTaxRate) {
        CommissionBoardEntity entity = toNewEntity(CommissionBoardLevels.GUILD_BOARD_START, usageTaxRate);
        entity.setGuildId(guildId.value().toString());
        return entity;
    }

    private static CommissionBoardEntity toNewEntity(CommissionBoardLevels levels, double usageTaxRate) {
        CommissionBoardEntity entity = new CommissionBoardEntity();
        copyLevels(levels, entity);
        entity.setUsageTaxRate(usageTaxRate);
        entity.setCompletedCommissionCount(0);
        return entity;
    }

    /** Recopie dans l'entité ce que le domaine peut modifier : niveaux et taxe. */
    public void copyChanges(CommissionBoard commissionBoard, CommissionBoardEntity entity) {
        copyLevels(commissionBoard.getLevels(), entity);
        entity.setUsageTaxRate(commissionBoard.getUsageTaxRate());
    }

    public CommissionBoard toDomain(CommissionBoardEntity entity) {
        CommissionBoardId boardId = new CommissionBoardId(entity.getId());
        CommissionBoardLevels levels = new CommissionBoardLevels(entity.getBoardTier(), entity.getMaxHarvestTier(), entity.getMaxCraftTier(),
                entity.getHarvestSlots(), entity.getCraftSlots(), entity.getMoneyBonusLevel());
        return Optional.ofNullable(entity.getGuildId())
                .map(guildId -> CommissionBoard.guildBoard(boardId, new GuildId(UUID.fromString(guildId)), levels,
                        entity.getUsageTaxRate()))
                .orElseGet(() -> CommissionBoard.spawnBoard(boardId, levels));
    }

    private static void copyLevels(CommissionBoardLevels levels, CommissionBoardEntity entity) {
        entity.setBoardTier(levels.boardTier());
        entity.setMaxHarvestTier(levels.maxHarvestTier());
        entity.setMaxCraftTier(levels.maxCraftTier());
        entity.setHarvestSlots(levels.harvestSlots());
        entity.setCraftSlots(levels.craftSlots());
        entity.setMoneyBonusLevel(levels.moneyBonusLevel());
    }
}

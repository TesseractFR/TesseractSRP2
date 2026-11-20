package onl.tesseract.srp.commission.adapter.serverside.jpa;

import onl.tesseract.srp.commission.adapter.serverside.jpa.entity.CommissionBoardEntity;
import onl.tesseract.srp.commission.domain.model.board.BoardId;
import onl.tesseract.srp.commission.domain.model.board.BoardLevels;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.common.domain.model.Coordinate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Convertit les tableaux du domaine en entités JPA et inversement. */
@Component
public class CommissionBoardMapper {

    /** @return l'entité du tableau du spawn, sans identifiant, que la base complète à l'insertion */
    public CommissionBoardEntity toNewSpawnBoardEntity(BoardLevels levels) {
        return toNewEntity(levels, 0);
    }

    /** @return l'entité d'un tableau de guilde neuf, sans identifiant, que la base complète à l'insertion */
    public CommissionBoardEntity toNewGuildBoardEntity(GuildId guildId, double usageTaxRate) {
        CommissionBoardEntity entity = toNewEntity(BoardLevels.GUILD_START, usageTaxRate);
        entity.setGuildId(guildId.value().toString());
        return entity;
    }

    private static CommissionBoardEntity toNewEntity(BoardLevels levels, double usageTaxRate) {
        CommissionBoardEntity entity = new CommissionBoardEntity();
        copyLevels(levels, entity);
        entity.setUsageTaxRate(usageTaxRate);
        entity.setCompletedCommissionCount(0);
        return entity;
    }

    /** Recopie dans l'entité ce que le domaine peut modifier : niveaux, taxe et position du tableau. */
    public void copyChanges(CommissionBoard commissionBoard, CommissionBoardEntity entity) {
        copyLevels(commissionBoard.getLevels(), entity);
        entity.setUsageTaxRate(commissionBoard.getUsageTaxRate());
        commissionBoard.findFurniturePosition().ifPresent(position -> {
            entity.setPositionWorld(position.chunkCoord().world());
            entity.setPositionX(position.x().intValue());
            entity.setPositionY(position.y().intValue());
            entity.setPositionZ(position.z().intValue());
        });
    }

    public CommissionBoard toDomain(CommissionBoardEntity entity) {
        BoardId boardId = new BoardId(entity.getId());
        BoardLevels levels = new BoardLevels(entity.getMaxCraftTier(), entity.getMaxHarvestTier(), entity.getSlotsPerType(),
                entity.getMaxRequiredQuality(), entity.getMoneyBonusLevel());
        return Optional.ofNullable(entity.getGuildId())
                .map(guildId -> toGuildBoard(entity, boardId, new GuildId(UUID.fromString(guildId)), levels))
                .orElseGet(() -> CommissionBoard.spawnBoard(boardId, levels));
    }

    private static CommissionBoard toGuildBoard(CommissionBoardEntity entity, BoardId boardId, GuildId guildId, BoardLevels levels) {
        CommissionBoard guildBoard = CommissionBoard.guildBoard(boardId, guildId, levels, entity.getUsageTaxRate());
        findFurniturePosition(entity).ifPresent(guildBoard::placeFurnitureAt);
        return guildBoard;
    }

    private static void copyLevels(BoardLevels levels, CommissionBoardEntity entity) {
        entity.setMaxCraftTier(levels.maxCraftTier());
        entity.setMaxHarvestTier(levels.maxHarvestTier());
        entity.setSlotsPerType(levels.slotsPerType());
        entity.setMaxRequiredQuality(levels.maxRequiredQuality());
        entity.setMoneyBonusLevel(levels.moneyBonusLevel());
    }

    /** Les quatre colonnes de position sont remplies ensemble : le monde suffit à savoir si le tableau est posé. */
    private static Optional<Coordinate> findFurniturePosition(CommissionBoardEntity entity) {
        return Optional.ofNullable(entity.getPositionWorld()).map(world -> new Coordinate((double) entity.getPositionX(),
                (double) entity.getPositionY(), (double) entity.getPositionZ(),
                new ChunkCoord(entity.getPositionX() >> 4, entity.getPositionZ() >> 4, world)));
    }
}

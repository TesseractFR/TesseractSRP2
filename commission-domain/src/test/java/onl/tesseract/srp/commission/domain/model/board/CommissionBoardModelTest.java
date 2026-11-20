package onl.tesseract.srp.commission.domain.model.board;

import onl.tesseract.srp.commission.domain.model.exception.InvalidUsageTaxRateException;
import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommissionBoardModelTest {

    private static final BoardId BOARD_ID = new BoardId(1);
    private static final GuildId GUILD_ID = new GuildId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final double USAGE_TAX_RATE = 0.25;

    private static CommissionBoard guildBoard() {
        return CommissionBoard.guildBoard(BOARD_ID, GUILD_ID, BoardLevels.GUILD_START, USAGE_TAX_RATE);
    }

    @Test
    void upgrade_raisesOnlyTheChosenLevel() {
        // when
        BoardLevels upgradedLevels = BoardLevels.GUILD_START.upgrade(UpgradeType.MAX_REQUIRED_QUALITY);

        // then
        assertEquals(Quality.NORMAL, upgradedLevels.maxRequiredQuality());
        assertEquals(BoardLevels.GUILD_START.slotsPerType(), upgradedLevels.slotsPerType());
    }

    @Test
    void changeUsageTax_throwsAndKeepsTheRateAboveTheMaximum() {
        // given
        CommissionBoard commissionBoard = guildBoard();

        // when / then
        assertThrows(InvalidUsageTaxRateException.class, () -> commissionBoard.changeUsageTax(0.55));
        assertEquals(USAGE_TAX_RATE, commissionBoard.getUsageTaxRate());
    }

    @Test
    void computeUsageTaxOn_roundsDownSoThatThePlayerIsNeverOvertaxed() {
        // given : 25 % de 10 lys donne 2,5 lys
        CommissionBoard commissionBoard = guildBoard();

        // when
        int usageTax = commissionBoard.computeUsageTaxOn(10);

        // then
        assertEquals(2, usageTax);
    }

    @Test
    void findAllParts_givesOnlyThePublicPartOnTheSpawnBoard() {
        // given
        CommissionBoard spawnBoard = CommissionBoard.spawnBoard(BOARD_ID, BoardLevels.spawn(6));

        // when / then
        assertEquals(Set.of(BoardPart.PUBLIC), Set.copyOf(spawnBoard.findAllParts()));
        assertEquals(Set.of(BoardPart.PUBLIC, BoardPart.GUILD), Set.copyOf(guildBoard().findAllParts()));
    }

    @Test
    void findAllOccupiedChunks_includesTheNeighbourChunkWhenTheBoardCrossesABorder() {
        // given : bloc central en x = 15, le tableau déborde sur x = 16, dans le chunk suivant
        BoardSpot boardSpot = new BoardSpot(new Coordinate(15.0, 64.0, 3.0, new ChunkCoord(0, 0, "world")), BoardFacing.NORTH);

        // when
        Set<ChunkCoord> occupiedChunks = boardSpot.findAllOccupiedChunks();

        // then
        assertEquals(Set.of(new ChunkCoord(0, 0, "world"), new ChunkCoord(1, 0, "world")), occupiedChunks);
    }
}

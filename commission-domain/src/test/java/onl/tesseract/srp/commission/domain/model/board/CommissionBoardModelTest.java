package onl.tesseract.srp.commission.domain.model.board;

import onl.tesseract.srp.commission.domain.model.exception.InvalidUsageTaxRateException;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommissionBoardModelTest {

    private static final CommissionBoardId BOARD_ID = new CommissionBoardId(1);
    private static final GuildId GUILD_ID = new GuildId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final UsageTaxRate USAGE_TAX_RATE = new UsageTaxRate(25);

    private static CommissionBoard guildBoard() {
        return CommissionBoard.guildBoard(BOARD_ID, GUILD_ID, CommissionBoardLevels.GUILD_BOARD_START, USAGE_TAX_RATE);
    }

    @Test
    void upgrade_raisesOnlyTheChosenLevel() {
        // when
        CommissionBoardLevels upgradedLevels = CommissionBoardLevels.GUILD_BOARD_START.withUpgrade(CommissionBoardUpgradeType.CRAFT_SLOTS);

        // then
        assertEquals(2, upgradedLevels.craftSlots());
        assertEquals(CommissionBoardLevels.GUILD_BOARD_START.harvestSlots(), upgradedLevels.harvestSlots());
        assertEquals(CommissionBoardLevels.GUILD_BOARD_START.boardTier(), upgradedLevels.boardTier());
    }

    @Test
    void usageTaxRate_throwsAboveTheMaximum() {
        // when / then
        assertThrows(InvalidUsageTaxRateException.class, () -> new UsageTaxRate(UsageTaxRate.MAX_PERCENT + 1));
    }

    @Test
    void changeUsageTax_throwsAndKeepsTheRateOnTheSpawnBoard() {
        // given
        CommissionBoard spawnBoard = CommissionBoard.spawnBoard(BOARD_ID, 6);
        UsageTaxRate newUsageTaxRate = new UsageTaxRate(10);

        // when / then
        assertThrows(IllegalStateException.class, () -> spawnBoard.changeUsageTax(newUsageTaxRate));
        assertEquals(UsageTaxRate.ZERO, spawnBoard.getUsageTaxRate());
    }

    @Test
    void upgrade_throwsAndKeepsTheLevelsOnTheSpawnBoard() {
        // given
        CommissionBoard spawnBoard = CommissionBoard.spawnBoard(BOARD_ID, 6);

        // when / then
        assertThrows(IllegalStateException.class, () -> spawnBoard.upgrade(CommissionBoardUpgradeType.HARVEST_SLOTS));
        assertEquals(CommissionBoardLevels.forSpawnBoard(6), spawnBoard.getLevels());
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
        CommissionBoard spawnBoard = CommissionBoard.spawnBoard(BOARD_ID, 6);

        // when / then
        assertEquals(Set.of(CommissionBoardPart.PUBLIC), Set.copyOf(spawnBoard.findAllParts()));
        assertEquals(Set.of(CommissionBoardPart.PUBLIC, CommissionBoardPart.GUILD), Set.copyOf(guildBoard().findAllParts()));
    }

    @Test
    void changeSpawnSlots_throwsAndKeepsTheLevelsOnAGuildBoard() {
        // given
        CommissionBoard commissionBoard = guildBoard();

        // when / then
        assertThrows(IllegalStateException.class, () -> commissionBoard.changeSpawnSlots(4));
        assertEquals(CommissionBoardLevels.GUILD_BOARD_START, commissionBoard.getLevels());
    }
}

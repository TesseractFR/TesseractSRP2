package onl.tesseract.srp.commission.domain.port.userside;

import onl.tesseract.srp.commission.domain.model.board.UsageTaxRate;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardLevels;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardPart;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.port.serverside.GuildMembershipPort;
import onl.tesseract.srp.common.domain.model.PlayerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommissionBoardAccessServiceTest {

    private static final GuildId GUILD_ID = new GuildId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final PlayerId MEMBER = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000010"));
    private static final PlayerId EXTERNAL = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000020"));
    private static final CommissionBoard GUILD_BOARD = CommissionBoard.guildBoard(new CommissionBoardId(1), GUILD_ID,
            CommissionBoardLevels.GUILD_BOARD_START, new UsageTaxRate(10));
    private static final CommissionBoard SPAWN_BOARD = CommissionBoard.spawnBoard(new CommissionBoardId(2), 4);

    private GuildMembershipPort guildMembershipPort;
    private CommissionBoardAccessService commissionBoardAccessService;

    @BeforeEach
    void setUp() {
        guildMembershipPort = mock(GuildMembershipPort.class);
        commissionBoardAccessService = new CommissionBoardAccessService(guildMembershipPort);
    }

    @Test
    void canAccessPart_opensTheGuildPartToMembersOnly() {
        // given
        when(guildMembershipPort.isMember(GUILD_ID, MEMBER)).thenReturn(true);

        // when / then
        assertTrue(commissionBoardAccessService.canAccessPart(MEMBER, GUILD_BOARD, CommissionBoardPart.GUILD));
        assertFalse(commissionBoardAccessService.canAccessPart(EXTERNAL, GUILD_BOARD, CommissionBoardPart.GUILD));
        assertTrue(commissionBoardAccessService.canAccessPart(EXTERNAL, GUILD_BOARD, CommissionBoardPart.PUBLIC));
    }

    @Test
    void findAllGuildMembers_listsTheMembersOfTheOwnerGuildAndNobodyAtTheSpawn() {
        // given
        when(guildMembershipPort.findAllMembers(GUILD_ID)).thenReturn(List.of(MEMBER));

        // when / then
        assertEquals(List.of(MEMBER), commissionBoardAccessService.findAllGuildMembers(GUILD_BOARD));
        assertEquals(List.of(), commissionBoardAccessService.findAllGuildMembers(SPAWN_BOARD));
    }
}

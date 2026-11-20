package onl.tesseract.srp.commission.domain.port.userside;

import onl.tesseract.srp.commission.domain.model.board.BoardId;
import onl.tesseract.srp.commission.domain.model.board.BoardLevels;
import onl.tesseract.srp.commission.domain.model.board.BoardPart;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.model.commission.CatalogItem;
import onl.tesseract.srp.commission.domain.model.commission.Commission;
import onl.tesseract.srp.commission.domain.model.commission.CommissionDraft;
import onl.tesseract.srp.commission.domain.model.commission.CommissionId;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionCatalogRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionRepository;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommissionBoardRefillServiceTest {

    private static final BoardId GUILD_BOARD_ID = new BoardId(1);
    private static final BoardId SPAWN_BOARD_ID = new BoardId(2);
    private static final GuildId GUILD_ID = new GuildId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final CommissionId COMMISSION_ID = new CommissionId(100);
    private static final MaterialName WOOD = new MaterialName("BOIS");
    private static final MaterialName STONE = new MaterialName("PIERRE");
    private static final MaterialName TITANIUM = new MaterialName("TITANE");

    private static CommissionBoard guildBoard(BoardLevels levels) {
        return CommissionBoard.guildBoard(GUILD_BOARD_ID, GUILD_ID, levels, 0.25);
    }

    private static CommissionBoard spawnBoard() {
        return CommissionBoard.spawnBoard(SPAWN_BOARD_ID, BoardLevels.spawn(6));
    }

    private static Commission commission(BoardId boardId, int deliveredQuantity) {
        return new Commission(COMMISSION_ID, boardId, BoardPart.PUBLIC, CommissionType.HARVEST, WOOD, 10, deliveredQuantity,
                Quality.NORMAL, MoneyValue.of(100));
    }

    private CommissionRepository commissionRepository;
    private CommissionCatalogRepository commissionCatalogRepository;
    private RandomGenerator random;
    private CommissionBoardRefillService commissionBoardRefillService;

    @BeforeEach
    void setUp() {
        commissionRepository = mock(CommissionRepository.class);
        CommissionConfigRepository commissionConfigRepository = mock(CommissionConfigRepository.class);
        commissionCatalogRepository = mock(CommissionCatalogRepository.class);
        random = mock(RandomGenerator.class);
        commissionBoardRefillService = new CommissionBoardRefillService(commissionRepository, commissionConfigRepository,
                commissionCatalogRepository, random);

        when(commissionCatalogRepository.getUnitValue(any(), any())).thenReturn(MoneyValue.of(2));
        when(commissionConfigRepository.getSpawnRewardRate()).thenReturn(0.5);
        when(random.nextDouble()).thenReturn(0.0);
        when(random.nextInt(anyInt())).thenReturn(0);
    }

    @SuppressWarnings("unchecked")
    private List<CommissionDraft> captureNewCommissions() {
        ArgumentCaptor<List<CommissionDraft>> newCommissions = ArgumentCaptor.forClass(List.class);
        verify(commissionRepository).replaceCommissions(any(), newCommissions.capture());
        return newCommissions.getValue();
    }

    @SuppressWarnings("unchecked")
    private List<CommissionId> captureRemovedCommissions() {
        ArgumentCaptor<List<CommissionId>> removedCommissions = ArgumentCaptor.forClass(List.class);
        verify(commissionRepository).replaceCommissions(removedCommissions.capture(), any());
        return removedCommissions.getValue();
    }

    @Test
    void refillBoard_fillsOneSlotPerTypeAndPartOnANewGuildBoard() {
        // given
        when(commissionCatalogRepository.findAllCatalogItems(any())).thenReturn(List.of(new CatalogItem(WOOD, 1, 1, 10, 0)));

        // when
        commissionBoardRefillService.refillBoard(guildBoard(BoardLevels.GUILD_START));

        // then : récolte et artisanat, sur la partie publique et sur la partie guilde
        List<CommissionDraft> newCommissions = captureNewCommissions();
        assertEquals(4, newCommissions.size());
        assertEquals(2, newCommissions.stream().filter(draft -> draft.part() == BoardPart.GUILD).count());
    }

    @Test
    void refillBoard_neverDrawsAnItemAboveTheTierOfTheBoard() {
        // given
        when(commissionCatalogRepository.findAllCatalogItems(any()))
                .thenReturn(List.of(new CatalogItem(TITANIUM, 2, 100, 10, 0), new CatalogItem(WOOD, 1, 1, 10, 0)));

        // when
        commissionBoardRefillService.refillBoard(guildBoard(BoardLevels.GUILD_START));

        // then
        assertTrue(captureNewCommissions().stream().allMatch(draft -> draft.material().equals(WOOD)));
    }

    @Test
    void refillBoard_drawsTheMinimalQualityUpToTheMaximumOfTheBoard() {
        // given
        when(commissionCatalogRepository.findAllCatalogItems(any())).thenReturn(List.of(new CatalogItem(WOOD, 1, 1, 10, 0)));
        when(random.nextInt(Quality.GOOD.ordinal() + 1)).thenReturn(Quality.GOOD.ordinal());

        // when
        commissionBoardRefillService.refillBoard(guildBoard(new BoardLevels(1, 1, 1, Quality.GOOD, 0)));

        // then
        assertTrue(captureNewCommissions().stream().allMatch(draft -> draft.minimalQuality() == Quality.GOOD));
    }

    @Test
    void refillBoard_appliesTheSpawnRewardRate() {
        // given : 10 items à 2 lys, payés à moitié au spawn
        when(commissionCatalogRepository.findAllCatalogItems(any())).thenReturn(List.of(new CatalogItem(WOOD, 1, 1, 10, 0)));

        // when
        commissionBoardRefillService.refillBoard(spawnBoard());

        // then
        assertTrue(captureNewCommissions().stream().allMatch(draft -> draft.reward().value().compareTo(BigDecimal.TEN) == 0));
    }

    @Test
    void refillBoard_replacesACompletedCommission() {
        // given : seule la liste de récolte publique a déjà une commission, terminée
        when(commissionRepository.findAllByBoard(GUILD_BOARD_ID)).thenReturn(List.of(commission(GUILD_BOARD_ID, 10)));
        when(commissionCatalogRepository.findAllCatalogItems(any())).thenReturn(List.of(new CatalogItem(STONE, 1, 1, 10, 0)));

        // when
        commissionBoardRefillService.refillBoard(guildBoard(BoardLevels.GUILD_START));

        // then
        assertEquals(List.of(COMMISSION_ID), captureRemovedCommissions());
    }

    @Test
    void refillBoard_neverReplacesACommissionInProgress() {
        // given : un slot par type ; la récolte publique a une commission en cours, l'artisanat aucune
        when(commissionRepository.findAllByBoard(SPAWN_BOARD_ID)).thenReturn(List.of(commission(SPAWN_BOARD_ID, 3)));
        when(commissionCatalogRepository.findAllCatalogItems(any())).thenReturn(List.of(new CatalogItem(STONE, 1, 1, 10, 0)));

        // when
        commissionBoardRefillService.refillBoard(CommissionBoard.spawnBoard(SPAWN_BOARD_ID, BoardLevels.spawn(1)));

        // then : seule la liste d'artisanat est complétée, rien n'est retiré
        assertEquals(List.of(), captureRemovedCommissions());
        assertTrue(captureNewCommissions().stream().allMatch(draft -> draft.type() == CommissionType.CRAFT));
    }

    @Test
    void refillBoard_changesNothingOnAFullBoard() {
        // given : un slot par type, chaque liste a déjà sa commission en cours
        Commission harvestCommission = commission(SPAWN_BOARD_ID, 3);
        Commission craftCommission = new Commission(new CommissionId(101), SPAWN_BOARD_ID, BoardPart.PUBLIC, CommissionType.CRAFT,
                STONE, 10, 0, Quality.NORMAL, MoneyValue.of(100));
        when(commissionRepository.findAllByBoard(SPAWN_BOARD_ID)).thenReturn(List.of(harvestCommission, craftCommission));

        // when
        commissionBoardRefillService.refillBoard(CommissionBoard.spawnBoard(SPAWN_BOARD_ID, BoardLevels.spawn(1)));

        // then
        verify(commissionRepository, never()).replaceCommissions(any(), any());
    }

    @Test
    void refillBoard_leavesASlotEmptyWhenNoItemIsAllowed() {
        // given : seul un item d'artisanat existe, la récolte n'a rien à proposer
        when(commissionCatalogRepository.findAllCatalogItems(CommissionType.CRAFT)).thenReturn(List.of(new CatalogItem(STONE, 1, 1, 10, 0)));
        when(commissionCatalogRepository.findAllCatalogItems(CommissionType.HARVEST)).thenReturn(List.of());

        // when
        commissionBoardRefillService.refillBoard(guildBoard(BoardLevels.GUILD_START));

        // then
        assertTrue(captureNewCommissions().stream().allMatch(draft -> draft.type() == CommissionType.CRAFT));
    }
    @Test
    void refillBoard_drawsTheQuantityAroundTheBaseOfTheItem() {
        // given : base 50, variance 50 % donne [25 ; 75]
        when(commissionCatalogRepository.findAllCatalogItems(any())).thenReturn(List.of(new CatalogItem(WOOD, 1, 1, 50, 50)));
        when(random.nextInt(25, 76)).thenReturn(75);

        // when
        commissionBoardRefillService.refillBoard(guildBoard(BoardLevels.GUILD_START));

        // then : la récompense suit la quantité tirée, à 2 lys l'unité
        assertTrue(captureNewCommissions().stream()
                .allMatch(draft -> draft.quantity() == 75 && draft.reward().value().compareTo(BigDecimal.valueOf(150)) == 0));
    }
}

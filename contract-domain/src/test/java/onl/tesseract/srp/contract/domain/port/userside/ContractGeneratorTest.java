package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.contract.domain.model.contract.CatalogItem;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;
import onl.tesseract.srp.contract.domain.model.contract.ContractDraft;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.port.serverside.ContractCatalogRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContractGeneratorTest {

    private static final Instant NOW = Instant.parse("2026-11-15T12:00:00Z");
    private static final CatalogItem WHEAT = new CatalogItem(ContractType.HARVEST, new ContractClient("farmer"),
            new MaterialName("wheat"), 1, 10);
    private static final CatalogItem SWORD = new CatalogItem(ContractType.CRAFT, new ContractClient("smith"),
            new MaterialName("sword"), 3, 2);

    private ContractConfigRepository contractConfigRepository;
    private ContractCatalogRepository contractCatalogRepository;
    private RandomGenerator random;
    private ContractGenerator contractGenerator;

    @BeforeEach
    void setUp() {
        contractConfigRepository = mock(ContractConfigRepository.class);
        contractCatalogRepository = mock(ContractCatalogRepository.class);
        random = mock(RandomGenerator.class);
        contractGenerator = new ContractGenerator(contractConfigRepository, contractCatalogRepository, random,
                Clock.fixed(NOW, ZoneOffset.UTC));
        when(contractConfigRepository.getScrollLifetime()).thenReturn(Duration.ofHours(72));
        when(contractCatalogRepository.getUnitValue(any(), any())).thenReturn(MoneyValue.of(5));
        when(random.nextInt(anyInt())).thenReturn(0);
    }

    @Test
    void generate_returnsEmptyWhenCatalogIsEmpty() {
        // given
        when(contractCatalogRepository.findAllCatalogItems()).thenReturn(List.of());

        // when / then
        assertTrue(contractGenerator.generate().isEmpty());
    }

    @Test
    void generate_picksFirstItemWhenDrawFallsInItsWeight() {
        // given : poids total 4, le premier élément couvre [0 ; 1[
        when(contractCatalogRepository.findAllCatalogItems()).thenReturn(List.of(WHEAT, SWORD));
        when(random.nextDouble()).thenReturn(0.2);

        // when
        ContractDraft draft = contractGenerator.generate().orElseThrow();

        // then
        assertEquals(WHEAT.material(), draft.material());
        assertEquals(WHEAT.client(), draft.client());
    }

    @Test
    void generate_picksSecondItemWhenDrawExceedsFirstWeight() {
        // given : 0,5 × 4 = 2, au-delà du poids 1 du premier élément
        when(contractCatalogRepository.findAllCatalogItems()).thenReturn(List.of(WHEAT, SWORD));
        when(random.nextDouble()).thenReturn(0.5);

        // when
        ContractDraft draft = contractGenerator.generate().orElseThrow();

        // then
        assertEquals(SWORD.material(), draft.material());
        assertEquals(ContractType.CRAFT, draft.type());
    }

    @Test
    void generate_computesRewardFromUnitValueAndQuantity() {
        // given
        when(contractCatalogRepository.findAllCatalogItems()).thenReturn(List.of(WHEAT));

        // when
        ContractDraft draft = contractGenerator.generate().orElseThrow();

        // then : 5 × 10
        assertEquals(0, BigDecimal.valueOf(50).compareTo(draft.reward().value()));
    }

    @Test
    void generate_rewardIsAtLeastOne() {
        // given
        when(contractCatalogRepository.findAllCatalogItems()).thenReturn(List.of(SWORD));
        when(contractCatalogRepository.getUnitValue(any(), any())).thenReturn(new MoneyValue(new BigDecimal("0.1")));

        // when
        ContractDraft draft = contractGenerator.generate().orElseThrow();

        // then : 0,1 × 2 arrondi à 0, relevé à 1
        assertEquals(0, BigDecimal.ONE.compareTo(draft.reward().value()));
    }

    @Test
    void generate_drawsMinimalQualityAtRandom() {
        // given
        when(contractCatalogRepository.findAllCatalogItems()).thenReturn(List.of(WHEAT));
        when(random.nextInt(Quality.values().length)).thenReturn(Quality.values().length - 1);

        // when
        ContractDraft draft = contractGenerator.generate().orElseThrow();

        // then
        assertEquals(Quality.values()[Quality.values().length - 1], draft.minimalQuality());
    }

    @Test
    void generate_setsExpirationFromScrollLifetime() {
        // given
        when(contractCatalogRepository.findAllCatalogItems()).thenReturn(List.of(WHEAT));

        // when
        ContractDraft draft = contractGenerator.generate().orElseThrow();

        // then
        assertEquals(NOW.plus(Duration.ofHours(72)), draft.expiresAt());
    }
}

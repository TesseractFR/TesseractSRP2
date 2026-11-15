package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;
import onl.tesseract.srp.contract.domain.model.contract.ContractId;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContractExpirationServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-15T12:00:00Z");
    private static final PlayerId PLAYER = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final ContractId CONTRACT_ID = new ContractId(42);
    private static final ContractClient FARMER = new ContractClient("farmer");
    private static final MaterialName WHEAT = new MaterialName("wheat");

    private static Contract contract(ContractType type, Instant expiresAt) {
        return new Contract(CONTRACT_ID, type, FARMER, WHEAT, 10, Quality.POOR, MoneyValue.of(100), expiresAt);
    }

    private static Contract validHarvestContract() {
        return contract(ContractType.HARVEST, NOW.plusSeconds(3600));
    }

    private ContractRepository contractRepository;
    private ContractExpirationService contractExpirationService;

    @BeforeEach
    void setUp() {
        contractRepository = mock(ContractRepository.class);
        contractExpirationService = new ContractExpirationService(contractRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void purgeExpired_deletesContractsExpiredAtCurrentInstant() {
        // given
        when(contractRepository.deleteExpired(NOW)).thenReturn(4);

        // when / then
        assertEquals(4, contractExpirationService.purgeExpired());
    }

    @Test
    void isScrollValid_trueWhenContractExistsAndIsNotExpired() {
        // given
        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(validHarvestContract()));

        // when / then
        assertTrue(contractExpirationService.isScrollValid(CONTRACT_ID));
    }

    @Test
    void isScrollValid_falseWhenContractIsExpired() {
        // given
        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(contract(ContractType.HARVEST, NOW)));

        // when / then
        assertFalse(contractExpirationService.isScrollValid(CONTRACT_ID));
    }

    @Test
    void isScrollValid_falseWhenContractNoLongerExists() {
        // given
        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.empty());

        // when / then
        assertFalse(contractExpirationService.isScrollValid(CONTRACT_ID));
    }
}

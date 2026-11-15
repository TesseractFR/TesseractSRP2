package onl.tesseract.srp.contract.domain.model.contract;

import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContractModelTest {

    private static final Instant EXPIRES_AT = Instant.parse("2026-11-15T12:00:00Z");
    private static final ContractClient FARMER = new ContractClient("farmer");

    private static Contract contract() {
        return new Contract(new ContractId(1), ContractType.HARVEST, FARMER, new MaterialName("wheat"), 10,
                Quality.POOR, MoneyValue.of(50), EXPIRES_AT);
    }

    @Test
    void isExpiredAt_falseBeforeExpiration() {
        assertFalse(contract().isExpiredAt(EXPIRES_AT.minusSeconds(1)));
    }

    @Test
    void isExpiredAt_trueAtExpirationInstant() {
        assertTrue(contract().isExpiredAt(EXPIRES_AT));
    }

    @Test
    void isExpiredAt_trueAfterExpiration() {
        assertTrue(contract().isExpiredAt(EXPIRES_AT.plusSeconds(1)));
    }

    @Test
    void isFor_trueForItsClient() {
        assertTrue(contract().isFor(new ContractClient("farmer")));
    }

    @Test
    void isFor_falseForAnotherClient() {
        assertFalse(contract().isFor(new ContractClient("miner")));
    }

    @Test
    void contract_throwsWhenQuantityIsBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> new Contract(new ContractId(1), ContractType.HARVEST,
                FARMER, new MaterialName("wheat"), 0, Quality.POOR, MoneyValue.of(50), EXPIRES_AT));
    }

    @Test
    void contractDraft_throwsWhenQuantityIsBelowOne() {
        assertThrows(IllegalArgumentException.class, () -> new ContractDraft(ContractType.HARVEST, FARMER,
                new MaterialName("wheat"), 0, Quality.POOR, MoneyValue.of(50), EXPIRES_AT));
    }

    @Test
    void contractId_throwsWhenNotStrictlyPositive() {
        assertThrows(IllegalArgumentException.class, () -> new ContractId(0));
    }

    @Test
    void contractClient_throwsWhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new ContractClient(" "));
    }

    @Test
    void catalogItem_throwsWhenWeightIsNotStrictlyPositive() {
        assertThrows(IllegalArgumentException.class,
                () -> new CatalogItem(ContractType.HARVEST, FARMER, new MaterialName("wheat"), 0, 10));
    }

    @Test
    void catalogItem_throwsWhenQuantityIsBelowOne() {
        assertThrows(IllegalArgumentException.class,
                () -> new CatalogItem(ContractType.HARVEST, FARMER, new MaterialName("wheat"), 1, 0));
    }
}

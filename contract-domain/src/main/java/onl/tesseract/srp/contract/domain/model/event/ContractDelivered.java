package onl.tesseract.srp.contract.domain.model.event;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;

public record ContractDelivered(PlayerID deliverer, PlayerID issuedTo, long contractId, ContractType type,
                                String material, int quantity, int reward) {
}

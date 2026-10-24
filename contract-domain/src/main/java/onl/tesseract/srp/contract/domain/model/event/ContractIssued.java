package onl.tesseract.srp.contract.domain.model.event;

import onl.tesseract.srp.contract.domain.model.PlayerID;

public record ContractIssued(PlayerID buyer, long contractId) {
}

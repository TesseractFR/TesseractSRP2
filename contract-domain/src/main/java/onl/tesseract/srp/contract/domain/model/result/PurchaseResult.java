package onl.tesseract.srp.contract.domain.model.result;

import onl.tesseract.srp.contract.domain.model.contract.Contract;

public record PurchaseResult(PurchaseStatus status, Contract contract) {

    public static PurchaseResult ok(Contract contract) {
        return new PurchaseResult(PurchaseStatus.OK, contract);
    }

    public static PurchaseResult failed(PurchaseStatus status) {
        return new PurchaseResult(status, null);
    }

    public boolean isOk() {
        return status == PurchaseStatus.OK;
    }
}
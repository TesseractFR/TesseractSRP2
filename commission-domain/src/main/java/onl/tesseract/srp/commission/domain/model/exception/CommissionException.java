package onl.tesseract.srp.commission.domain.model.exception;

/**
 * Racine des refus métier du module commission. La hiérarchie est scellée pour que les adapters
 * qui traduisent ces exceptions en message soient forcés par le compilateur de traiter chaque cas.
 */
public abstract sealed class CommissionException extends RuntimeException
        permits CommissionBoardManagementNotAllowedException,
        CommissionBoardPartAccessDeniedException,
        CommissionBoardTierTooLowException,
        CommissionBoardNotFoundException,
        CommissionNotAvailableException,
        InvalidUsageTaxRateException,
        ItemQualityTooLowException,
        NoItemToDeliverException,
        NotEnoughBankMoneyException,
        NotEnoughDeliveredItemsException,
        CommissionBoardUpgradeMaxLevelReachedException {

    protected CommissionException(String message) {
        super(message);
    }
}

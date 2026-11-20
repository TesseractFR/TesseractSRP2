package onl.tesseract.srp.commission.domain.model.exception;

/**
 * Racine des refus métier du module commission. La hiérarchie est scellée pour que les adapters
 * qui traduisent ces exceptions en message soient forcés par le compilateur de traiter chaque cas.
 */
public abstract sealed class CommissionException extends RuntimeException
        permits BoardFurnitureUnavailableException,
        BoardManagementNotAllowedException,
        BoardOutsideTerritoryException,
        BoardPartAccessDeniedException,
        BoardPlacementNotStartedException,
        BoardSpotNotFreeException,
        CommissionBoardNotFoundException,
        CommissionNotAvailableException,
        GuildLevelTooLowException,
        InvalidUsageTaxRateException,
        ItemQualityTooLowException,
        NoItemToDeliverException,
        NotEnoughBankMoneyException,
        UpgradeMaxLevelReachedException {

    protected CommissionException(String message) {
        super(message);
    }
}

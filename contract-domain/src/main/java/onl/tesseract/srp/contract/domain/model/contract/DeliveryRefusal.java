package onl.tesseract.srp.contract.domain.model.contract;

/** Motifs pour lesquels la livraison d'un contrat peut être refusée. */
public enum DeliveryRefusal {
    /** Le contrat vise un autre métier ou destinataire. */
    WRONG_CLIENT,
    /** Le parchemin présenté ne correspond pas à un contrat valide. */
    INVALID_SCROLL,
    /** Le contrat a dépassé sa date d'expiration. */
    EXPIRED,
    /** Le joueur ne possède pas la quantité de matériaux demandée. */
    MISSING_ITEMS,
    /** Les matériaux possédés n'atteignent pas la qualité minimale requise. */
    NOT_ENOUGH_QUALITY,
    /** Le versement de la récompense a échoué. */
    PAYMENT_FAILED
}

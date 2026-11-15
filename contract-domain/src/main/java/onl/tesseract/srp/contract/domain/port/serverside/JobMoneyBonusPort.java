package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;

/** Port d'accès au bonus d'argent qu'un joueur tire de son métier. */
public interface JobMoneyBonusPort {
    /**
     * @return taux de bonus (0,05 pour 5 %) du joueur pour le métier et le matériau, zéro s'il n'en a pas
     */
    double getMoneyBonusRate(PlayerId player, ContractClient job, MaterialName material);
}

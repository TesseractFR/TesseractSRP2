package onl.tesseract.srp.commission.domain.port.userside;

import onl.tesseract.srp.commission.domain.model.board.BoardId;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.model.board.UpgradeStep;
import onl.tesseract.srp.commission.domain.model.board.UpgradeType;
import onl.tesseract.srp.commission.domain.model.exception.BoardManagementNotAllowedException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardNotFoundException;
import onl.tesseract.srp.commission.domain.model.exception.GuildLevelTooLowException;
import onl.tesseract.srp.commission.domain.model.exception.InvalidUsageTaxRateException;
import onl.tesseract.srp.commission.domain.model.exception.NotEnoughBankMoneyException;
import onl.tesseract.srp.commission.domain.model.exception.UpgradeMaxLevelReachedException;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionBoardRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.commission.domain.port.serverside.GuildProgressPort;
import onl.tesseract.srp.common.domain.model.PlayerId;

import java.util.Optional;

/** Gère un tableau de guilde : ses améliorations, payées par la banque, et sa taxe d'usage. */
public class CommissionBoardManagementService {
    private final CommissionBoardRepository commissionBoardRepository;
    private final CommissionConfigRepository commissionConfigRepository;
    private final GuildProgressPort guildProgressPort;
    private final CommissionBoardService commissionBoardService;
    private final CommissionBoardRefillService commissionBoardRefillService;

    public CommissionBoardManagementService(CommissionBoardRepository commissionBoardRepository,
                                            CommissionConfigRepository commissionConfigRepository,
                                            GuildProgressPort guildProgressPort, CommissionBoardService commissionBoardService,
                                            CommissionBoardRefillService commissionBoardRefillService) {
        this.commissionBoardRepository = commissionBoardRepository;
        this.commissionConfigRepository = commissionConfigRepository;
        this.guildProgressPort = guildProgressPort;
        this.commissionBoardService = commissionBoardService;
        this.commissionBoardRefillService = commissionBoardRefillService;
    }

    /** @return le prochain palier de l'amélioration, ou {@link Optional#empty()} si elle est au niveau maximal */
    public Optional<UpgradeStep> findNextUpgradeStep(CommissionBoard commissionBoard, UpgradeType upgradeType) {
        return commissionConfigRepository.findUpgradeStep(upgradeType, commissionBoard.getLevels().getLevel(upgradeType) + 1);
    }

    /** @return taux du bonus d'argent à ce niveau d'amélioration */
    public double getMoneyBonusRate(int moneyBonusLevel) {
        return commissionConfigRepository.getMoneyBonusRate(moneyBonusLevel);
    }

    /** @return niveau actuel de la guilde */
    public int getGuildLevel(GuildId guildId) {
        return guildProgressPort.getGuildLevel(guildId);
    }

    /** @return solde de la banque de la guilde, en lys */
    public int getBankBalance(GuildId guildId) {
        return guildProgressPort.getBankBalance(guildId);
    }

    /**
     * Monte l'amélioration d'un palier, payé par la banque de la guilde. Débloquer un slot le remplit
     * aussitôt ; les autres améliorations ne s'appliquent qu'aux commissions générées ensuite.
     *
     * @return le tableau amélioré
     * @throws CommissionBoardNotFoundException si le tableau n'existe pas
     * @throws BoardManagementNotAllowedException si le joueur ne peut pas gérer le tableau
     * @throws UpgradeMaxLevelReachedException si l'amélioration est au niveau maximal
     * @throws GuildLevelTooLowException si la guilde n'a pas le niveau du palier
     * @throws NotEnoughBankMoneyException si la banque ne peut pas payer le palier
     */
    public CommissionBoard upgradeBoard(PlayerId player, BoardId boardId, UpgradeType upgradeType) {
        CommissionBoard commissionBoard = commissionBoardService.getBoard(boardId);
        GuildId guildId = getManagedGuildId(player, commissionBoard);
        UpgradeStep nextStep = findNextUpgradeStep(commissionBoard, upgradeType)
                .orElseThrow(() -> new UpgradeMaxLevelReachedException(upgradeType));
        int guildLevel = getGuildLevel(guildId);
        if (guildLevel < nextStep.guildLevel()) {
            throw new GuildLevelTooLowException(guildId, nextStep.guildLevel(), guildLevel);
        }
        if (!guildProgressPort.withdrawFromBank(guildId, nextStep.cost())) {
            throw new NotEnoughBankMoneyException(guildId, nextStep.cost());
        }
        commissionBoard.upgrade(upgradeType);
        commissionBoardRepository.save(commissionBoard);
        commissionBoardRefillService.refillBoard(commissionBoard);
        return commissionBoard;
    }

    /**
     * Change le taux de taxe d'usage, gratuitement. Le nouveau taux s'applique aux livraisons suivantes.
     *
     * @throws CommissionBoardNotFoundException si le tableau n'existe pas
     * @throws BoardManagementNotAllowedException si le joueur ne peut pas gérer le tableau
     * @throws InvalidUsageTaxRateException si le taux sort de l'intervalle autorisé
     */
    public void changeUsageTax(PlayerId player, BoardId boardId, double usageTaxRate) {
        CommissionBoard commissionBoard = commissionBoardService.getBoard(boardId);
        getManagedGuildId(player, commissionBoard);
        commissionBoard.changeUsageTax(usageTaxRate);
        commissionBoardRepository.save(commissionBoard);
    }

    private GuildId getManagedGuildId(PlayerId player, CommissionBoard commissionBoard) {
        return commissionBoard.findGuildId()
                .filter(guildId -> commissionBoardService.canManageGuildBoard(player, guildId))
                .orElseThrow(() -> new BoardManagementNotAllowedException(player));
    }
}

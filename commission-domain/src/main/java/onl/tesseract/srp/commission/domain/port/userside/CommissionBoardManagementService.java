package onl.tesseract.srp.commission.domain.port.userside;

import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardDeliveryProgress;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeConditions;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeStep;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeType;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.model.board.UsageTaxRate;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardManagementNotAllowedException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardNotFoundException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardTierTooLowException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardUpgradeMaxLevelReachedException;
import onl.tesseract.srp.commission.domain.model.exception.InvalidUsageTaxRateException;
import onl.tesseract.srp.commission.domain.model.exception.NotEnoughBankMoneyException;
import onl.tesseract.srp.commission.domain.model.exception.NotEnoughDeliveredItemsException;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionBoardRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionRepository;
import onl.tesseract.srp.commission.domain.port.serverside.GuildBankPort;
import onl.tesseract.srp.commission.domain.port.serverside.GuildMembershipPort;
import onl.tesseract.srp.common.domain.model.PlayerId;

import java.util.Optional;

/** Gère un tableau de guilde : qui peut le gérer, ses améliorations payées par la banque, et sa taxe d'usage. */
public class CommissionBoardManagementService {
    private final CommissionBoardRepository commissionBoardRepository;
    private final CommissionConfigRepository commissionConfigRepository;
    private final CommissionRepository commissionRepository;
    private final GuildBankPort guildBankPort;
    private final GuildMembershipPort guildMembershipPort;
    private final CommissionBoardService commissionBoardService;
    private final CommissionBoardRefillService commissionBoardRefillService;

    public CommissionBoardManagementService(CommissionBoardRepository commissionBoardRepository,
                                            CommissionConfigRepository commissionConfigRepository,
                                            CommissionRepository commissionRepository,
                                            GuildBankPort guildBankPort, GuildMembershipPort guildMembershipPort,
                                            CommissionBoardService commissionBoardService,
                                            CommissionBoardRefillService commissionBoardRefillService) {
        this.commissionBoardRepository = commissionBoardRepository;
        this.commissionConfigRepository = commissionConfigRepository;
        this.commissionRepository = commissionRepository;
        this.guildBankPort = guildBankPort;
        this.guildMembershipPort = guildMembershipPort;
        this.commissionBoardService = commissionBoardService;
        this.commissionBoardRefillService = commissionBoardRefillService;
    }

    /** @return {@code true} si le joueur peut améliorer le tableau et changer sa taxe (Adjoint ou chef) ; toujours faux au spawn */
    public boolean canManageBoard(PlayerId player, CommissionBoard commissionBoard) {
        return findManagedGuildId(player, commissionBoard).isPresent();
    }

    /**
     * Évalue les conditions du prochain palier de l'amélioration : solde de la banque, tier du tableau et
     * livraisons faites. Les livraisons comptent sur des commissions de la qualité demandée ou d'une qualité supérieure.
     *
     * @return les conditions, ou {@link Optional#empty()} si l'amélioration est au niveau maximal
     */
    public Optional<CommissionBoardUpgradeConditions> findNextUpgradeConditions(CommissionBoard commissionBoard,
                                                                                CommissionBoardUpgradeType upgradeType) {
        return findNextUpgradeStep(commissionBoard, upgradeType).map(step -> evaluateConditions(commissionBoard, step));
    }

    /**
     * Les paliers achetables ne vont pas au-delà de ce que permet le tier du tableau : c'est ce que monter
     * de tier débloque.
     *
     * @return niveau le plus haut que l'amélioration peut atteindre au tier actuel du tableau, au moins son niveau actuel
     */
    public int getMaxLevelAtBoardTier(CommissionBoard commissionBoard, CommissionBoardUpgradeType upgradeType) {
        int boardTier = commissionBoard.getLevels().boardTier();
        int level = commissionBoard.getLevels().getLevel(upgradeType);
        while (isUnlockedAtBoardTier(upgradeType, level + 1, boardTier)) {
            level++;
        }
        return level;
    }

    /** @return taux du bonus d'argent à ce niveau d'amélioration */
    public double getMoneyBonusRate(int moneyBonusLevel) {
        return commissionConfigRepository.getMoneyBonusRate(moneyBonusLevel);
    }

    /** @return solde de la banque de la guilde, en lys */
    public int getBankBalance(GuildId guildId) {
        return guildBankPort.getBankBalance(guildId);
    }

    /**
     * Monte l'amélioration d'un palier, payé par la banque de la guilde. Débloquer un slot le remplit
     * aussitôt ; les autres améliorations ne s'appliquent qu'aux commissions générées ensuite. La banque est
     * débitée avant l'enregistrement du tableau, hors de toute transaction commune : si l'enregistrement
     * échoue, la guilde a payé sans être améliorée.
     *
     * @throws CommissionBoardNotFoundException si le tableau n'existe pas
     * @throws CommissionBoardManagementNotAllowedException si le joueur ne peut pas gérer le tableau
     * @throws CommissionBoardUpgradeMaxLevelReachedException si l'amélioration est au niveau maximal
     * @throws CommissionBoardTierTooLowException si le tableau n'a pas le tier du palier
     * @throws NotEnoughDeliveredItemsException si le tableau n'a pas reçu les livraisons demandées par le palier
     * @throws NotEnoughBankMoneyException si la banque ne peut pas payer le palier
     */
    public void upgradeBoard(PlayerId player, CommissionBoardId boardId, CommissionBoardUpgradeType upgradeType) {
        CommissionBoard commissionBoard = commissionBoardService.getBoard(boardId);
        GuildId guildId = getManagedGuildId(player, commissionBoard);
        CommissionBoardUpgradeConditions conditions = findNextUpgradeConditions(commissionBoard, upgradeType)
                .orElseThrow(() -> new CommissionBoardUpgradeMaxLevelReachedException(boardId, upgradeType));
        CommissionBoardUpgradeStep step = conditions.step();
        if (!conditions.hasBoardTier()) {
            throw new CommissionBoardTierTooLowException(boardId, step.requiredBoardTier(), commissionBoard.getLevels().boardTier());
        }
        for (CommissionBoardDeliveryProgress progress : conditions.deliveryProgress()) {
            if (!progress.isMet()) {
                throw new NotEnoughDeliveredItemsException(boardId, progress.requirement(), progress.deliveredItemCount());
            }
        }
        if (!guildBankPort.withdrawFromBank(guildId, step.cost())) {
            throw new NotEnoughBankMoneyException(guildId, step.cost());
        }
        commissionBoard.upgrade(upgradeType);
        commissionBoardRepository.save(commissionBoard);
        commissionBoardRefillService.refillBoard(commissionBoard);
    }

    /**
     * Change le taux de taxe d'usage, gratuitement. Le nouveau taux s'applique aux livraisons suivantes.
     *
     * @param usageTaxPercent nouveau taux, en pourcentage
     * @throws CommissionBoardNotFoundException si le tableau n'existe pas
     * @throws CommissionBoardManagementNotAllowedException si le joueur ne peut pas gérer le tableau
     * @throws InvalidUsageTaxRateException si le taux sort de l'intervalle autorisé
     */
    public void changeUsageTax(PlayerId player, CommissionBoardId boardId, int usageTaxPercent) {
        CommissionBoard commissionBoard = commissionBoardService.getBoard(boardId);
        if (!canManageBoard(player, commissionBoard)) {
            throw new CommissionBoardManagementNotAllowedException(player, boardId);
        }
        commissionBoard.changeUsageTax(new UsageTaxRate(usageTaxPercent));
        commissionBoardRepository.save(commissionBoard);
    }

    private Optional<CommissionBoardUpgradeStep> findNextUpgradeStep(CommissionBoard commissionBoard,
                                                                     CommissionBoardUpgradeType upgradeType) {
        return commissionConfigRepository.findUpgradeStep(upgradeType, commissionBoard.getLevels().getLevel(upgradeType) + 1);
    }

    private CommissionBoardUpgradeConditions evaluateConditions(CommissionBoard commissionBoard, CommissionBoardUpgradeStep step) {
        boolean hasEnoughMoney = commissionBoard.findGuildId()
                .map(guildId -> guildBankPort.getBankBalance(guildId) >= step.cost())
                .orElse(false);
        boolean hasBoardTier = commissionBoard.getLevels().boardTier() >= step.requiredBoardTier();
        return new CommissionBoardUpgradeConditions(step, hasEnoughMoney, hasBoardTier, step.deliveryRequirements().stream()
                .map(requirement -> new CommissionBoardDeliveryProgress(requirement, commissionRepository.countDeliveredItemsAtLeast(
                        commissionBoard.getId(), requirement.type(), requirement.minimalQuality())))
                .toList());
    }

    private boolean isUnlockedAtBoardTier(CommissionBoardUpgradeType upgradeType, int upgradeLevel, int boardTier) {
        return commissionConfigRepository.findUpgradeStep(upgradeType, upgradeLevel)
                .filter(step -> step.requiredBoardTier() <= boardTier)
                .isPresent();
    }

    private Optional<GuildId> findManagedGuildId(PlayerId player, CommissionBoard commissionBoard) {
        return commissionBoard.findGuildId().filter(guildId -> guildMembershipPort.canManageBoard(guildId, player));
    }

    private GuildId getManagedGuildId(PlayerId player, CommissionBoard commissionBoard) {
        return findManagedGuildId(player, commissionBoard)
                .orElseThrow(() -> new CommissionBoardManagementNotAllowedException(player, commissionBoard.getId()));
    }
}

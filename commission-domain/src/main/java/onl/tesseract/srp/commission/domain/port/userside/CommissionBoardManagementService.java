package onl.tesseract.srp.commission.domain.port.userside;

import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardDeliveryRequirement;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeStep;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeType;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardManagementNotAllowedException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardTierTooLowException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardNotFoundException;
import onl.tesseract.srp.commission.domain.model.exception.InvalidUsageTaxRateException;
import onl.tesseract.srp.commission.domain.model.exception.NotEnoughBankMoneyException;
import onl.tesseract.srp.commission.domain.model.exception.NotEnoughDeliveredItemsException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardUpgradeMaxLevelReachedException;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionBoardRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionRepository;
import onl.tesseract.srp.commission.domain.port.serverside.GuildBankPort;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;

import java.util.Arrays;
import java.util.Optional;

/** Gère un tableau de guilde : ses améliorations, payées par la banque, et sa taxe d'usage. */
public class CommissionBoardManagementService {
    private final CommissionBoardRepository commissionBoardRepository;
    private final CommissionConfigRepository commissionConfigRepository;
    private final CommissionRepository commissionRepository;
    private final GuildBankPort guildBankPort;
    private final CommissionBoardService commissionBoardService;
    private final CommissionBoardRefillService commissionBoardRefillService;

    public CommissionBoardManagementService(CommissionBoardRepository commissionBoardRepository,
                                            CommissionConfigRepository commissionConfigRepository,
                                            CommissionRepository commissionRepository,
                                            GuildBankPort guildBankPort, CommissionBoardService commissionBoardService,
                                            CommissionBoardRefillService commissionBoardRefillService) {
        this.commissionBoardRepository = commissionBoardRepository;
        this.commissionConfigRepository = commissionConfigRepository;
        this.commissionRepository = commissionRepository;
        this.guildBankPort = guildBankPort;
        this.commissionBoardService = commissionBoardService;
        this.commissionBoardRefillService = commissionBoardRefillService;
    }

    /** @return le prochain palier de l'amélioration, ou {@link Optional#empty()} si elle est au niveau maximal */
    public Optional<CommissionBoardUpgradeStep> findNextUpgradeStep(CommissionBoard commissionBoard, CommissionBoardUpgradeType upgradeType) {
        return commissionConfigRepository.findUpgradeStep(upgradeType, commissionBoard.getLevels().getLevel(upgradeType) + 1);
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

    private boolean isUnlockedAtBoardTier(CommissionBoardUpgradeType upgradeType, int upgradeLevel, int boardTier) {
        return commissionConfigRepository.findUpgradeStep(upgradeType, upgradeLevel)
                .filter(step -> step.requiredBoardTier() <= boardTier)
                .isPresent();
    }

    /** @return taux du bonus d'argent à ce niveau d'amélioration */
    public double getMoneyBonusRate(int moneyBonusLevel) {
        return commissionConfigRepository.getMoneyBonusRate(moneyBonusLevel);
    }

    /** @return items livrés sur ce tableau pour cette condition, sur des commissions de sa qualité ou d'une qualité supérieure */
    public int countDeliveredItems(CommissionBoard commissionBoard, CommissionBoardDeliveryRequirement requirement) {
        return Arrays.stream(Quality.values())
                .filter(quality -> quality.isAtLeast(requirement.minimalQuality()))
                .mapToInt(quality -> commissionRepository.countDeliveredItems(commissionBoard.getId(), requirement.type(), quality))
                .sum();
    }

    /** @return solde de la banque de la guilde, en lys */
    public int getBankBalance(GuildId guildId) {
        return guildBankPort.getBankBalance(guildId);
    }

    /**
     * Monte l'amélioration d'un palier, payé par la banque de la guilde. Débloquer un slot le remplit
     * aussitôt ; les autres améliorations ne s'appliquent qu'aux commissions générées ensuite.
     *
     * @return le tableau amélioré
     * @throws CommissionBoardNotFoundException si le tableau n'existe pas
     * @throws CommissionBoardManagementNotAllowedException si le joueur ne peut pas gérer le tableau
     * @throws CommissionBoardUpgradeMaxLevelReachedException si l'amélioration est au niveau maximal
     * @throws CommissionBoardTierTooLowException si le tableau n'a pas le tier du palier
     * @throws NotEnoughDeliveredItemsException si le tableau n'a pas reçu les livraisons demandées par le palier
     * @throws NotEnoughBankMoneyException si la banque ne peut pas payer le palier
     */
    public CommissionBoard upgradeBoard(PlayerId player, CommissionBoardId boardId, CommissionBoardUpgradeType upgradeType) {
        CommissionBoard commissionBoard = commissionBoardService.getBoard(boardId);
        GuildId guildId = getManagedGuildId(player, commissionBoard);
        CommissionBoardUpgradeStep nextStep = findNextUpgradeStep(commissionBoard, upgradeType)
                .orElseThrow(() -> new CommissionBoardUpgradeMaxLevelReachedException(upgradeType));
        int boardTier = commissionBoard.getLevels().boardTier();
        if (boardTier < nextStep.requiredBoardTier()) {
            throw new CommissionBoardTierTooLowException(boardId, nextStep.requiredBoardTier(), boardTier);
        }
        for (CommissionBoardDeliveryRequirement requirement : nextStep.deliveryRequirements()) {
            int deliveredItemCount = countDeliveredItems(commissionBoard, requirement);
            if (deliveredItemCount < requirement.itemCount()) {
                throw new NotEnoughDeliveredItemsException(boardId, requirement, deliveredItemCount);
            }
        }
        if (!guildBankPort.withdrawFromBank(guildId, nextStep.cost())) {
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
     * @throws CommissionBoardManagementNotAllowedException si le joueur ne peut pas gérer le tableau
     * @throws InvalidUsageTaxRateException si le taux sort de l'intervalle autorisé
     */
    public void changeUsageTax(PlayerId player, CommissionBoardId boardId, double usageTaxRate) {
        CommissionBoard commissionBoard = commissionBoardService.getBoard(boardId);
        getManagedGuildId(player, commissionBoard);
        commissionBoard.changeUsageTax(usageTaxRate);
        commissionBoardRepository.save(commissionBoard);
    }

    private GuildId getManagedGuildId(PlayerId player, CommissionBoard commissionBoard) {
        return commissionBoard.findGuildId()
                .filter(guildId -> commissionBoardService.canManageBoard(player, commissionBoard))
                .orElseThrow(() -> new CommissionBoardManagementNotAllowedException(player));
    }
}

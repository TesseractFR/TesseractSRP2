package onl.tesseract.srp.commission.domain.port.userside;

import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.commission.Commission;
import onl.tesseract.srp.commission.domain.model.commission.CommissionDelivery;
import onl.tesseract.srp.commission.domain.model.commission.CommissionId;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardPartAccessDeniedException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionNotAvailableException;
import onl.tesseract.srp.commission.domain.model.exception.ItemQualityTooLowException;
import onl.tesseract.srp.commission.domain.model.exception.NoItemToDeliverException;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionRepository;
import onl.tesseract.srp.commission.domain.port.serverside.GuildIncomePort;
import onl.tesseract.srp.commission.domain.port.serverside.PlayerMaterialPort;
import onl.tesseract.srp.commission.domain.port.serverside.PlayerPaymentPort;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;

/** Fait livrer aux joueurs les items d'une commission, et les paie au fil des livraisons. */
public class CommissionDeliveryService {
    private final CommissionRepository commissionRepository;
    private final PlayerMaterialPort playerMaterialPort;
    private final PlayerPaymentPort playerPaymentPort;
    private final GuildIncomePort guildIncomePort;
    private final CommissionConfigRepository commissionConfigRepository;
    private final CommissionBoardService commissionBoardService;
    private final CommissionBoardAccessService commissionBoardAccessService;
    private final CommissionBoardRefillService commissionBoardRefillService;

    public CommissionDeliveryService(CommissionRepository commissionRepository, PlayerMaterialPort playerMaterialPort,
                                     PlayerPaymentPort playerPaymentPort, GuildIncomePort guildIncomePort,
                                     CommissionConfigRepository commissionConfigRepository, CommissionBoardService commissionBoardService,
                                     CommissionBoardAccessService commissionBoardAccessService,
                                     CommissionBoardRefillService commissionBoardRefillService) {
        this.commissionRepository = commissionRepository;
        this.playerMaterialPort = playerMaterialPort;
        this.playerPaymentPort = playerPaymentPort;
        this.guildIncomePort = guildIncomePort;
        this.commissionConfigRepository = commissionConfigRepository;
        this.commissionBoardService = commissionBoardService;
        this.commissionBoardAccessService = commissionBoardAccessService;
        this.commissionBoardRefillService = commissionBoardRefillService;
    }

    /**
     * Calcule ce que le joueur livrerait maintenant : au plus la quantité restante, et le gain qui va avec.
     * Un externe paie la taxe d'usage du tableau au taux en vigueur.
     *
     * @return la livraison, vide si le joueur n'a aucun item à la qualité demandée
     */
    public CommissionDelivery computeDelivery(PlayerId player, CommissionBoard commissionBoard, Commission commission) {
        int ownedQuantity = playerMaterialPort.countAtLeast(player, commission.material(), commission.minimalQuality());
        int itemCount = Math.min(ownedQuantity, commission.getRemainingQuantity());
        int itemsValue = commission.computeRewardShareFor(itemCount);
        boolean isGuildMember = commissionBoardAccessService.isGuildMember(player, commissionBoard);
        int guildTax = isGuildMember ? 0 : commissionBoard.computeUsageTaxOn(itemsValue);
        boolean completesCommission = itemCount > 0 && itemCount == commission.getRemainingQuantity();
        return new CommissionDelivery(commission.id(), commissionBoard.getId(), itemCount,
                itemsValue - guildTax, guildTax, computeGuildXp(commissionBoard, commission, itemCount), isGuildMember,
                completesCommission);
    }

    /**
     * Livre tout ce que le joueur peut livrer. La progression est enregistrée en premier : si la base
     * échoue, rien n'a été retiré ni versé. Les items sont retirés ensuite, de la plus basse qualité
     * admissible à la plus haute, puis le livreur et la guilde propriétaire sont payés. Une commission
     * terminée laisse aussitôt sa place à une nouvelle.
     *
     * @return la livraison effectuée
     * @throws CommissionNotAvailableException si la commission n'existe plus ou est déjà terminée
     * @throws CommissionBoardPartAccessDeniedException si le joueur n'a pas accès à la partie de la commission
     * @throws ItemQualityTooLowException si le joueur n'a l'item que sous la qualité minimale
     * @throws NoItemToDeliverException si le joueur n'a pas l'item
     */
    public CommissionDelivery deliver(PlayerId player, CommissionId commissionId) {
        Commission commission = commissionRepository.findById(commissionId)
                .filter(foundCommission -> !foundCommission.isCompleted())
                .orElseThrow(() -> new CommissionNotAvailableException(commissionId));
        CommissionBoard commissionBoard = commissionBoardService.getBoard(commission.boardId());
        if (!commissionBoardAccessService.canAccessPart(player, commissionBoard, commission.part())) {
            throw new CommissionBoardPartAccessDeniedException(player, commissionId);
        }
        CommissionDelivery delivery = computeDelivery(player, commissionBoard, commission);
        if (delivery.isEmpty()) {
            throw buildNothingToDeliverException(player, commission);
        }

        commissionRepository.recordDelivery(delivery);
        playerMaterialPort.removeAtLeast(player, commission.material(), commission.minimalQuality(), delivery.itemCount());
        playerPaymentPort.pay(player, delivery.playerEarnings());
        commissionBoard.findGuildId()
                .ifPresent(guildId -> guildIncomePort.addCommissionIncome(guildId, delivery.guildTax(), delivery.guildXp()));
        if (delivery.completesCommission()) {
            commissionBoardRefillService.refillBoard(commissionBoard);
        }
        return delivery;
    }

    /**
     * L'XP d'une commission vaut sa récompense multipliée par un taux de configuration ; le spawn n'en donne pas.
     *
     * @return XP que la commission donne à la guilde une fois terminée
     */
    public int computeTotalGuildXp(CommissionBoard commissionBoard, Commission commission) {
        if (commissionBoard.isSpawnBoard()) {
            return 0;
        }
        return (int) Math.round(commission.reward().value().doubleValue() * commissionConfigRepository.getGuildXpPerLys());
    }

    private int computeGuildXp(CommissionBoard commissionBoard, Commission commission, int itemCount) {
        return commission.computeProgressShareOf(computeTotalGuildXp(commissionBoard, commission), itemCount);
    }

    private CommissionException buildNothingToDeliverException(PlayerId player, Commission commission) {
        boolean ownsLowerQuality = playerMaterialPort.countAtLeast(player, commission.material(), Quality.POOR) > 0;
        return ownsLowerQuality
                ? new ItemQualityTooLowException(player, commission.id(), commission.minimalQuality())
                : new NoItemToDeliverException(player, commission.id());
    }
}

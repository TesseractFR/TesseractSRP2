package onl.tesseract.srp.commission.adapter.serverside.jpa;

import onl.tesseract.srp.commission.adapter.serverside.jpa.entity.CommissionBoardEntity;
import onl.tesseract.srp.commission.adapter.serverside.jpa.entity.CommissionEntity;
import onl.tesseract.srp.commission.adapter.serverside.jpa.entity.CommissionDeliveredItemCountEntity;
import onl.tesseract.srp.commission.adapter.serverside.jpa.repository.CommissionBoardJpaRepository;
import onl.tesseract.srp.commission.adapter.serverside.jpa.repository.CommissionJpaRepository;
import onl.tesseract.srp.commission.adapter.serverside.jpa.repository.CommissionDeliveredItemCountJpaRepository;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;
import onl.tesseract.srp.commission.domain.model.commission.Commission;
import onl.tesseract.srp.commission.domain.model.commission.CommissionDraft;
import onl.tesseract.srp.commission.domain.model.commission.CommissionId;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.commission.domain.model.commission.CommissionDelivery;
import onl.tesseract.srp.commission.domain.model.exception.CommissionNotAvailableException;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionRepository;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.port.userside.PlayerService;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persiste les commissions dans la table {@code t_commissions} et les items livrés dans
 * {@code t_commission_delivered_items}. Une livraison crédite aussi le joueur
 * et la guilde dans la même transaction, d'où les services de joueur et de guilde.
 */
@Component
public class JpaCommissionRepository implements CommissionRepository {

    private final CommissionJpaRepository commissionJpaRepository;
    private final CommissionBoardJpaRepository commissionBoardJpaRepository;
    private final CommissionDeliveredItemCountJpaRepository commissionDeliveredItemCountJpaRepository;
    private final CommissionMapper commissionMapper;
    private final PlayerService playerService;
    private final GuildService guildService;

    public JpaCommissionRepository(CommissionJpaRepository commissionJpaRepository,
                                   CommissionBoardJpaRepository commissionBoardJpaRepository,
                                   CommissionDeliveredItemCountJpaRepository commissionDeliveredItemCountJpaRepository,
                                   CommissionMapper commissionMapper,
                                   PlayerService playerService, GuildService guildService) {
        this.commissionJpaRepository = commissionJpaRepository;
        this.commissionBoardJpaRepository = commissionBoardJpaRepository;
        this.commissionDeliveredItemCountJpaRepository = commissionDeliveredItemCountJpaRepository;
        this.commissionMapper = commissionMapper;
        this.playerService = playerService;
        this.guildService = guildService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Commission> findById(CommissionId commissionId) {
        return commissionJpaRepository.findById(commissionId.value()).map(commissionMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Commission> findAllByBoard(CommissionBoardId boardId) {
        return commissionJpaRepository.findByBoardIdOrderById(boardId.value()).stream()
                .map(commissionMapper::toDomain)
                .toList();
    }

    /**
     * Un gain n'est jamais négatif, donc {@link PlayerService#adjustMoney} ne le refuse pas. Une panne de
     * base annule toute la transaction, progression comprise. Les items livrés sont comptés à la qualité
     * demandée par la commission.
     *
     * @throws CommissionNotAvailableException si la commission n'existe plus
     */
    @Override
    @Transactional
    public void recordDelivery(CommissionDelivery delivery) {
        CommissionEntity commission = commissionJpaRepository.findById(delivery.commissionId().value())
                .orElseThrow(() -> new CommissionNotAvailableException(delivery.commissionId()));
        addDeliveredItems(commission, delivery.itemCount());
        commissionJpaRepository.addDeliveredQuantity(commission.getId(), delivery.itemCount());
        playerService.adjustMoney(delivery.deliverer().value(), delivery.playerEarnings());
        long boardId = delivery.boardId().value();
        commissionBoardJpaRepository.findById(boardId)
                .map(CommissionBoardEntity::getGuildId)
                .ifPresent(guildId -> guildService.addCommissionIncome(UUID.fromString(guildId), delivery.guildTax(), delivery.guildXp()));
        if (delivery.completesCommission()) {
            commissionBoardJpaRepository.incrementCompletedCommissionCount(boardId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int countDeliveredItems(CommissionBoardId boardId, CommissionType type, Quality minimalQuality) {
        return commissionDeliveredItemCountJpaRepository.findByBoardIdAndTypeAndMinimalQuality(boardId.value(), type, minimalQuality)
                .map(CommissionDeliveredItemCountEntity::getItemCount)
                .orElse(0);
    }

    private void addDeliveredItems(CommissionEntity commission, int itemCount) {
        CommissionDeliveredItemCountEntity deliveredItemCount = commissionDeliveredItemCountJpaRepository
                .findByBoardIdAndTypeAndMinimalQuality(commission.getBoardId(), commission.getType(), commission.getMinimalQuality())
                .orElseGet(() -> {
                    CommissionDeliveredItemCountEntity newCount = new CommissionDeliveredItemCountEntity();
                    newCount.setBoardId(commission.getBoardId());
                    newCount.setType(commission.getType());
                    newCount.setMinimalQuality(commission.getMinimalQuality());
                    return newCount;
                });
        deliveredItemCount.setItemCount(deliveredItemCount.getItemCount() + itemCount);
        commissionDeliveredItemCountJpaRepository.save(deliveredItemCount);
    }

    @Override
    @Transactional
    public void replaceCommissions(List<CommissionId> removedCommissions, List<CommissionDraft> newCommissions) {
        commissionJpaRepository.deleteAllByIdInBatch(removedCommissions.stream().map(CommissionId::value).toList());
        commissionJpaRepository.saveAll(newCommissions.stream().map(commissionMapper::toEntity).toList());
    }
}

package onl.tesseract.srp.commission.domain.port.userside;

import onl.tesseract.srp.commission.domain.model.board.BoardPart;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.commission.CatalogItem;
import onl.tesseract.srp.commission.domain.model.commission.Commission;
import onl.tesseract.srp.commission.domain.model.commission.CommissionDraft;
import onl.tesseract.srp.commission.domain.model.commission.CommissionId;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionCatalogRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionRepository;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

/** Garde un tableau plein : retire les commissions qui ne peuvent plus être livrées et en tire de nouvelles au hasard. */
public class CommissionBoardRefillService {
    private final CommissionRepository commissionRepository;
    private final CommissionConfigRepository commissionConfigRepository;
    private final CommissionCatalogRepository commissionCatalogRepository;
    private final RandomGenerator random;

    public CommissionBoardRefillService(CommissionRepository commissionRepository, CommissionConfigRepository commissionConfigRepository,
                                        CommissionCatalogRepository commissionCatalogRepository, RandomGenerator random) {
        this.commissionRepository = commissionRepository;
        this.commissionConfigRepository = commissionConfigRepository;
        this.commissionCatalogRepository = commissionCatalogRepository;
        this.random = random;
    }

    /**
     * Retire les commissions terminées et tire de quoi compléter chaque liste : les suivantes remontent
     * et les nouvelles arrivent en fin de liste. Une commission en cours n'est jamais remplacée. Un slot
     * sans item possible reste vide jusqu'au prochain remplissage.
     */
    public void refillBoard(CommissionBoard commissionBoard) {
        List<Commission> commissions = commissionRepository.findAllByBoard(commissionBoard.getId());
        List<CommissionDraft> newCommissions = generateMissingCommissions(commissionBoard, commissions);
        List<CommissionId> removedCommissions = commissions.stream()
                .filter(Commission::isCompleted)
                .map(Commission::id)
                .toList();
        if (!removedCommissions.isEmpty() || !newCommissions.isEmpty()) {
            commissionRepository.replaceCommissions(removedCommissions, newCommissions);
        }
    }

    /** Génère ce qu'il manque pour que chaque liste du tableau ait {@code slotsPerType} commissions en cours. */
    private List<CommissionDraft> generateMissingCommissions(CommissionBoard commissionBoard, List<Commission> existingCommissions) {
        List<CommissionDraft> newCommissions = new ArrayList<>();
        for (CommissionType commissionType : CommissionType.values()) {
            List<BoardPart> partsToFill = findAllPartsMissingCommissions(commissionBoard, commissionType, existingCommissions);
            if (partsToFill.isEmpty()) {
                continue;
            }
            List<CatalogItem> allowedItems = findAllItemsAllowedOn(commissionBoard, commissionType);
            if (allowedItems.isEmpty()) {
                continue;
            }
            for (BoardPart boardPart : partsToFill) {
                newCommissions.add(drawCommission(commissionBoard, boardPart, commissionType, allowedItems));
            }
        }
        return newCommissions;
    }

    private List<CatalogItem> findAllItemsAllowedOn(CommissionBoard commissionBoard, CommissionType commissionType) {
        int maxTier = commissionBoard.getLevels().getMaxTierFor(commissionType);
        return commissionCatalogRepository.findAllCatalogItems(commissionType).stream()
                .filter(catalogItem -> catalogItem.tier() <= maxTier && catalogItem.weight() > 0)
                .toList();
    }

    /** @return chaque partie du tableau, autant de fois qu'il lui manque de commissions de ce type */
    private List<BoardPart> findAllPartsMissingCommissions(CommissionBoard commissionBoard, CommissionType commissionType,
                                                           List<Commission> existingCommissions) {
        List<BoardPart> partsToFill = new ArrayList<>();
        for (BoardPart boardPart : commissionBoard.findAllParts()) {
            long openCount = existingCommissions.stream()
                    .filter(commission -> commission.isInList(boardPart, commissionType) && !commission.isCompleted())
                    .count();
            int missingCount = (int) Math.max(0, commissionBoard.getLevels().slotsPerType() - openCount);
            partsToFill.addAll(Collections.nCopies(missingCount, boardPart));
        }
        return partsToFill;
    }

    /** La qualité minimale est tirée entre {@code POOR} et la qualité maximale du tableau. */
    private CommissionDraft drawCommission(CommissionBoard commissionBoard, BoardPart boardPart, CommissionType commissionType,
                                           List<CatalogItem> allowedItems) {
        CatalogItem catalogItem = pickWeighted(allowedItems);
        int quantity = drawQuantity(catalogItem);
        Quality minimalQuality = Quality.values()[random.nextInt(commissionBoard.getLevels().maxRequiredQuality().ordinal() + 1)];
        return new CommissionDraft(commissionBoard.getId(), boardPart, commissionType, catalogItem.material(),
                quantity, minimalQuality, computeReward(commissionBoard, catalogItem.material(), quantity, minimalQuality));
    }

    /**
     * Tire uniformément la quantité entre {@code base × (1 − variance)} et {@code base × (1 + variance)},
     * bornes arrondies à l'unité et comprises, avec un minimum de un. La base et la variance viennent
     * de l'item du catalogue.
     */
    private int drawQuantity(CatalogItem catalogItem) {
        int baseQuantity = catalogItem.quantity();
        double varianceRate = catalogItem.quantityVariancePercent() / 100;
        int minimalQuantity = (int) Math.max(1, Math.round(baseQuantity * (1 - varianceRate)));
        int maximalQuantity = (int) Math.max(minimalQuantity, Math.round(baseQuantity * (1 + varianceRate)));
        if (minimalQuantity == maximalQuantity) {
            return minimalQuantity;
        }
        return random.nextInt(minimalQuantity, maximalQuantity + 1);
    }

    /**
     * La récompense vaut la valeur des items demandés, multipliée par la part payée au spawn et par le
     * bonus d'argent du tableau, arrondie à l'unité avec un minimum d'un lys.
     */
    private MoneyValue computeReward(CommissionBoard commissionBoard, MaterialName material, int quantity, Quality minimalQuality) {
        BigDecimal unitValue = commissionCatalogRepository.getUnitValue(material, minimalQuality).value();
        double boardRewardRate = commissionBoard.isSpawn() ? commissionConfigRepository.getSpawnRewardRate() : 1;
        double moneyBonusFactor = 1 + commissionConfigRepository.getMoneyBonusRate(commissionBoard.getLevels().moneyBonusLevel());
        BigDecimal reward = unitValue.multiply(BigDecimal.valueOf(quantity))
                .multiply(BigDecimal.valueOf(boardRewardRate * moneyBonusFactor))
                .setScale(0, RoundingMode.HALF_UP)
                .max(BigDecimal.ONE);
        return new MoneyValue(reward);
    }

    /** Sélectionne un item proportionnellement à son poids. */
    private CatalogItem pickWeighted(List<CatalogItem> catalogItems) {
        double remainingWeight = random.nextDouble() * catalogItems.stream().mapToDouble(CatalogItem::weight).sum();
        for (CatalogItem catalogItem : catalogItems) {
            remainingWeight -= catalogItem.weight();
            if (remainingWeight < 0) {
                return catalogItem;
            }
        }
        return catalogItems.getLast();
    }
}

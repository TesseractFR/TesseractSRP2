package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.model.contract.DeliveryRefusal;
import onl.tesseract.srp.contract.domain.model.exception.PaymentFailedException;
import onl.tesseract.srp.contract.domain.port.serverside.ContractMessagingRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractScrollRepository;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerInfoRepository;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerMaterialRepository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static onl.tesseract.srp.contract.domain.model.contract.DeliveryRefusal.*;

/**
 * Coordonne les opérations liées aux contrats proposées aux joueurs : achat,
 * consultation, livraison et gestion des tickets.
 * <p>
 * Les accès aux contrats, aux objets et aux informations des joueurs ainsi que
 * les messages sont délégués aux ports correspondants.
 */
public class ContractService {

    private final ContractRepository contractRepository;
    private final PlayerMaterialRepository playerMaterialRepository;
    private final ContractScrollRepository contractScrollRepository;
    private final ContractGenerator contractGenerator;
    private final PlayerInfoRepository playerInfoRepository;
    private final ContractMessagingRepository contractMessagingRepository;

    /**
     * Crée le service avec les dépôts et composants nécessaires à la gestion
     * des contrats.
     *
     * @param contractRepository dépôt des contrats
     * @param playerMaterialRepository accès aux matériaux des joueurs
     * @param contractScrollRepository gestion des parchemins de contrat
     * @param contractGenerator génération des contrats proposés
     * @param playerInfoRepository accès aux quotas, tickets et bonus des joueurs
     * @param contractMessagingRepository envoi des messages liés aux contrats
     */
    public ContractService(ContractRepository contractRepository, PlayerMaterialRepository playerMaterialRepository,
                           ContractScrollRepository contractScrollRepository, ContractGenerator contractGenerator,
                           PlayerInfoRepository playerInfoRepository,
                           ContractMessagingRepository contractMessagingRepository) {
        this.contractRepository = contractRepository;
        this.playerMaterialRepository = playerMaterialRepository;
        this.contractScrollRepository = contractScrollRepository;
        this.contractGenerator = contractGenerator;
        this.playerInfoRepository = playerInfoRepository;
        this.contractMessagingRepository = contractMessagingRepository;
    }

    /**
     * Tente de faire acheter un parchemin au joueur. Un ticket est consommé
     * uniquement si un contrat est généré et enregistré ; il est restitué si
     * l'enregistrement ou la remise du parchemin échoue.
     *
     * @param player joueur qui achète le parchemin
     */
    public void buyScroll(PlayerId player) {
        if (!hasTicket(player)) {
            contractMessagingRepository.sendPurchaseNoTicket(player);
            return;
        }
        if (!contractScrollRepository.canReceive(player)) {
            contractMessagingRepository.sendPurchaseInventoryFull(player);
            return;
        }
        Optional<Contract> draftContract = contractGenerator.generate(player);
        if (draftContract.isEmpty()) {
            contractMessagingRepository.sendPurchaseNoContractAvailable(player);
            return;
        }

        withdrawTicket(player);
        Contract savedContract = saveContractOrRefundTicket(player, draftContract.get());

        if (!giveScroll(player, savedContract)) {
            cancelPurchase(player, savedContract);
            contractMessagingRepository.sendPurchaseInventoryFull(player);
            return;
        }
        contractMessagingRepository.sendPurchaseSucceeded(player, savedContract, getTickets(player));
    }

    /**
     * Vérifie qu'un contrat existe, vise le métier du PNJ et n'est pas expiré.
     * En cas de refus, le joueur est informé.
     *
     * @param player joueur qui présente le contrat
     * @param contractId identifiant du contrat
     * @param npcJob métier du PNJ sollicité
     * @return le contrat s'il peut être livré, sinon un résultat vide
     */
    public Optional<Contract> checkDeliverable(PlayerId player, long contractId, String npcJob) {
        Optional<Contract> foundContract = contractRepository.findById(contractId);
        if (foundContract.isEmpty()) {
            refuse(player, npcJob, contractId, INVALID_SCROLL);
            return Optional.empty();
        }
        Contract contract = foundContract.get();
        if (!contract.isForJob(npcJob)) {
            refuse(player, npcJob, contractId, WRONG_CLIENT);
            return Optional.empty();
        }
        if (contract.isExpired(Instant.now())) {
            refuse(player, npcJob, contractId, EXPIRED);
            return Optional.empty();
        }
        return foundContract;
    }

    /**
     * Traite la livraison d'un contrat : valide le parchemin et les matériaux,
     * les retire, puis règle la récompense et informe le joueur du résultat.
     *
     * @param player joueur qui livre le contrat
     * @param contractId identifiant du contrat
     * @param npcJob métier du PNJ destinataire
     */
    public void deliver(PlayerId player, long contractId, String npcJob) {
        Optional<Contract> foundContract = checkDeliverable(player, contractId, npcJob);
        if (foundContract.isEmpty()) {
            return;
        }
        Contract contract = foundContract.get();

        if (!contractScrollRepository.has(player, contractId)) {
            refuse(player, npcJob, contractId, INVALID_SCROLL);
            return;
        }
        int itemsOwned = countOwnedItems(player, contract);
        if (itemsOwned < contract.quantity()) {
            refuseMissingItems(player, npcJob, contract, itemsOwned);
            return;
        }
        withdrawAndPay(player, npcJob, contract);
    }

    /**
     * Ajoute le parchemin du contrat à l'inventaire du joueur, en indiquant
     * si le contrat ouvre droit à un bonus monétaire.
     *
     * @param player joueur destinataire
     * @param contract contrat associé au parchemin
     * @return {@code true} si le parchemin a été ajouté
     */
    private boolean giveScroll(PlayerId player, Contract contract) {
        return contractScrollRepository.give(player, contract, moneyBonus(player, contract) > 0);
    }

    /**
     * Compte les matériaux du contrat possédés par le joueur à la qualité minimale requise.
     *
     * @param player joueur dont l'inventaire est compté
     * @param contract contrat concerné
     * @return nombre de matériaux admissibles
     */
    public int countOwnedItems(PlayerId player, Contract contract) {
        return playerMaterialRepository.countAtLeast(player, contract.material(), contract.minimalQuality());
    }

    /**
     * Calcule le bonus monétaire applicable à une livraison de récolte.
     * Les autres types de contrat ne donnent aucun bonus.
     *
     * @param deliverer joueur qui livre le contrat
     * @param contract contrat concerné
     * @return montant du bonus, arrondi à l'entier le plus proche
     */
    public int moneyBonus(PlayerId deliverer, Contract contract) {
        if (contract.type() != ContractType.HARVEST) {
            return 0;
        }
        double bonusRate = Math.max(0, playerInfoRepository.getMoneyBonus(deliverer, contract.job(), contract.material()));
        return (int) Math.round(contract.reward() * bonusRate);
    }

    /** Informe le joueur qu'il doit tenir un parchemin pour interagir avec le PNJ. */
    public void noScrollInHand(PlayerId player, String npcJob) {
        contractMessagingRepository.sendNoScrollInHand(player, npcJob);
    }

    /**
     * Vérifie le parchemin utilisé et le détruit si son contrat est absent ou expiré.
     *
     * @param player joueur qui utilise le parchemin
     * @param contractId identifiant du contrat associé
     */
    public void useScroll(PlayerId player, long contractId) {
        boolean contractStillValid = contractRepository.findById(contractId)
                .map(contract -> !contract.isExpired(Instant.now()))
                .orElse(false);
        if (!contractStillValid && contractScrollRepository.remove(player, contractId)) {
            contractMessagingRepository.sendScrollDestroyed(player);
        }
    }

    /**
     * Supprime du dépôt les contrats expirés.
     *
     * @return nombre de contrats supprimés
     */
    public int purgeExpired() {
        return contractRepository.deleteExpired(Instant.now());
    }

    /** Nettoie les contrats expirés puis annonce au joueur le nombre de parchemins concernés. */
    public void reportExpiredScrolls(PlayerId player, int count) {
        purgeExpired();
        contractMessagingRepository.sendScrollsExpired(player, count);
    }

    /** @return quota quotidien de tickets du joueur */
    public int getDailyQuota(PlayerId player) {
        return playerInfoRepository.getDailyQuota(player);
    }

    /**
     * Retourne le solde enregistré, ou le quota quotidien si aucun solde n'est enregistré.
     *
     * @param player joueur concerné
     * @return nombre de tickets disponibles
     */
    public int getTickets(PlayerId player) {
        return playerInfoRepository.findTickets(player).orElseGet(() -> getDailyQuota(player));
    }

    /** Affiche au joueur son solde de tickets et son quota quotidien. */
    public void showTickets(PlayerId player) {
        contractMessagingRepository.sendTicketsShown(player, getTickets(player), getDailyQuota(player));
    }

    /**
     * Ajoute des tickets à un joueur à la demande d'un membre du staff.
     * Les quantités nulles ou négatives sont refusées.
     *
     * @param staff membre du staff à l'origine de l'action
     * @param target joueur qui reçoit les tickets
     * @param amount nombre de tickets à ajouter
     */
    public void giveTicketsAsStaff(PlayerId staff, PlayerId target, int amount) {
        if (amount <= 0) {
            contractMessagingRepository.sendStaffInvalidAmount(staff);
            return;
        }
        addTickets(target, amount);
        contractMessagingRepository.sendStaffTicketsGiven(staff, target, amount);
    }

    /** Affiche au membre du staff le solde et le quota d'un joueur. */
    public void showTicketsAsStaff(PlayerId staff, PlayerId target) {
        contractMessagingRepository.sendStaffTicketsShown(staff, target, getTickets(target), getDailyQuota(target));
    }

    /** Réinitialise le solde du joueur à son quota quotidien si les deux valeurs diffèrent. */
    public void renewDaily(PlayerId player) {
        int quota = getDailyQuota(player);
        if (getTickets(player) != quota) {
            playerInfoRepository.saveTickets(player, quota);
        }
    }

    /**
     * Vérifie que le joueur possède au moins un ticket.
     *
     * @param player joueur à vérifier
     * @return {@code true} si le joueur peut acheter un contrat
     */
    private boolean hasTicket(PlayerId player) {
        return getTickets(player) >= 1;
    }

    /** Ajoute le nombre indiqué au solde de tickets du joueur. */
    private void addTickets(PlayerId player, int amount) {
        playerInfoRepository.saveTickets(player, getTickets(player) + amount);
    }

    /** Décrémente d'un ticket le solde du joueur. */
    private void withdrawTicket(PlayerId player) {
        playerInfoRepository.saveTickets(player, getTickets(player) - 1);
    }

    /**
     * Enregistre le contrat généré et restitue le ticket si l'enregistrement
     * échoue avec une exception d'exécution.
     *
     * @param player joueur à rembourser en cas d'échec
     * @param draftContract contrat à enregistrer
     * @return contrat enregistré
     * @throws RuntimeException si l'enregistrement échoue, après restitution du ticket
     */
    private Contract saveContractOrRefundTicket(PlayerId player, Contract draftContract) {
        try {
            return contractRepository.save(draftContract);
        } catch (RuntimeException e) {
            addTickets(player, 1);
            throw e;
        }
    }

    /** Supprime le contrat créé et restitue le ticket après l'échec de remise du parchemin. */
    private void cancelPurchase(PlayerId player, Contract savedContract) {
        contractRepository.delete(savedContract.id());
        addTickets(player, 1);
    }

    /**
     * Signale le refus de livraison et supprime le contrat du dépôt s'il est expiré.
     *
     * @param player joueur concerné
     * @param npcJob métier du PNJ sollicité
     * @param contractId identifiant du contrat refusé
     * @param refusalReason motif du refus
     */
    private void refuse(PlayerId player, String npcJob, long contractId, DeliveryRefusal refusalReason) {
        if (refusalReason == EXPIRED) {
            contractRepository.delete(contractId);
        }
        contractMessagingRepository.sendDeliveryRefused(player, npcJob, refusalReason, 0);
    }

    /**
     * Détermine si les matériaux manquent en quantité ou en qualité, puis
     * informe le joueur du motif et du nombre d'unités manquantes.
     *
     * @param player joueur concerné
     * @param npcJob métier du PNJ sollicité
     * @param contract contrat à livrer
     * @param itemsOwned nombre d'unités possédées à la qualité minimale requise
     */
    private void refuseMissingItems(PlayerId player, String npcJob, Contract contract, int itemsOwned) {
        int itemsOwnedAnyQuality = playerMaterialRepository.countAtLeast(player, contract.material(), Quality.POOR);
        DeliveryRefusal refusalReason = itemsOwnedAnyQuality > itemsOwned ? NOT_ENOUGH_QUALITY : MISSING_ITEMS;
        contractMessagingRepository.sendDeliveryRefused(player, npcJob, refusalReason, contract.quantity() - itemsOwned);
    }

    /** Retire le parchemin et les matériaux, règle la récompense, puis annonce le résultat. */
    private void withdrawAndPay(PlayerId player, String npcJob, Contract contract) {
        int payout = contract.reward() + moneyBonus(player, contract);

        Map<Quality, Integer> removedItems = takeScrollAndItemsOrRefuse(player, npcJob, contract);
        if (removedItems.isEmpty()) {
            return;
        }
        if (payOrRefuse(player, npcJob, contract, payout, removedItems)) {
            contractMessagingRepository.sendDeliverySucceeded(player, npcJob, payout);
        }
    }

    /**
     * Retire le parchemin puis les matériaux requis. En cas d'échec du retrait
     * des matériaux, le parchemin est rendu au joueur.
     *
     * @param player joueur qui livre le contrat
     * @param npcJob métier du PNJ sollicité
     * @param contract contrat à traiter
     * @return quantités de matériaux retirées par qualité, ou une map vide si le retrait échoue
     */
    private Map<Quality, Integer> takeScrollAndItemsOrRefuse(PlayerId player, String npcJob, Contract contract) {
        if (!contractScrollRepository.remove(player, contract.id())) {
            contractMessagingRepository.sendDeliveryRefused(player, npcJob, INVALID_SCROLL, 0);
            return Map.of();
        }
        Map<Quality, Integer> removedItems;
        try {
            removedItems = playerMaterialRepository.removeAtLeast(player, contract.material(),
                    contract.minimalQuality(), contract.quantity());
        } catch (RuntimeException e) {
            giveScroll(player, contract);
            throw e;
        }
        if (removedItems.isEmpty()) {
            giveScroll(player, contract);
            contractMessagingRepository.sendDeliveryRefused(player, npcJob, MISSING_ITEMS, contract.quantity());
        }
        return removedItems;
    }

    /**
     * Tente de régler la récompense. Si le règlement échoue ou si le contrat
     * n'est plus valide, restitue les matériaux et le parchemin.
     *
     * @param player joueur à payer
     * @param npcJob métier du PNJ sollicité
     * @param contract contrat à régler
     * @param payout montant total à verser
     * @param removedItems matériaux retirés, regroupés par qualité
     * @return {@code true} si le règlement a réussi
     */
    private boolean payOrRefuse(PlayerId player, String npcJob, Contract contract, int payout,
                                Map<Quality, Integer> removedItems) {
        boolean isPaid;
        try {
            isPaid = contractRepository.settle(contract.id(), player, payout);
        } catch (PaymentFailedException e) {
            giveBackToPlayer(player, contract, removedItems);
            contractMessagingRepository.sendDeliveryRefused(player, npcJob, PAYMENT_FAILED, 0);
            return false;
        } catch (RuntimeException e) {
            giveBackToPlayer(player, contract, removedItems);
            throw e;
        }
        if (!isPaid) {
            giveBackToPlayer(player, contract, removedItems);
            contractMessagingRepository.sendDeliveryRefused(player, npcJob, INVALID_SCROLL, 0);
        }
        return isPaid;
    }

    /** Restitue au joueur les matériaux retirés et le parchemin du contrat. */
    private void giveBackToPlayer(PlayerId player, Contract contract, Map<Quality, Integer> removedItems) {
        playerMaterialRepository.giveBack(player, contract.material(), removedItems);
        giveScroll(player, contract);
    }
}

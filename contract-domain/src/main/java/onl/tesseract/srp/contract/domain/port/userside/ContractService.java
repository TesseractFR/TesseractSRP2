package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.port.serverside.ContractMessagingRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractScrollRepository;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerInfoRepository;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerMaterialRepository;

import java.time.Instant;
import java.util.Optional;

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
        if (!contractScrollRepository.canReceiveScroll(player)) {
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

    /** Recherche un contrat par son identifiant dans le dépôt. */
    public Optional<Contract> getContract(long contractId) {
        return contractRepository.findById(contractId);
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
        //On récupère le contrat
        Optional<Contract> foundContract = getContract(contractId);
        if (foundContract.isEmpty()) {
            contractMessagingRepository.sendDeliveryInvalidScroll(player, npcJob);
            return;
        }
        Contract contract = foundContract.get();
        //On vérifie qu'il est valide
        if(contract.isExpired()){
            contractRepository.delete(contractId);
            contractMessagingRepository.sendDeliveryExpired(player, npcJob);
            return;
        }
        if(!contract.isForJob(npcJob)){
            contractMessagingRepository.sendDeliveryWrongClient(player, npcJob);
            return;
        }
        //Le contract est valide on peut l'accepter
        // On vérifie que le joueur a LE scroll en main
        if (!contractScrollRepository.hasScroll(player, contractId)) {
            contractMessagingRepository.sendNoScrollInHand(player, npcJob);
            return;
        }
        // On vérifie si il a les items requis
        if (!checkContractItems(player, contract)) {
            refuseMissingItems(player, npcJob, contract);
            return;
        }
        withdrawScroll(player,contractId);
        withdrawContractItems(player,contract);
        giveMoney(player,contract.reward());
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
        return contractScrollRepository.giveScroll(player, contract, moneyBonus(player, contract) > 0);
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

    /** Vérifie que le joueur possède la quantité requise et signale le motif en cas d'échec. */
    private boolean checkContractItems(PlayerId player, Contract contract) {
        int itemsOwned = countOwnedItems(player, contract);
        return itemsOwned >= contract.quantity();
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
                .map(contract -> !contract.isExpired())
                .orElse(false);
        if (!contractStillValid && withdrawScroll(player, contractId)) {
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
     * informe le joueur du motif et du nombre d'unités manquantes.
     *
     * @param player joueur concerné
     * @param npcJob métier du PNJ sollicité
     * @param contract contrat à livrer
     */
    private void refuseMissingItems(PlayerId player, String npcJob, Contract contract) {
        int itemsOwned = playerMaterialRepository.countAtLeast(player, contract.material(), contract.minimalQuality());
        int missing = contract.quantity() - itemsOwned;
        contractMessagingRepository.sendDeliveryMissingItems(player, npcJob, missing);
    }

    /** Verse la récompense au joueur via le port d'informations des joueurs. */
    private void giveMoney(PlayerId player, int amount) {
        playerInfoRepository.giveMoney(player, MoneyValue.of(amount));
    }

    /** Retire du joueur le parchemin associé au contrat indiqué. */
    private boolean withdrawScroll(PlayerId player, long contractId) {
        return contractScrollRepository.removeScroll(player, contractId);
    }

    /**
     * Retire les matériaux du contrat au joueur et retourne les quantités retirées par qualité.
     */
    private void withdrawContractItems(PlayerId player, Contract contract) {
        playerMaterialRepository.removeAtLeast(player, contract.material(),
                contract.minimalQuality(), contract.quantity());
    }
}

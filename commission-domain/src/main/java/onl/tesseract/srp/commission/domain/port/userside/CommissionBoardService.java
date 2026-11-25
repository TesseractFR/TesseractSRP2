package onl.tesseract.srp.commission.domain.port.userside;

import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.model.commission.Commission;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardNotFoundException;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionBoardRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionCatalogRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionRepository;
import onl.tesseract.srp.common.domain.model.PlayerId;

import java.util.List;
import java.util.Optional;

/** Ouvre les tableaux de commissions, donne ce qu'un joueur y voit, et les supprime avec leur guilde. */
public class CommissionBoardService {
    private final CommissionBoardRepository commissionBoardRepository;
    private final CommissionRepository commissionRepository;
    private final CommissionCatalogRepository commissionCatalogRepository;
    private final CommissionConfigRepository commissionConfigRepository;
    private final CommissionBoardAccessService commissionBoardAccessService;
    private final CommissionBoardRefillService commissionBoardRefillService;

    public CommissionBoardService(CommissionBoardRepository commissionBoardRepository, CommissionRepository commissionRepository,
                                  CommissionCatalogRepository commissionCatalogRepository,
                                  CommissionConfigRepository commissionConfigRepository,
                                  CommissionBoardAccessService commissionBoardAccessService,
                                  CommissionBoardRefillService commissionBoardRefillService) {
        this.commissionBoardRepository = commissionBoardRepository;
        this.commissionRepository = commissionRepository;
        this.commissionCatalogRepository = commissionCatalogRepository;
        this.commissionConfigRepository = commissionConfigRepository;
        this.commissionBoardAccessService = commissionBoardAccessService;
        this.commissionBoardRefillService = commissionBoardRefillService;
    }

    /**
     * Prépare le tableau du spawn pour un joueur qui l'ouvre : il est créé au premier appel, son nombre
     * de slots suit la configuration, et il est rempli. Un slot ajouté en configuration est donc rempli
     * aussitôt, et les commissions d'un slot retiré ne sont plus affichées.
     *
     * @return le tableau du spawn, avec ses slots remplis
     */
    public CommissionBoard openSpawnBoard() {
        int spawnSlotsPerType = commissionConfigRepository.getSpawnSlotsPerType();
        CommissionBoard spawnBoard = commissionBoardRepository.findSpawnBoard()
                .orElseGet(() -> commissionBoardRepository.createSpawnBoard(spawnSlotsPerType));
        // Au spawn, les deux types ont toujours le même nombre de commissions.
        if (spawnBoard.getLevels().harvestSlots() != spawnSlotsPerType) {
            spawnBoard.changeSpawnSlots(spawnSlotsPerType);
            commissionBoardRepository.save(spawnBoard);
        }
        commissionBoardRefillService.refillBoard(spawnBoard);
        return spawnBoard;
    }

    /**
     * Prépare le tableau de la guilde pour un joueur qui l'ouvre : il est créé au premier appel avec la
     * taxe par défaut de la configuration, puis rempli.
     *
     * @return le tableau de la guilde, avec ses slots remplis
     */
    public CommissionBoard openGuildBoard(GuildId guildId) {
        CommissionBoard guildBoard = commissionBoardRepository.findByGuild(guildId)
                .orElseGet(() -> commissionBoardRepository.createGuildBoard(guildId, commissionConfigRepository.getDefaultUsageTaxRate()));
        commissionBoardRefillService.refillBoard(guildBoard);
        return guildBoard;
    }

    /**
     * @return le tableau
     * @throws CommissionBoardNotFoundException s'il n'existe pas
     */
    public CommissionBoard getBoard(CommissionBoardId boardId) {
        return commissionBoardRepository.findById(boardId)
                .orElseThrow(() -> new CommissionBoardNotFoundException(boardId));
    }

    /** @return les commissions du tableau que le joueur voit, de la plus ancienne à la plus récente */
    public List<Commission> findAllVisibleCommissions(PlayerId player, CommissionBoard commissionBoard) {
        return commissionRepository.findAllByBoard(commissionBoard.getId()).stream()
                .filter(commission -> commissionBoardAccessService.canAccessPart(player, commissionBoard, commission.part()))
                .toList();
    }

    /** @return part de la valeur des items payée au spawn, pour l'afficher aux joueurs */
    public double getSpawnRewardRate() {
        return commissionConfigRepository.getSpawnRewardRate();
    }

    /**
     * @return tier de l'item de la commission, pour l'afficher, ou {@link Optional#empty()} si l'item
     * a quitté le catalogue depuis la génération de la commission
     */
    public Optional<Integer> findCommissionTier(Commission commission) {
        return commissionCatalogRepository.findItemTier(commission.type(), commission.material());
    }

    /** Supprime le tableau de la guilde et ses commissions. Ne fait rien s'il n'existe pas. */
    public void deleteGuildBoard(GuildId guildId) {
        commissionBoardRepository.deleteByGuild(guildId);
    }
}

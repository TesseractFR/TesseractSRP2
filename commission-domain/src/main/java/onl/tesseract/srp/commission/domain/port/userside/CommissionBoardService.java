package onl.tesseract.srp.commission.domain.port.userside;

import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardLevels;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardPart;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.model.commission.Commission;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardNotFoundException;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionBoardRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionCatalogRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.commission.domain.port.serverside.GuildMembershipPort;
import onl.tesseract.srp.common.domain.model.PlayerId;

import java.util.List;

/** Donne accès aux tableaux de commissions : lecture, droits des joueurs et suppression avec la guilde. */
public class CommissionBoardService {
    private final CommissionBoardRepository commissionBoardRepository;
    private final GuildMembershipPort guildMembershipPort;
    private final CommissionCatalogRepository commissionCatalogRepository;
    private final CommissionConfigRepository commissionConfigRepository;
    private final CommissionBoardRefillService commissionBoardRefillService;

    public CommissionBoardService(CommissionBoardRepository commissionBoardRepository, GuildMembershipPort guildMembershipPort,
                                  CommissionCatalogRepository commissionCatalogRepository,
                                  CommissionConfigRepository commissionConfigRepository,
                                  CommissionBoardRefillService commissionBoardRefillService) {
        this.commissionBoardRepository = commissionBoardRepository;
        this.guildMembershipPort = guildMembershipPort;
        this.commissionCatalogRepository = commissionCatalogRepository;
        this.commissionConfigRepository = commissionConfigRepository;
        this.commissionBoardRefillService = commissionBoardRefillService;
    }

    /**
     * Le nombre de slots du spawn suit la configuration à chaque appel : un slot ajouté est rempli
     * aussitôt, et les commissions d'un slot retiré ne sont plus affichées.
     *
     * @return le tableau du spawn, créé au premier appel, avec ses slots remplis
     */
    public CommissionBoard getSpawnBoard() {
        int spawnSlotsPerType = commissionConfigRepository.getSpawnSlotsPerType();
        CommissionBoard spawnBoard = commissionBoardRepository.findSpawnBoard()
                .orElseGet(() -> commissionBoardRepository.createSpawnBoard(CommissionBoardLevels.forSpawnBoard(spawnSlotsPerType)));
        if (!spawnBoard.getLevels().equals(CommissionBoardLevels.forSpawnBoard(spawnSlotsPerType))) {
            spawnBoard.changeSpawnSlots(spawnSlotsPerType);
            commissionBoardRepository.save(spawnBoard);
        }
        commissionBoardRefillService.refillBoard(spawnBoard);
        return spawnBoard;
    }

    /** @return part de la valeur des items payée au spawn, pour l'afficher aux joueurs */
    public double getSpawnRewardRate() {
        return commissionConfigRepository.getSpawnRewardRate();
    }

    /** @return le tableau de la guilde, créé au premier appel avec la taxe par défaut de la configuration, avec ses slots remplis */
    public CommissionBoard getGuildBoard(GuildId guildId) {
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

    /** @return {@code true} si le joueur voit cette partie : la partie publique est ouverte à tous */
    public boolean canAccessPart(PlayerId player, CommissionBoard commissionBoard, CommissionBoardPart boardPart) {
        return boardPart == CommissionBoardPart.PUBLIC || isGuildMember(player, commissionBoard);
    }

    /** @return {@code true} si le joueur est membre de la guilde propriétaire ; toujours faux au spawn */
    public boolean isGuildMember(PlayerId player, CommissionBoard commissionBoard) {
        return commissionBoard.findGuildId()
                .map(guildId -> guildMembershipPort.isMember(guildId, player))
                .orElse(false);
    }

    /** @return les membres de la guilde propriétaire, à prévenir des gains de la guilde ; liste vide au spawn */
    public List<PlayerId> findAllGuildMembers(CommissionBoard commissionBoard) {
        return commissionBoard.findGuildId()
                .map(guildMembershipPort::findAllMembers)
                .orElse(List.of());
    }

    /** @return {@code true} si le joueur peut améliorer le tableau et changer sa taxe ; toujours faux au spawn */
    public boolean canManageBoard(PlayerId player, CommissionBoard commissionBoard) {
        return commissionBoard.findGuildId()
                .map(guildId -> guildMembershipPort.canManageBoard(guildId, player))
                .orElse(false);
    }

    /** @return tier de l'item de la commission, pour l'afficher */
    public int getCommissionTier(Commission commission) {
        return commissionCatalogRepository.getItemTier(commission.type(), commission.material());
    }

    /** Supprime le tableau de la guilde et ses commissions. Ne fait rien s'il n'existe pas. */
    public void deleteGuildBoard(GuildId guildId) {
        commissionBoardRepository.deleteByGuild(guildId);
    }
}

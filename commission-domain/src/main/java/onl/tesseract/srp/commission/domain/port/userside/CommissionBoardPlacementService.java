package onl.tesseract.srp.commission.domain.port.userside;

import onl.tesseract.srp.commission.domain.model.board.BoardSpot;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.model.exception.BoardFurnitureUnavailableException;
import onl.tesseract.srp.commission.domain.model.exception.BoardManagementNotAllowedException;
import onl.tesseract.srp.commission.domain.model.exception.BoardOutsideTerritoryException;
import onl.tesseract.srp.commission.domain.model.exception.BoardPlacementNotStartedException;
import onl.tesseract.srp.commission.domain.model.exception.BoardSpotNotFreeException;
import onl.tesseract.srp.commission.domain.port.serverside.BoardFurniturePort;
import onl.tesseract.srp.commission.domain.port.serverside.BoardPlacementSessionPort;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionBoardRepository;
import onl.tesseract.srp.commission.domain.port.serverside.GuildTerritoryPort;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.common.domain.model.PlayerId;

/**
 * Pose un tableau de guilde. Le joueur lance la pose depuis le menu de guilde, puis a
 * {@link CommissionBoard#BOARD_PLACEMENT_SECONDS} secondes pour choisir l'emplacement. Une guilde n'a
 * qu'un tableau : le poser ailleurs retire l'ancien.
 */
public class CommissionBoardPlacementService {
    private final CommissionBoardService commissionBoardService;
    private final CommissionBoardRepository commissionBoardRepository;
    private final BoardPlacementSessionPort boardPlacementSessionPort;
    private final BoardFurniturePort boardFurniturePort;
    private final GuildTerritoryPort guildTerritoryPort;

    public CommissionBoardPlacementService(CommissionBoardService commissionBoardService, CommissionBoardRepository commissionBoardRepository,
                                           BoardPlacementSessionPort boardPlacementSessionPort, BoardFurniturePort boardFurniturePort,
                                           GuildTerritoryPort guildTerritoryPort) {
        this.commissionBoardService = commissionBoardService;
        this.commissionBoardRepository = commissionBoardRepository;
        this.boardPlacementSessionPort = boardPlacementSessionPort;
        this.boardFurniturePort = boardFurniturePort;
        this.guildTerritoryPort = guildTerritoryPort;
    }

    /** @return {@code true} si le joueur peut poser ou déplacer le tableau de la guilde (Adjoint ou chef) */
    public boolean canPlaceBoard(PlayerId player, GuildId guildId) {
        return commissionBoardService.canManageGuildBoard(player, guildId);
    }

    /**
     * Lance la pose : le joueur choisit ensuite l'emplacement avec {@link #placeGuildBoard}.
     *
     * @throws BoardManagementNotAllowedException si le joueur ne peut pas gérer le tableau de la guilde
     */
    public void startBoardPlacement(PlayerId player, GuildId guildId) {
        if (!canPlaceBoard(player, guildId)) {
            throw new BoardManagementNotAllowedException(player);
        }
        boardPlacementSessionPort.startSession(player, guildId);
    }

    /** @return {@code true} si le joueur doit encore choisir l'emplacement du tableau */
    public boolean isPlacingBoard(PlayerId player) {
        return boardPlacementSessionPort.findGuildOfPlacingPlayer(player).isPresent();
    }

    /** @return {@code false} si le joueur ne posait pas de tableau */
    public boolean cancelBoardPlacement(PlayerId player) {
        return boardPlacementSessionPort.endSession(player);
    }

    /**
     * Pose le tableau de la guilde dont le joueur a lancé la pose. En cas de refus, l'ancien tableau
     * reste en place et le joueur peut réessayer ailleurs tant que le temps n'est pas écoulé.
     *
     * @throws BoardPlacementNotStartedException si le joueur ne pose pas de tableau
     * @throws BoardOutsideTerritoryException si le tableau dépasserait du territoire de la guilde
     * @throws BoardSpotNotFreeException si les blocs ne sont pas libres
     * @throws BoardFurnitureUnavailableException si le tableau n'a pas pu être posé
     */
    public void placeGuildBoard(PlayerId player, BoardSpot boardSpot) {
        GuildId guildId = boardPlacementSessionPort.findGuildOfPlacingPlayer(player)
                .orElseThrow(() -> new BoardPlacementNotStartedException(player));
        if (!boardSpot.findAllOccupiedChunks().stream().allMatch(chunk -> guildTerritoryPort.ownsChunk(guildId, chunk))) {
            throw new BoardOutsideTerritoryException(guildId);
        }
        if (!boardFurniturePort.hasFreeSpace(boardSpot)) {
            throw new BoardSpotNotFreeException(boardSpot);
        }
        // Le tableau est chargé avant d'être posé dans le monde : si le chargement échoue, rien n'est posé.
        CommissionBoard commissionBoard = commissionBoardService.getGuildBoard(guildId);
        Coordinate newPosition = boardFurniturePort.placeFurniture(boardSpot)
                .orElseThrow(() -> new BoardFurnitureUnavailableException(boardSpot));
        commissionBoard.findFurniturePosition().ifPresent(boardFurniturePort::removeFurniture);
        commissionBoard.placeFurnitureAt(newPosition);
        commissionBoardRepository.save(commissionBoard);
        boardPlacementSessionPort.endSession(player);
    }

    /** @return {@code true} si le tableau de la guilde est posé */
    public boolean isBoardPlaced(GuildId guildId) {
        return commissionBoardRepository.findByGuild(guildId)
                .flatMap(CommissionBoard::findFurniturePosition)
                .isPresent();
    }
}

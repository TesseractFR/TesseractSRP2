package onl.tesseract.srp.commission.domain.port.serverside;

import onl.tesseract.srp.commission.domain.model.board.BoardSpot;
import onl.tesseract.srp.common.domain.model.Coordinate;

import java.util.Optional;

/** Port de pose et de retrait des tableaux dans le monde. */
public interface BoardFurniturePort {
    /** @return {@code true} si tous les blocs que le tableau occuperait sont libres */
    boolean hasFreeSpace(BoardSpot boardSpot);

    /** @return position du tableau posé, ou {@link Optional#empty()} s'il n'a pas pu être posé */
    Optional<Coordinate> placeFurniture(BoardSpot boardSpot);

    /** Retire le tableau posé à cette position, même loin des joueurs. Ne fait rien s'il n'y en a pas. */
    void removeFurniture(Coordinate furniturePosition);
}

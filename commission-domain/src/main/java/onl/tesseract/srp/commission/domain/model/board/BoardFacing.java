package onl.tesseract.srp.commission.domain.model.board;

/** Orientation d'un tableau posé. */
public enum BoardFacing {
    NORTH, SOUTH, EAST, WEST;

    /** @return {@code true} si la largeur du tableau s'étend le long de l'axe X */
    public boolean isWidthAlongX() {
        return this == NORTH || this == SOUTH;
    }
}

package onl.tesseract.srp.commission.adapter.furniture;

import onl.tesseract.srp.common.adapter.mapper.ChunkCoordMapper;
import onl.tesseract.srp.common.domain.model.Coordinate;
import org.bukkit.Location;

/** Identité ItemsAdder des tableaux de commissions et calcul de leur position. */
public final class CommissionBoardFurniture {
    /** Identifiant ItemsAdder du tableau. */
    public static final String BOARD_ITEM_ID = "tesseract:commission_board";

    private CommissionBoardFurniture() {
    }

    /** La position d'un tableau est le bloc sur lequel il se tient, pour pouvoir la comparer à celle enregistrée. */
    public static Coordinate toBlockCoordinate(Location location) {
        return new Coordinate((double) location.getBlockX(), (double) location.getBlockY(), (double) location.getBlockZ(),
                ChunkCoordMapper.toChunkCoord(location));
    }
}

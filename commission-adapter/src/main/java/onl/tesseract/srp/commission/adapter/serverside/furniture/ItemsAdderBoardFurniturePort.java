package onl.tesseract.srp.commission.adapter.serverside.furniture;

import dev.lone.itemsadder.api.CustomFurniture;
import onl.tesseract.srp.commission.adapter.furniture.CommissionBoardFurniture;
import onl.tesseract.srp.commission.domain.model.board.BoardFacing;
import onl.tesseract.srp.commission.domain.model.board.BoardSpot;
import onl.tesseract.srp.commission.domain.port.serverside.BoardFurniturePort;
import onl.tesseract.srp.common.domain.model.Coordinate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Pose et retire les tableaux ItemsAdder dans le monde. */
@Component
public class ItemsAdderBoardFurniturePort implements BoardFurniturePort {
    private static final int BOARD_HEIGHT = 2;

    @Override
    public boolean hasFreeSpace(BoardSpot boardSpot) {
        return findBlockAt(boardSpot.centerBlock())
                .map(centerBlock -> hasFreeSpaceAround(centerBlock, boardSpot.facing()))
                .orElse(false);
    }

    @Override
    public Optional<Coordinate> placeFurniture(BoardSpot boardSpot) {
        return findBlockAt(boardSpot.centerBlock())
                .flatMap(centerBlock -> Optional.ofNullable(CustomFurniture.spawn(CommissionBoardFurniture.BOARD_ITEM_ID, centerBlock)))
                .flatMap(boardFurniture -> turnTowards(boardFurniture, boardSpot.facing()));
    }

    /** Fonctionne aussi loin des joueurs : demander le chunk suffit à le charger. */
    @Override
    public void removeFurniture(Coordinate furniturePosition) {
        Optional<World> world = Optional.ofNullable(Bukkit.getWorld(furniturePosition.chunkCoord().world()));
        world.ifPresent(foundWorld -> {
            for (Entity entity : foundWorld.getChunkAt(furniturePosition.chunkCoord().x(), furniturePosition.chunkCoord().z()).getEntities()) {
                removeIfBoardFurnitureAt(entity, furniturePosition);
            }
        });
    }

    private static boolean hasFreeSpaceAround(Block centerBlock, BoardFacing facing) {
        for (int offset = -BoardSpot.HALF_WIDTH; offset <= BoardSpot.HALF_WIDTH; offset++) {
            for (int height = 0; height < BOARD_HEIGHT; height++) {
                int offsetX = facing.isWidthAlongX() ? offset : 0;
                int offsetZ = facing.isWidthAlongX() ? 0 : offset;
                if (!centerBlock.getRelative(offsetX, height, offsetZ).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * {@link CustomFurniture#getEntity} est nul si ItemsAdder n'a pas créé l'entité du tableau : le tableau
     * compte alors comme non posé.
     *
     * @return position du tableau tourné, ou {@link Optional#empty()} s'il n'a pas d'entité
     */
    private static Optional<Coordinate> turnTowards(CustomFurniture boardFurniture, BoardFacing facing) {
        return Optional.ofNullable(boardFurniture.getEntity()).map(furnitureEntity -> {
            Location rotatedLocation = furnitureEntity.getLocation();
            rotatedLocation.setYaw(toYaw(facing));
            boardFurniture.teleport(rotatedLocation);
            return CommissionBoardFurniture.toBlockCoordinate(rotatedLocation);
        });
    }

    /** {@link CustomFurniture#byAlreadySpawned} lève une exception sur une entité qui ne peut pas être un meuble ItemsAdder, comme un joueur. */
    private static void removeIfBoardFurnitureAt(Entity entity, Coordinate furniturePosition) {
        if (!(entity instanceof ArmorStand) || !furniturePosition.equals(CommissionBoardFurniture.toBlockCoordinate(entity.getLocation()))) {
            return;
        }
        Optional.ofNullable(CustomFurniture.byAlreadySpawned(entity))
                .filter(boardFurniture -> CommissionBoardFurniture.BOARD_ITEM_ID.equals(boardFurniture.getNamespacedID()))
                .ifPresent(boardFurniture -> boardFurniture.remove(false));
    }

    private static Optional<Block> findBlockAt(Coordinate coordinate) {
        return Optional.ofNullable(Bukkit.getWorld(coordinate.chunkCoord().world()))
                .map(world -> world.getBlockAt(coordinate.x().intValue(), coordinate.y().intValue(), coordinate.z().intValue()));
    }

    private static float toYaw(BoardFacing facing) {
        return switch (facing) {
            case NORTH -> 0;
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> -90;
        };
    }
}

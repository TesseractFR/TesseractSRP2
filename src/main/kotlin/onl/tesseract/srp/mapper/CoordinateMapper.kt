package onl.tesseract.srp.mapper

import onl.tesseract.srp.domain.commun.Coordinate
import org.bukkit.Bukkit
import org.bukkit.Location

fun Location.toCoordinate(): Coordinate {
    return Coordinate(this.x, this.y, this.z, this.toChunkCoord())
}

fun Coordinate.toLocation(): Location {
    val world = Bukkit.getWorld(chunkCoord.world)
    return Location(world, x, y,  z)
}

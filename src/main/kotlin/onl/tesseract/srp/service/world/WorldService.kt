package onl.tesseract.srp.service.world

import onl.tesseract.srp.domain.world.SrpWorld
import onl.tesseract.srp.exception.WorldMissingException
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.springframework.stereotype.Service

@Service
class WorldService {

    fun getSrpWorld(world: World): SrpWorld? {
        return SrpWorld.entries.find { it.bukkitName == world.name }
    }

    fun getSrpWorld(location: Location): SrpWorld? {
        return getSrpWorld(location.world)
    }

    /**
     * @throws WorldMissingException If the world does not exist in bukkit
     */
    fun getBukkitWorld(srpWorld: SrpWorld): World {
        return Bukkit.getWorld(srpWorld.bukkitName)
            ?: throw WorldMissingException("Missing world ${srpWorld.bukkitName}!")
    }
}
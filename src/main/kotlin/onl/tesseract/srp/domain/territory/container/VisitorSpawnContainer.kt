package onl.tesseract.srp.domain.territory.container

import onl.tesseract.srp.domain.commun.Coordinate
import onl.tesseract.srp.domain.territory.enum.result.SetSpawnResult
import java.util.UUID

interface VisitorSpawnContainer {
    fun setVisitorSpawnpoint(newLocation: Coordinate, player: UUID): SetSpawnResult
    fun canSetSpawn(player: UUID): Boolean
    fun getVisitorSpawnpoint() : Coordinate
}

class DefaultVisitorSpawnContainer(protected var visitorSpawnPoint: Coordinate) : VisitorSpawnContainer{
    override fun setVisitorSpawnpoint(newLocation: Coordinate, player: UUID): SetSpawnResult {
        visitorSpawnPoint = newLocation
        return SetSpawnResult.SUCCESS
    }

    override fun canSetSpawn(player: UUID): Boolean {
        throw IllegalAccessException("Must be defined")
    }

    override fun getVisitorSpawnpoint(): Coordinate {
        return visitorSpawnPoint
    }
}
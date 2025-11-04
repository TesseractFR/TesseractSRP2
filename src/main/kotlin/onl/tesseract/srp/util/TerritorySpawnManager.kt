package onl.tesseract.srp.util

import org.bukkit.Location

object TerritorySpawnManager {

    enum class SetSpawnResult { SUCCESS, NOT_AUTHORIZED, INVALID_WORLD, OUTSIDE_TERRITORY }

    data class SetSpawnPolicy(
        val isCorrectWorld: (Location) -> Boolean,
        val requireInsideTerritory: Boolean = true
    )

    data class SetSpawnOperations(
        val authorized: () -> Boolean,
        val isInsideTerritory: (Location) -> Boolean = { true },
        val setAndPersist: (Location) -> Boolean
    )

    fun setSpawn(
        newLocation: Location,
        policy: SetSpawnPolicy,
        io: SetSpawnOperations
    ): SetSpawnResult {
        val result = when {
            !io.authorized() -> SetSpawnResult.NOT_AUTHORIZED
            !policy.isCorrectWorld(newLocation) -> SetSpawnResult.INVALID_WORLD
            policy.requireInsideTerritory && !io.isInsideTerritory(newLocation) ->
                SetSpawnResult.OUTSIDE_TERRITORY
            else -> if (io.setAndPersist(newLocation)) SetSpawnResult.SUCCESS
            else SetSpawnResult.OUTSIDE_TERRITORY
        }
        return result
    }
}

package onl.tesseract.srp.domain.territory.enum

import onl.tesseract.srp.domain.world.SrpWorld

private const val ELYSEA_CREATION_DISTANCE = 10
private const val ELYSEA_CLAIM_DISTANCE = 3
private const val GUILDWORLD_CREATION_DISTANCE = 50
private const val GUILDWORLD_CLAIM_DISTANCE = 10

enum class TerritoryWorld(
    val srpWorld: SrpWorld,
    val creationDistance : Int,
    val claimDistance : Int,
) {
    ELYSEA(SrpWorld.Elysea,ELYSEA_CREATION_DISTANCE,ELYSEA_CLAIM_DISTANCE),
    GUILDWORLD(SrpWorld.GuildWorld,GUILDWORLD_CREATION_DISTANCE,GUILDWORLD_CLAIM_DISTANCE)
}

package onl.tesseract.srp.domain.territory.enum

import onl.tesseract.srp.domain.world.SrpWorld

enum class TerritoryWorld(
    val srpWorld: SrpWorld,
    val creationDistance : Int,
    val claimDistance : Int,
) {
    ELYSEA(SrpWorld.Elysea,10,3),
    GUILDWORLD(SrpWorld.GuildWorld,50,10)
}
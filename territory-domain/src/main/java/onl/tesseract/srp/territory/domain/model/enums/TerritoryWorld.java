package onl.tesseract.srp.territory.domain.model.enums;


import lombok.Getter;
import onl.tesseract.srp.common.domain.model.world.SrpWorld;

@Getter
public enum TerritoryWorld {
    ELYSEA(SrpWorld.Elysea, 10, 3),
    GUILDWORLD(SrpWorld.GuildWorld, 50, 10);

    private final SrpWorld srpWorld;
    private final int creationDistance;
    private final int claimDistance;

    TerritoryWorld(SrpWorld srpWorld, int creationDistance, int claimDistance) {
        this.srpWorld = srpWorld;
        this.creationDistance = creationDistance;
        this.claimDistance = claimDistance;
    }
}


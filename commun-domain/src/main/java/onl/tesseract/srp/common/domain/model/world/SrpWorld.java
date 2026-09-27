package onl.tesseract.srp.common.domain.model.world;

import lombok.Getter;

@Getter
public enum SrpWorld{

    Elysea("Elyséa", new WorldName("elysea"), false),
    Anterra("Anterra", new WorldName("anterra"),  true),
    Helya("Helya", new WorldName("helya"),  true),
    Nemesis("Némesis", new WorldName("nemesis"),  true),
    GuildWorld("Uh", new WorldName("guildworld"), false),
    ;

    private final String displayName;
    private final WorldName bukkitName;
    private final Boolean resourceWorld;

    SrpWorld(
            String displayName,
            WorldName bukkitName,
            Boolean resourceWorld
    ){
        this.displayName = displayName;
        this.bukkitName = bukkitName;
        this.resourceWorld = resourceWorld;
    }
}

package onl.tesseract.srp.common.domain.model.world;

import lombok.Getter;

@Getter
public enum SrpWorld{

    Elysea("Elyséa", "elysea", false),
    Anterra("Anterra", "anterra",  true),
    Helya("Helya", "helya",  true),
    Nemesis("Némesis", "nemesis",  true),
    GuildWorld("Uh", "guildworld", false),
    ;

    private final String displayName;
    private final String bukkitName;
    private final Boolean resourceWorld;

    SrpWorld(
            String displayName,
            String bukkitName,
            Boolean resourceWorld
    ){
        this.displayName = displayName;
        this.bukkitName = bukkitName;
        this.resourceWorld = resourceWorld;
    }
}
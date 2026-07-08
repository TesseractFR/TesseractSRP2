package onl.tesseract.srp.skill.domain.model.crafting;

import java.util.ArrayList;
import java.util.List;

public record LootCache(
        List<CraftElement> garbage,
        List<CraftElement> done
) {
    public LootCache(){
        this(new ArrayList<>(), new ArrayList<>());
    }
}

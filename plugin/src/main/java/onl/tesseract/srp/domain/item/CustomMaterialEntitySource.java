package onl.tesseract.srp.domain.item;

import org.bukkit.entity.EntityType;

public record CustomMaterialEntitySource(EntityType entityType) implements CustomMaterialSource {
}


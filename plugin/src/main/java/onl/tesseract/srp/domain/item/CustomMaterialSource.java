package onl.tesseract.srp.domain.item;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public sealed interface CustomMaterialSource permits CustomMaterialBlockSource, CustomMaterialEntitySource {
}


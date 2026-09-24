package onl.tesseract.srp.customitem.domain.model;

import onl.tesseract.srp.common.domain.model.ItemTag;

public record CustomMaterial(
    MaterialName name,
    MaterialName displayName,
    ItemTag itemTag,
    Rarity rarity
) {
}

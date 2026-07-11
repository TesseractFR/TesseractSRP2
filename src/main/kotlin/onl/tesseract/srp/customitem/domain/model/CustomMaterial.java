package onl.tesseract.srp.customitem.domain.model;

public record CustomMaterial(
    MaterialName name,
    MaterialName displayName,
    ItemTag itemTag,
    Rarity rarity
) {
}

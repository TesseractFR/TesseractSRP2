package onl.tesseract.srp.domain.item

data class CustomItem(
    val material: CustomMaterial,
    val quality: Quality,
    val quantity: Int
) {
}

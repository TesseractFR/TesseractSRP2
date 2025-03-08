package onl.tesseract.srp.domain.item

data class CustomItem(
    val material: CustomMaterial,
    val quality: Int,
) {

    init {
        require(quality in 1..100)
    }
}

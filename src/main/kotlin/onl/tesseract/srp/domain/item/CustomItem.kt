package onl.tesseract.srp.domain.item

import onl.tesseract.srp.customitem.domain.model.Quality

data class CustomItem(
    val material: CustomMaterial,
    val quality: Quality,
    val quantity: Int
) {
}

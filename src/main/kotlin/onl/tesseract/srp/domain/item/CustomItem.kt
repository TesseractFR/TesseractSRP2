package onl.tesseract.srp.domain.item

data class CustomItem(
    val material: CustomMaterial,
    val quality: Int,
) {

    init {
        require(quality in 1..100)
    }

    fun toStack(amount: Int = 1) = CustomItemStack(this, amount)
}

class CustomItemStack(val item: CustomItem, val amount: Int = 1)
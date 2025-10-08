package onl.tesseract.srp.domain.craftmanship

data class Recipe(
    val components : Map<Int, Map<ComponentWrapper, Int>>,
    val result: CustomItem
)

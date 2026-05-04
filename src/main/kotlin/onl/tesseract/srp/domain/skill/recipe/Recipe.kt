package onl.tesseract.srp.domain.skill.recipe

data class Recipe(
    val name: String,
    val slot: Int,
    val components : Map<Int, RecipeComponent>,
    val result: RecipeComponent,
    val tier: Int = 1
){
}

data class RecipeComponent(
    val item: ComponentWrapper,
    val quantity: Int
)
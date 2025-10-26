package onl.tesseract.srp.domain.skill

data class Structure(
    val name : String,
    val menuName: String,
    val recipes : Map<Int,Map<Int, Recipe>>
)

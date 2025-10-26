package onl.tesseract.srp.domain.skill

data class Skill(
    val recipe : Map<Int,SkillTier>,
    val structureName: String,
    val name: String
) {
}
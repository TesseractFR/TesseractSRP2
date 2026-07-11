package onl.tesseract.srp.domain.skill

import onl.tesseract.srp.domain.skill.recipe.Recipe

data class Skill(
    val recipe : Map<Int,SkillTier>,
    val structureName: String,
    val name: String
)

data class SkillTier(
    val recipes: Map<Int, Recipe>,
    val recipeByName: Map<String, Recipe>
)
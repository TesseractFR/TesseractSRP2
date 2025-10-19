package onl.tesseract.srp.controller.menu.craftmanship

import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.srp.controller.menu.ItemAdderMenu
import onl.tesseract.srp.domain.craftingjob.Recipe

class RecipeMenu(val recipeMap: Map<Int,Map<Int, Recipe>>?) : ItemAdderMenu(MenuSize.Six,"tesseract:test","testMenu",
    null){


}
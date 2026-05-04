package onl.tesseract.srp.domain.skill.crafting

import onl.tesseract.srp.domain.item.CustomItem

data class LootCache(
    val done: MutableList<CustomItem> = mutableListOf(),
    val garbage: MutableList<CustomItem> = mutableListOf()
    )
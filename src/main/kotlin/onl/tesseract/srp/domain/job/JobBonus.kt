package onl.tesseract.srp.domain.job

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.item.CustomMaterial

interface JobBonus {
    fun getLootChanceBonus(event: JobHarvestEvent): Float
    fun getMoneyBonus(event: JobHarvestEvent): Float
    fun getQualityBonus(event: JobHarvestEvent): Float

    fun getDescription(): Component
}

data class GenericMaterialJobBonus(val material: CustomMaterial, val type: JobBonusType, val value: Float) : JobBonus {
    override fun getLootChanceBonus(event: JobHarvestEvent): Float {
        return if (event.material == this.material && type == JobBonusType.LootChance) value else 0f
    }
    override fun getMoneyBonus(event: JobHarvestEvent): Float {
        return if (event.material == this.material && type == JobBonusType.Money) value else 0f
    }
    override fun getQualityBonus(event: JobHarvestEvent): Float {

        return if (event.material == this.material && type == JobBonusType.Quality) value else 0f
    }

    override fun getDescription(): Component {
        return NamedTextColor.BLUE + material.displayName +
                (NamedTextColor.GRAY + " : $type +${(value * 100).toInt()}%")
    }
}

/**
 * Bonuses that a job skill can contain
 */
enum class JobBonusType {
    /**
     * Additional money in a mission's reward, percentage of the base gain.
     */
    Money,

    /**
     * Additional loot chance during harvest, as a percentage of the base chance
     */
    LootChance,

    Quality
}
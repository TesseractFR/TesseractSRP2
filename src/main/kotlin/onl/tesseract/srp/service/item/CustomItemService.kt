package onl.tesseract.srp.service.item

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.persistantcontainer.NamedspacedKeyProvider
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.item.CustomItem
import onl.tesseract.srp.domain.item.CustomItemStack
import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.domain.job.BaseStat
import onl.tesseract.srp.domain.job.Job
import onl.tesseract.srp.repository.hibernate.job.JobsConfigRepository
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.springframework.stereotype.Service
import java.util.Random

@Service
class CustomItemService(
    private val namespacedKeyProvider: NamedspacedKeyProvider,
    private val jobConfig: JobsConfigRepository
) {
    private fun getJobs(): Map<String, Job> = jobConfig.getJobs()

    private fun getJobByMaterial(material: CustomMaterial): Job? {
        return getJobs().values.find { job ->
            job.materials.any {
                it.name.equals(material.name, ignoreCase = true) ||
                        it.dropSource.toString().equals(material.dropSource.toString(), ignoreCase = true)
            }
        }
    }

    private fun getBaseStat(material: CustomMaterial): BaseStat? {
        val job = getJobByMaterial(material) ?: return null
        return job.baseStats[material]
    }

    fun attemptDrop(material: CustomMaterial): CustomItem? {
        val baseStat = getBaseStat(material) ?: return null
        val roll = Random().nextFloat()
        return if (roll <= baseStat.lootChance) {
            val quality = generateQuality(baseStat)
            CustomItem(material, quality)
        } else null
    }

    fun createCustomItem(customItem: CustomItemStack): ItemStack {
        val item = ItemBuilder(customItem.item.material.customMaterial)
            .name(customItem.item.material.displayName)
            .color(NamedTextColor.GREEN)
            .lore()
            .newline()
            .append(NamedTextColor.GRAY + "Objet de métier")
            .newline()
            .append(NamedTextColor.GRAY + "Qualité : " + (getQualityColorGradient(customItem.item.quality) + "${customItem.item.quality}%"))
            .buildLore()
            .amount(customItem.amount)
            .build()
        item.editMeta {
            val dataContainer = it.persistentDataContainer
            dataContainer.set(namespacedKeyProvider.get("customMaterial"), PersistentDataType.STRING, customItem.item.material.name)
            dataContainer.set(namespacedKeyProvider.get("quality"), PersistentDataType.INTEGER, customItem.item.quality)
        }
        return item
    }

    private fun generateQuality(baseStat: BaseStat): Int {
        val gaussian = Random().nextGaussian() * baseStat.qualityDistribution.stddev + baseStat.qualityDistribution.expectation
        return gaussian.toInt().coerceIn(1, 100)
    }

    private fun getQualityColorGradient(quality: Int): TextColor {
        val ratio: Float = quality.coerceAtMost(80) / 80.0f
        return TextColor.color(1.0f - ratio, ratio, 0.0f)
    }
}
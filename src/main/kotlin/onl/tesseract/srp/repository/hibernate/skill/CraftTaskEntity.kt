package onl.tesseract.srp.repository.hibernate.skill

import jakarta.persistence.*
import onl.tesseract.srp.domain.item.CustomItem
import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.domain.item.Quality
import onl.tesseract.srp.domain.skill.crafting.CraftTask
import onl.tesseract.srp.domain.skill.crafting.CraftingBonus
import onl.tesseract.srp.domain.skill.crafting.QueuedRecipe
import onl.tesseract.srp.domain.skill.recipe.Recipe
import java.util.*
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

@Entity
@Table(name = "t_craft_task")
class CraftTaskEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val playerUuid: UUID,
    val skillName: String,

    @Embedded
    val queuedRecipe: QueuedRecipeEmbeddable,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "t_craft_task_done", joinColumns = [JoinColumn(name = "task_id")])
    val done: MutableList<CustomItemEmbeddable> = mutableListOf(),

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "t_craft_task_garbage", joinColumns = [JoinColumn(name = "task_id")])
    val garbage: MutableList<CustomItemEmbeddable> = mutableListOf(),

    val unitDurationNanos: Long,
    val timeLeftNanos: Long,

    @Embedded
    val bonus: CraftingBonusEmbeddable,

    val isQueue: Boolean = false,
    val queueOrder: Int = 0
) {
    fun toCraftTask(recipe: Recipe): CraftTask {
        return CraftTask(
            queuedRecipe = queuedRecipe.toDomain(recipe),
            done = done.map { it.toDomain() }.toMutableList(),
            garbage = garbage.map { it.toDomain() }.toMutableList(),
            unitDuration = unitDurationNanos.nanoseconds,
            bonus = bonus.toDomain(),
            timeLeft = timeLeftNanos.nanoseconds
        )
    }

    companion object {
        fun fromDomain(
            playerUuid: UUID,
            skillName: String,
            task: CraftTask,
            recipeId: String,
            isQueue: Boolean = false,
            queueOrder: Int = 0
        ): CraftTaskEntity {
            return CraftTaskEntity(
                playerUuid = playerUuid,
                skillName = skillName,
                queuedRecipe = QueuedRecipeEmbeddable.fromDomain(task.queuedRecipe, recipeId),
                done = task.done.map { CustomItemEmbeddable.fromDomain(it) }.toMutableList(),
                garbage = task.garbage.map { CustomItemEmbeddable.fromDomain(it) }.toMutableList(),
                unitDurationNanos = task.unitDuration.inWholeNanoseconds,
                timeLeftNanos = task.getTotalTimeLeft().inWholeNanoseconds,
                bonus = CraftingBonusEmbeddable.fromDomain(task.bonus),
                isQueue = isQueue,
                queueOrder = queueOrder
            )
        }

        fun fromQueuedRecipe(
            playerUuid: UUID,
            skillName: String,
            queuedRecipe: QueuedRecipe,
            recipeId: String,
            order: Int,
            bonus: CraftingBonus
        ): CraftTaskEntity {
            return CraftTaskEntity(
                playerUuid = playerUuid,
                skillName = skillName,
                queuedRecipe = QueuedRecipeEmbeddable.fromDomain(queuedRecipe, recipeId),
                unitDurationNanos = 5.seconds.inWholeNanoseconds, // Valeur par défaut dans SkillService
                timeLeftNanos = 5.seconds.inWholeNanoseconds * queuedRecipe.quantity,
                bonus = CraftingBonusEmbeddable.fromDomain(bonus),
                isQueue = true,
                queueOrder = order
            )
        }
    }
}

@Embeddable
class QueuedRecipeEmbeddable(
    val recipeId: String,
    val recipeTier: Int,
    @Enumerated(EnumType.STRING)
    val compoQuality: Quality,
    var quantity: Int
) {
    fun toDomain(recipe: Recipe): QueuedRecipe {
        return QueuedRecipe(recipe, compoQuality, quantity)
    }

    companion object {
        fun fromDomain(queuedRecipe: QueuedRecipe, recipeId: String): QueuedRecipeEmbeddable {
            return QueuedRecipeEmbeddable(
                recipeId = recipeId,
                recipeTier = queuedRecipe.recipe.tier,
                compoQuality = queuedRecipe.compoQuality,
                quantity = queuedRecipe.quantity
            )
        }
    }
}

@Embeddable
class CustomItemEmbeddable(
    @Enumerated(EnumType.STRING)
    val material: CustomMaterial,
    @Enumerated(EnumType.STRING)
    val quality: Quality,
    val quantity: Int
) {
    fun toDomain(): CustomItem {
        return CustomItem(material, quality, quantity)
    }

    companion object {
        fun fromDomain(item: CustomItem): CustomItemEmbeddable {
            return CustomItemEmbeddable(item.material, item.quality, item.quantity)
        }
    }
}

@Embeddable
class CraftingBonusEmbeddable(
    val qualityBonus: Double,
    val successBonus: Double,
    val doubleCraftBonus: Double,
    val garbageRefundBonus: Double,
    val craftRefundBonus: Double
) {
    fun toDomain(): CraftingBonus {
        return CraftingBonus(qualityBonus, successBonus, doubleCraftBonus, garbageRefundBonus, craftRefundBonus)
    }

    companion object {
        fun fromDomain(bonus: CraftingBonus): CraftingBonusEmbeddable {
            return CraftingBonusEmbeddable(
                bonus.qualityBonus,
                bonus.successBonus,
                bonus.doubleCraftBonus,
                bonus.garbageRefundBonus,
                bonus.craftRefundBonus
            )
        }
    }
}

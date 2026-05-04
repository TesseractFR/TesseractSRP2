package onl.tesseract.srp.repository.hibernate.skill

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "t_skill_result_cache")
class SkillResultCacheEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val playerUuid: UUID,
    val skillName: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "t_skill_result_cache_done", joinColumns = [JoinColumn(name = "cache_id")])
    val done: MutableList<CustomItemEmbeddable> = mutableListOf(),

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "t_skill_result_cache_garbage", joinColumns = [JoinColumn(name = "cache_id")])
    val garbage: MutableList<CustomItemEmbeddable> = mutableListOf()
)

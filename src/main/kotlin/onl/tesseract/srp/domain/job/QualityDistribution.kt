package onl.tesseract.srp.domain.job

import kotlinx.serialization.Serializable

@Serializable
data class QualityDistribution(
    val expectation: Int,
    val stddev: Float
)

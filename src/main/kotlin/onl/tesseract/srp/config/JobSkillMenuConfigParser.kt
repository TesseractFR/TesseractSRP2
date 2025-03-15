package onl.tesseract.srp.config

import onl.tesseract.lib.exception.ConfigurationException
import onl.tesseract.srp.config.JobSkillMenuConfig.Arrow
import onl.tesseract.srp.config.JobSkillMenuConfig.ArrowType.*
import onl.tesseract.srp.config.JobSkillMenuConfig.CellType
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.domain.job.JobSkill
import org.springframework.stereotype.Component

@Component
class JobSkillMenuConfigParser {

    /**
     * @throws ConfigurationException If config file not found
     * @throws java.io.IOException On IO error
     */
    fun parseForJob(enumJob: EnumJob): JobSkillMenuConfig {
        javaClass.classLoader.getResourceAsStream("$enumJob.skills")?.use { stream ->
            var str = String(stream.readAllBytes())
            val lines = str.split("\n")

            val symbolDefs = parseSymbolDefs(lines)

            val graphLines = lines.dropWhile { !it.startsWith("=") }
                .drop(1)
                .takeWhile { it != "END" }
                .reversed()
            return parseGraph(graphLines, symbolDefs)
        } ?: throw ConfigurationException("Configuration file not found for skill menu of job $enumJob")
    }

    private fun parseGraph(
        lines: List<String>,
        symbolDefs: Map<Char, CellType>,
    ): JobSkillMenuConfig {
        val matrix: Array<Array<CellType>> = Array(lines.size) { lineIndex ->
            val line = lines[lineIndex]
            return@Array Array(9) {
                if (it >= line.length)
                    JobSkillMenuConfig.EmptyCell
                else
                    symbolDefs[line.toCharArray()[it]]!!
            }
        }
        return JobSkillMenuConfig(matrix)
    }

    private fun parseSymbolDefs(lines: List<String>): Map<Char, CellType> {
        val map = generateDefaultSymbols()
        lines.takeWhile { !it.startsWith("=") }
            .forEach { line ->
                val symbol = line.elementAt(0)
                val def = line.substringAfter("=")
                map[symbol] = if (def.equals("ROOT", ignoreCase = true))
                    JobSkillMenuConfig.RootCell
                else
                    JobSkillMenuConfig.SkillCell(JobSkill.valueOf(def))
            }
        return map
    }

    private fun generateDefaultSymbols(): MutableMap<Char, CellType> {
        return mutableMapOf(
            '•' to JobSkillMenuConfig.EmptyCell,
            ' ' to JobSkillMenuConfig.EmptyCell,
            '└' to Arrow(TopRight),
            '┘' to Arrow(TopLeft),
            '─' to Arrow(Horizontal),
            '│' to Arrow(Vertical),
            '┬' to Arrow(T),
            '┼' to Arrow(Cross),
            '┴' to Arrow(ReversedT),
            '├' to Arrow(RightT),
            '┤' to Arrow(LeftT),
        )
    }
}

data class JobSkillMenuConfig(
    val cells: Array<Array<CellType>>
) {

    inline fun forEach(startLine: Int, height: Int, action: (Int, Int, CellType) -> Unit) {
        val maxLineIndex = (startLine + height).coerceAtMost(cells.size)
        for (lineIndex in startLine until maxLineIndex) {
            this.cells[lineIndex].forEachIndexed { colIndex, cellType ->
                action(lineIndex, colIndex, cellType)
            }
        }
    }

    sealed interface CellType
    data object EmptyCell : CellType
    data class Arrow(val type: ArrowType) : CellType
    data object RootCell : CellType
    data class SkillCell(val skill: JobSkill) : CellType

    enum class ArrowType(val customModelData: Int) {
        TopRight(1),
        TopLeft(2),
        Horizontal(3),
        Vertical(4),
        T(5),
        Cross(6),
        ReversedT(7),
        RightT(8),
        LeftT(9),
    }
}
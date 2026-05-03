package onl.tesseract.srp.domain.item

enum class Quality {
    POOR,
    NORMAL,
    GOOD,
    VERY_GOOD,
    EXCEPTIONAL;

    fun next(): Quality {
        return entries.getOrNull(ordinal + 1) ?: EXCEPTIONAL
    }
}
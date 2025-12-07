package onl.tesseract.srp.domain.territory.enum.result

enum class ClaimResult {
    SUCCESS,
    TERRITORY_NOT_FOUND,
    ALREADY_OWNED,
    ALREADY_OTHER,
    NOT_ADJACENT,
    NOT_ALLOWED,
    TOO_CLOSE,
    INVALID_WORLD
}

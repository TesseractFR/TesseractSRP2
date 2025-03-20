package onl.tesseract.srp.domain.player

enum class PlayerRank(val cost: Int) {
    Survivant(0),
    Explorateur(500),
    Aventurier(2000),
    Noble(10_000),
    Baron(50_000),
    Seigneur(250_000),
    Vicomte(1_000_000),
    Comte(5_000_000),
    Duc(15_000_000),
    Roi(50_000_000),
    Empereur(100_000_000),
}
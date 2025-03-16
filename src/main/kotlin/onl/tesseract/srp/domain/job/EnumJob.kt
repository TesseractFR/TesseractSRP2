package onl.tesseract.srp.domain.job

import org.bukkit.Material

enum class EnumJob(val displayName: String, val icon: Material) {
    Mineur("Mineur", Material.IRON_PICKAXE),
    Terrassier("Terrassier", Material.IRON_SHOVEL),
    Bucheron("Bucheron", Material.IRON_AXE),
    Pecheur("Pecheur", Material.FISHING_ROD),
    Chasseur("Chasseur", Material.BOW),
    Gardien("Gardien", Material.IRON_SWORD),
    Agriculteur("Agriculteur", Material.IRON_HOE),
    Fleuriste("Fleuriste", Material.RED_TULIP),
}
package onl.tesseract.srp.util.equipment.annexionStick

import org.bukkit.Material
import java.util.UUID

class GuildAnnexionStickInvocable(
    playerUUID: UUID,
    isInvoked: Boolean = false,
    handSlot: Int = 0
) : AnnexionStickInvocable(playerUUID, isInvoked, handSlot) {
    override val baseCommand: String = "guild"
    override val displayName: String = "Bâton d'Annexion (Guilde)"
    override val material: Material = Material.BREEZE_ROD
}

package onl.tesseract.srp.util.equipment.annexionStick

import org.bukkit.Material
import java.util.UUID

class CampementAnnexionStickInvocable(
    playerUUID: UUID,
    isInvoked: Boolean = false,
    handSlot: Int = 0
) : AnnexionStickInvocable(playerUUID, isInvoked, handSlot) {
    override val baseCommand: String = "campement"
    override val displayName: String = "Bâton d'Annexion (Campement)"
    override val material: Material = Material.STICK
}

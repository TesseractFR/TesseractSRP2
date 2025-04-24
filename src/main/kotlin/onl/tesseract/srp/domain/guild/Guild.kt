package onl.tesseract.srp.domain.guild

import org.bukkit.Location
import java.util.*

class Guild(
    val id: Int,
    val leaderId: UUID,
    val name: String,
    val spawnLocation: Location,
)
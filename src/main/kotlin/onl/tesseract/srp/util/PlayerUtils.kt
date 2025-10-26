package onl.tesseract.srp.util

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.springframework.stereotype.Component

@Component
open class PlayerUtils {
    open fun asPlayer(actor: Any?): Player? = when (actor) {
        is Player -> actor
        is Projectile -> actor.shooter as? Player
        is Entity -> null
        else -> null
    }
}
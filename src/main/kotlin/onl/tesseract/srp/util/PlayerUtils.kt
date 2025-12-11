package onl.tesseract.srp.util

import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile

object PlayerUtils {
    fun asPlayer(actor: Any?): Player? = when (actor) {
        is Player -> actor
        is Projectile -> actor.shooter as? Player
        is Entity -> null
        else -> null
    }

    fun tryFreeChestplateSlot(player: Player): Boolean {
        val chestplate = player.inventory.chestplate ?: return true
        val leftovers = player.inventory.addItem(chestplate)
        return if (leftovers.isEmpty()) {
            player.inventory.chestplate = null
            true
        } else {
            false
        }
    }

}
package onl.tesseract.srp.util

import com.destroystokyo.paper.profile.PlayerProfile
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object PlayerUtils {
    private val profileMap: MutableMap<UUID, PlayerProfile> = ConcurrentHashMap()

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

    private fun getPlayerSkinProfile(playerUUID: UUID): PlayerProfile {
        return profileMap[playerUUID] ?: preloadPlayerProfile(playerUUID)
    }

    /**
     * Get the head of a player. May perform a blocking request to complete the profile, be
     * careful to call this method in an async context. The profile will be cached for next retrievals.
     */
    fun getPlayerHead(uuid: UUID): ItemStack {
        val playerProfile = getPlayerSkinProfile(uuid)
        val item = ItemStack(Material.PLAYER_HEAD)
        item.editMeta { meta ->
            meta as SkullMeta
            meta.playerProfile = playerProfile
        }
        return item
    }

    private fun registerPlayerProfile(uuid: UUID, profile: PlayerProfile) {
        profileMap[uuid] = profile
    }

    /**
     * Loads the player profile to store the skin texture. Will make a blocking request to complete the profile, be
     * careful to call this method in an async context.
     */
    private fun preloadPlayerProfile(playerUUID: UUID): PlayerProfile {
        val playerProfile = Bukkit.createProfile(playerUUID)
        playerProfile.complete()
        registerPlayerProfile(playerUUID, playerProfile)
        return playerProfile
    }

}
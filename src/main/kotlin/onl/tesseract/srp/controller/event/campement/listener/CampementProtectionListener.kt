package onl.tesseract.srp.controller.event.campement.listener

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.controller.event.global.listener.ChunkProtectionListener
import onl.tesseract.srp.service.campement.CampementService
import onl.tesseract.srp.service.campement.InteractionAllowResult
import onl.tesseract.srp.util.CampementChatError
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class CampementProtectionListener(val campementService: CampementService) : ChunkProtectionListener() {
    override fun hasProcessingResponsibility(chunk: Chunk): Boolean {
        return campementService.getCampementByChunk(chunk.x, chunk.z) != null
    }

    override fun getProtectionMessage(chunk : Chunk): Component {
        val camp = campementService.getCampementByChunk(chunk.x, chunk.z)
        require(camp!=null){

        }
        val ownerName = Bukkit.getOfflinePlayer(camp.ownerID).name ?: "Inconnu"
        return CampementChatError + "Tu ne peux pas interagir ici ! Ce terrain appartient à " +
                    Component.text(ownerName, NamedTextColor.GOLD) + "."

    }


    override fun canPlaceBlock(player: Player, block: Block): Boolean {
        return (InteractionAllowResult.Deny != campementService.canInteractInChunk(player.uniqueId,block.chunk))
    }

    override fun canBreakBlock(player: Player, block: Block): Boolean {
        return (InteractionAllowResult.Deny != campementService.canInteractInChunk(player.uniqueId,block.chunk))
    }

    override fun canOpenContainer(
        player: Player,
        container: Container,
    ): Boolean {
        if(Material.ENDER_CHEST == container.type) return true
        return (InteractionAllowResult.Deny != campementService.canInteractInChunk(player.uniqueId,container.chunk))
    }

}
package onl.tesseract.srp.controller.event.global.listener

import net.kyori.adventure.text.Component
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.service.campement.CampementService
import onl.tesseract.srp.util.CampementChatError
import org.bukkit.Chunk
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class NatureProtectionListener(val campementService: CampementService) : ChunkProtectionListener() {
    override fun hasProcessingResponsibility(chunk: Chunk): Boolean {
        return campementService.getCampementByChunk(chunk.x,chunk.z) == null
    }

    override fun getProtectionMessage(chunk: Chunk): Component {
        return CampementChatError + "Tu ne peux pas interagir dans la nature."
    }

    override fun canPlaceBlock(player: Player, block: Block): Boolean {
        return false
    }

    override fun canBreakBlock(player: Player, block: Block): Boolean {
        return false
    }

    override fun canOpenContainer(
        player: Player,
        container: Container,
    ): Boolean {
        return true
    }
}
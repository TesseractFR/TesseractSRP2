package onl.tesseract.srp.controller.event.global.listener

import net.kyori.adventure.text.Component
import org.bukkit.Chunk
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent

abstract class ChunkProtectionListener : Listener {
    protected abstract fun hasProcessingResponsibility(chunk: Chunk) : Boolean

    protected abstract fun getProtectionMessage(chunk: Chunk): Component

    protected abstract fun canPlaceBlock(player: Player,block: Block) : Boolean

    protected abstract fun canBreakBlock(player: Player,block: Block) : Boolean

    protected abstract fun canOpenContainer(player: Player, container: Container) : Boolean

//    protected abstract fun canOpenContainer(player: Player)

    @EventHandler
    fun onPlayerPlaceBlock(event: BlockPlaceEvent){
        if(!hasProcessingResponsibility(event.block.chunk))return
        if(!canPlaceBlock(event.player,event.block)){
            event.player.sendMessage { getProtectionMessage(event.block.chunk) }
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerOpenContainer(event: PlayerInteractEvent) {
        if(!event.hasBlock()
            || event.clickedBlock == null
            || event.clickedBlock!!.state !is Container)return
        if(!canOpenContainer(event.player,event.clickedBlock!!.state as Container)){
                event.player.sendMessage { getProtectionMessage(event.clickedBlock!!.chunk) }
                event.isCancelled = true
            }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if(!hasProcessingResponsibility(event.block.chunk))return
        if(!canBreakBlock(event.player,event.block)){
            event.player.sendMessage { getProtectionMessage(event.block.chunk) }
            event.isCancelled = true
        }
    }
}
package onl.tesseract.srp.controller.event.structure

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class ForgeStructureListener :  CustomStructureListener("tesseract:foundry") {
    override fun onClick(player: Player) {
        player.sendMessage { Component.text(structureName) }
    }
}
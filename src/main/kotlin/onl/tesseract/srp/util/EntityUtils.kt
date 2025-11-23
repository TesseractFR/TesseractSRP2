package onl.tesseract.srp.util

import org.bukkit.entity.*

object EntityUtils {
    fun isSaddlable(entity: Entity): Boolean = when (entity) {
        is Pig, is Strider, is Horse, is Donkey, is Mule, is Camel -> true
        else -> false
    }

    fun isLivingMount(entity: Entity): Boolean = when (entity) {
        is Horse, is Donkey, is Mule, is Camel, is Pig, is Strider -> true
        else -> false
    }

}
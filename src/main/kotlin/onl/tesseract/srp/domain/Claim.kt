package onl.tesseract.srp.domain

import org.bukkit.Location

data class Claim(val x: Int, val z: Int) {

    constructor(location: Location): this(location.chunk.x, location.chunk.z)

    override fun toString(): String = "($x, $z)"
}

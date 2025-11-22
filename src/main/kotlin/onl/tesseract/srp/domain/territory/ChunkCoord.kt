package onl.tesseract.srp.domain.territory

data class ChunkCoord(val x: Int, val z: Int, val world: String){
    override fun toString(): String = "($x, $z, $world)"
}

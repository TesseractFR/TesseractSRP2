package onl.tesseract.srp.domain.commun

data class ChunkCoord(val x: Int, val z: Int, val world: String){
    override fun toString(): String = "($x, $z, $world)"

    override fun equals(other: Any?): Boolean {
        if(other==null || other::class != this::class)return false
        other as ChunkCoord
        return this.x == other.x && this.z == other.z && this.world == other.world
    }

}

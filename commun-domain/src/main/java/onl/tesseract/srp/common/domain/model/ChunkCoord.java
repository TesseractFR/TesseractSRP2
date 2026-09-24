package onl.tesseract.srp.common.domain.model;

public record ChunkCoord(int x, int z, String world){

    @Override
    public String toString(){
        return x+", "+z+", "+world;
    }
}

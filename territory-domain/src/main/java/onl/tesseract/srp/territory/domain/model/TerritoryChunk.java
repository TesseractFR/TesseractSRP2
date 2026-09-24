package onl.tesseract.srp.territory.domain.model;

import lombok.Getter;
import onl.tesseract.srp.common.domain.model.ChunkCoord;


public abstract class TerritoryChunk{

    @Getter
    private final ChunkCoord chunkCoord;

    public TerritoryChunk(ChunkCoord chunkCoord){
        this.chunkCoord = chunkCoord;
    }


    @Override
    public String toString(){
        return chunkCoord.toString();
    }

    public abstract Territory<? extends TerritoryChunk> getOwner();

    @Override
    public boolean equals(Object other){
        if(other == null || other.getClass() != this.getClass()) return false;
        TerritoryChunk otherTerritoryChunk = (TerritoryChunk) other;
        return this.getOwner().equals(otherTerritoryChunk.getOwner())
                && this.chunkCoord.equals(otherTerritoryChunk.chunkCoord);

    }
}

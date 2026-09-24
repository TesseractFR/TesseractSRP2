package onl.tesseract.srp.territory.domain.model.guild;

import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.territory.domain.model.TerritoryChunk;

public class GuildChunk extends TerritoryChunk {


    private final Guild guild;

    public GuildChunk(ChunkCoord chunkCoord, Guild guild){
        super(chunkCoord);
        this.guild = guild;
    }

    @Override
    public Guild getOwner() {
        return guild;
    }
}

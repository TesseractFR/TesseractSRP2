package onl.tesseract.srp.common.domain.model;

public record Coordinate(
    Double x,
    Double y,
    Double z,
    ChunkCoord chunkCoord
){}

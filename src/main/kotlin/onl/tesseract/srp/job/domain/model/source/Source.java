package onl.tesseract.srp.job.domain.model.source;

import onl.tesseract.srp.job.domain.model.Material;

public record Source(
        Material material,
        SourceType type
) {


}

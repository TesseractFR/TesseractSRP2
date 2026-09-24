package onl.tesseract.srp.customitem.domain.model;

import onl.tesseract.srp.common.domain.model.enums.Quality;

public record CustomItem(
        CustomMaterial material,
        Quality quality
) {
}

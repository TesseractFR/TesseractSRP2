package onl.tesseract.srp.contract.adapter.utils;

import onl.tesseract.srp.common.domain.model.enums.Quality;

public final class QualityConverter {

    private QualityConverter() {
    }

    public static onl.tesseract.srp.common.domain.model.enums.Quality toCommon(Quality quality) {
        return onl.tesseract.srp.common.domain.model.enums.Quality.valueOf(quality.name());
    }
}
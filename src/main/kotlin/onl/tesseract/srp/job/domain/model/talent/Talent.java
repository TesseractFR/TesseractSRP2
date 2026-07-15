package onl.tesseract.srp.job.domain.model.talent;

import lombok.Builder;

import java.util.Map;
import java.util.Set;

@Builder
public record Talent(TalentName name,
                     int maxLevel,
                     Bonus bonus,
                     Set<TalentName> parents,
                     Map<Integer, Integer> pricePerLevel
                     ) {

}

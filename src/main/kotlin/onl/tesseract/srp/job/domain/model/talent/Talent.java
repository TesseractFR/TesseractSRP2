package onl.tesseract.srp.job.domain.model.talent;

import lombok.Builder;
import onl.tesseract.srp.job.domain.model.Material;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Builder
public record Talent(TalentName name,
                     int maxLevel,
                     Bonus bonus,
                     Set<TalentName> parents,
                     Map<Integer, Integer> pricePerLevel,
                     Material item,
                     List<Double> values
                     ) {

    public double getValue(int level) {
        if(level <= 0)return 0.0;
        return values.get(level-1);
    }
}

package onl.tesseract.srp.job.domain.model.talent;

import onl.tesseract.srp.job.domain.model.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Talents(Map<TalentName, Talent> value){

    public Talents()
    {
        this(new HashMap<>());
    }

    public void add(TalentName talentName, Talent talent) {
        value.put(talentName, talent);
    }

    public Talent get(TalentName talentName){
        return value.get(talentName);
    }

    public List<Talent> get(Material item, Bonus bonus) {
        return value.values()
                .stream()
                .filter(talent ->
                        talent.item().equals(item) && talent.bonus().equals(bonus))
                .toList();
    }
}
package onl.tesseract.srp.job.domain.model.talent;

import java.util.HashMap;
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
}
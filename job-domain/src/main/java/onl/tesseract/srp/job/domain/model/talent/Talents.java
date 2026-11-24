package onl.tesseract.srp.job.domain.model.talent;

import onl.tesseract.srp.job.domain.model.Material;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Talents(Map<TalentName, Talent> value){

    public Talents()
    {
        this(new HashMap<>());
    }

    public void add(TalentName talentName, Talent talent) {
        value.put(talentName, talent);
    }

    /**
     * Cherche un talent par son nom.
     *
     * @param talentName le nom du talent
     * @return le talent, ou vide s'il n'est pas défini pour ce métier
     */
    public Optional<Talent> findByName(TalentName talentName) {
        return Optional.ofNullable(value.get(talentName));
    }

    public List<Talent> findAllByItemAndBonus(Material item, Bonus bonus) {
        return value.values()
                .stream()
                .filter(talent ->
                        talent.item().equals(item) && talent.bonus().equals(bonus))
                .toList();
    }
}
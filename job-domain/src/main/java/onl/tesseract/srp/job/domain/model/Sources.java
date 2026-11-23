package onl.tesseract.srp.job.domain.model;

import onl.tesseract.srp.job.domain.model.source.Source;

import java.util.Map;
import java.util.List;

public record Sources(Map<Source, List<Material>> value) {

    public Sources() {
        this(new java.util.HashMap<>());
    }

    /**
     * Donne les matériaux produits par une source.
     *
     * @param source la source exploitée
     * @return les matériaux de sortie, liste vide si la source n'a aucune sortie
     */
    public List<Material> getOutputItems(Source source) {
        return value.getOrDefault(source, List.of());
    }
}

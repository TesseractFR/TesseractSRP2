package onl.tesseract.srp.job.domain.model;

import onl.tesseract.srp.job.domain.model.source.Source;

import java.util.Map;
import java.util.List;

public record Sources(Map<Source, List<Material>> value) {

    public Sources() {
        this(new java.util.HashMap<>());
    }

    public List<Material> getOutputItems(Source source) {
        return value.get(source);
    }
}

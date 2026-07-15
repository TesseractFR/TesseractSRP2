package onl.tesseract.srp.job.domain.model.talent;


public record TalentName(String value) {
    public TalentName {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Talent name cannot be null or empty");
        }
    }
}
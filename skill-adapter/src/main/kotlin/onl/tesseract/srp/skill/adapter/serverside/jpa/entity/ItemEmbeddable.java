package onl.tesseract.srp.skill.adapter.serverside.jpa.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.NoArgsConstructor;
import onl.tesseract.srp.skill.domain.model.Quality;

@Embeddable
@Data
@NoArgsConstructor
public class ItemEmbeddable {
    String material;

    @Enumerated(EnumType.STRING)
    Quality quality;

    int quantity;
}

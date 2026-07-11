package onl.tesseract.srp.skill.adapter.serverside.jpa.entity.station;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StationEntityKey {
    private String skillName;
    private UUID territory;
}

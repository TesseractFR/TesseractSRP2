package onl.tesseract.srp.skill.adapter.serverside.jpa.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerSkillEmbeddable {

    private UUID playerUuid;

    private String skillName;
}

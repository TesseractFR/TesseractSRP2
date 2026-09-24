package onl.tesseract.srp.adapter.serverside.jpa.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPlayerTalentProgressionEntity {


    private String talentName;

    private int level;
}

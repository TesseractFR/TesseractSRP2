package onl.tesseract.srp.adapter.serverside.jpa.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Data
public class JobPlayerProgressionEntityKey {

    UUID playerUuid;

    String jobName;
}

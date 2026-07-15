package onl.tesseract.srp.job.adapter.serverside.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "t_job_player_progression")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPlayerProgressionEntity {

    @EmbeddedId
    private JobPlayerProgressionEntityKey id;

    private int availableTalentPoint;

    private int totalTalentPoint;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "t_job_player_talent_progressions", joinColumns = {@JoinColumn(name = "player_id"), @JoinColumn(name = "job_name")})
    private List<JobPlayerTalentProgressionEntity> talentProgressions;
}

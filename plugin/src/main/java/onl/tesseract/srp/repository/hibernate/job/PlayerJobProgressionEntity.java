package onl.tesseract.srp.repository.hibernate.job;

import jakarta.persistence.*;
import onl.tesseract.srp.domain.job.EnumJob;
import onl.tesseract.srp.domain.job.JobSkill;
import onl.tesseract.srp.domain.job.PlayerJobProgression;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;

import java.util.*;

/**
 * JPA entity for PlayerJobProgression domain object.
 */
@Entity
@Table(name = "t_player_job_progression")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PlayerJobProgressionEntity {
    @Id
    private UUID playerID;

    private int level;
    private int xp;
    private int skillPoints;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "t_player_job_reputation",
            joinColumns = @JoinColumn(name = "player_id")
    )
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "job")
    @Column(name = "reputation")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Map<EnumJob, Double> reputationByJob;

    @ElementCollection(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<JobSkill> skills;

    public PlayerJobProgressionEntity() {
        this.reputationByJob = new HashMap<>();
        for (EnumJob job : EnumJob.values()) {
            this.reputationByJob.put(job, 1.0);
        }
        this.skills = new ArrayList<>();
    }

    public PlayerJobProgressionEntity(UUID playerID, int level, int xp, int skillPoints,
                                       Map<EnumJob, Double> reputationByJob, List<JobSkill> skills) {
        this.playerID = playerID;
        this.level = level;
        this.xp = xp;
        this.skillPoints = skillPoints;
        this.reputationByJob = new HashMap<>(reputationByJob);
        this.skills = new ArrayList<>(skills);
    }

    public UUID getPlayerID() {
        return playerID;
    }

    public void setPlayerID(UUID playerID) {
        this.playerID = playerID;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public Map<EnumJob, Double> getReputationByJob() {
        return reputationByJob;
    }

    public void setReputationByJob(Map<EnumJob, Double> reputationByJob) {
        this.reputationByJob = reputationByJob;
    }

    public List<JobSkill> getSkills() {
        return skills;
    }

    public void setSkills(List<JobSkill> skills) {
        this.skills = skills;
    }

    /**
     * Converts this entity to a domain PlayerJobProgression object.
     */
    public PlayerJobProgression toDomain() {
        return new PlayerJobProgression(playerID, level, xp, skillPoints, skills, new HashMap<>(reputationByJob));
    }
}

/**
 * Extension function to convert PlayerJobProgression to PlayerJobProgressionEntity.
 */
class PlayerJobProgressionEntityExtensions {
    /**
     * Converts a PlayerJobProgression domain object to PlayerJobProgressionEntity.
     */
    public static PlayerJobProgressionEntity toEntity(PlayerJobProgression progression) {
        return new PlayerJobProgressionEntity(
                progression.getPlayerID(),
                progression.getLevel(),
                progression.getXp(),
                progression.getSkillPoints(),
                progression.getReputationByJob(),
                progression.getSkills()
        );
    }
}


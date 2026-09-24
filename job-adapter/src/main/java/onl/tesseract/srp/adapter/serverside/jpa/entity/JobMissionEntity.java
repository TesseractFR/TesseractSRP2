package onl.tesseract.srp.adapter.serverside.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.job.domain.model.mission.JobMission;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;

import java.util.UUID;

/**
 * JPA entity for JobMission domain object.
 */
@Setter
@Getter
@Entity
@Table(name = "t_job_missions")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class JobMissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID playerId;

    @Column(nullable = false)
    private String job;

    @Column(nullable = false)
    private String material;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Quality minimalQuality;

    @Column(name = "delivered")
    private int delivered;

    @Column(nullable = false)
    private int reward;

    public JobMissionEntity() {
    }

    public JobMissionEntity(Long id, UUID playerId, String job, String material,
                            int quantity, Quality minimalQuality, int delivered, int reward) {
        this.id = id;
        this.playerId = playerId;
        this.job = job;
        this.material = material;
        this.quantity = quantity;
        this.minimalQuality = minimalQuality;
        this.delivered = delivered;
        this.reward = reward;
    }

    /**
     * Converts this entity to a domain JobMission object.
     */
    public JobMission toDomain() {
        return new JobMission(id, playerId, job, material, quantity, minimalQuality, delivered, reward);
    }

    public static JobMissionEntity fromDomain(JobMission mission) {
        return new JobMissionEntity(
                mission.id(),
                mission.playerId(),
                mission.job(),
                mission.material(),
                mission.quantity(),
                mission.minimalQuality(),
                mission.delivered(),
                mission.reward()
        );
    }
}

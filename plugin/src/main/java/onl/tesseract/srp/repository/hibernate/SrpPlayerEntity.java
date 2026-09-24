package onl.tesseract.srp.repository.hibernate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import onl.tesseract.srp.domain.player.PlayerRank;
import onl.tesseract.srp.domain.player.SrpPlayer;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "t_srp_player")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SrpPlayerEntity {
    @Id
    private UUID uuid;

    @Column(name = "player_rank")
    @Enumerated(EnumType.STRING)
    private PlayerRank rank;

    private int money;
    private String titleId;
    private int illuminationPoints;

    public SrpPlayerEntity() {}

    public SrpPlayerEntity(UUID uuid, PlayerRank rank, int money, String titleId, int illuminationPoints) {
        this.uuid = uuid;
        this.rank = rank;
        this.money = money;
        this.titleId = titleId;
        this.illuminationPoints = illuminationPoints;
    }

    public SrpPlayer toDomain() {
        return new SrpPlayer(uuid, rank, money, titleId, illuminationPoints);
    }

    public static SrpPlayerEntity fromDomain(SrpPlayer srpPlayer) {
        return new SrpPlayerEntity(srpPlayer.getUniqueId(), srpPlayer.getRank(), srpPlayer.getMoney(),
                srpPlayer.getTitleID(), srpPlayer.getIlluminationPoints());
    }
}


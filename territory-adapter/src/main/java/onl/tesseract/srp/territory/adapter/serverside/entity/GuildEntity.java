package onl.tesseract.srp.territory.adapter.serverside.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.territory.adapter.serverside.entity.chunk.GuildChunkEntity;
import onl.tesseract.srp.territory.domain.model.container.DefaultVisitorSpawnContainer;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.model.guild.GuildMember;
import onl.tesseract.srp.territory.domain.model.guild.GuildMemberContainerImpl;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRank;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "t_guilds")
@Getter
@NoArgsConstructor
public class GuildEntity {

    @Id
    @Column(name = "id", length = 36, columnDefinition = "VARCHAR(36)")
    @JdbcTypeCode(Types.VARCHAR)
    private UUID id;

    @Column(nullable = false)
    @JdbcTypeCode(Types.VARCHAR)
    private UUID leaderId;

    @Column(unique = true)
    private String name;

    @Embedded
    private CoordinateEntity spawnLocation;

    @Setter
    private int money;

    @Setter
    private int level;

    @Setter
    private int xp;

    @Setter
    @Enumerated(EnumType.STRING)
    private GuildRank rank;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "visitor_spawn_x")),
            @AttributeOverride(name = "y", column = @Column(name = "visitor_spawn_y")),
            @AttributeOverride(name = "z", column = @Column(name = "visitor_spawn_z")),
            @AttributeOverride(name = "world", column = @Column(name = "visitor_spawn_world"))
    })
    private CoordinateEntity visitorSpawnLocation;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "guild", fetch = FetchType.EAGER)
    Set<GuildChunkEntity> chunks;

    @ElementCollection
    @CollectionTable(
            name = "guild_members",
            joinColumns = @JoinColumn(name = "guild_id")
    )
    private List<MemberEmbeddable> members;

    @ElementCollection
    @CollectionTable(
            name = "guild_invitations",
            joinColumns = @JoinColumn(name = "guild_id")
    )
    private Set<UUID> invitations;

    @ElementCollection
    @CollectionTable(
            name = "guild_join_requests",
            joinColumns = @JoinColumn(name = "guild_id")
    )
    private Set<UUID> joinRequests;

    @PrePersist
    public void prePersist() {
        if (spawnLocation == null) {
            spawnLocation = new CoordinateEntity();
        }
        if (visitorSpawnLocation == null) {
            visitorSpawnLocation = new CoordinateEntity();
        }
    }

    @Getter
    @Embeddable
    @NoArgsConstructor
    public static class MemberEmbeddable {
        private UUID playerID;

        @Enumerated(EnumType.STRING)
        private GuildRole role;

        public MemberEmbeddable(UUID playerID, GuildRole role) {
            this.playerID = playerID;
            this.role = role;
        }

        public static GuildMember toDomain(MemberEmbeddable embeddable) {
            return new GuildMember(embeddable.getPlayerID(), embeddable.getRole());
        }
    }

    public List<GuildMember> getMembersAsDomain() {
        return members.stream()
                .map(MemberEmbeddable::toDomain)
                .toList();
    }

    public static GuildEntity fromDomain(
            onl.tesseract.srp.territory.domain.model.guild.Guild guild) {
        GuildEntity entity = new GuildEntity();
        entity.id = guild.getId();
        entity.name = guild.getName();
        entity.spawnLocation = new CoordinateEntity(guild.getSpawnpoint());
        entity.money = guild.getMoney();
        entity.level = guild.getLevel();
        entity.xp = guild.getXp();
        entity.rank = guild.getRank();
        entity.visitorSpawnLocation = new CoordinateEntity(guild.getVisitorSpawnpoint());
        entity.members = guild.getMembers().stream()
                .map(m -> new MemberEmbeddable(m.getPlayerID(), m.getRole()))
                .toList();
        entity.invitations = guild.getInvitations();
        entity.joinRequests = guild.getJoinRequests();
        return entity;
    }

    public Guild toDomain() {
        Coordinate spawn = spawnLocation.toDomain();
        Coordinate visitorSpawn = visitorSpawnLocation != null ? visitorSpawnLocation.toDomain() : spawn;

        var memberContainer = new GuildMemberContainerImpl(
                leaderId,
                getMembersAsDomain(),
                invitations != null ? invitations : Set.of(),
                joinRequests != null ? joinRequests : Set.of()
        );

        return new Guild(id,spawn,new HashSet<>(),memberContainer,
                new DefaultVisitorSpawnContainer(visitorSpawn),name,money,level,xp,rank);
    }
}


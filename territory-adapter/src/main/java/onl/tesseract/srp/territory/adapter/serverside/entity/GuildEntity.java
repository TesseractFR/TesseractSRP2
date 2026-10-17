package onl.tesseract.srp.territory.adapter.serverside.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.territory.adapter.serverside.entity.chunk.GuildChunkEntity;
import onl.tesseract.srp.territory.adapter.serverside.entity.chunk.ChunkEntityId;
import onl.tesseract.srp.territory.domain.model.guild.GuildChunk;
import onl.tesseract.srp.territory.domain.model.container.DefaultVisitorSpawnContainer;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.model.guild.GuildJoinRequest;
import onl.tesseract.srp.territory.domain.model.guild.GuildMember;
import onl.tesseract.srp.territory.domain.model.guild.GuildMemberContainerImpl;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRank;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "t_guilds")
@Getter
@NoArgsConstructor
public class GuildEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(nullable = false)
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
    @Column(name = "`rank`")
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

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(
            name = "guild_members",
            joinColumns = @JoinColumn(name = "guild_id")
    )
    private List<MemberEmbeddable> members;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(
            name = "guild_invitations",
            joinColumns = @JoinColumn(name = "guild_id")
    )
    private Set<UUID> invitations;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "guild_join_requests", joinColumns = @JoinColumn(name = "guild_id"))
    private Set<JoinRequestEmbeddable> joinRequests;

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(name = "guild_trusted_players", joinColumns = @JoinColumn(name = "guild_id"))
    private Set<UUID> trustedPlayers = new HashSet<>();

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

    @Getter
    @Embeddable
    @NoArgsConstructor
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class JoinRequestEmbeddable {
        @EqualsAndHashCode.Include
        private UUID playerID;

        @Column(length = 256)
        private String message;

        private Instant requestedDate;

        public JoinRequestEmbeddable(UUID playerID, String message, Instant requestedDate) {
            this.playerID = playerID;
            this.message = message;
            this.requestedDate = requestedDate;
        }

        public GuildJoinRequest toDomain() {
            return new GuildJoinRequest(playerID, message, requestedDate);
        }
    }

    public static GuildEntity fromDomain(
            onl.tesseract.srp.territory.domain.model.guild.Guild guild) {
        GuildEntity entity = new GuildEntity();
        entity.id = guild.getId();
        entity.leaderId = guild.getLeaderId();
        entity.name = guild.getName();
        entity.spawnLocation = new CoordinateEntity(guild.getSpawnpoint());
        entity.money = guild.getMoney();
        entity.level = guild.getLevel();
        entity.xp = guild.getXp();
        entity.rank = guild.getRank();
        entity.visitorSpawnLocation = new CoordinateEntity(guild.getVisitorSpawnpoint());
        entity.members = guild.getMembers().stream()
                .map(m -> new MemberEmbeddable(m.getPlayerID(), m.getRole()))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        entity.invitations = new HashSet<>(guild.getInvitations());
        entity.joinRequests = guild.getJoinRequests().stream()
                .map(r -> new JoinRequestEmbeddable(r.getPlayerID(), r.getMessage(), r.getRequestedDate()))
                .collect(Collectors.toCollection(HashSet::new));
        entity.trustedPlayers = new HashSet<>(guild.getTrusted());
        entity.chunks = guild.getChunks().stream()
                .map(chunk -> new GuildChunkEntity(ChunkEntityId.fromDomain(chunk.getChunkCoord()), entity))
                .collect(java.util.stream.Collectors.toSet());
        return entity;
    }

    public Guild toDomain() {
        Coordinate spawn = spawnLocation.toDomain();
        Coordinate visitorSpawn = visitorSpawnLocation != null ? visitorSpawnLocation.toDomain() : spawn;

        var memberContainer = new GuildMemberContainerImpl(
                leaderId,
                getMembersAsDomain(),
                invitations != null ? new HashSet<>(invitations) : new HashSet<>(),
                joinRequests != null
                        ? joinRequests.stream().map(JoinRequestEmbeddable::toDomain).collect(Collectors.toCollection(HashSet::new))
                        : new HashSet<>()
        );

        Guild guild = new Guild(id,spawn,trustedPlayers != null ? new HashSet<>(trustedPlayers) : new HashSet<>(),memberContainer,
                new DefaultVisitorSpawnContainer(visitorSpawn),name,money,level,xp,rank);
        if (chunks != null) {
            guild.addChunks(chunks.stream()
                    .map(entity -> new GuildChunk(entity.getChunkEntityId().toDomain(), guild))
                    .collect(java.util.stream.Collectors.toSet()));
        }
        return guild;
    }
}


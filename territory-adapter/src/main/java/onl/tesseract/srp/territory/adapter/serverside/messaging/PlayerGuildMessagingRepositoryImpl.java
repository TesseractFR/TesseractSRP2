package onl.tesseract.srp.territory.adapter.serverside.messaging;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.srp.territory.adapter.userside.controller.display.GuildRankDisplay;
import onl.tesseract.srp.territory.adapter.userside.controller.display.GuildRoleDisplay;
import onl.tesseract.srp.territory.domain.model.guild.GuildMember;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRank;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;
import onl.tesseract.srp.territory.domain.port.serverside.PlayerGuildMessagingRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static onl.tesseract.srp.common.adapter.SrpChatFormats.*;

@Repository
public class PlayerGuildMessagingRepositoryImpl implements PlayerGuildMessagingRepository {

    private static final Component BORDER_HINT = Component.text("Visualise les bordures avec ")
            .append(Component.text("/guild border", NamedTextColor.GOLD))
            .append(Component.text("."));
    private static final String DEFAULT_JOIN_MESSAGE = "Bonjour, j'aimerais rejoindre votre guilde !";

    private void send(UUID id, Component msg) {
        Player p = Bukkit.getPlayer(id);
        if (p != null) p.sendMessage(msg);
    }
    private void error(UUID id, String text) { send(id, GUILD_CHAT_ERROR.append(Component.text(text))); }
    private void error(UUID id, Component extra) { send(id, GUILD_CHAT_ERROR.append(extra)); }
    private void success(UUID id, String text) { send(id, GUILD_CHAT_SUCCESS.append(Component.text(text))); }
    private void info(UUID id, String text) { send(id, GUILD_CHAT_FORMAT.append(Component.text(text))); }
    private String nameOf(UUID id) {
        String n = Bukkit.getOfflinePlayer(id).getName();
        return n != null ? n : "?";
    }

    @Override public void playerNoGuild(UUID p) {
        send(p, GUILD_CHAT_ERROR
                .append(Component.text("Tu ne possèdes pas de guilde. Rejoins ou crées-en une avec "))
                .append(Component.text("/guild create <nom>", NamedTextColor.GOLD)));
    }
    @Override public void playerNotAllowed(UUID p) { error(p, "Tu n'es pas autorisé(e) à utiliser cette commande."); }
    @Override public void playerInvalidWorld(UUID p) {
        error(p, "Tu n'es pas dans le bon monde, cette commande n'est utilisable que dans le monde des guildes.");
    }

    // create
    @Override public void playerRankTooLowCreateGuild(UUID p) { error(p, "Tu n'as pas le grade minimal nécessaire pour créer une guilde (Baron)."); }
    @Override public void playerNoMoneyCreateGuild(UUID p) { error(p, "Tu n'as pas assez d'argent pour créer ta guilde."); }
    @Override public void playerNameTakenCreateGuild(UUID p) { error(p, "Ce nom de guilde est déjà pris, choisis-en un autre."); }
    @Override public void playerNearSpawnCreateGuild(UUID p) { error(p, "Tu es trop proche du spawn pour créer ta guilde."); }
    @Override public void playerTooCloseCreateGuild(UUID p) { error(p, "Impossible de créer ta guilde ici, tu es trop proche d'une autre guilde."); }
    @Override public void playerAlreadyHasGuildCreateGuild(UUID p) { error(p, "Tu es déjà dans une guilde. Quitte-la pour pouvoir en créer une nouvelle."); }
    @Override public void playerOnOtherTerritoryCreateGuild(UUID p) { error(p, "Tu ne peux pas créer une guilde ici, tu es sur un autre territoire."); }
    @Override public void playerGuildCreated(UUID p, String name) { success(p, "Nouvelle guilde créée sous le nom de " + name); }

    // delete
    @Override public void playerGuildDeleted(UUID p) { success(p, "Ta guilde a été supprimée avec succès."); }
    @Override public void playerDeleteGuildFailed(UUID p) { error(p, "Suppression impossible."); }

    // invite
    @Override public void playerCannotInviteSelf(UUID p) { error(p, "Tu ne peux pas t'inviter toi même."); }
    @Override public void playerInviteTargetHasGuild(UUID p, UUID t) { error(p, "Ce joueur est déjà dans une guilde."); }
    @Override public void playerInviteSent(UUID s, UUID t) { info(s, "Votre invitation a bien été envoyée à " + nameOf(t)); }
    @Override public void playerInviteJoined(UUID s, UUID t, String guild) {
        success(s, nameOf(t) + " a rejoint votre guilde.");
        success(t, "Vous avez rejoint la guilde " + guild + ".");
    }
    @Override public void playerJoinGuildNotFound(UUID p, String name) { error(p, "La guilde \"" + name + "\" n'existe pas."); }
    @Override public void playerAlreadyInGuild(UUID p) { error(p, "Tu es déjà dans une guilde."); }
    @Override public void playerGuildFull(UUID p) { error(p, "Cette guilde a atteint son nombre maximum de membres."); }
    @Override public void playerAlreadyRequested(UUID p) { info(p, "Tu as déjà une demande en attente pour cette guilde."); }
    @Override public void playerJoinRequestSent(UUID p, String guild) { success(p, "Ta demande pour rejoindre la guilde " + guild + " a été envoyée."); }
    @Override public void playerJoinRequestAccepted(UUID s, UUID t, String guild) {
        success(s, nameOf(t) + " a rejoint votre guilde.");
        success(t, "Votre demande pour rejoindre la guilde " + guild + " a été acceptée !");
    }
    @Override public void playerJoinRequestDeclined(UUID s, UUID t, String guild) {
        info(s, "La demande de " + nameOf(t) + " a été refusée.");
        error(t, "Votre demande pour rejoindre la guilde " + guild + " a été refusée.");
    }
    @Override public void playerNoJoinRequest(UUID p) { error(p, "Aucune demande en attente pour ce joueur."); }
    @Override
    public String defaultJoinRequestMessage() { return DEFAULT_JOIN_MESSAGE; }

    // kick / leave
    @Override public void playerKickNotMember(UUID p, UUID t) { error(p, nameOf(t) + " n'est pas membre de ta guilde."); }
    @Override public void playerCannotKickLeader(UUID p) { error(p, "Tu ne peux pas exclure le chef de la guilde."); }
    @Override public void playerKicked(UUID s, UUID t) {
        success(s, nameOf(t) + " a été exclu(e) de la guilde.");
        success(t, "Tu as été exclu(e) de ta guilde.");
    }
    @Override public void playerLeaderMustDelete(UUID p) { error(p, "Tu es le/la chef(fe) de la guilde. Supprime-la ou transfère le leadership."); }
    @Override public void playerLeftGuild(UUID p) { success(p, "Tu as quitté ta guilde."); }

    // claim / unclaim
    @Override public void playerChunkClaimed(UUID p, int x, int z) { success(p, "Le chunk (" + x + ", " + z + ") a été annexé avec succès pour la guilde."); }
    @Override public void playerChunkAlreadyOwned(UUID p) { send(p, GUILD_CHAT_FORMAT.append(Component.text("Ta guilde possède déjà ce chunk. ")).append(BORDER_HINT)); }
    @Override public void playerChunkOwnedByOther(UUID p) { error(p, Component.text("Ce chunk appartient à une autre guilde. ").append(BORDER_HINT)); }
    @Override public void playerChunkNotAdjacent(UUID p) { error(p, Component.text("Tu dois sélectionner un chunk collé au territoire de ta guilde. ").append(BORDER_HINT)); }
    @Override public void playerChunkTooClose(UUID p) { error(p, "Tu ne peux pas annexer ce chunk, il est trop proche d'une autre guilde."); }
    @Override public void playerChunkUnclaimed(UUID p, int x, int z) { success(p, "Le chunk (" + x + ", " + z + ") a été retiré de ta guilde."); }
    @Override public void playerChunkNotOwned(UUID p) { error(p, "Ce chunk ne fait pas partie du territoire de ta guilde."); }
    @Override public void playerLastChunk(UUID p) { error(p, "Tu ne peux pas retirer le dernier chunk de ta guilde ! Si tu veux supprimer ta guilde, utilise /guild delete."); }
    @Override public void playerChunkIsSpawn(UUID p) { error(p, "Tu ne peux pas désannexer ce chunk, il contient un point de spawn de ta guilde. Déplace-le avec /guild setspawn (private/visitor) avant de le retirer."); }
    @Override public void playerChunkSplit(UUID p) { error(p, Component.text("Tu ne peux pas désannexer ce chunk, cela diviserait ta guilde en plusieurs parties. ").append(BORDER_HINT)); }

    // spawn
    @Override public void playerSpawnSet(UUID p, boolean visitor) {
        success(p, "Le point de spawn " + (visitor ? "visiteurs" : "privé") + " de la guilde a été défini ici.");
    }
    @Override public void playerSpawnOutsideTerritory(UUID p) { error(p, Component.text("Tu dois être dans un chunk de ta guilde pour définir le spawn. ").append(BORDER_HINT)); }
    @Override public void playerNoSpawnDefined(UUID p) { error(p, "Aucun spawn défini pour cette guilde."); }
    @Override public void playerGuildNotFound(UUID p, String name) { error(p, "La guilde \"" + name + "\" n'existe pas."); }
    @Override public void playerTeleportedToSpawn(UUID p, String guild, boolean own) {
        success(p, own ? "Tu as été téléporté au spawn de ta guilde." : "Tu as été téléporté au spawn de la guilde " + guild + ".");
    }

    // border
    @Override public void playerBordersShown(UUID p) { success(p, "Les bordures de ta guilde sont maintenant visibles !"); }
    @Override public void playerBordersHidden(UUID p) { success(p, "Les bordures de ta guilde ont été masquées."); }

    // rôles
    @Override public void playerRoleChanged(UUID s, UUID t, GuildRole role) {
        success(s, "Le rôle de " + nameOf(t) + " est maintenant " + GuildRoleDisplay.of(role).getDisplayName() + ".");
    }
    @Override public void playerLeaderTransferred(UUID s, UUID t) {
        success(s, nameOf(t) + " est maintenant chef de la guilde.");
    }
    @Override public void playerRoleChangeImpossible(UUID s) { error(s, "Ce changement de rôle est impossible."); }

    // rang
    @Override public void playerRankUpgraded(UUID p, GuildRank rank) {
        send(p, GUILD_CHAT_SUCCESS
                .append(Component.text("Félicitations, votre guilde est maintenant au rang de "))
                .append(Component.text(GuildRankDisplay.of(rank).getTitle(), GuildRankDisplay.of(rank).getColor()))
                .append(Component.text(" !")));
    }
    @Override public void playerRankMax(UUID p) { info(p, "Votre guilde est déjà au rang maximum."); }
    @Override public void playerRankMoneyMissing(UUID p, int cost) { error(p, "Argent insuffisant : il faut " + cost + " Lys."); }
    @Override public void playerRankLevelMissing(UUID p, int min) { error(p, "Niveau insuffisant : votre guilde doit être niveau " + min + " minimum."); }

    // banque
    @Override public void playerInvalidAmount(UUID p) { error(p, "Nombre invalide."); }
    @Override public void playerNotEnoughMoney(UUID p) { error(p, "Argent insuffisant."); }
    @Override public void playerGuildNotEnoughMoney(UUID p) { error(p, "La banque de guilde n'a pas assez d'argent."); }
    @Override public void playerBankDeposited(UUID p, int a) { success(p, a + " Lys déposés dans la banque de guilde."); }
    @Override public void playerBankWithdrawn(UUID p, int a) { success(p, a + " Lys retirés de la banque de guilde."); }

    private void staffError(UUID id, String text) { send(id, STAFF_CHAT_ERROR.append(Component.text(text))); }
    private void staffSuccess(UUID id, String text) { send(id, STAFF_CHAT_SUCCESS.append(Component.text(text))); }

    @Override public void staffGuildDeleted(UUID s, String guild) { staffSuccess(s, "La guilde " + guild + " a été supprimée avec succès."); }
    @Override public void staffDeleteFailed(UUID s) { staffError(s, "Suppression impossible."); }
    @Override public void staffShowMembers(UUID s, String guild, List<GuildMember> members) {
        send(s, STAFF_CHAT_FORMAT.append(Component.text("Membres de la guilde " + guild + ":")));
        for (GuildMember member : members) {
            String name = Bukkit.getOfflinePlayer(member.getPlayerID()).getName();
            if (name == null) name = member.getPlayerID().toString();
            send(s, STAFF_CHAT_FORMAT.append(Component.text("- " + name + " : " + member.getRole())));
        }
    }
    @Override public void staffMemberAlreadyInGuild(UUID s, UUID t) { staffError(s, "Le joueur " + nameOf(t) + " fait déjà partie de la guilde."); }
    @Override public void staffMemberHasOtherGuild(UUID s, UUID t) { staffError(s, "Le joueur " + nameOf(t) + " fait déjà partie d'une autre guilde."); }
    @Override public void staffMemberAdded(UUID s, UUID t, String guild) { staffSuccess(s, "Opération effectuée - " + nameOf(t) + " ajouté à la guilde " + guild); }
    @Override public void staffMemberNotInGuild(UUID s, UUID t) { staffError(s, "Le joueur " + nameOf(t) + " ne fait pas partie de la guilde."); }
    @Override public void staffCannotKickLeader(UUID s) { staffError(s, "Impossible d'expulser le leader de la guilde. Change son rôle."); }
    @Override public void staffMemberKicked(UUID s, UUID t, String guild) { staffSuccess(s, "Opération effectuée - " + nameOf(t) + " expulsé de la guilde " + guild); }
    @Override public void staffPlayerNotFound(UUID s) {staffError(s, "Joueur introuvable dans cette guilde.");}
    @Override public void staffLeaderSet(UUID s, String guild, UUID newLeader, UUID oldLeader) {
        staffSuccess(s, "Leader de la guilde " + guild + " défini sur " + nameOf(newLeader) + ". L'ancien leader " + nameOf(oldLeader) + " a été passé Citoyen.");
    }
    @Override public void staffRoleSet(UUID s, UUID t, GuildRole role, String guild) {
        staffSuccess(s, "Rôle de " + nameOf(t) + " défini à " + role.name() + " dans la guilde " + guild + ".");
    }
    @Override public void staffRoleSetWithReplacement(UUID s, UUID t, GuildRole role, UUID repl, String guild) {
        staffSuccess(s, "Rôle de " + nameOf(t) + " défini à " + role.name() + " et " + nameOf(repl) + " à Leader pour la guilde " + guild + ".");
    }
    @Override public void staffNeedNewLeader(UUID s, String guild, UUID t, GuildRole role) {
        staffError(s, "La cible est le leader actuel. Indique un nouveau leader : /staff guild members setRole " + guild + " " + nameOf(t) + " " + role.name() + " <newLeader>");
    }
    @Override public void staffNewLeaderSameAsTarget(UUID s) { staffError(s, "Le nouveau leader ne peut pas être le même joueur que la cible."); }
    @Override public void staffSameRole(UUID s, UUID t, GuildRole role, String guild) {
        staffError(s, nameOf(t) + " a déjà le rôle " + role.name() + " dans la guilde " + guild + ".");
    }

    @Override public void staffMoneyGiven(UUID s, String guild, int amount) {
        staffSuccess(s, "Opération effectuée - " + amount + " Lys ajoutés à " + guild);
    }
    @Override public void staffMoneyTaken(UUID s, String guild, int amount) {
        staffSuccess(s, "Opération effectuée - " + amount + " Lys retirés à " + guild);
    }
    @Override public void staffShowMoney(UUID s, String guild, int money) {
        send(s, STAFF_CHAT_FORMAT.append(Component.text(guild + " : " + money + " Lys")));
    }
    @Override public void staffInvalidAmount(UUID s) { staffError(s, "Le montant doit être strictement positif."); }
    @Override public void staffNotEnoughGuildMoney(UUID s, String guild) {
        staffError(s, "La guilde " + guild + " n'a pas assez d'argent.");
    }
    @Override public void staffLevelSet(UUID s, String guild, int level) {
        staffSuccess(s, "Opération effectuée - niveau de " + guild + " défini à " + level);
    }
    @Override public void staffLevelAdded(UUID s, String guild, int amount) {
        staffSuccess(s, "Opération effectuée - " + amount + " niveaux ajoutés à " + guild);
    }
    @Override public void staffShowLevel(UUID s, String guild, int level) {
        send(s, STAFF_CHAT_FORMAT.append(Component.text(guild + " : niveau " + level)));
    }
    @Override public void staffXpAdded(UUID s, String guild, int amount) {
        staffSuccess(s, "Opération effectuée - " + amount + " XP ajoutés à " + guild);
    }
    @Override public void staffRankSet(UUID s, String guild, GuildRank rank) {
        staffSuccess(s, "Opération effectuée - rang de la guilde " + guild + " défini à " + rank);
    }
    @Override public void staffShowRank(UUID s, String guild, GuildRank rank) {
        send(s, STAFF_CHAT_FORMAT.append(Component.text(guild + " : rang " + rank)));
    }
}
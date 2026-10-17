package onl.tesseract.srp.territory.adapter.serverside.messaging;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.srp.territory.domain.port.serverside.PlayerCampementMessagingRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import static onl.tesseract.srp.common.adapter.SrpChatFormats.*;

@Repository
public class PlayerCampementMessagingRepositoryImpl implements PlayerCampementMessagingRepository {

    private static final Component BORDER_HINT = Component.text("Visualise les bordures avec ")
            .append(Component.text("/campement border", NamedTextColor.GOLD))
            .append(Component.text("."));

    private void send(UUID id, Component msg) {
        Player p = Bukkit.getPlayer(id);
        if (p != null) p.sendMessage(msg);
    }
    private void error(UUID id, String text) { send(id, CAMPEMENT_CHAT_ERROR.append(Component.text(text))); }
    private void error(UUID id, Component extra) { send(id, CAMPEMENT_CHAT_ERROR.append(extra)); }
    private void success(UUID id, String text) { send(id, CAMPEMENT_CHAT_SUCCESS.append(Component.text(text))); }
    private void info(UUID id, String text) { send(id, CAMPEMENT_CHAT_FORMAT.append(Component.text(text))); }
    private String nameOf(UUID id) {
        String n = Bukkit.getOfflinePlayer(id).getName();
        return n != null ? n : "?";
    }

    @Override public void playerNoCampement(UUID p) {
        send(p, CAMPEMENT_CHAT_ERROR
                .append(Component.text("Tu ne possèdes pas de campement. Crées-en un avec "))
                .append(Component.text("/campement create", NamedTextColor.GOLD)));
    }
    @Override public void playerNotAllowed(UUID p) { error(p, "Tu n'es pas autorisé(e) à utiliser cette commande."); }
    @Override public void playerInvalidWorld(UUID p) {
        error(p, "Tu n'es pas dans le bon monde, cette commande n'est utilisable que dans le monde des campements.");
    }

    // create
    @Override public void playerNearSpawnCreateCampement(UUID p) { error(p, "Tu es trop proche du spawn pour créer un campement."); }
    @Override public void playerTooCloseCreateCampement(UUID p) { error(p, "Tu es trop proche d'un autre campement, tu ne peux pas en créer un ici."); }
    @Override public void playerAlreadyHasCampement(UUID p) { error(p, "Tu possèdes déjà un campement."); }
    @Override public void playerOnOtherTerritoryCreateCampement(UUID p) { error(p, "Tu ne peux pas créer un campement ici, tu es sur un autre territoire."); }
    @Override public void playerCampementCreated(UUID p) { success(p, "Campement créé avec succès ! Tu peux désormais t'installer confortablement dans ce chunk ;)"); }

    // delete
    @Override public void playerCampementDeleted(UUID p) { success(p, "Ton campement a été supprimé avec succès."); }

    // spawn
    @Override public void playerNoSpawnDefined(UUID p) { error(p, "Aucun spawn défini pour ton campement."); }
    @Override public void playerTargetNoCampement(UUID p, UUID t) { error(p, nameOf(t) + " ne possède pas de campement."); }
    @Override public void playerTeleportedToOwnCampement(UUID p) { success(p, "Tu as été téléporté à ton campement."); }
    @Override public void playerTeleportedToCampement(UUID p, UUID owner) { success(p, "Tu as été téléporté au campement de " + nameOf(owner) + "."); }
    @Override public void playerSpawnSet(UUID p) { success(p, "Nouveau point de spawn défini ici !"); }
    @Override public void playerSpawnOutsideTerritory(UUID p) { error(p, Component.text("Tu dois être dans un chunk de ton campement pour définir le spawn. ").append(BORDER_HINT)); }

    // claim / unclaim
    @Override public void playerChunkClaimed(UUID p, int x, int z) { success(p, "Le chunk (" + x + ", " + z + ") a été annexé avec succès."); }
    @Override public void playerChunkAlreadyOwned(UUID p) { send(p, CAMPEMENT_CHAT_FORMAT.append(Component.text("Tu possèdes déjà ce chunk. ")).append(BORDER_HINT)); }
    @Override public void playerChunkOwnedByOther(UUID p) { error(p, Component.text("Ce chunk appartient à un autre campement. ").append(BORDER_HINT)); }
    @Override public void playerChunkNotAdjacent(UUID p) { error(p, Component.text("Tu dois sélectionner un chunk collé à ton campement. ").append(BORDER_HINT)); }
    @Override public void playerChunkTooClose(UUID p) { error(p, Component.text("Tu ne peux pas annexer ce chunk, il est trop proche d'un autre campement. ").append(BORDER_HINT)); }
    @Override public void playerChunkUnclaimed(UUID p, int x, int z) { success(p, "Le chunk (" + x + ", " + z + ") a été retiré de ton campement."); }
    @Override public void playerChunkNotOwned(UUID p) { error(p, Component.text("Ce chunk ne fait pas partie de ton campement. ").append(BORDER_HINT)); }
    @Override public void playerLastChunk(UUID p) { error(p, "Tu ne peux pas désannexer ton dernier chunk de campement."); }
    @Override public void playerChunkIsSpawn(UUID p) { error(p, "Tu ne peux pas désannexer ce chunk, il contient ton point de spawn."); }
    @Override public void playerChunkSplit(UUID p) { error(p, Component.text("Impossible de désannexer ce chunk, cela diviserait ton campement en 2 parties. ").append(BORDER_HINT)); }

    // trust / untrust
    @Override public void playerCannotTrustSelf(UUID p) { info(p, "C'est bien, tu as confiance en toi ! Mais bon, t'es déjà propriétaire :)"); }
    @Override public void playerTrusted(UUID owner, UUID t) {
        success(owner, nameOf(t) + " a été ajouté en tant que joueur de confiance dans ton campement !");
        success(t, "Tu as été ajouté en tant que joueur de confiance dans le campement de " + nameOf(owner) + ".");
    }
    @Override public void playerAlreadyTrusted(UUID owner) { info(owner, "Ce joueur est déjà ajouté à ton campement."); }
    @Override public void playerUntrusted(UUID owner, UUID t) { success(owner, nameOf(t) + " a été retiré de la liste des joueurs de confiance de ton campement !"); }
    @Override public void playerNotTrusted(UUID owner) { error(owner, "Ce joueur n'est pas dans ta liste de confiance."); }

    // border
    @Override public void playerBordersShown(UUID p) { success(p, "Les bordures de ton campement sont maintenant visibles !"); }
    @Override public void playerBordersHidden(UUID p) { info(p, "Les bordures de ton campement ont été masquées."); }

    @Override public void staffNotAllowed(UUID s) { error(s, "Action non autorisée sur ce campement."); }
    @Override public void staffTargetNoCampement(UUID s, UUID owner) { error(s, nameOf(owner) + " ne possède pas de campement."); }
    @Override public void staffCampementCreated(UUID s, UUID owner, int x, int z) {
        success(s, "Campement créé pour " + nameOf(owner) + " dans le chunk " + x + "," + z + ".");
    }
    @Override public void campementCreatedByStaff(UUID owner, int x, int z) {
        success(owner, "Un administrateur t'a créé un campement dans le chunk " + x + "," + z + ".");
    }
    @Override public void staffCreateAlreadyHasCampement(UUID s) { error(s, "Ce joueur possède déjà un campement."); }
    @Override public void staffCreateInvalidWorld(UUID s) { error(s, "Tu ne peux pas créer de campement dans ce monde."); }
    @Override public void staffCreateNearSpawn(UUID s) { error(s, "Trop proche du spawn pour créer un campement."); }
    @Override public void staffCreateTooClose(UUID s) { error(s, "Un autre territoire est trop proche d'ici."); }
    @Override public void staffCreateOnOtherTerritory(UUID s) { error(s, "Impossible de créer un campement ici, c'est un autre territoire."); }
    @Override public void staffCampementDeleted(UUID s, UUID owner) { success(s, "Le campement de " + nameOf(owner) + " a été supprimé avec succès."); }
    @Override public void campementDeletedByStaff(UUID owner) { error(owner, "Ton campement a été supprimé par un administrateur."); }
    @Override public void staffCannotTrustOwner(UUID s) { info(s, "Impossible d'ajouter le propriétaire lui-même en tant que joueur de confiance."); }
    @Override public void staffTrusted(UUID s, UUID owner, UUID t) { success(s, nameOf(t) + " a été ajouté dans le campement de " + nameOf(owner) + "."); }
    @Override public void trustedByStaff(UUID t, UUID owner) { success(t, "Tu as été ajouté au campement de " + nameOf(owner) + " en tant que joueur de confiance."); }
    @Override public void staffAlreadyTrusted(UUID s) { info(s, "Ce joueur est déjà dans la liste de confiance."); }
    @Override public void staffUntrusted(UUID s, UUID owner, UUID t) { success(s, nameOf(t) + " a été retiré du campement de " + nameOf(owner) + "."); }
    @Override public void staffNotTrusted(UUID s) { error(s, "Ce joueur n'est pas dans la liste de confiance de ce campement."); }
    @Override public void staffTrustedList(UUID s, UUID owner, java.util.Collection<UUID> trusted) {
        var names = trusted.stream().map(this::nameOf).filter(n -> !n.equals("?")).toList();
        if (names.isEmpty()) info(s, "Aucun joueur de confiance pour le campement de " + nameOf(owner) + ".");
        else info(s, "Joueurs de confiance du campement de " + nameOf(owner) + " : " + String.join(", ", names));
    }
}
# Spec — `commun-domain`

## Objectif

Fournir les concepts partagés par les autres domaines : identité et progression générale des joueurs, coordonnées, qualité d’objet, monde SRP, événements et quelques résultats/erreurs métier. Le module ne porte pas d’API Bukkit dans ses modèles.

## Modèle et règles

- `SrpPlayer` est identifié par un UUID et porte un rang, un solde d’argent, des points d’illumination et un identifiant de titre.
- Un joueur créé avec son seul UUID reçoit le rang `Survivant`, le titre associé et des soldes à zéro.
- `addMoney` et `addIlluminationPoints` acceptent les variations positives ou négatives, mais rejettent toute opération qui ferait passer le solde correspondant sous zéro. La valeur mise à jour est retournée.
- `buyNextRank` achète le rang suivant au coût défini par ce rang. L’opération retourne `false` si le rang est déjà maximal ou si le joueur n’a pas les fonds ; sinon le coût est débité et le rang est modifié.
- `Coordinate` et `ChunkCoord` représentent les coordonnées monde et chunk utilisées par les domaines spatiaux. `WorldName` encapsule un nom de monde ; `SrpWorld` associe les mondes SRP à leur nom Bukkit.
- `Quality` et `PlayerRank` sont des référentiels partagés. `InteractionAllowResult` et `StaffSetRoleResult` codifient des résultats d’opérations métier.
- `PlayerRankUpEvent` est l’événement de montée de rang. `EventPublisher` définit le port de publication sans imposer de bus d’événements.
- `WorldService.getSrpWorld` recherche un monde par égalité de nom Bukkit et retourne `null` en l’absence de correspondance.

## Interfaces et dépendances

Le domaine expose `EventPublisher` et `WorldService`. Les autres modules peuvent réutiliser ses types. Le cœur des modèles ne doit pas dépendre d’une implémentation Bukkit ; les adaptateurs fournissent les intégrations concrètes.

## Critères d’acceptation

1. Un joueur neuf possède les valeurs initiales décrites ci-dessus.
2. Un débit d’argent ou de points d’illumination qui laisserait un solde négatif échoue avec l’exception métier correspondante et ne modifie pas le solde.
3. L’achat d’un rang disponible débite exactement son coût ; l’achat impossible ne modifie ni le rang ni le solde.
4. La résolution d’un nom Bukkit connu renvoie le `SrpWorld` correspondant ; un nom inconnu renvoie `null`.

## Limites constatées

La validation des montants négatifs en entrée, les bornes supérieures de solde et les règles de cohérence titre/rang ne sont pas spécifiées par les modèles actuels. `WorldService` est une classe concrète et non un port abstrait.

## Catalogue des rangs et progression joueur

`PlayerRank` ordonne onze rangs, de `Survivant` à `Empereur`. Chaque rang associe un titre, un coût d’accès au rang (utilisé pour acheter le rang suivant), un délai de téléportation, un nombre d’emplacements de missions métier et un niveau de campement. `next()` retourne le rang suivant ou `null` au sommet. `getRequiredRankForMissionSlot(slot)` retourne le premier rang dont le nombre de slots couvre la demande, et lève `IllegalArgumentException` si aucun rang ne le permet.

`PlayerService` sert de façade de lecture et mutation autour de `PlayerRepository`. Une lecture absente renvoie un joueur neuf avec rang survivant, titre associé et soldes à zéro, sans sauvegarde immédiate. `setRank` synchronise le titre au rang et persiste seulement si le rang change. `buyNextRank` appelle la logique du modèle, synchronise le titre et persiste uniquement en cas d’achat réussi. Les ajustements d’argent et de points d’illumination refusent un solde final négatif (retour `false`) et sauvegardent toute modification acceptée; l’ajustement peut être positif, nul ou négatif.

## Détail de la téléportation différée

`TeleportationService.teleport(player, destination, callback)` lit d’abord les paramètres via `TeleportationSettingsPort`, puis utilise la surcharge durée/animation. La surcharge valide les références et rejette une durée négative. Elle mémorise la position de départ, applique les effets et le son, lance l’animation au départ et, si le chunk de destination est déjà chargé, à destination également.

Un timer déclenché immédiatement puis toutes les 10 ticks accumule le temps par tranches de 10. À expiration, il annule le timer, téléporte et exécute le callback. Avant l’expiration, le départ du joueur arrête le timer; un changement de bloc (monde ou coordonnées entières) annule la téléportation, retire la cécité et envoie le message d’annulation. Une simple rotation ou un déplacement dans le même bloc ne l’annule pas. La comparaison de départ ne protège pas contre un joueur hors ligne dès le premier tick avant la fin. Les durées ne sont pas nécessairement multiples de 10 et l’expiration est donc arrondie au prochain tick de vérification.

## Types partagés complémentaires

`Quality` est une échelle ordonnée de qualité dont `next()` progresse jusqu’à la qualité maximale (au maximum, elle reste plafonnée selon le code de l’enum). `InteractionAllowResult` encode les résultats d’autorisation d’interaction consommés par les domaines territoriaux. `StaffSetRoleResult` distingue succès, rôle identique, nécessité de désigner un nouveau chef et tentative de choisir la cible actuelle. `ItemTag` porte un identifiant d’objet, tandis que `WorldName`, `Coordinate` et `ChunkCoord` donnent des types explicites pour les emplacements et chunks. `PlayerRankUpEvent` transporte UUID du joueur et nouveau rang; le port `EventPublisher` laisse le mécanisme de publication à l’adaptateur.

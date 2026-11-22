# Spec — `commun-domain`

## Objectif

Fournir les concepts partagés par les autres domaines : identité des joueurs (`PlayerId`), monnaie (`MoneyValue`), coordonnées, qualité d’objet, monde SRP, événements et quelques résultats/erreurs métier. Le module ne porte pas d’API Bukkit dans ses modèles.

## Modèle et règles

- Le profil SRP du joueur (`SrpPlayer`, `PlayerRank`, grades, argent, points d’illumination) vit dans le module dédié [player-domain](../../player-domain/spec/player-domain.md). `commun-domain` ne garde que l’identifiant `PlayerId` et l’exception générique `NotEnoughMoneyException`.
- `Coordinate` et `ChunkCoord` représentent les coordonnées monde et chunk utilisées par les domaines spatiaux. `WorldName` encapsule un nom de monde ; `SrpWorld` associe les mondes SRP à leur nom Bukkit.
- `Quality` est un référentiel partagé. `InteractionAllowResult` et `StaffSetRoleResult` codifient des résultats d’opérations métier.
- `EventPublisher` définit le port de publication sans imposer de bus d’événements.
- `WorldService.getSrpWorld` recherche un monde par égalité de nom Bukkit et retourne `null` en l’absence de correspondance.

## Interfaces et dépendances

Le domaine expose `EventPublisher` et `WorldService`. Les autres modules peuvent réutiliser ses types. Le cœur des modèles ne doit pas dépendre d’une implémentation Bukkit ; les adaptateurs fournissent les intégrations concrètes.

## Critères d’acceptation

1. La résolution d’un nom Bukkit connu renvoie le `SrpWorld` correspondant ; un nom inconnu renvoie `null`.

## Limites constatées

`WorldService` est une classe concrète et non un port abstrait.

## Détail de la téléportation différée

`TeleportationService.teleport(player, destination, callback)` lit d’abord les paramètres via `TeleportationSettingsPort`, puis utilise la surcharge durée/animation. La surcharge valide les références et rejette une durée négative. Elle mémorise la position de départ, applique les effets et le son, lance l’animation au départ et, si le chunk de destination est déjà chargé, à destination également.

Un timer déclenché immédiatement puis toutes les 10 ticks accumule le temps par tranches de 10. À expiration, il annule le timer, téléporte et exécute le callback. Avant l’expiration, le départ du joueur arrête le timer; un changement de bloc (monde ou coordonnées entières) annule la téléportation, retire la cécité et envoie le message d’annulation. Une simple rotation ou un déplacement dans le même bloc ne l’annule pas. La comparaison de départ ne protège pas contre un joueur hors ligne dès le premier tick avant la fin. Les durées ne sont pas nécessairement multiples de 10 et l’expiration est donc arrondie au prochain tick de vérification.

## Types partagés complémentaires

`Quality` est une échelle ordonnée de qualité dont `next()` progresse jusqu’à la qualité maximale (au maximum, elle reste plafonnée selon le code de l’enum). `InteractionAllowResult` encode les résultats d’autorisation d’interaction consommés par les domaines territoriaux. `StaffSetRoleResult` distingue succès, rôle identique, nécessité de désigner un nouveau chef et tentative de choisir la cible actuelle. `ItemTag` porte un identifiant d’objet, tandis que `WorldName`, `Coordinate` et `ChunkCoord` donnent des types explicites pour les emplacements et chunks. Le port `EventPublisher` laisse le mécanisme de publication à l’adaptateur.

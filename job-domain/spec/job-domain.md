# Spec — `job-domain`

## Objectif

Modéliser les métiers, leurs sources de récolte et produits, la progression des joueurs, les talents et les arbres de talents. Le service de métier transforme les événements de récolte en éventuelles récompenses.

## Modèle et règles

- Un `Job` regroupe un nom, un libellé d’affichage, un ensemble de talents, des sources et des produits.
- Une `Source` est classée par `SourceType`. `Sources` associe des sources à des sorties matérielles ; `Products` représente les produits du métier.
- `PlayerJobProgression` porte la progression et le niveau des talents d’un joueur pour un métier. Les identifiants métier et joueur sont des types dédiés (`JobName`, `PlayerID`).
- Les talents déclarent leurs prérequis, leur niveau maximal, le coût de chaque niveau et les bonus associés. Un talent sans parent est disponible ; sinon tous ses parents doivent être au niveau supérieur à zéro.
- `JobService.listJobs` charge les métiers depuis le dépôt et prépare l’index source → métier. `getJob` lit un métier par nom.
- `JobService.process` ignore une source nulle, inconnue ou sans sorties. Pour chaque sortie, il effectue un tirage de chance de butin à partir des bonus du talent, puis attribue un objet de quantité 1 et une qualité déterminée par le bonus de qualité.
- La qualité part de `POOR` et progresse tant que le tirage aléatoire est inférieur au bonus, jusqu’à `EXCEPTIONAL`.
- Le coût d’amélioration est le coût du prochain niveau. `canBuyUpgrade` compare ce coût aux points de talent disponibles. L’arbre est fourni par `JobSkillTreeRepository`.

## Interfaces et dépendances

Les ports serveur couvrent la lecture des métiers, des progressions, des arbres, des missions et des objets. Les services côté utilisateur sont `JobService`, `JobPlayerProgressionService` et `JobTalentTreeService`.

## Critères d’acceptation

1. Une source associée à un métier et ayant des sorties déclenche un tirage indépendant par sortie.
2. Le bonus effectif d’un type est la somme des valeurs de bonus de talents correspondantes au niveau actuel du joueur.
3. Une sortie n’est accordée que si le tirage de chance réussit ; sa qualité est déterminée séparément.
4. Un talent sans parent est disponible ; un talent à prérequis est disponible uniquement si chacun de ses parents a un niveau positif.
5. Le coût d’amélioration utilise le niveau suivant, et `canBuyUpgrade` vérifie les points disponibles.

## Limites constatées

`upgradeSkill` retourne actuellement toujours `false` : l’achat effectif d’un talent n’est pas implémenté dans ce service. Les bornes des bonus, la normalisation des valeurs de chance et la persistance/cache des métiers doivent être précisées avant de constituer des garanties supplémentaires.

## Détails complémentaires du modèle de métier

- `JobMission` stocke un objectif de livraison sous la forme ID, UUID joueur, métier, matériau, quantité demandée, qualité minimale, quantité déjà livrée et récompense. `MissionDepositResult` exprime la quantité livrée et le reste. Le modèle record ne porte pas à lui seul de validation ou d’algorithme de dépôt.
- `Source` est un couple matériau/type; `SourceType` distingue la nature de la source. `Sources` mappe chaque source vers une liste de matériaux de sortie et retourne la liste associée (ou `null` si absente). `Products` est un ensemble séparé de matériaux déclarés comme produits.
- `Talent` contient son identifiant, le bonus, l’objet/matériau visé, les valeurs par niveau, les prérequis, le niveau maximal et le coût par niveau. `getValue(level)` fournit la valeur de bonus au niveau demandé. `Talents.get(material, bonus)` filtre les talents à la fois par matériau et type de bonus.
- `TalentTree` contient une matrice de `CellType`; `SkillCell` référence un talent, `RootCell` la racine et `Arrow` une orientation/connexion rendue via `ArrowType` et ses données de modèle d’objet.
- `JobPlayerProgression` contient métier, points disponibles et progressions de talents. Un talent absent de la map est de niveau zéro.

## État réel des achats de talents

`JobPlayerProgressionService` sait tester la disponibilité des prérequis, lire un niveau, calculer le prix du niveau suivant et comparer ce prix aux points disponibles. Le calcul du prix lève `IllegalStateException` quand le talent a atteint son niveau maximal. Il ne vérifie pas les prérequis dans `canBuyUpgrade`. L’opération `upgradeSkill` n’effectue aucune mutation ni persistance et retourne toujours `false`.

## Détails du traitement de récolte

`JobService.listJobs` initialise son cache de métiers et son index source-vers-métier au premier besoin. `process` ignore une source nulle/inconnue ou dépourvue de sorties. Chaque matériau de sortie reçoit un tirage indépendant comparé au bonus cumulé `LOOT_CHANCE`; si le tirage réussit, un item est accordé et un autre bonus détermine la qualité. La qualité commence à `POOR` et avance d’un rang par tirage réussi, jusqu’à `EXCEPTIONAL`. Les bonus cumulés ne sont ni bornés ni normalisés ici; la qualité excelle donc immédiatement si le bonus est supérieur ou égal à 1.

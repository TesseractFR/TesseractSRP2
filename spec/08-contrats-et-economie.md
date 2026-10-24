# Contrats et économie

[← Retour à la vision globale](./main.md)

## Contrats et économie

Les contrats et les commissions sont la seule source d'argent du jeu. Tout le reste déplace ou détruit de l'argent déjà créé. Les deux systèmes ont chacun leur définition (voir le [lexique](./00-lexique.md)) : les contrats s'achètent chez un marchand du spawn et se livrent à un PNJ, les commissions sur des tableaux.

*Contrats et commissions sont deux modules distincts (`contrat`, `commission`) ; ils ne partagent que le calcul de valeur (module `economie`). Ce document intègre les discussions de design de juillet 2026 et suivantes. Les points marqués « à confirmer » sont ambigus ou non tranchés.*

**Deux types, pour les contrats comme pour les commissions.** Récolte (livrer des items récoltés) et artisanat (livrer des items fabriqués). Chaque mission a un **tier**. Côté récolte, l'arbre de talents donne les tiers (palier de l'item) ; côté artisanat, c'est le tier de la recette. Les 273 recettes prévues ne sont pas toutes proposées d'emblée.

**Contrats (parchemins).**

- Chaque joueur a un stock de **tickets de contrat** (nom provisoire), **renouvelé chaque jour** : le nombre dépend du grade, avec un maximum initial. On peut aussi gagner un ticket en jouant (activité, loot, vote) : le mécanisme reste à déterminer.
- Chez l'**unique PNJ marchand de contrats**, un ticket achète **un parchemin physique** contenant une mission **aléatoire**, de récolte ou d'artisanat.
- La difficulté suit la **progression du joueur dans le métier et l'item** concernés (talents débloqués, chance de loot), pas son grade. Elle est figée à l'achat.
- On livre le parchemin et les items demandés au **PNJ du métier concerné** (**un PNJ par métier**), **en une seule fois** (tout ou rien, la quantité est bornée par la place d'inventaire). On touche **toute la récompense** en lys, sans XP : l'XP de métier se gagne à la récolte (`06`).
- Celui qui livre touche la récompense, même s'il n'a pas acheté le parchemin.
- Le parchemin porte une **date d'expiration** (3 jours proposés), ce qui évite d'accumuler du stock. Non livré, il est gardé, perdu, ou **revendu à l'hôtel des ventes** avec un **prix encadré** (plafond en pourcentage de la récompense, à fixer). Un parchemin expiré est supprimé automatiquement, y compris de l'hôtel des ventes.
- Un contrat indique un métier (dont le PNJ est le client), un type, un item, une quantité, une qualité minimale et une récompense en lys. Un parchemin de découverte accompagne les débutants. Certains contrats demandent des qualités faibles pour écouler les items de rebut.
- Les valeurs de départ (tickets par jour et par grade, durée du parchemin, plafond de prix) restent provisoires.

**Commissions (tableaux de guilde).**

- Une commission est une mission affichée sur un **tableau de commissions**, et elle est **par tableau** : elle ne dépend pas d'un joueur. Sa difficulté **s'adapte au tableau** (tier maximal, qualité maximale).
- Le tableau est une **structure**, au spawn comme dans chaque ville de guilde.
- Le tableau a une partie **publique** (visible de tous ; les joueurs externes à la guilde sont taxés à l'usage) et une partie **guilde** (réservée aux membres). Présentation : 2 lignes (une par type d'item) et 3 colonnes (1 publique, 2 de guilde), ou 2 pages avec plusieurs slots, solution préférée.
- Les commissions sont **communes au tableau** : il y a de la concurrence entre joueurs.
- Les commissions sont générées automatiquement : les guildes ne les définissent pas. Elles se renouvellent à un rythme à fixer (2 à 3 heures en première valeur).
- **Tableau du spawn** : commissions de base, tier 1, qualité médiocre au maximum, récompenses basses, pour gagner un peu d'argent. Les structures de tier 1 y sont toutes disponibles.
- **Tableau de ville (guilde)** : il s'améliore avec des lys, à prix croissant et avec un niveau de ville minimum :
    - tier maximal (1 au départ, jusqu'à 3) ;
    - déblocage de slots supplémentaires (slots 2 et 3, formule à éclaircir) ;
    - qualité maximale des commissions d'un slot ;
    - bonus d'argent, 5 % au maximum.
- Sur la partie guilde, la récompense est partagée entre le joueur et la banque de sa guilde. La part de la banque est prélevée sur la récompense, pas ajoutée, pour ne pas créer plus d'argent. Elle serait plus forte pour les membres de la guilde, avec une taxe plus lourde sur une commission faite pour une autre ville (à définir).
- Sur la partie publique, un joueur externe paie une **taxe d'usage** à la banque de la guilde propriétaire du tableau.
- Chaque commission livrée donne de l'XP à la guilde propriétaire du tableau.
- Si la ville ne sait pas fabriquer l'item demandé, le joueur peut relancer la commission (« attendre son reset ») ou aller le fabriquer dans une autre ville (à confirmer).

**Siphons.** Les grades, les kits d'amélioration des ailes, les structures de guilde et les améliorations de station ou de tableau (payées en lys à prix croissant), les catalyseurs, l'assurance contre la perte de stuff, la taxe de l'hôtel des ventes.

&#91;embedded content: flux d'argent · 1 robinet, 2 dépenses, 1 banque\]

Le système crée de l'argent uniquement par les contrats et les commissions. La banque de guilde reçoit une part des récompenses des **commissions** de son tableau, la taxe d'usage payée par les joueurs externes sur la partie publique, et les ventes des shops de ville. Les contrats ne versent rien à une guilde.

**Valeur d'un item.** Tous les métiers doivent rapporter à peu près autant à l'heure. La valeur de référence d'un item de récolte se calcule ainsi, avec les cadences mesurées pendant la bêta :

```latex
valeur = \frac{1}{\text{chance de loot} \times \text{récoltes par heure}} \times \text{taux horaire cible} \times \text{coef. qualité} \times \text{coef. rareté}
```

La valeur d'un item fabriqué reste à définir (valeur des ingrédients, durée du craft, échecs possibles).

La récompense est la somme des valeurs demandées, multipliée par un coefficient de tier (ou de réputation, si les deux notions n'en font qu'une) et un bonus de qualité, puis par un coefficient d'échelle : celui **du joueur** pour un contrat, celui **du tableau** pour une commission (avec le bonus d'argent du tableau ; le tableau du spawn paie moins que les villes). Ce calcul est commun aux deux modules. Prévoir dès maintenant un champ de multiplicateur de demande (bonus sur ce dont la cité a besoin, malus sur ce qui est saturé), même inactif au début.

**Anti-comptes secondaires.** Un compte neuf ne peut ni acheter de parchemin ni transférer d'argent avant un certain temps de jeu, pour ne pas multiplier les tickets quotidiens. La revente de parchemins est voulue, mais le prix est encadré. Les tickets gagnés en jeu sont plafonnés par joueur.

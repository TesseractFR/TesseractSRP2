# Spec — `commission-domain`

*Les issues O1 à O9 et `spec/08-contrats-et-economie.md` ne sont qu’indicatifs : en cas de différence, cette spec fait foi.*

## Objectif

Gérer les commissions : des missions de récolte ou d’artisanat générées automatiquement et affichées sur un tableau, au spawn ou dans une guilde. Une commission appartient au tableau, pas à un joueur : elle est visible par tous ceux qui ont accès à sa partie et les joueurs y contribuent en concurrence, chacun livrant ce qu’il peut jusqu’à atteindre la quantité demandée. Chaque joueur touche 100 % de la valeur de ce qu’il livre, moins une taxe s’il est externe à la guilde. Sur un tableau de guilde, la guilde gagne de l’XP et reçoit cette taxe.

Avec les contrats, les commissions sont la seule source d’argent du jeu. Les deux modules sont distincts : un contrat est personnel à l’achat et passe par un parchemin ; une commission est commune et n’a ni parchemin ni ticket.

## Modèle et règles

### Tableau

- Un `CommissionBoard` appartient soit au spawn (un seul), soit à une guilde (un par guilde). Il porte son tier, son tier de récolte, son tier d’artisanat, ses slots de récolte et d’artisanat, son bonus d’argent et son taux de taxe d’usage. Les guildes ne choisissent pas les commissions.
- Chaque guilde a son tableau en base dès son premier accès. Dans le monde, le tableau est un item, obtenu par craft ou par achat, que la guilde pose et récupère : cette partie est gérée par le moteur des structures, pas par ce module.
- Un tableau a deux parties, visibles sur un seul écran. La partie **publique** (`PUBLIC`) est accessible à tous. La partie **guilde** (`GUILD`) est réservée aux membres de la guilde propriétaire : un externe ne la voit pas et ne peut pas y livrer.
- Au départ, un tableau de guilde a **un slot par type de chaque partie**, soit 4 commissions : une de récolte et une d’artisanat sur la partie publique, de même sur la partie guilde. Toutes ses améliorations sont au niveau 1 et il n’a aucun bonus.
- La guilde améliore son tableau avec sa banque, sur décision d’un rôle Adjoint ou supérieur. Une amélioration coûte des lys à prix croissant :
  - le **tier du tableau** est l’amélioration centrale. Pour monter d’un tier, le tableau doit avoir reçu assez de livraisons, par type et par qualité (par exemple 100 items de récolte et 100 d’artisanat sur des commissions exceptionnelles), en plus du coût ;
  - les autres améliorations demandent un tier de tableau minimal à chaque palier : le tier de récolte et le tier d’artisanat (les items que le tableau peut demander), les slots de récolte et les slots d’artisanat (+1 commission du type dans la partie publique et dans la partie guilde), et le bonus d’argent.
- Les mêmes rôles (Adjoint et chef) peuvent ajuster le taux de taxe d’usage de leur tableau, gratuitement, entre 0 et 50 %. Le taux d’un tableau neuf vient de la configuration (25 % par défaut).
- Le tableau du spawn n’est pas améliorable : artisanat et récolte de tier 1, partie publique seulement, récompenses basses grâce à un coefficient dédié. Son nombre de commissions par type vient de la configuration (6 par défaut), disponibles d’emblée.

### Commission

- Une `Commission` porte son tableau, sa partie, son type (`HARVEST` ou `CRAFT`), le matériau, la quantité demandée, la quantité déjà livrée, la qualité minimale, la récompense totale et son état.
- Invariants : tier de l’item ≤ tier du tableau pour son type, pas plus de commissions à livrer que de slots débloqués, quantité ≥ 1, 0 ≤ quantité livrée ≤ quantité demandée, récompense ≥ 0.
- Une commission est terminée quand toute la quantité est livrée. Il n’y a ni réservation ni date d’expiration : elle reste sur le tableau jusqu’à ce qu’elle soit terminée.

### Génération et remplacement

- `CommissionBoardRefillService` remplit les slots débloqués à partir d’un catalogue d’items. Pour `CRAFT`, ce sont les résultats des recettes dont le tier ne dépasse pas le tier d’artisanat du tableau. Pour `HARVEST`, ce sont les items listés en configuration dont le tier ne dépasse pas le tier de récolte du tableau : cette liste décide quels items peuvent être demandés et à quel tier, et un item absent n’est jamais demandé. Il n’y a pas de tier 0, car un joueur n’a aucun item débloqué de base mais un point à placer où il veut. La progression des joueurs n’entre jamais en compte, puisqu’elle est individuelle. Il tire la qualité minimale uniformément entre `POOR` et `EXCEPTIONAL` ; une pondération viendra plus tard, comme pour les contrats. La source aléatoire est injectée pour les tests.
- Comme pour les contrats, le tirage ne tient compte ni du joueur, ni de ce que la guilde sait fabriquer : on peut tomber sur n’importe quel item. Le joueur se le procure comme il veut (autre guilde, achat à un autre joueur).
- Comme pour les contrats, la quantité de base, sa variance et le poids de tirage viennent de la rareté de l’item, en configuration ; la quantité est tirée au hasard autour de la base. La quantité peut dépasser ce que tient un inventaire, puisque la livraison se fait en plusieurs fois.
- Terminer une commission débloque la suivante : elle sort de la liste, les suivantes remontent et une nouvelle commission arrive aussitôt à la fin. C’est ce qui donne un intérêt à terminer une mission, puisque les gains sont versés au fil des livraisons.
- Le tableau compte les items livrés par type et par qualité demandée, pour les conditions de son tier. Il compte aussi les commissions terminées depuis sa création, quel que soit le livreur. Ce compteur est stocké mais pas affiché ; il pourra servir à un classement. Il disparaît avec le tableau si la guilde est supprimée.
- Il n’y a pas de renouvellement périodique : une commission en cours ne change pas, même si personne ne la livre. Un item mis en configuration est toujours chargé et garde son prix ; une commission en cours ne peut donc pas tomber sur un item indisponible. Un item sans prix est seulement écarté du tirage, avec un warning.
- Débloquer un slot le remplit aussitôt. Les autres améliorations ne s’appliquent qu’aux commissions générées ensuite.
- Un slot sans item possible reste vide : la génération ne doit jamais échouer. Il est rempli à la prochaine ouverture du tableau.

### Livraison

- `CommissionDeliveryService.deliver` vérifie dans l’ordre : commission existante et encore ouverte, accès à la partie, au moins un item à la qualité minimale ou au-dessus.
- La progression est **partagée** : un joueur livre `min(items possédés, quantité restante)`. Plusieurs joueurs peuvent contribuer à la même commission, sans limite par joueur, et un même joueur peut y revenir plusieurs fois.
- Les items de plus basse qualité admissible partent d’abord et sont détruits. Livrer une qualité supérieure ne rapporte pas plus : le prix unitaire est figé à la qualité minimale.
- La progression et les crédits (joueur, banque, XP) forment une seule opération atomique, enregistrée avant le retrait des items : si la base échoue, rien n’est payé ni retiré. Tout s’exécute sur le thread principal du serveur, donc une commission ne peut pas changer pendant une livraison.
- Un refus est signalé par une exception métier, que l’adapter traduit en message pour le joueur.

### Récompense

Récompense totale = valeur des items demandés (B7) × part payée au spawn (100 % sur un tableau de guilde) × (1 + bonus d’argent). Tant que B7 n’existe pas, la valeur vient du prix de base par item et par qualité déjà disponible dans `customItem`. La récompense est calculée à la génération et figée dans la commission.

Chaque livraison rapporte **100 % de ce qui est livré** : pour « 10 bois pour 100 lys », deux joueurs qui en livrent 5 touchent 50 lys chacun. Le montant d’une livraison est `arrondi(récompense × livré après / quantité) − arrondi(récompense × livré avant / quantité)`, si bien que la somme des paiements est exactement la récompense, quel que soit le découpage.

Les guildes ne prélèvent aucune part sur les récompenses. Le seul argent qu’elles reçoivent des commissions est la taxe d’usage :

| Tableau | Partie | Livreur | Joueur | Banque de la guilde |
|---|---|---|---|---|
| Spawn | Publique | Tout joueur | 100 % | Aucune |
| Guilde | Guilde | Membre | 100 % | Aucune |
| Guilde | Publique | Membre | 100 % | Aucune |
| Guilde | Publique | Externe | 100 % − taxe d’usage | Taxe d’usage |

La taxe est retirée du paiement, au taux en vigueur au moment de la livraison : l’externe ne paie rien d’avance et voit le montant net avant de livrer.

Sur un tableau de guilde, chaque livraison donne de l’XP à la guilde propriétaire, quel que soit le livreur. L’XP d’une commission vaut sa récompense multipliée par un taux de configuration, et elle est accordée au fur et à mesure, proportionnellement à la quantité livrée. Seuls les membres de la guilde voient cette XP. Le spawn ne donne pas d’XP. La livraison ne donne pas d’XP de métier.

## Interfaces et dépendances

Le domaine n’a aucune annotation Spring. Il utilise `PlayerId`, `MaterialName`, `MoneyValue` et `Quality` de `commun-domain`, et atteint les guildes, les items et la monnaie par ses ports.

**Côté utilisateur**, un service par cas d’usage :
- `CommissionBoardService` : obtenir un tableau, savoir ce qu’un joueur peut voir et gérer, supprimer le tableau d’une guilde.
- `CommissionBoardManagementService` : améliorer le tableau (tier, conditions de livraison, coût) et ajuster la taxe.
- `CommissionDeliveryService` : lister les commissions visibles, préparer et enregistrer une livraison.
- `CommissionBoardRefillService` : générer et remplacer les commissions d’un tableau.

**Côté serveur :**

| Port | Rôle |
|---|---|
| `CommissionBoardRepository` | Créer, lire et enregistrer les tableaux, supprimer celui d’une guilde |
| `CommissionRepository` | Lire et remplacer les commissions d’un tableau ; `recordDelivery` applique atomiquement la progression, les crédits et les compteurs ; `countDeliveredItems` lit les items livrés par type et qualité |
| `CommissionCatalogRepository` | Catalogue par type et tier, valeur d’un item |
| `CommissionConfigRepository` | Valeurs lues dans le fichier de configuration |
| `GuildMembershipPort` | Appartenance et rôle d’un joueur, membres d’une guilde |
| `GuildBankPort` | Solde et débit de la banque pour les améliorations |
| `PlayerMaterialPort` | Compter et retirer des items à partir d’une qualité minimale |

**Issues liées, toutes ouvertes :**

| Dépendance | Solution provisoire |
|---|---|
| B7 #170, valeur et récompense | Prix de base de `itemValue.yml` |
| B2 #72, `MoneyService` et journal | `PlayerMoneyService.adjustMoney` |
| A7 #70, comptage à partir d’une qualité | Boucle sur les qualités avec `CustomItemService` |
| E4 #92, banque de guilde | `GuildService.payFromBank` et `GuildService.addCommissionIncome` |
| F8 #146, structures du spawn | Aucune : les recettes de tier 1 ne sont pas encore disponibles au spawn |

`GuildService.addCommissionIncome` crédite la banque et donne l’XP de guilde, avec le passage de niveau et son `GuildLevelUpEvent`.

## Critères d’acceptation

1. Toute commission générée respecte les slots de son type et le tier de son tableau pour son type.
2. La génération ne dépend ni du joueur qui consulte, ni des recettes connues de la guilde.
3. Un externe ne voit et ne peut livrer que la partie publique ; une tentative sur la partie guilde est refusée sans rien retirer.
4. Sans aucun item de qualité suffisante, rien n’est retiré ni versé.
5. Une livraison retire au plus la quantité restante, les items de plus basse qualité admissible d’abord, jamais sous la qualité minimale.
6. Deux joueurs qui livrent 5 items sur une commission de 10 à 100 lys touchent 50 lys chacun et la commission est terminée.
7. Quel que soit le découpage des livraisons, la somme versée (joueurs et taxes) est exactement égale à la récompense totale.
8. La quantité livrée ne dépasse jamais la quantité demandée.
9. Un membre touche 100 % sur les deux parties ; un externe touche le montant moins la taxe, et cette taxe va à la banque de la guilde.
10. Le tableau du spawn ne verse rien à une banque et ne donne pas d’XP de guilde ; sur un tableau de guilde, chaque livraison donne de l’XP à la guilde propriétaire et les passages de niveau sont appliqués.
11. Une amélioration refusée (rôle, tier du tableau, livraisons insuffisantes pour monter de tier, fonds) ne change ni le tableau ni la banque ; une amélioration réussie s’applique aux commissions générées ensuite, et un slot débloqué est rempli aussitôt.
12. Un Adjoint ou le chef peut changer le taux de taxe entre 0 et 50 % ; hors limites ou sans le rôle, le taux ne change pas. Le nouveau taux s’applique aux livraisons suivantes.
13. Une commission terminée sort aussitôt de la liste : les suivantes remontent et une nouvelle arrive à la fin ; une commission en cours n’est jamais remplacée.
14. Une guilde a un tableau en base sans action de sa part ; sa suppression supprime son tableau, ses commissions et ses compteurs.
15. Pour monter de tier, les items livrés sur des commissions d’une qualité supérieure comptent aussi pour une condition d’une qualité inférieure.

## Configuration

Les valeurs (taux, coûts, paliers, XP par lys, quantités) sont dans le fichier de configuration du module, comme pour les autres modules.

## Incohérences à corriger dans les specs générales

- `08-contrats-et-economie.md` prévoit encore une part de banque sur les récompenses et un taux non ajustable, et sa présentation reste en 2 pages.
- `09-guildes.md` parle encore de « tableau de contrats », de « contrats publiés » payés par la banque et de « contrats de guilde » comme source d’XP, alors que les guildes ne publient rien et que la récompense est de l’argent créé.
- `15-architecture-technique.md` et `16-decisions-et-todo.md` décrivent encore un tableau personnel de contrats.
- `00-lexique.md`, cité par `08`, n’existe pas dans le repo.

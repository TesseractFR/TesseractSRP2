# Documentation du code — `commission-adapter`

Ce document décrit **ce que fait le code** de `commission-adapter` : chaque classe, ses appels et ses cas particuliers. Il complète la spec (`commission-adapter.md`, ce que le module doit faire) et la documentation du domaine (`commission-domain-doc.md`, dont il suppose la lecture).

Rôle du module : implémenter les ports `serverside` du domaine avec le serveur réel (JPA, YAML, ItemsAdder, Bukkit), et exposer les services du domaine aux joueurs (clic sur le tableau, menus). La pose de l'item du tableau dans le monde relève du moteur des structures. Il ne contient aucune règle métier.

## 1. Carte du module

```
onl.tesseract.srp.commission.adapter
├── furniture/CommissionBoardItem                identifiant ItemsAdder du tableau
├── serverside                                        implémentations des ports du domaine
│   ├── jpa
│   │   ├── JpaCommissionBoardRepository              → CommissionBoardRepository
│   │   ├── JpaCommissionRepository                   → CommissionRepository
│   │   ├── CommissionBoardMapper, CommissionMapper   conversion domaine ↔ entités
│   │   ├── entity/CommissionBoardEntity, CommissionEntity
│   │   └── repository/CommissionBoardJpaRepository, CommissionJpaRepository   (Spring Data)
│   ├── catalog/SkillCustomItemCommissionCatalogRepository   → CommissionCatalogRepository
│   ├── yaml/YamlCommissionConfigRepository           → CommissionConfigRepository et CommissionCatalogConfig
│   ├── guild/GuildServiceMembershipPort              → GuildMembershipPort
│   │   guild/GuildServiceBankPort                → GuildBankPort
│   ├── material/CustomItemPlayerMaterialPort         → PlayerMaterialPort
└── userside
    ├── config/CommissionProvider                     déclaration Spring des services du domaine
    └── controller
        ├── event/CommissionBoardClickListener    clic sur le tableau
        │   event/CommissionGuildDeletedListener      suppression d'une guilde
        ├── menu/CommissionBoardMenu, CommissionDeliveryMenu, CommissionManagementMenu
        │   menu/CommissionDisplay                    textes et lignes de lore
        └── message/CommissionMessageFormatter, CommissionChatFormats
```

Schéma des appels :

```mermaid
flowchart TD
    JOUEUR[Joueur] --> FL[CommissionBoardClickListener]
    FL --> BM[CommissionBoardMenu]
    BM --> DM[CommissionDeliveryMenu]
    BM --> MM[CommissionManagementMenu]
    BM & DM & MM --> SVC[Services du domaine]
    GUILDE[GuildDeletedEvent] --> GL[CommissionGuildDeletedListener] --> SVC
    GMENU[GuildMenu de territory-adapter] --> SVC
    SVC --> PORTS[Implémentations des ports]
```

## 2. Assemblage : `CommissionProvider`

`@Configuration` Spring. Le domaine n'a aucune annotation Spring ; ce fichier déclare ses cinq services comme beans, et Spring y injecte les implémentations des ports **par type d'interface**. `CommissionBoardRefillService` reçoit un `new Random()` comme source d'aléa.

## 3. Persistance (JPA)

### Tables

`CommissionBoardEntity` → table `t_commission_boards` :

| Colonne | Type | Remarque |
|---|---|---|
| `id` | `Long`, clé auto | |
| `guild_id` | UUID en texte (36), unique | `null` pour le spawn |
| `board_tier`, `max_harvest_tier`, `max_craft_tier`, `harvest_slots`, `craft_slots`, `money_bonus_level` | entier | |
| `usage_tax_rate` | décimal | |
| `completed_commission_count` | entier | incrémenté par une requête dans la transaction de la livraison |

`CommissionEntity` → table `t_commissions` :

| Colonne | Type | Remarque |
|---|---|---|
| `id` | `Long`, clé auto | |
| `board_id` | `Long` | indexée, sans relation JPA |
| `part`, `type` | enum en texte | |
| `material` | texte | |
| `quantity`, `delivered_quantity`, `reward` | entier | |
| `minimal_quality` | enum en texte | |

`CommissionDeliveredItemCountEntity` → table `t_commission_delivered_items` : `id`, `board_id`, `type`, `minimal_quality` et `item_count`, avec une contrainte d'unicité sur (`board_id`, `type`, `minimal_quality`). Une ligne compte les items livrés sur les commissions de ce type et de cette qualité demandée.

Les tables sont créées automatiquement. Il n'y a pas de colonne d'état : une commission est terminée quand `delivered_quantity = quantity`.

### Mappers

- `CommissionMapper` : `toEntity(draft)` crée une entité sans id ni livraison ; `toDomain(entity)`.
- `CommissionBoardMapper` : `toNewSpawnBoardEntity(levels)` et `toNewGuildBoardEntity(guildId, taxRate)` créent les entités de départ, celle d'une guilde avec `CommissionBoardLevels.GUILD_BOARD_START` ; `copyChanges(board, entity)` recopie niveaux et taxe ; `toDomain(entity)` reconstruit le tableau, ou le tableau du spawn si `guild_id` est nul.

### Requêtes

`CommissionBoardJpaRepository` : `findByGuildId`, `findByGuildIdIsNull` (le spawn), et `incrementCompletedCommissionCount`.

`CommissionJpaRepository` : `findByBoardIdOrderById`, et deux requêtes JPQL avec `@Modifying(flushAutomatically = true, clearAutomatically = true)` :

- `deleteByBoardId(boardId)` ;
- `addDeliveredQuantity(id, itemCount)` : ajoute `itemCount` à `delivered_quantity`.

`CommissionDeliveredItemCountJpaRepository` : `findByBoardIdAndTypeAndMinimalQuality`, et `deleteByBoardId`.

### `JpaCommissionBoardRepository` (→ `CommissionBoardRepository`)

Toutes les méthodes sont `@Transactional` (lecture seule pour les lectures). `save` relit l'entité et y recopie les changements, sans toucher au compteur de commissions terminées, que seule la livraison incrémente. `deleteByGuild` supprime d'abord les commissions et les compteurs du tableau, puis le tableau, dans la même transaction.

### `JpaCommissionRepository` (→ `CommissionRepository`)

**`recordDelivery(delivery)`**, dans une seule transaction :

1. relit la commission, ajoute les items livrés au compteur de son type et de sa qualité demandée (la ligne est créée au besoin), puis `addDeliveredQuantity` ;
2. `playerService.adjustMoney(joueur, gains)`, qui ne refuse jamais un gain positif ou nul ;
3. si le tableau appartient à une guilde, `guildService.addCommissionIncome(guilde, taxe, xp)` ;
4. si la livraison termine la commission, incrémente le compteur du tableau.

Une panne de base annule toute la transaction. Il n'y a pas de contrôle de concurrence : tout s'exécute sur le thread principal, et la commission est relue juste avant dans le même appel.

## 4. Configuration : `YamlCommissionConfigRepository`

Lit `plugins/Tesseract/commission/commissions.yml` (chemin relatif au dossier du serveur). Le modèle est dans les ressources du plugin, qui le copie sur le serveur au démarrage s'il manque. La classe implémente le port `CommissionConfigRepository` du domaine et l'interface `CommissionCatalogConfig` de l'adapter, qui sert au catalogue.

- **Chargement** : le fichier est lu une fois, à la création du bean ; une modification demande un redémarrage. Fichier absent : valeurs par défaut.
- **Fichier absent** : configuration vide, donc les valeurs par défaut ci-dessous. Avec un poids par défaut de 0, aucune commission n'est générée.
- **Aucune validation** des valeurs lues, sauf celle des records du domaine (`CommissionBoardUpgradeStep` refuse un prix ou un niveau négatif).

| Méthode | Clé | Défaut |
|---|---|---|
| `getSpawnRewardRate()` | `spawn-reward-rate` | 1 |
| `getMoneyBonusRate(level)` | `upgrades.money-bonus.<level>.value` | 0 |
| `getGuildXpPerLys()` | `guild-xp-per-lys` | 0 |
| `getDefaultUsageTaxRate()` | `default-usage-tax-rate` | 0,25 |
| `getSpawnSlotsPerType()` | `spawn-slots-per-type` | 6 |
| `findUpgradeStep(type, level)` | voir ci-dessous | vide |
| `findAllHarvestTiers()` | `harvest-tiers` | table vide |
| `getRarityQuantity(rareté)` | `quantity-by-rarity.<RARETÉ>.quantity` et `.variance` | 0 et 0 |
| `getRarityWeight(rareté)` | `rarity-weights.<RARETÉ>` | 0 |

`findUpgradeStep` lit `upgrades.<amélioration>.<niveau>`, où l'amélioration est `board-tier`, `max-harvest-tier`, `max-craft-tier`, `harvest-slots`, `craft-slots` ou `money-bonus`. Un palier a un `cost`, un `board-tier` minimal (1 s'il est absent) et, pour le tier du tableau, des `deliveries` indexées par type : `HARVEST: { quality: GOOD, count: 100 }`. Une section absente signifie que le niveau maximal est atteint.

Le nombre de slots configurés ne doit pas dépasser 6 par type, le menu n'en affichant pas plus.

## 5. Implémentations des ports

### 5.1 `SkillCustomItemCommissionCatalogRepository` (→ `CommissionCatalogRepository`)

`findAllCatalogItems(type)` :

- `HARVEST` : les items listés dans `harvest-tiers`, avec leur tier. Un item absent de la liste n'est jamais demandé.
- `CRAFT` : le résultat de toutes les recettes des compétences, lues avec `SkillService.findAllSkills()`. Si plusieurs recettes donnent le même item, le plus petit tier est retenu.

Un item sans prix (`customItemService.hasMoneyValue`) est ignoré avec un warning. Les items mis en configuration sont toujours chargés par ItemsAdder : leur chargement n'est pas vérifié. Pour les autres, le poids et la quantité viennent de la rareté : `customMaterial.rarity()`, ou `COMMON` pour un item vanilla.

`getUnitValue` lit `customItemService.getMoneyValue(material, quality)`. `getItemTier` relit `harvest-tiers` ou les recettes ; un item demandé par une commission y est toujours.

### 5.2 Ports de guilde

Tous s'appuient sur `GuildService` de `territory-domain` :

- `GuildServiceMembershipPort` : `isMember` compare la guilde du joueur à celle du tableau ; `canManageBoard` demande en plus `GuildRole.canManageBoard()` (Adjoint ou chef).
- `GuildServiceBankPort` : `getGuildMoney` et `payFromBank`.

### 5.3 `CustomItemPlayerMaterialPort` (→ `PlayerMaterialPort`)

S'appuie sur `CustomItemService`, en bouclant sur les qualités :

- item custom : qualités de la qualité minimale à `EXCEPTIONAL`, la plus basse d'abord ;
- item vanilla : il n'a pas de qualité, donc une seule lecture en `POOR`, quelle que soit la qualité minimale.

`removeAtLeast` ne recompte pas : le service vient de compter les items dans le même appel.

## 6. Messages : `CommissionMessageFormatter`

Composant Spring qui construit tous les messages de chat, préfixés par `CommissionChatFormats`, en vert pour une réussite et en rouge pour un refus.

- `formatRefusal(exception)` : un `switch` sur la hiérarchie scellée `CommissionException`, avec un texte par exception. Ajouter une exception au domaine fait échouer la compilation tant qu'elle n'a pas de texte.
- `formatDeliverySucceeded(commission, delivery)` : une seule ligne pour le livreur, ses items et ses lys (taxe déjà déduite, détaillée dans le menu avant la livraison), l'XP de sa guilde s'il en est membre, et « Commission terminée ! » au bout si c'est la dernière livraison.
- `formatGuildDeliveryNotice(livreur, commission, delivery)` : aux autres membres connectés de la guilde propriétaire (le livreur a déjà sa ligne), le nom du livreur, ses items et ce que gagne la guilde (taxe d'un externe, XP). Rien n'est envoyé au spawn ni si la guilde ne gagne rien.
- `formatMaterialName(material)` : le nom affiché d'un item, lu dans la configuration des items custom (`display_name`) ; un item vanilla garde un nom tiré de son identifiant (`IRON_ORE` → « Iron ore »). Les menus et les messages l'utilisent.
- Les messages d'information utilisent `COMMISSION_CHAT_FORMAT` : texte gris, valeurs en couleur (items en blanc, lys en vert, XP en bleu). `COMMISSION_CHAT_SUCCESS` reste pour les confirmations de gestion.
- Un message de confirmation pour une amélioration et pour un changement de taxe.

## 7. Entrées : clic sur le tableau, menus, événements

### 7.1 Listeners

`CommissionBoardClickListener` :

- `onFurnitureInteract` (`FurnitureInteractEvent` d'ItemsAdder) réagit au clic sur le tableau `BOARD_ITEM_ID`. L'événement est annulé, puis le tableau est choisi selon le chunk du tableau : celui de la guilde propriétaire, sinon celui du spawn, quel que soit le monde. `CommissionBoardMenu` s'ouvre ensuite.

`CommissionGuildDeletedListener` (Spring, `GuildDeletedEvent`) appelle `deleteGuildBoard`.

### 7.2 `CommissionBoardMenu`

Étend `ItemAdderMenu`, en 6 lignes sur 9 colonnes, avec le fond `tesseract:commission_board_menu`, déclaré dans `configs/gui/guis.yml` d'ItemsAdder. Sans texture installée, le menu s'ouvre sans fond et un warning est journalisé à chaque ouverture.

| Emplacement | Contenu |
|---|---|
| 0, 8 | Icône de récolte |
| 1, 2, 3, 10, 11, 12 / 7, 6, 5, 16, 15, 14 | Slots de récolte, public puis guilde |
| 27, 35 | Icône d'artisanat |
| 28, 29, 30, 37, 38, 39 / 34, 33, 32, 43, 42, 41 | Slots d'artisanat, public puis guilde |
| 45 | Informations : partage, paiement, public et guilde, taxe du tableau, remplacement des commissions |
| 53 | Gestion, pour les Adjoints et le chef |

Affichage d'un slot :

- au-delà du nombre de slots du type (`getSlotsFor`) : vitre grise « À débloquer » sur un tableau de guilde, rien au spawn, qui ne s'améliore pas ;
- commission ouverte : l'icône de l'item, avec `CommissionDisplay.describeCommission` en lore. Un clic ouvre `CommissionDeliveryMenu` ;
- slot débloqué sans commission (catalogue vide) : rien, jusqu'à la prochaine ouverture du tableau.

Au spawn, les 12 slots de droite sont des vitres grises « Pas de commissions de guilde au spawn ». Pour un externe, ce sont des vitres grises « Réservé aux membres de la guilde ». Les étiquettes « PUBLIC » et « GUILDE » font partie de la texture.

`reopenWithUpToDateBoard(viewer)` relit le tableau et ouvre un nouveau menu, ce qui rafraîchit l'affichage.

### 7.3 `CommissionDeliveryMenu`

Étend `ItemAdderMenu`, en 3 lignes, avec le fond `tesseract:commission_board_delivery_menu` : « Confirmer » (11), la commission sans indication de clic (13), « Annuler » (15). « Confirmer » appelle `deliver`, affiche le résultat ou le refus dans le chat, puis rouvre le tableau.

### 7.4 `CommissionManagementMenu`

Étend `ItemAdderMenu`, en 5 lignes, avec le fond `tesseract:commission_board_upgrade_menu`. Emplacements : en haut, le tier de récolte (9), les slots de récolte (11), le bonus d'argent (13), les slots d'artisanat (15) et le tier d'artisanat (17) ; sous la séparation, le tier du tableau (30) et la taxe (32) ; le bouton de retour commun `MENU_BACK_ARROW_BUTTON` (36) et les informations (44).

- Une amélioration affiche sa description, son niveau actuel, le suivant, son prix, le tier de tableau requis et, pour le tier du tableau, les livraisons faites sur le nombre demandé (« Items de récolte livrés : 37 / 100 », puis « sur des commissions de qualité Bonne ou plus », la qualité dans sa couleur), chaque condition en vert si elle est remplie et en rouge sinon. Au maximum, elle affiche « Niveau maximal atteint ».
- La taxe change de +5 % au clic gauche et de -5 % avec les autres clics. Hors limites, le service refuse et le refus s'affiche.
- Le bouton d'information affiche la banque de la guilde et le tier du tableau, explique le fonctionnement (le tier fixe jusqu'où vont les autres améliorations et monte avec les livraisons), puis le niveau maximal de chaque amélioration au tier actuel (`getMaxLevelAtBoardTier`).
- Après chaque action, le menu est reconstruit avec le tableau relu.

### 7.5 `CommissionDisplay`

Lignes de lore et libellés : `describeCommission` (type, tier, progression, qualité minimale, récompense, XP de guilde totale pour un membre, puis items, taxe, gain et XP pour un membre), `describeBoard(board, spawnRewardRate)` (au spawn, la part payée affichée vient de `spawn-reward-rate`), `describeUpgrade`, `formatUpgradeLevel(type, niveau, taux)`, `formatPercent`. Les libellés et couleurs de qualité viennent de `QualityAdapter` (`common-adapter`).

## 8. Liens avec les autres modules

- `territory-domain` : `GuildService` fournit `getGuildMoney`, `getMemberIds`, `addCommissionIncome`, `payFromBank`, et publie `GuildDeletedEvent` à la suppression ; `GuildRole.canManageBoard()`.
- `territory-adapter` : `NatureProtectionListener` laisse le staff poser l'item du tableau dans la nature, pour les tableaux du spawn. La pose des tableaux de guilde relève du moteur des structures.
- `skill-domain` : `SkillService.findAllSkills()` fournit les recettes au catalogue.
- `customItem` : `MoneyRepository.hasValue` et `CustomItemService.hasMoneyValue`.
- `common-adapter` : `ItemAdderMenu` s'ouvre sans fond, avec un warning, si la texture n'existe pas ; `QualityAdapter` y est partagé.
- `plugin` : dépendances vers les deux modules et modèle `Tesseract/commission/commissions.yml`.

## 9. Flux complets

### Livraison

```mermaid
sequenceDiagram
    actor J as Joueur
    participant L as CommissionBoardClickListener
    participant B as CommissionBoardMenu
    participant D as CommissionDeliveryMenu
    participant S as CommissionDeliveryService
    participant R as JpaCommissionRepository
    participant M as CustomItemPlayerMaterialPort
    J->>L: clic droit sur le tableau
    L->>B: open
    J->>B: clic sur une commission
    B->>D: open
    J->>D: confirmer
    D->>S: deliver
    S->>R: recordDelivery (progression, joueur, banque, XP)
    S->>M: removeAtLeast
    D->>J: message de réussite ou de refus
    D->>B: reopenWithUpToDateBoard
```

### Remplacement

Quand une livraison termine une commission, `CommissionBoardRefillService` génère la suivante et `replaceCommissions` fait le remplacement dans une transaction. Un joueur qui avait un menu ouvert reçoit « Cette commission n'est plus disponible » s'il confirme ensuite la commission remplacée.

## 10. Points d'attention connus

- **Non testé en jeu ni sur une vraie base** : le module n'a aucun test propre.
- **Transaction de `recordDelivery`** : `PlayerMoneyService` et `GuildService` ne sont pas conçus autour de cette transaction. S'ils gardent des objets en mémoire, un retour arrière en base peut ne pas s'y refléter. À vérifier.
- **Warnings répétés** : un item sans prix écrit un warning à chaque lecture du catalogue, donc à chaque génération.
- **Tableau du spawn** : tout tableau posé hors d'une guilde ouvre le tableau du spawn, y compris dans un campement.
- **Permission ItemsAdder** : la configuration ItemsAdder de l'item peut demander une permission, que le code ne gère pas.
- **Limite d'affichage** : le menu affiche au plus 6 slots par type, même si la configuration en permet plus.

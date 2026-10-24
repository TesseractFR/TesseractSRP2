# Spec — `contrat-domain`

Module Maven `contrat-domain`. Il contient les règles métier des contrats, **sans Bukkit, sans Spring, sans JPA**. Tout ce qui touche au serveur (items, PNJ, base de données, configuration, tâches) est dans `contrat-adapter` (voir `contrat-adapter.md`).

Version 2, après les décisions de Flo et Guillaume sur le parchemin physique. Les points non confirmés sont marqués **(hypothèse)**. Périmètre : les contrats. Les commissions de guilde sont dans un autre module (`commission`, épopée O).

## 1. Principe

1. Chaque joueur dispose d'un stock de **tickets de contrat** (nom provisoire), renouvelé chaque jour selon son grade. On peut aussi en gagner en jouant.
2. Chez l'**unique PNJ marchand**, un ticket achète **un parchemin physique** contenant une mission **aléatoire**, dont la difficulté dépend de la progression du joueur dans le métier et de l'item.
3. On livre le parchemin et les items demandés, **en une seule fois**, au **PNJ du métier** concerné (un PNJ par métier). On touche **toute** la récompense en lys, **sans XP**.
4. Le parchemin **expire**. Non livré, il est gardé, perdu ou **revendu à l'hôtel des ventes** (prix encadré). Expiré, il est supprimé, y compris de l'hôtel des ventes.

## 2. Module Maven

- **Dépendances autorisées** : **aucun autre module SRP**, comme `skill-domain` (qui définit son propre `PlayerID` et son propre `Quality` dans `model`). Le domaine atteint `commun`, `job`, `customItem` et `economie` par ses propres ports `serverside` ; l'adapter convertit les types.
- **Aucune** dépendance Bukkit, Spring ni Jakarta Persistence.
- **Tests** : JUnit seul, sans serveur ; les ports sont remplacés par des faux en mémoire. `RandomSource` et `Clock` sont injectés pour des tests déterministes.
- **Racine des packages** : `onl.tesseract.srp.contrat.domain`, avec `model` (sous-packages `contract`, `ticket`, `profile`, `event`), `port.userside` et `port.serverside`, comme `skill-domain`.
- **Services** : classes concrètes dans `port.userside` (comme `CraftingService`, `SkillService`), construites avec leurs ports par le constructeur, sans annotation Spring.
- **Types propres** : `PlayerID`, `Quality` (copie de l'échelle `POOR` à `EXCEPTIONAL`).

## 3. Existant et corrections

Aucun service n'existe : seuls le modèle, un port de dépôt et son implémentation JPA sont présents dans `job-domain` / `job-adapter`. Aucune mission n'est en production, donc **aucune migration de données** : on remplace tout.

| # | Constat sur `JobMission` et `JobMissionRepository` | Conséquence |
|---|---|---|
| 1 | `id` est un `long` primitif ; `fromDomain` envoie `0` à l'entité `Long`, donc `save` fait un `merge` sur un id inexistant. | `Contract.id` est un `Long`, nul avant la première sauvegarde. |
| 2 | Le port ne déclare que `findAllByPlayerId` et `deleteById` ; `save` n'existe que sur l'adapter. | Le port déclare `save`, `findById`, `delete` et `deleteExpired`. |
| 3 | `MissionDepositResult(delivered, remaining)` suppose une livraison partielle. | Supprimé : remplacé par `DeliveryResult`. |
| 4 | `delivered` n'a plus de sens. | Champ supprimé : tout ou rien. |
| 5 | `job` et `material` sont des `String`. | **(hypothèse)** On les garde en `String` : le domaine ne dépend ni de `job-domain` ni de `customItem-domain`. |
| 6 | `PlayerRank` porte des « slots de missions ». | Ils deviennent le **nombre de tickets par jour** (issue D2). |

Les classes `JobMission`, `MissionDepositResult` et `JobMissionRepository` sont **supprimées** de `job-domain` quand `contrat-domain` les remplace.

## 4. Modèle (`model`)

- `Contract` : `Long id`, `PlayerID issuedTo` (acheteur d'origine), `ContractType type`, `String job`, `String material`, `int quantity`, `Quality minimalQuality`, `int reward`, `int tier`, `Instant issuedAt`, `Instant expiresAt`, `long version`.
  - Pas de propriétaire : le porteur du parchemin est celui qui livre.
  - Pas de client nommé : le **client est le PNJ du métier** (`job`).
  - Un contrat livré ou expiré est **supprimé**. Un parchemin sans contrat est **invalide**.
  - Méthodes pures : `isExpired(Instant)`, `isForJob(String)`.
- `ContractType` : `HARVEST`, `CRAFT`.
- `ContractPlayerState` : `PlayerID playerId`, `int tickets`, `LocalDate ticketsDay`, `int deliveredCount`, `boolean discoveryGiven` (le parchemin de découverte n'est donné qu'une fois). Méthodes pures : `renewedFor(LocalDate, int dailyQuota)`, `spendOne()`, `gain(int cap)`. `deliveredCount` sert à la condition de grade D3.
- `PlayerContractProfile` : par métier, `JobProfile(String job, int level, List<UnlockedMaterial> materials, Quality maxQuality)`. `UnlockedMaterial(String material, int tier, double lootChance, double harvestsPerHour)`. Construit par l'adapter à partir de `job-domain`. Le **nombre de tickets par jour** (selon le grade) n'est pas dans ce profil : il vient du port `PlayerProfileRepository.dailyTickets`.
- `ContractSettings` : valeurs de configuration typées (section 8), fournies par un port.
- `TicketSource` : `DAILY`, `ACTIVITY`, `LOOT`, `VOTE`, `ADMIN`.
- `PurchaseResult` : `OK(contract)`, `NO_TICKET`, `INVENTORY_FULL`, `NOTHING_UNLOCKED`.
- `DeliveryResult` : `DELIVERED(reward)`, `WRONG_CLIENT`, `INVALID_SCROLL`, `EXPIRED`, `MISSING_ITEMS(missing)`, `NOT_ENOUGH_QUALITY`, `PAYMENT_FAILED`.

## 5. Ports

### Côté utilisateur (`port.userside`)

Classes concrètes, assemblées par le constructeur :

- `ContractTicketService`
  - `int ticketsOf(PlayerID player)` : applique d'abord le renouvellement quotidien.
  - `boolean grantTicket(PlayerID player, TicketSource source)` : ajoute un ticket dans la limite `bonus-cap`.
- `ContractService`
  - `PurchaseResult buyScroll(PlayerID player)`
  - `DeliveryResult deliver(PlayerID player, long contractId, String npcJob)` : `npcJob` est le métier du PNJ qui reçoit.
  - `boolean giveDiscoveryScroll(PlayerID player)` : parchemin de découverte, une seule fois, sans ticket.
  - `boolean isValid(long contractId)` : lue par l'hôtel des ventes.
  - `Optional<Contract> find(long contractId)`
  - `int purgeExpired()` : supprime les contrats échus, publie `ContractExpired` pour chacun, retourne leur nombre. Appelée par une tâche de l'adapter.

### Côté serveur (`port.serverside`, implémentés par `contrat-adapter`)

| Port | Contrat |
|---|---|
| `ContractRepository` | `Contract save(Contract)`, `Optional<Contract> findById(long)`, `boolean delete(long id)` (vrai si une ligne a été supprimée), `List<Long> deleteExpired(Instant)` |
| `ContractPlayerStateRepository` | `ContractPlayerState find(PlayerID)` (état neuf par défaut), `void save(ContractPlayerState)` |
| `PlayerProfileRepository` | `PlayerContractProfile profileOf(PlayerID)`, `int dailyTickets(PlayerID)` (selon le grade) |
| `ScrollItemRepository` | `boolean canReceive(PlayerID)`, `boolean give(PlayerID, Contract)`, `boolean has(PlayerID, long contractId)`, `boolean remove(PlayerID, long contractId)` |
| `ItemRepository` | `int countAtLeast(PlayerID, String material, Quality min)` ; `Map<Quality,Integer> removeAtLeast(PlayerID, String material, Quality min, int amount)` : **tout ou rien**, retire de la plus basse qualité admissible vers la plus haute et retourne ce qui a été retiré par qualité (map vide si le joueur n'en a pas assez) ; `void giveBack(PlayerID, String material, Map<Quality,Integer>)` pour rendre ce qui a été retiré |
| `MoneyRepository` | `boolean pay(PlayerID, int amount, String reason)` |
| `RewardCalculator` | `int reward(Contract draft)` |
| `ContractConfigRepository` | `ContractSettings current()` (rechargeable) |
| `TransactionRunner` | `<T> T inTransaction(Supplier<T>)` : le domaine n'importe pas Spring ; l'adapter fournit l'implémentation transactionnelle |
| `ContractEventPublisher` | `void publish(Object event)` : le domaine ne dépend pas de `commun-domain` ; l'adapter relaie vers l'`EventPublisher` existant |
| `Clock`, `RandomSource` | `java.time.Clock` et une interface `RandomSource` (`nextDouble`, `nextInt(bound)`) |

Les implémentations **provisoires** tant que d'autres modules manquent (détaillées dans `contrat-adapter.md`) laissent le domaine inchangé :
- `MoneyRepository` passera de `PlayerService.addMoney` à `MoneyService` (B2).
- `ItemRepository` passera de la boucle sur les qualités au comptage natif (A7).
- `RewardCalculator` passera du calcul par configuration à celui de `economie-domain` (B7).

## 6. Règles (services de `port.userside`)

### Tickets
- **Renouvellement quotidien** : si `ticketsDay` est antérieure à la date du jour, `tickets` est **remis** au quota du grade (`dailyTickets`) et `ticketsDay` mis à jour. **(hypothèse)** Le stock de la veille ne s'accumule pas : le renouvellement remplit, il n'ajoute pas.
- **Gagner un ticket en jouant** (le staff, source `ADMIN`, n'est pas limité par le plafond) : `grantTicket` ajoute 1 au stock, tant que `tickets < quota + bonus-cap` **(hypothèse : le ticket gagné est un incrément du stock, pas un item)**. Les sources (activité, loot, vote) sont déclenchées côté adapter (C8).
- Un joueur neuf reçoit **un parchemin de découverte** (matériau de palier 0, quantité faible), sans ticket.

### Achat d'un parchemin (`buyScroll`)
1. Renouveler le stock du jour ; `tickets < 1` ⇒ `NO_TICKET`.
2. `ScrollItemRepository.canReceive` faux ⇒ `INVENTORY_FULL`, sans toucher au ticket.
3. Obtenir le profil ; aucun matériau débloqué ⇒ `NOTHING_UNLOCKED`.
4. Générer le contrat (ci-dessous), calculer la récompense, puis **dans une transaction** : sauvegarder le contrat, décrémenter le ticket, sauvegarder l'état.
5. `ScrollItemRepository.give` ; s'il échoue, **annuler** : supprimer le contrat et rendre le ticket.
6. Publier `ContractIssued`.

### Génération (aléatoire, selon l'acheteur)
1. Tirer le métier parmi ceux qui ont au moins un matériau débloqué, **pondéré par le niveau du joueur dans le métier** **(hypothèse)**, puis un matériau de ce métier.
2. **Quantité** : `round(effort-hours × harvestsPerHour × lootChance)`, bornée par `[1, max-stacks × pile-max]` **(hypothèse : 4 piles)**. Un matériau à faible `lootChance` donne peu d'unités. La borne garantit que la livraison en une fois tient dans l'inventaire.
3. **Qualité minimale** : tirée entre `POOR` et `maxQuality`. Les basses qualités écoulent le rebut.
4. **Tier** : tier du matériau. **Récompense** : `RewardCalculator`.
5. `expiresAt = now + scroll-ttl` **(hypothèse : 3 jours)**.

La difficulté repose sur la **progression du joueur dans le métier et l'item** (niveau et talents débloqués), pas sur son grade. Le type `CRAFT` existe mais n'est pas généré en phase 1 (F9).

### Livraison (`deliver`)
Ordre de vérification :
1. `find(contractId)` absent ⇒ `INVALID_SCROLL`.
2. `contract.isForJob(npcJob)` faux ⇒ `WRONG_CLIENT`.
3. `contract.isExpired(now)` ⇒ `EXPIRED` et suppression du contrat.
4. `ScrollItemRepository.has(player, id)` faux ⇒ `INVALID_SCROLL`.
5. `countAtLeast` < `quantity` ⇒ `MISSING_ITEMS(missing)` ou, si des items existent sous la qualité minimale, `NOT_ENOUGH_QUALITY`. Rien n'est retiré.

Puis, **tout ou rien** :
6. Retirer le parchemin (`remove`). Échec ⇒ `INVALID_SCROLL`.
7. Retirer les items (`removeAtLeast`). Si le retrait renvoie moins que `quantity`, **rendre** ce qui a été retiré et le parchemin, puis `MISSING_ITEMS`.
8. Dans une transaction : `delete(contractId)`. Retourne faux (déjà livré par un autre porteur du même parchemin) ⇒ rendre items et parchemin, `INVALID_SCROLL`.
9. Même transaction : `pay(deliverer, reward)`. Échec ⇒ **annuler** (la transaction ne supprime pas le contrat), rendre items et parchemin, `PAYMENT_FAILED`.
10. Incrémenter `deliveredCount` du livreur, publier `ContractDelivered`, retourner `DELIVERED(reward)`.

La récompense va **au joueur qui livre**, pas forcément à l'acheteur. Le succès de `delete` est l'arbitre unique : un parchemin dupliqué ne paie qu'une fois. **Le domaine compte sur le retour en arrière de la transaction** : si le paiement échoue, une exception annule la suppression du contrat, donc l'implémentation de `TransactionRunner` **doit** faire un rollback sur exception.

### Expiration (`purgeExpired`)
Supprime les contrats dont `expiresAt` ≤ maintenant, publie `ContractExpired(id)` pour chacun. L'hôtel des ventes écoute l'événement et appelle `isValid` avant chaque achat.

## 7. Événements (`model.event`)

Publiés par `ContractEventPublisher` (records sans dépendance serveur) :
- `ContractIssued(PlayerID buyer, long contractId)`
- `ContractDelivered(PlayerID deliverer, PlayerID issuedTo, long contractId, ContractType type, String material, int quantity, int reward)` : consommé par les succès, la jauge d'événement, les grades.
- `ContractExpired(long contractId)`

## 8. Paramètres (`ContractSettings`)

`bonusCap`, `scrollTtl: Duration`, `effortHours`, `maxStacks`, `stackSize`, `discoveryQuantity`, `activity { ticketChance, cooldown }` (lu par l'adapter pour C8), `market { maxPriceRatio }` (lu par `marche`). Les valeurs de base par matériau, les coefficients de qualité et les cadences de récolte (`harvestsPerHour`) restent côté adapter : le calcul de récompense est derrière `RewardCalculator` et les cadences arrivent par le profil du joueur. Le nombre de tickets par jour vient du grade (D2) via `PlayerProfileRepository.dailyTickets`. Le format YAML est dans `contrat-adapter.md`. Valeurs provisoires jusqu'à la bêta.

## 9. Critères d'acceptation (tests unitaires)

1. `renewedFor` remet le quota le premier appel du jour et ne le refait pas le même jour.
2. `buyScroll` sans ticket retourne `NO_TICKET` et ne crée rien ; avec l'inventaire plein, `INVENTORY_FULL` sans dépenser le ticket.
3. `buyScroll` réussi décrémente d'un ticket, sauvegarde un contrat et appelle `give` ; si `give` échoue, le contrat est supprimé et le ticket rendu.
4. La quantité générée ne dépasse jamais `max-stacks × pile-max` et diminue quand `lootChance` augmente, à effort constant.
5. Avec `RandomSource` et `Clock` fixés, la génération est déterministe.
6. Un joueur sans matériau débloqué obtient `NOTHING_UNLOCKED`.
7. Si un item manque, rien n'est retiré ni versé.
8. Livrer à un PNJ d'un autre métier retourne `WRONG_CLIENT` sans rien retirer.
9. La livraison réussie retire d'abord les items de plus basse qualité admissible, jamais sous `minimalQuality`, verse toute la récompense **au livreur** et supprime le contrat.
10. Un contrat supprimé entre-temps (`delete` faux) rend items et parchemin, et ne verse rien.
11. Un échec de paiement rend items et parchemin, et conserve le contrat.
12. Un contrat expiré est rejeté avec `EXPIRED` ; `purgeExpired` publie un `ContractExpired` par contrat supprimé.
13. `grantTicket` refuse au-delà de `quota + bonus-cap`.

## 10. Plan de réalisation

| Issue | Contenu | Bloquée par |
|---|---|---|
| C1 | Valider cette spec et `contrat-adapter.md` | — |
| C2 | Modèle, générateur, `RandomSource` | — |
| C3 | `RewardCalculator` (port et règles) | B7 pour la version finale |
| C4 | `ContractTicketService`, achat de parchemin | C2 |
| C5 | Livraison en une fois | C3, C4 ; B2 et A7 pour les versions finales des adapters |
| C8 | Sources de tickets (déclenchées par l'adapter) | L2 |

## 11. Décisions restantes

1. Le stock quotidien ne s'accumule pas ; un ticket gagné s'ajoute au stock avec plafond.
2. Tickets par jour par grade ; plafond de tickets bonus.
3. Durée de vie d'un parchemin (3 jours proposé).
4. Pondération du métier tiré (par niveau proposé).
5. Nombre de piles maximal par contrat (4 proposé).
6. Prix encadré à l'hôtel des ventes (70 % de la récompense proposé).
7. Contrats d'artisanat : un PNJ par compétence (hypothèse).
8. `job` et `material` en `String` dans le domaine.
# Spec — `contrat-adapter`

Module Maven `contrat-adapter`. Il relie `contrat-domain` au serveur : base de données, configuration, items et PNJ Bukkit, menus, commandes, tâches planifiées. Les règles métier sont dans `contrat-domain.md` ; ce document décrit uniquement ce que l'adapter doit fournir et comment il s'assemble.

Les points non confirmés sont marqués **(hypothèse)**.

## 1. Module Maven

- **Dépend de** :
  - `contrat-domain` (ses ports `serverside` à implémenter, ses services `userside`) ;
  - `commun-domain` (`PlayerRank`, `Quality`, `EventPublisher`, `PlayerService`) : l'adapter **convertit** les types de `commun` vers ceux de `contrat-domain` (`PlayerID`, `Quality`) ;
  - `job-domain` (lecture de la progression et des talents, par ses services `userside`) ;
  - `customItem-domain` (`CustomItemService`, pour compter et retirer les items) ;
  - Spring (contexte, `@Component`, `@Configuration`, transactions), Spring Data JPA / Hibernate, l'API Bukkit/Paper, **Citizens** (PNJ) et **ItemsAdder** (item du parchemin).
- **Dépendances inverses** : le module `plugin` dépend de `contrat-adapter` pour l'assembler dans le serveur. `contrat-domain` ne dépend jamais de l'adapter.
- Règle d'architecture respectée : `contrat-domain` ne lit pas `job-domain` ; c'est **cet adapter** qui implémente `PlayerProfileRepository` en appelant `job-domain`.
- **Racine des packages** : `onl.tesseract.srp.contrat.adapter`, avec la même arborescence que `skill-adapter` :
  - `serverside.jpa.adapter`, `serverside.jpa.entity`, `serverside.jpa.mapper`, `serverside.jpa.repository` ;
  - `serverside.yaml`, `serverside.task` ;
  - à la racine de `serverside`, les adapters de ports non JPA, nommés d'après le port (`ItemsAdderScrollRepository`, comme `TesseractItemRepository` ou `BukkitMessagingRepository` dans `skill-adapter`) ;
  - `userside.config` (classe `ContractProvider`, comme `SkillProvider`), `userside.controller.event`, `userside.controller.menu`, `userside.controller.commands`.
- Code source en Java, dans `src/main/java` (le dossier `kotlin` de `skill-adapter` est une erreur à corriger, pas un modèle).

## 2. Nettoyage de l'existant

À faire quand cet adapter est opérationnel (issue C6) :
- supprimer de `job-adapter` : `JobMissionEntity`, `JobMissionJpaRepository`, `JobMissionRepositoryJpaAdapter` ;
- supprimer de `job-domain` : `JobMission`, `MissionDepositResult`, `JobMissionRepository` (et le port correspondant dans les listes de la spec `job-domain`) ;
- supprimer la table `t_job_missions` (aucune donnée de production).

## 3. Persistance (`serverside.jpa`)

### Entités

`ContractEntity` — table `t_contracts`

| Colonne | Type | Notes |
|---|---|---|
| `id` | `Long`, `IDENTITY` | nul avant sauvegarde |
| `issued_to` | `BINARY(16)` | UUID de l'acheteur |
| `type` | enum string | `HARVEST` / `CRAFT` |
| `job` | string | non nul |
| `material` | string | non nul |
| `quantity` | int | ≥ 1 |
| `minimal_quality` | enum string | `Quality` |
| `reward` | int | ≥ 0 |
| `tier` | int | |
| `issued_at`, `expires_at` | `Instant` / `TIMESTAMP` | index sur `expires_at` |
| `version` | long, `@Version` | contrôle optimiste |

`ContractPlayerStateEntity` — table `t_contract_player_state`

| Colonne | Type |
|---|---|
| `player_id` (clé) | `BINARY(16)` |
| `tickets` | int |
| `tickets_day` | `DATE` |
| `delivered_count` | int |

- **Conventions des entités** (comme `CraftingEntity` dans `skill-adapter`) : `@Entity`, `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` (Lombok), imports `jakarta.persistence`, nom de table préfixé `t_`, `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id`. Un groupe de colonnes réutilisable passe par un `@Embeddable` (inutile ici : les deux tables sont plates).
- **Pas de `@Cacheable`** ni de cache de second niveau (lignes créées et supprimées en continu).
- Aucune relation JPA vers une autre entité : UUID uniquement.
- Conversions dans `serverside.jpa.mapper` (`ContractMapper`, `ContractPlayerStateMapper`), comme dans `skill-adapter` : l'`id` d'un contrat neuf reste nul.

### Dépôts
- `ContractJpaRepository` : `JpaRepository<ContractEntity, Long>` avec `findIdsByExpiresAtBefore(Instant)` et une suppression en lot par date.
- `ContractRepositoryJpaAdapter` implémente `ContractRepository` :
  - `save` : retourne le contrat avec son id ;
  - `delete(id)` : retourne **vrai si une ligne a été supprimée** (`deleteById` ne suffit pas : utiliser une requête `DELETE ... WHERE id = :id` et lire le nombre de lignes). C'est l'arbitre de la double livraison ;
  - `deleteExpired(Instant)` : retourne les ids supprimés.
- `ContractPlayerStateRepositoryJpaAdapter` : `find` retourne un état neuf (`tickets` = 0, `ticketsDay` nul) si absent ; `save` insère ou met à jour.
- `SpringTransactionRunner` implémente `TransactionRunner` avec `TransactionTemplate`. Toute opération multi-étapes du domaine passe par lui.

La création des tables est **automatique** (confirmé) : les deux tables sont décrites par les entités, et **aucun script SQL** n'est écrit dans cette spec. La suppression de l'ancienne table `t_job_missions` est à faire à la main en base.

## 4. Configuration (`serverside.yaml`)

`YamlContractConfigRepository` implémente `ContractConfigRepository` : lit `contracts.yml` dans le dossier du plugin, le valide, le convertit en `ContractSettings` et le **recharge à chaud** (L1). Une valeur invalide est refusée avec un message clair (clé, valeur, attendu) et la **configuration précédente est conservée**.

```yaml
bonus-cap: 2               # tickets gagnés en jeu au-delà du quota
scroll-ttl-days: 3
effort-hours: 0.5
max-stacks: 4
discovery:
  quantity: 8
activity:
  ticket-chance: 0.01
  cooldown-minutes: 30
materials:
  OAK_LOG:
    harvests-per-hour: 900
    base-value: 3
quality-coefficients:
  POOR: 0.6
  # …
market:
  max-price-ratio: 0.7     # lu par le module marche
scroll:
  item-id: "srp:contract_scroll"   # item ItemsAdder
npcs:                      # identifiants Citizens
  merchant: 12
  jobs:                    # un PNJ par métier
    13: MINEUR
    14: BUCHERON
```

Le **nombre de tickets par jour** n'est pas dans ce fichier : il est lu via `PlayerProfileRepository.dailyTickets`, branché sur la configuration des grades (D2). **Provisoire** : un bloc `tickets-per-day` par grade dans `contracts.yml` tant que D2 n'est pas fait.

Contrôles de validation : valeurs ≥ 0, `scroll-ttl-days` ≥ 1, `max-stacks` ≥ 1, chaque métier de `npcs.jobs` existe dans `job-domain`, chaque matériau cité a une cadence.

## 5. Adapters de ports (`serverside`)

| Classe | Implémente | Comportement |
|---|---|---|
| `JobProfileRepository` | `PlayerProfileRepository` | Pour chaque métier : niveau et talents via `JobPlayerProgressionService` / `JobService` ; un matériau est **débloqué** si son bonus `LOOT_CHANCE` effectif est > 0 (`Talents.get(material, LOOT_CHANCE)` au niveau du joueur) ; qualité maximale déduite des bonus de qualité ; grade via `PlayerService`. |
| `CustomItemInventoryRepository` | `ItemRepository` | **Provisoire** : boucle de `min` à `EXCEPTIONAL` avec `CustomItemService.getItemQuantity` / `removeItem`. Retire de la plus basse qualité admissible. Le travail A7 le remplace sans changer le port. |
| `ItemsAdderScrollRepository` | `ScrollItemRepository` | Fabrique, remet, détecte et retire l'item parchemin (section 6). |
| `PlayerMoneyRepository` | `MoneyRepository` | **Provisoire** : `PlayerService.addMoney`. Le futur `MoneyService` (B2) le remplace avec la raison `CONTRAT_RECOMPENSE`. |
| `ConfigRewardCalculator` | `RewardCalculator` | **Provisoire** : `valeur de base × quantité × coefficient de qualité` lus en configuration. Le calcul de `economie-domain` (B7) le remplace. |
| `SpringTransactionRunner` | `TransactionRunner` | voir section 3 |

Les événements du domaine sont publiés par l'`EventPublisher` existant (`commun`), appelé par un adapter de `ContractEventPublisher`, qui les relaie vers Spring ou Bukkit selon l'implémentation en place.

**Threads** **(hypothèse)** : les appels Bukkit (inventaires, items) se font sur le thread principal ; les accès JPA sont synchrones comme dans les modules existants. Si la base devient lente, on déplacera l'accès JPA hors du thread principal sans changer les ports.

## 6. Item parchemin

- Item **ItemsAdder** dédié, avec une **texture personnalisée** : identifiant `namespace:item` en configuration (`scroll.item-id`).
- Nom, et dans le lore : métier et **PNJ où le livrer**, matériau, quantité, qualité minimale, récompense, date d'expiration.
- **Identifiant du contrat** dans les données persistantes de l'item (clé `srp:contract_id`, type `long`). L'item ne porte **rien d'autre** : la base fait foi. Un parchemin dupliqué pointe vers le même contrat ; seul le premier livreur est payé.
- **Non empilable** : l'identifiant unique suffit.
- `has` et `remove` cherchent le parchemin par identifiant dans l'inventaire du joueur (et non dans la main seulement).
- `canReceive` : au moins un emplacement libre ou un empilement possible.
- Un parchemin dont le contrat n'existe plus (expiré, livré) reste un item inerte : à l'interaction avec un PNJ, il est **détruit** avec un message.
- Protection : le parchemin ne peut pas être posé, utilisé en craft ni consommé (annuler `BlockPlaceEvent`, `CraftItemEvent`, interactions).

## 7. Tâches (`serverside.task`)

- `ContractExpirationTask` : tâche répétée (toutes les 5 minutes **(hypothèse)**) qui appelle `ContractService.purgeExpired()`. Elle exécute la suppression hors du thread principal si possible, puis publie les événements sur le thread principal.

## 8. Interface joueur (`userside`)

### Listeners (`controller.event`)
- `ContractNpcListener` : sur clic droit d'un PNJ Citizens, lit son identifiant et son rôle dans `npcs` :
  - **marchand** : ouvre `ContractMerchantMenu` ;
  - **PNJ de métier** : lit le parchemin tenu, appelle `ContractService.deliver(player, contractId, npcJob)` et affiche le message correspondant au `DeliveryResult`. Le parchemin est lu depuis la **main principale**.
- `ContractActivityListener` (issue C8) : sur récolte ou craft, applique le tirage et le délai de `activity` puis appelle `ContractTicketService.grantTicket(player, ACTIVITY)`. Les sources loot et vote appellent la même méthode avec `LOOT` / `VOTE`.
- `ScrollProtectionListener` : protections de la section 6.
- `ContractJoinListener` : à la première connexion, remet le parchemin de découverte.

### Menu (`controller.menu`)
- `ContractMerchantMenu` (réutilise `common-adapter` pour les menus) : affiche le solde de tickets, un bouton « acheter un parchemin », et les messages d'échec (`NO_TICKET`, `INVENTORY_FULL`, `NOTHING_UNLOCKED`).

### Commandes (`controller.commands`)
- `/contrats` : affiche le solde de tickets et le prochain renouvellement.
- `/contrat ticket give <joueur> <n>` (staff) : appelle `grantTicket(..., ADMIN)`.
- `/contrat reload` (staff) : recharge `contracts.yml`.

### Messages
Tous les textes sont dans un fichier de langue ; un message par `DeliveryResult` et par `PurchaseResult`.

### PNJ
Les PNJ sont des PNJ **Citizens**. `ContractNpcListener` écoute le clic droit sur un PNJ (`NPCRightClickEvent`), lit l'**identifiant Citizens** du PNJ et le cherche dans `npcs` (marchand, ou métier). Un PNJ inconnu est ignoré.

## 9. Assemblage (`userside.config`)

`ContractProvider` (`@Configuration`, sur le modèle de `SkillProvider`) déclare les beans des services du domaine (`ContractTicketService`, `ContractService`), construits par leur constructeur avec leurs ports :

```java
@Configuration
public class ContractProvider {
    @Bean
    public ContractService contractService(ContractRepository contracts, ContractPlayerStateRepository states,
            PlayerProfileRepository profiles, ScrollItemRepository scrolls, ItemRepository items,
            MoneyRepository money, RewardCalculator rewards, ContractConfigRepository config,
            TransactionRunner tx, ContractEventPublisher events, Clock clock, RandomSource random) {
        return new ContractService(/* … */);
    }
}
```

Les adapters (`@Component`) sont découverts par Spring. Les services du domaine ne portent **aucune annotation Spring**.

## 10. Critères d'acceptation

1. Sauvegarder un contrat neuf génère un id ; le relire donne les mêmes valeurs ; deux sauvegardes d'états différents ne créent pas de doublon d'état.
2. `delete(id)` retourne vrai la première fois et faux la deuxième ; deux suppressions simultanées ne réussissent qu'une fois.
3. `deleteExpired` ne supprime que les contrats dont `expires_at` est dépassé et retourne leurs ids.
4. Une config avec un grade manquant ou une valeur négative est refusée sans remplacer la configuration active ; `/contrat reload` applique une config valide sans redémarrage.
5. Un matériau est considéré débloqué si et seulement si sa chance de loot effective est > 0.
6. `remove` d'items de qualité mixte retire d'abord la plus basse qualité admissible et jamais sous la qualité minimale.
7. Le parchemin porte l'identifiant du contrat ; il ne peut ni être posé, ni servir au craft ; un parchemin de contrat supprimé est détruit à l'interaction.
8. Un joueur dont l'inventaire est plein ne peut pas acheter ; son ticket n'est pas dépensé.
9. Cliquer un PNJ de métier avec un parchemin d'un autre métier affiche le message `WRONG_CLIENT` et ne retire rien.
10. La tâche d'expiration supprime les contrats échus et chaque suppression déclenche `ContractExpired`.
11. Le plugin démarre avec `contrat-adapter` assemblé sans `economie`, sans A7 ni `commission` (adapters provisoires).

## 11. Plan de réalisation

| Issue | Contenu | Bloquée par |
|---|---|---|
| C6 | Entités, dépôts JPA, `SpringTransactionRunner`, nettoyage de `job-*` | C2 (modèle) |
| C6 | `YamlContractConfigRepository`, `contracts.yml` | C2 |
| C7 | Item parchemin, marchand, PNJ de métier, menu, commandes | choix du système de PNJ, C4, C5 |
| C8 | `ContractActivityListener` et sources de tickets | C4, L2 |
| C5 | Adapters provisoires `CustomItemInventoryRepository`, `PlayerMoneyRepository`, `ConfigRewardCalculator` | C5 (domaine) |

## 12. Décisions restantes

1. Fréquence de la tâche d'expiration (5 minutes proposé).
2. Appels JPA sur le thread principal (comme aujourd'hui) ou hors thread.
3. Commandes : dans `contrat-adapter` (comme `territory-adapter`) ou dans le module `plugin`.

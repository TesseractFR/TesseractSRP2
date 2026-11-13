# Documentation du code — `contract-adapter`

Ce document décrit les adaptateurs et l'interface joueur dans leur état actuel. La spec `contract-adapter.md` décrit l'intention fonctionnelle.

Le module implémente les ports `serverside` de `contract-domain` et expose `ContractService` via Spring, Bukkit et ItemsAdder. Les identifiants et types communs (`PlayerId`, `MaterialName`, `Quality`) viennent de `common-domain`.

## Assemblage Spring

`ContractProvider` construit `ContractGenerator` avec un `Random`, puis `ContractService` à partir des ports. Son bean `contractStartup`, un `SmartInitializingSingleton`, enregistre `ContractJobCatalog` dans le `ServiceContainer` et lance `ContractExpirationTask` après l'initialisation des beans.

## Persistance : JPA

- `ContractEntity` mappe `t_contracts` : type, job, matériau (texte), quantité, qualité minimale, récompense, expiration, id auto-généré et version optimiste. L'index `idx_contracts_expires_at` couvre `expires_at`.
- `fromDomain` et `toDomain` convertissent l'entité et le record `Contract` ; le matériau texte est converti en `MaterialName`.
- `ContractTicketEntity` stocke le solde dans `t_contract_player_state`, indexé par l'UUID texte du joueur.
- `ContractJpaRepositoryAdapter` fournit `save`, `findById`, `delete` et `deleteExpired`. Les opérations sont transactionnelles, la recherche est en lecture seule. `deleteExpired(now)` délègue la requête de suppression en masse au repository JPA.

Le port de contrat n'a pas d'opération `settle` : la livraison du domaine n'utilise pas de transaction atomique entre base, inventaire et paiement.

## Configuration YAML

`YamlContractConfigRepository` lit `plugins/Tesseract/contract/contracts.yml`. Chaque accès recharge la configuration lorsque la date de modification du fichier change. Si le fichier est absent, les méthodes utilisent leurs valeurs par défaut.

| Méthode | Clé | Défaut |
|---|---|---|
| `getScrollTtl()` | `scroll-lifetime-hours` | 72 heures |
| `getQuantityByRarity(rarity)` | `quantity-by-rarity.<RARITY>` | 300 |
| `getRarityWeight(rarity)` | `rarity-weights.<RARITY>` | 0 |
| `getDailyTickets(rank)` | `daily-tickets.<RANK>` | 0 |
| `getNpcName(job)` | `npc-names.<JOB>` | absent |

La recherche du nom de PNJ ignore la casse et ignore les noms vides.

## Adaptateurs `serverside`

### Catalogue et valeurs : `ContractItemInfoRepository`

À chaque `getCatalogItems()`, le repository parcourt les métiers de `JobService` et les compétences de `SkillConfigRepository`. Il collecte les matériaux produits ou fabriqués, sans doublons au sein de chaque métier ou compétence. Les matériaux sans valeur monétaire sont ignorés avec un avertissement. Pour les autres, il lit la rareté, le poids et la quantité depuis la configuration ; un poids inférieur ou égal à zéro exclut l'entrée. `getValue(material, quality)` lit directement la valeur monétaire du `CustomItemService`.

### Tickets et paiement : `ContractPlayerInfoRepository`

Les tickets sont lus et enregistrés dans `ContractTicketEntity`. Le quota est déterminé par le grade du joueur et `daily-tickets` ; un joueur sans grade obtient un quota nul. Le bonus monétaire est fourni par `JobService` sous forme de taux. `giveMoney` délègue le versement au service joueur et traduit un refus de paiement en `PaymentFailedException`.

### Inventaire de matériaux : `TesseractPlayerMaterialRepository`

`countAtLeast` additionne les quantités de qualité minimale ou supérieure pour les matériaux custom. Pour un matériau non custom, il compte la qualité `POOR`. `removeAtLeast` commence par vérifier le stock suffisant, puis retire des qualités admissibles de la plus basse à la plus haute ; pour un matériau non custom, il retire en qualité `POOR`. La méthode ne renvoie pas de résultat et ne restaure pas les retraits en cas d'erreur intermédiaire.

### Parchemins : `BukkitContractScrollRepository`

Les opérations exigent un joueur en ligne. `canReceiveScroll` vérifie un emplacement vide. `giveScroll` construit l'item de base `ContractConstantes.BASE_SCROLL`, écrit l'id de contrat et la date d'expiration dans les données persistantes, ajoute le lore et le nom, impose une taille de pile de 1 et remet l'item. Il renvoie `false` si le joueur est hors ligne, si le contrat n'a pas d'id ou si l'inventaire ne peut pas recevoir l'item. Le paramètre `hasJobBonus` n'est actuellement pas utilisé.

`hasScroll` et `removeScroll` parcourent l'inventaire complet et identifient le parchemin par son id persistant. `ContractItemUtils` lit cet id ; la date d'expiration persistée est formatée par le lore mais le nettoyage actuel vérifie l'expiration du contrat en base.

### Messagerie : `BukkitContractMessagingRepository`

Ce repository centralise les messages de contrat et les envoie au joueur en ligne. Il gère les achats, les refus et réussites de livraison, les tickets, les actions staff, l'usage d'un parchemin détruit et le signalement de parchemins expirés.

## Interface joueur

### Commandes

`ContractCommands` expose `/contract` aux joueurs :

- `merchant` ouvre `ContractMerchantMenu` ;
- `deliver <metier>` lit l'id du parchemin tenu en main principale, puis ouvre `ContractDeliveryMenu` si le contrat existe ; sans id, appelle `noScrollInHand` ;
- `tickets` affiche le solde et le quota.

`ContractJobArg` résout le texte de l'argument via `ContractJobCatalog`. Le catalogue contient les métiers et compétences et fournit les noms canoniques ainsi que leur complétion.

### Menus

- `ContractMerchantMenu` affiche les tickets et permet d'acheter un parchemin. Après l'achat, il rouvre le menu pour rafraîchir le solde.
- `ContractDeliveryMenu` affiche les détails et le nombre d'objets admissibles, puis appelle `deliver` lors de la confirmation. Le lore est construit directement par le menu avec `QualityAdapter`.

### Événements

- `ContractDailyTicketListener` appelle `renewDaily` à la première connexion quotidienne (`PlayerServerDailyConnectionEvent`).
- `ContractScrollCleanupListener` nettoie les parchemins expirés à la connexion et à l'ouverture d'un inventaire. Il lit l'id sur chaque parchemin candidat, puis consulte le contrat en base. Lors d'un ramassage, il annule et supprime un parchemin expiré au sol, puis appelle `reportExpiredScrolls`. Le nettoyage d'un inventaire ouvert appelle `purgeExpired` si des items ont été retirés ; celui de l'inventaire du joueur signale le nombre au joueur et purge également.

Le nettoyage ne parcourt pas les contenus de boîtes shulker portées, et les parchemins sans id ou dont le contrat n'est plus en base ne sont pas identifiés comme expirés par cette logique.

## Purge périodique : `ContractExpirationTask`

`start()` planifie `run()` toutes les `20 × 60 × 10` ticks, soit dix minutes, avec un délai initial égal à la même période. Chaque exécution appelle `ContractService.purgeExpired()`. Le nombre supprimé est écrit dans le journal au niveau `INFO` uniquement s'il est supérieur à zéro.

## Éléments utilitaires

- `ContractConstantes` centralise l'item de base du parchemin, ses clés de données persistantes, les symboles et le format de date.
- `ContractItemUtils` lit l'id du contrat à partir des métadonnées persistantes de l'item.
- `ContractChatFormats` fournit les formats de message du module contrat.
- `QualityAdapter` fournit libellés et couleurs d'affichage. Il consomme directement `common-domain.Quality` ; aucun `QualityConverter` n'est nécessaire.

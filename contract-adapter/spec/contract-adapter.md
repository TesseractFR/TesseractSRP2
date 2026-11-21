# Spec — `contract-adapter`

## Objectif

Relier le domaine contrat au serveur. Le module implémente les ports `serverside` de `contract-domain` avec JPA, YAML, ItemsAdder, Bukkit et les services des autres domaines. Il expose aussi les services du domaine aux joueurs par des commandes, des menus, des événements et une tâche planifiée. Il ne contient aucune règle métier : chaque décision (achat, livraison, validité d'un parchemin, tickets) est prise par un service du domaine.

La documentation détaillée du code est dans `contract-adapter-doc.md`.

## Implémentation des ports

| Port du domaine | Implémentation | Source |
|---|---|---|
| `ContractRepository` | `JpaContractRepository` | table `t_contracts` |
| `ContractTicketRepository` | `JpaContractTicketRepository` | table `t_contract_player_state` |
| `DailyTicketQuotaPort` | `YamlDailyTicketQuotaPort` | grade du joueur (`SrpPlayerProfileService`) et `daily-tickets` du YAML |
| `JobMoneyBonusPort` | `JobServiceMoneyBonusPort` | `JobService.getBonusValue(..., Bonus.MONEY)` |
| `PaymentPort` | `PlayerServicePaymentPort` | `PlayerMoneyService.adjustMoney` |
| `PlayerMaterialPort` | `CustomItemPlayerMaterialPort` | `CustomItemService` |
| `ContractScrollPort` | `BukkitContractScrollPort` | inventaire Bukkit du joueur en ligne |
| `ContractCatalogRepository` | `JobSkillContractCatalogRepository` | métiers (`JobService`), compétences (`SkillCatalogPort`), raretés et valeurs (`CustomItemService`) |
| `ContractConfigRepository` | `YamlContractConfigRepository` | `plugins/Tesseract/contract/contracts.yml` |

Un adapter utilise le service d'un autre domaine, jamais son repository. Seule exception, connue et isolée : `SkillConfigSkillCatalogPort` lit le repository de skill-domain, qui n'expose pas encore la liste des compétences par un service (dette A-1).

## Comportement attendu

### Achat

- `/contract merchant` ouvre le menu du marchand. Il affiche le solde de tickets et le quota du jour, et propose d'acheter un parchemin.
- Un clic sur l'achat appelle `ContractPurchaseService.buyScroll`. Le joueur reçoit le détail du contrat et ses tickets restants. Un refus (plus de ticket, inventaire plein, catalogue vide) est traduit en message d'erreur. Le menu se rouvre ensuite pour rafraîchir le solde.

### Livraison

- Les PNJ exécutent `/contract deliver <métier>` au nom du joueur. Le module ne lit rien sur le PNJ : le métier passé en argument désigne le destinataire.
- Le parchemin tenu en main principale désigne le contrat. Sans parchemin, le PNJ le fait remarquer. Si le parchemin ne correspond à aucun contrat, le PNJ répond qu'il est invalide.
- Sinon, un menu de confirmation présente le contrat, ce que le joueur possède, et le bonus de métier qu'il toucherait. À la confirmation, `ContractDeliveryService.deliver` décide. Une réussite ou un refus s'affiche comme une réplique du PNJ, tirée au hasard parmi plusieurs.

### Tickets

- `/contract tickets` affiche le solde et le quota.
- Le solde est remis au quota à la première connexion du jour (`PlayerServerDailyConnectionEvent`).
- Le staff donne et consulte les tickets par `/contract tickets give|get`, dans le module `plugin`.

### Parchemins invalides

- Un parchemin est invalide quand son contrat n'existe plus ou a expiré. C'est `ContractExpirationService.isScrollValid` qui le décide.
- Les parchemins invalides sont retirés à la connexion, à l'ouverture d'un inventaire (le conteneur puis l'inventaire du joueur) et au ramassage. Le joueur est prévenu du nombre retiré de son propre inventaire.
- Les contrats expirés sont supprimés de la base toutes les dix minutes, et à chaque retrait de parchemin chez un joueur.

## Configuration

Le fichier `plugins/Tesseract/contract/contracts.yml` est lu une fois au démarrage : une modification demande un redémarrage. S'il est absent, les valeurs par défaut s'appliquent ; une valeur invalide empêche le démarrage.

| Clé | Rôle | Défaut |
|---|---|---|
| `scroll-lifetime-hours` | durée de vie d'un parchemin, figée à l'achat | 72 |
| `quantity-by-rarity.<RARETÉ>.quantity` | quantité de base demandée par contrat | 300 |
| `quantity-by-rarity.<RARETÉ>.variance` | écart maximal en % autour de la quantité de base (50 : base 50 → [25 ; 75]) | 0 |
| `quantity-rate-by-quality.<QUALITÉ>` | coefficient appliqué à la quantité selon la qualité minimale, arrondi et au moins 1 (`EXCEPTIONAL: 0.25` : 600 → 150) | 1 |
| `rarity-weights.<RARETÉ>` | poids de tirage, 0 exclut la rareté | 0 |
| `daily-tickets.<Grade>` | quota quotidien du grade | 0 |
| `npc-names.<métier>` | nom affiché du PNJ destinataire | nom du métier, première lettre en majuscule |

## Contraintes

- Chaque matériau de métier ou de recette doit avoir une valeur dans `itemValue.yml` pour entrer au catalogue. Sinon il est ignoré avec un avertissement dans le journal.
- Le parchemin ne porte que l'identifiant du contrat : la base fait foi.
- Les textes affichés aux joueurs sont tous dans `ContractMessageFormatter`.

# Documentation du code — `contract-domain`

Ce document décrit le comportement du code actuel. La spec `contract-domain.md` décrit l'intention fonctionnelle ; en cas d'écart, le code fait foi pour le comportement observé.

Le domaine dépend de `common-domain` pour `PlayerId`, `MaterialName`, `Quality` et `MoneyValue`. Il ne dépend pas de Bukkit, Spring ni JPA : les accès au serveur passent par les ports `serverside` implémentés dans `contract-adapter`.

## Modèle

- `Contract` est un record contenant `id` (null avant sauvegarde), `type`, `job`, `material`, `quantity`, `minimalQuality`, `reward` et `expiresAt`. `type`, `job`, `material`, `minimalQuality` et `expiresAt` sont non nuls ; la quantité doit être au moins 1 et la récompense non négative.
- `Contract.isExpired()` compare `expiresAt` à `Instant.now()` ; `Contract.isForJob(npcJob)` compare les noms sensibles à la casse.
- `CatalogItem` contient le type, le métier ou la compétence, le matériau, un poids strictement positif et une quantité au moins égale à 1.
- `DeliveryRefusal` énumère les motifs de refus transmis à la messagerie. `PaymentFailedException` est une exception de paiement exposée par le port joueur.

## Génération : `ContractGenerator`

`generate(buyer)` demande le catalogue à `ItemInfoRepository`. Si la liste est vide, le résultat est `Optional.empty()`. Sinon, le générateur sélectionne un élément proportionnellement à son poids, tire uniformément une qualité dans `Quality.values()`, calcule `max(1, round(valeur unitaire × quantité))`, puis crée un contrat sans id dont l'expiration est `Instant.now() + getScrollTtl()`. Le paramètre `buyer` n'est pas enregistré dans le modèle actuel.

Le générateur reçoit un `Random` injecté, ce qui permet de contrôler les tirages. Le démarrage Spring construit le générateur avec `new Random()`.

## Service : `ContractService`

Le service coordonne six ports : contrat, matériaux, parchemins, informations joueur, messagerie, ainsi que le générateur.

### Tickets et achat

- `getTickets` renvoie le solde enregistré ou, en l'absence de ligne, le quota quotidien.
- `renewDaily` remplace le solde par le quota si les valeurs diffèrent. `showTickets` et les opérations staff délèguent l'affichage à la messagerie. `giveTicketsAsStaff` refuse un montant inférieur ou égal à zéro.
- `buyScroll` vérifie le ticket, la capacité de réception et la disponibilité d'un contrat avant de consommer le ticket. Il enregistre ensuite le contrat et remet le parchemin. Une exception de sauvegarde rend le ticket avant d'être relancée ; un échec de remise supprime le contrat sauvegardé et rend le ticket.

### Livraison et parchemin

`deliver(player, contractId, npcJob)` suit cet ordre :

1. Recherche le contrat ; s'il manque, envoie le refus `INVALID_SCROLL`.
2. Si le contrat est expiré, le supprime et envoie `EXPIRED`. Puis vérifie le client (`WRONG_CLIENT`).
3. Vérifie la présence du parchemin dans l'inventaire, puis le nombre d'objets admissibles. L'absence du parchemin produit `sendNoScrollInHand`; le manque de matériaux produit `MISSING_ITEMS` avec le nombre manquant.
4. Retire le parchemin, retire les matériaux, puis verse `contract.reward()`.

La livraison ne passe pas par une opération atomique `settle` et n'attrape pas d'exception de paiement ou de retrait de matériaux. `moneyBonus` calcule un montant additionnel pour les contrats `HARVEST`, mais `deliver` verse actuellement la récompense de base uniquement.

`useScroll` retire et signale un parchemin si le contrat correspondant est absent ou expiré. `purgeExpired` appelle `deleteExpired(Instant.now())`. `reportExpiredScrolls` purge la base puis envoie au joueur le nombre de parchemins retirés par le nettoyage.

### Ports `serverside`

- `ContractRepository` : `save`, `findById`, `delete` et `deleteExpired(now)`.
- `PlayerInfoRepository` : tickets, quota, bonus monétaire et versement de `MoneyValue`.
- `PlayerMaterialRepository` : `countAtLeast` et `removeAtLeast`. Le retrait ne retourne pas de map ni ne propose de restitution.
- `ContractScrollRepository` : capacité, remise, présence et retrait d'un parchemin.
- `ItemInfoRepository` : catalogue et valeur d'un matériau selon sa qualité.
- `ContractConfigRepository` : durée de vie, quantités et poids selon rareté, quotas et noms de PNJ.
- `ContractMessagingRepository` : messages pour achats, livraisons, tickets, staff et nettoyage des parchemins.

## Cycle de vie et limites

Les achats créent une ligne et un parchemin. Une livraison réussie retire le parchemin et les matériaux, puis paie ; la ligne de contrat n'est pas supprimée par le chemin courant de `deliver`. Les contrats expirés peuvent être supprimés par `deliver`, `useScroll` ne purge pas la base, `reportExpiredScrolls` ou la purge périodique de l'adapter.

Le service et le générateur utilisent `Instant.now()` directement. Le domaine ne gère pas la concurrence ni les transactions entre base, inventaire et paiement.

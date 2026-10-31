# Spec — `contract-domain`

## Objectif

Gérer les contrats : un joueur dépense un ticket quotidien pour acheter, chez le marchand, un parchemin physique qui contient une mission aléatoire de récolte (métier) ou d’artisanat (compétence). Il livre le parchemin et les items demandés au PNJ concerné et touche des lys. Le domaine ne dépend d’aucun autre module SRP : il définit son `PlayerID` et sa `Quality`, et atteint les métiers, compétences, items, monnaie et grades par ses ports `serverside`.

## Modèle et règles

- Un `Contract` porte l’acheteur (`issuedTo`), le type (`HARVEST` ou `CRAFT`), le `job` (métier ou compétence dont le PNJ est le client), le matériau, la quantité (au moins 1), la qualité minimale, la récompense de base et les dates d’émission et d’expiration. Il n’a pas de propriétaire : celui qui livre touche la récompense. Un contrat livré ou expiré est supprimé.
- Un `CatalogItem` (dans `model.contract`) est un item qu’un contrat peut demander, avec son poids de tirage et sa quantité, tous deux donnés par la rareté. L’adapter le construit.
- `ContractGenerator.generate` tire un item du catalogue selon son poids, une qualité minimale uniforme entre `POOR` et `EXCEPTIONAL`, puis fixe l’expiration (`scrollTtl`) et la récompense de base : valeur de l’item pour la qualité minimale × quantité, au moins 1. Le contenu ne dépend pas de l’acheteur et le parchemin est échangeable. Catalogue vide : aucun contrat.
- `ContractService` gère aussi le stock de tickets : un joueur sans stock enregistré est nouveau et a son quota du grade, `renewDaily` remet le stock au quota à la première connexion du jour, `giveTicketsAsStaff` en ajoute sans plafond ; l’achat dépense un ticket et le rend en cas d’échec.
- `ContractService.buyScroll` vérifie le ticket et la place dans l’inventaire, génère le contrat, dépense le ticket puis sauvegarde le contrat (le ticket est rendu si la sauvegarde échoue) et remet le parchemin (sinon le contrat est supprimé et le ticket rendu).
- `ContractService.deliver` vérifie dans l’ordre le contrat (`INVALID_SCROLL`), le PNJ (`WRONG_CLIENT`), l’expiration (`EXPIRED`, contrat supprimé), le parchemin porté et les items de qualité suffisante (`MISSING_ITEMS` ou `NOT_ENOUGH_QUALITY`). Il retire ensuite le parchemin et les items, appelle `ContractRepository.settle`, et rend le tout en cas de refus ou d’exception.
- `ContractRepository.settle` supprime le contrat et paie le livreur en une seule transaction, côté adaptateur : faux si le contrat n’existe plus (double livraison), `ContractRepository.PaymentFailedException` si le paiement échoue, ce qui annule la suppression (`PAYMENT_FAILED`). La somme versée est la récompense de base plus, pour la récolte seulement, le bonus `MONEY` du livreur (`moneyBonus`).
- `checkDeliverable` vérifie un parchemin avant la confirmation, `useScroll` détruit un parchemin sans contrat valide, `purgeExpired` supprime les contrats échus et `moneyBonus` donne le bonus affiché avant de livrer.
- Les messages passent tous par `ContractMessagingRepository` ; les refus de livraison utilisent `DeliveryRefusal` (dans `model.contract`).

## Interfaces et dépendances

Les services côté utilisateur sont `ContractService` (achat, livraison, parchemin, tickets) et `ContractGenerator`. Aucun n’a plus de cinq dépendances. Les ports serveur couvrent les contrats (`ContractRepository`), les infos du joueur (`PlayerInfoRepository` : tickets, quota par grade, bonus d’argent), son inventaire (`PlayerInventoryRepository` : matériaux et parchemin), les infos des items (`ItemInfoRepository` : catalogue et valeur), la configuration (`ContractConfigRepository` : durée de vie du parchemin) et la messagerie.

## Critères d’acceptation

1. Un joueur sans stock enregistré a le quota de son grade. `renewDaily` remet le stock au quota du grade ; sans connexion du jour, le stock ne change pas.
2. Sans ticket, rien n’est créé ; avec un inventaire plein ou un catalogue vide, le ticket n’est pas dépensé.
3. Un achat réussi dépense un ticket et sauvegarde un contrat ; si la remise du parchemin échoue, le contrat est supprimé et le ticket rendu.
4. Sur un grand nombre d’achats, les items rares sortent proportionnellement moins que les communs.
5. Si un item manque, rien n’est retiré ni versé ; livrer à un autre métier est refusé.
6. Une livraison réussie retire les items de plus basse qualité admissible, jamais sous la qualité minimale, verse la somme au livreur et supprime le contrat.
7. Un contrat supprimé entre-temps rend items et parchemin sans rien verser ; un échec de paiement rend items et parchemin et conserve le contrat.
8. Un contrat expiré est refusé et supprimé.
9. La somme versée pour la récolte augmente avec le bonus `MONEY` du livreur, pas celui de l’acheteur ; celle de l’artisanat non.

## Limites constatées

Le domaine demande la valeur d’un item pour la qualité minimale du contrat ; l’adapter renvoie pour l’instant la valeur unique du matériau (la même quelle que soit la qualité) en attendant `getMonetaryValue`. Il n’y a aucun gain de tickets en dehors de la commande staff, aucun plafond de quantité et aucun test unitaire. `ContractService` ignore le résultat de `giveScroll` quand il rend le parchemin : si l’inventaire est plein à ce moment, le parchemin est perdu et le contrat reste en base jusqu’à son expiration.
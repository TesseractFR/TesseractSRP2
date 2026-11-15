# Spec — `contract-domain`

## Objectif

Gérer les contrats : un joueur dépense un ticket quotidien pour acheter, chez le marchand, un parchemin physique qui contient une mission aléatoire de récolte (métier) ou d’artisanat (compétence). Il livre le parchemin et les items demandés au PNJ concerné et touche des lys. Le domaine ne dépend que de `common-domain` (`PlayerId`, `MaterialName`, `Quality`, `MoneyValue`) et atteint les métiers, compétences, items, monnaie et grades par ses ports `serverside`.

## Modèle et règles

- Un `Contract` porte son identifiant (`ContractId`), le type (`HARVEST` ou `CRAFT`), le destinataire (`ContractClient` : métier ou compétence dont le PNJ est le client), le matériau, la quantité (au moins 1), la qualité minimale, la récompense de base et la date d’expiration. Il n’a pas de propriétaire et ne retient pas son acheteur : celui qui livre touche la récompense. Un contrat livré ou expiré est supprimé.
- Un `CatalogItem` (dans `model.contract`) est un item qu’un contrat peut demander, avec son poids de tirage, sa quantité de base et la variance de cette quantité, tous donnés par la rareté. L’adapter le construit.
- `ContractGenerator.generate` tire un item du catalogue selon son poids, une quantité uniforme entre `base × (1 − variance)` et `base × (1 + variance)` (bornes arrondies et comprises, au moins 1 ; la base et la variance en % sont portées par l’item de catalogue et viennent de la rareté : 50 sur une base de 50 donne [25 ; 75]), une qualité minimale uniforme entre `POOR` et `EXCEPTIONAL`, puis fixe l’expiration (`scrollLifetime`) et la récompense de base : valeur de l’item pour la qualité minimale × quantité tirée, au moins 1. Il produit un `ContractDraft` sans identifiant ; c’est `ContractRepository.create` qui attribue l’identifiant. Le contenu ne dépend pas de l’acheteur et le parchemin est échangeable. Catalogue vide : aucun contrat.
- Tickets (`ContractTicketService`) : un joueur sans stock enregistré est nouveau et a son quota du grade, `renewDaily` remet le stock au quota à la première connexion du jour, `giveTickets` en ajoute sans plafond (nombre ≤ 0 refusé : `InvalidTicketAmountException`) ; l’achat dépense un ticket et le rend en cas d’échec.
- Achat (`ContractPurchaseService.buyScroll`, qui s’appuie sur `ContractTicketService`) : vérifie le ticket (`NoContractTicketLeftException`) et la place dans l’inventaire (`ScrollInventoryFullException`), génère le contrat (`NoContractAvailableException` si le catalogue est vide), dépense le ticket puis enregistre le contrat (le ticket est rendu si l’enregistrement échoue) et remet le parchemin (sinon le contrat est supprimé, le ticket rendu et `ScrollInventoryFullException` levée). Renvoie le contrat acheté.
- Livraison (`ContractDeliveryService.deliver`) : vérifie dans l’ordre le contrat (`ContractNotFoundException`), l’expiration (`ContractExpiredException`, contrat supprimé), le PNJ (`WrongContractClientException`), le parchemin porté (`ContractScrollNotOwnedException`) et les items de qualité suffisante (`MissingContractItemsException`). Il retire ensuite le parchemin et les items, supprime le contrat puis verse la somme. Si le paiement échoue après le retrait, l’adapter journalise une erreur pour que le staff dédommage le joueur, et `PaymentFailedException` remonte. La somme versée est la récompense de base plus, pour la récolte seulement, le bonus `MONEY` du livreur. La règle du bonus (récolte seulement, taux négatif compté comme nul, arrondi à l’unité) vit dans le modèle : `Contract.computeMoneyBonus`. Elle est renvoyée à l’appelant.
- Expiration (`ContractExpirationService`) : `purgeExpired` supprime les contrats échus, `isScrollValid` dit si un parchemin doit rester dans un inventaire (son contrat existe et n’a pas expiré).
- Tous les refus sont des exceptions métier qui héritent de la classe scellée `ContractException`. Le domaine n’envoie aucun message : les adapters userside traduisent les refus et les succès en messages pour le joueur.

## Interfaces et dépendances

Les services côté utilisateur sont des classes concrètes, une par cas d’usage : `ContractPurchaseService`, `ContractDeliveryService`, `ContractTicketService` et `ContractExpirationService`. L’achat s’appuie sur `ContractGenerator`. Les ports serveur couvrent les contrats (`ContractRepository`), les tickets (`ContractTicketRepository`), le quota par grade (`DailyTicketQuotaPort`), le bonus d’argent (`JobMoneyBonusPort`), le versement (`PaymentPort`), les matériaux du joueur (`PlayerMaterialPort` : compter, retirer), son parchemin (`ContractScrollPort` : remettre, détecter, retirer), le catalogue (`ContractCatalogRepository` : éléments et valeur) et la configuration (`ContractConfigRepository` : durée de vie du parchemin).

## Critères d’acceptation

1. Un joueur sans stock enregistré a le quota de son grade. `renewDaily` remet le stock au quota du grade ; sans connexion du jour, le stock ne change pas.
2. Sans ticket, rien n’est créé ; avec un inventaire plein ou un catalogue vide, le ticket n’est pas dépensé.
3. Un achat réussi dépense un ticket et sauvegarde un contrat ; si la remise du parchemin échoue, le contrat est supprimé et le ticket rendu.
4. Sur un grand nombre d’achats, les items rares sortent proportionnellement moins que les communs.
5. La quantité d’un contrat reste dans `[base × (1 − variance) ; base × (1 + variance)]`, jamais sous 1 ; avec une variance nulle, elle vaut la base.
6. Si un item manque, rien n’est retiré ni versé ; livrer à un autre métier est refusé.
7. Une livraison réussie retire les items de plus basse qualité admissible, jamais sous la qualité minimale, verse la somme au livreur et supprime le contrat.
8. Un contrat supprimé entre-temps est refusé avant tout retrait ; un échec de paiement, qui survient après le retrait des items et du parchemin, est journalisé en erreur pour que le staff dédommage le joueur.
9. Un contrat expiré est refusé et supprimé.
10. La somme versée pour la récolte augmente avec le bonus `MONEY` du livreur, pas celui de l’acheteur ; celle de l’artisanat non.

## Limites constatées

Le domaine demande la valeur d’un item pour la qualité minimale du contrat ; l’adapter la lit dans le service d’items (`getMoneyValue`, fichier `itemValue.yml`). Cette valeur dépend de la qualité : le prix de base est multiplié par le coefficient de la section `QualityRate` de `itemValue.yml`. Un item sans prix est exclu du catalogue par l’adapter, avec un avertissement. La livraison n’est pas atomique entre base, inventaire et paiement. Il n’y a aucun gain de tickets en dehors de la commande staff et aucun plafond de quantité.

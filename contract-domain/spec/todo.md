# Reste à faire — `contract-domain`

## Valeur des items (en attente)

- [ ] Brancher la valeur monétaire fournie par le service d'items : remplacer l'implémentation de `ItemInfoRepository.getValue` par `getMonetaryValue(matériau, qualité)` (le changement est dans l'adapter, pas dans le domaine).
- [ ] S'assurer que chaque item récoltable ou fabriqué qui peut sortir d'un contrat a une valeur : un item sans valeur rapporte le minimum, 1 lys.

## Règles à décider

- [ ] Comment gagner des tickets en jeu (vote, loot, activité) : aucune méthode de gain n'existe pour l'instant, seule la commande staff ajoute des tickets.
- [ ] Parchemin de découverte du tutoriel : à gérer dans le tutoriel, pas dans ce module.
- [ ] Qui touche le bonus `MONEY` : celui du livreur (actuel) ou celui de l'acheteur (la récompense est fixée à l'achat).
- [ ] Retirer des tickets en staff : `withdrawTicket` existe mais n'est pas exposé ; il faudrait une méthode publique et un message dans `ContractMessagingRepository`.
- [ ] Borne maximale de quantité : aucune aujourd'hui, un contrat peut demander plusieurs centaines d'items.
- [ ] Quantité des contrats d'artisanat : elle vient de la durée des recettes, pas de `quantity-by-rarity` ; vérifier l'équilibre.

## Nettoyage

- [ ] `ContractService` ignore le résultat de `giveScroll` quand il rend le parchemin (deux fois dans `takeScrollAndItemsOrRefuse`, une fois dans `giveBackToPlayer`) : si l'inventaire est plein à ce moment, le parchemin est perdu alors que le contrat reste en base jusqu'à son expiration. Pistes : le poser au sol, prévenir le joueur et supprimer le contrat, ou vérifier `canReceiveScroll` avant de retirer le parchemin.

## Tests

- [ ] Écrire les tests unitaires de `ContractService` et `ContractGenerator` avec de faux ports, d'après les critères d'acceptation de la spec.
- [ ] Injecter une `Clock` à la place de `Instant.now()` (service et générateur) pour tester l'expiration à date fixe.

## Alignement avec les autres modules

- [ ] Encapsuler `job` et `material` dans des types valeur (comme `Material`, `SkillName`, `Tier` dans `skill-domain`) plutôt que des `String`.
- [ ] Sortir le parchemin physique de `PlayerInventoryRepository` (matériaux seulement dans `ItemRepository` de `skill-domain`) vers un port dédié.
- [ ] `ContractService.noScrollInHand` ne fait que relayer un message : le traiter dans `checkDeliverable`.

## Après la mise en place
- [ ] Aligner les specs voisines (`09-guildes`, `10-rivalite-et-recrutement`, `16-decisions-et-todo`) avec les décisions de cette spec.
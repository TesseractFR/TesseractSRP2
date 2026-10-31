# Reste à faire — `contract-adapter`

## Corrections et rangement


## Remplacements provisoires

- [ ] `ContractJpaRepositoryAdapter.settle` : passer à `MoneyService` (B2) et enregistrer la raison du paiement ; vérifier que `adjustMoney` participe à la même transaction.
- [ ] `TesseractPlayerMaterialRepository` : passer au comptage natif par qualité (A7).

## Alignement avec les autres modules

- [ ] Les menus étendent `Menu` avec des icônes vanilla : passer à `ItemAdderMenu` (fonds graphiques partagés, comme `skill-adapter`) quand les textures existent.
# Reste à faire — `contract-adapter`

## Valeur des items (en attente)

- [ ] `ContractItemInfoRepository.getValue` lit `CustomMaterial.value()` en attendant : le remplacer par l'appel à `customItemService.getMonetaryValue(matériau, qualité)` quand il existera.

## Corrections et rangement

- [ ] Migrer les colonnes `t_contracts.issued_to` et `t_contract_player_state.player_id` de `BINARY(16)` vers `VARCHAR(36)` (UUID en texte) sur les bases déjà créées : la création automatique des tables ne les convertit pas.

## Remplacements provisoires

- [ ] `ContractJpaRepositoryAdapter.settle` : passer à `MoneyService` (B2) et enregistrer la raison du paiement ; vérifier que `adjustMoney` participe à la même transaction.
- [ ] `TesseractPlayerInventoryRepository` : passer au comptage natif par qualité (A7).

## Alignement avec les autres modules

- [ ] Les menus étendent `Menu` avec des icônes vanilla : passer à `ItemAdderMenu` (fonds graphiques partagés, comme `skill-adapter`) quand les textures existent.
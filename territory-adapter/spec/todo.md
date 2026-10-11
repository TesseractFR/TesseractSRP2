# Reste à faire — `territory-adapter`

## Implémentation à compléter

- [ ] Implémenter `TerritoryChunkRepositoryImpl.save` et définir la suppression/persistance des sous-types de chunks.
- [ ] Implémenter `CampementRepositoryImpl.isChunkClaimed` et `getCampementByChunk` à partir des revendications réelles.
- [ ] Corriger les requêtes de `CampementJpaRepository`: les méthodes `isChunkClaimed` et `getCampementByOwner` filtrent actuellement avec des identifiants qui ne correspondent pas à leur nom/paramètre.
- [ ] Corriger les recherches de guildes qui appellent `toDomain()` sans gérer l’absence de résultat (nom, chunk, membre); vérifier aussi `findByPlayer` et `areChunksClaimed`.
- [ ] Relier `SrpPlayerRepositoryImpl` au vrai service de rang et niveau de campement; retirer les valeurs fixes Baron=false et niveau=1.
- [ ] Relier `MoneyServiceImpl` au système économique effectif pour solde, coût de création, dépôts, retraits et transfert staff.
- [ ] Revoir le callback de confirmation de `/guild kick`: l’expulsion se produit avant l’écran de confirmation et le callback mentionne un départ.
- [ ] Compléter les messages actuellement silencieux de `/campement create` pour `RANK_TOO_LOW`, `NAME_TAKEN` et `NOT_ENOUGH_MONEY`.
- [ ] Vérifier les tâches de particules asynchrones: Bukkit/Paper peut limiter les appels de particules/état joueur au thread principal.

## Intégration à vérifier

- [ ] Vérifier que les listeners de protection couvrent tous les événements attendus, respectent `ignoreCancelled` et déterminent sans ambiguïté lequel traite chaque chunk.
- [ ] Vérifier le maintien du rendu de bordure lors d’un changement de monde, d’une nouvelle revendication et d’une annulation de tâche.
- [ ] Vérifier le comportement des bâtons en main secondaire et lorsque plusieurs types d’invocables sont enregistrés.

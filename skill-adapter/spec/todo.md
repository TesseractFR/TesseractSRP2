# Reste à faire — `skill-adapter`

## Bugs et fonctions incomplètes identifiés

- [ ] Implémenter `SkillResultCacheRepositoryJpaAdapter.save` afin que les résultats non collectés survivent aux redémarrages.
- [ ] Dans `CraftingRepositoryJpaAdapter.saveQueuedRecipe`, supprimer les anciennes lignes même lorsque la file devient vide.
- [ ] Vérifier et corriger le stockage des durées : le champ nommé `unitDurationNanos` reçoit actuellement `toSeconds()`.
- [ ] Corriger le message de fin de craft contenant littéralement `${skillName}`.
- [ ] Vérifier `CustomStructureListener` : il construit `ChunkCoord` à partir des coordonnées de bloc X/Y, au lieu d’indices de chunk X/Z; vérifier aussi si la position doit être celle de la structure ou du joueur.
- [ ] Alimenter les niveaux/bonus de récupération succès/échec et craft multiple dans `SkillUpgradeMenu`, ou désactiver ces améliorations tant qu’elles ne sont pas implémentées.
- [ ] Valider le délai minimal d’une tâche pour éviter un intervalle Bukkit de zéro tick.
- [ ] Vérifier les conversions JPA et la reprise des tâches/files après redémarrage; ajouter la lecture des tâches persistées si elles doivent être reprises.

## Vérifications fonctionnelles

- [ ] Vérifier le calcul des lots dans `CraftingMenu` quand une recette n’a pas de composants, quand le résultat dépasse une pile ou quand les items custom ne sont pas disponibles.
- [ ] Vérifier et traiter les items excédentaires renvoyés par l’inventaire Bukkit.
- [ ] Vérifier que `StationJpaRepositoryAdapter` persiste effectivement les changements renvoyés par `StationService.upgradeStat`.
- [ ] Confirmer que tous les callbacks UI et accès Bukkit restent sur le thread principal.

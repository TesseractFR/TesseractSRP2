# Reste à faire — `common-adapter`

Ce document déduit les travaux des limites signalées dans [common-adapter.md](common-adapter.md) et de ses intégrations.

## Corrections / complétions identifiées

- [ ] Gérer explicitement un monde absent dans `LocationMapper.toLocation` (retour contrôlé ou erreur métier claire) et couvrir cette situation.
- [ ] Vérifier/corriger `PlayerUtils.tryFreeChestplateSlot` : déplacer réellement le plastron hors du slot d’armure et ne pas dupliquer l’objet si la tentative échoue.
- [ ] Décider la politique de lecture d’un joueur absent dans `SrpPlayerJpaRepositoryAdapter` : laisser le service créer le profil par défaut ou ajouter une création/persistance explicite.

## Vérifications à faire

- [ ] Vérifier les chemins de clic/fermeture/rafraîchissement des menus partagés et les propriétés `InventoryView` requises par les versions Bukkit supportées.
- [ ] Vérifier que les opérations Bukkit de téléportation et d’effets s’exécutent sur le thread requis, notamment les timers.
- [ ] Vérifier les dépendances obligatoires à ItemAdder/Tesseract au démarrage et documenter les composants qui ne sont disponibles qu’avec ces plugins.

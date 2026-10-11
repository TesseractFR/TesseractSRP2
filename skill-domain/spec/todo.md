# Reste à faire — `skill-domain`

## Corrections fonctionnelles identifiées

- [ ] Corriger `processCraftStep` pour sortir immédiatement après `finishTask` lorsque la quantité est déjà nulle, afin d’éviter un pas supplémentaire.
- [ ] Définir une formule de réussite bornée et des règles de bornage pour les bonus de réussite et de qualité.
- [ ] Implémenter ou retirer les champs de récupération et craft multiple qui ne sont pas consommés par le traitement actuel.
- [ ] Définir les validations cohérentes pour quantités, durées, ingrédients, résultat et structures de maps des recettes.
- [ ] Définir les règles de qualité requise des composants; le flux actuel vérifie et retire les ingrédients en `POOR` uniquement.
- [ ] Assurer la persistance cohérente des caches, tâches actives et files; spécifier les garanties de reprise après redémarrage.
- [ ] Valider que le cache `done`/`garbage` soit collecté une seule fois et supprimé uniquement après remise effective des items.
- [ ] Définir les règles d’accès aux stations territoriales et le comportement si le territoire/station disparaît pendant un craft.

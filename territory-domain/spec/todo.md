# Reste à faire — `territory-domain`

## Corrections fonctionnelles identifiées

- [x] Corriger le retrait bancaire : le solde de guilde est diminué après le transfert au joueur.
- [x] Refuser les montants nuls ou négatifs pour les dépôts, retraits, coût de création et transferts.
- [ ] Définir une stratégie de cohérence pour les opérations monétaires entre joueur et persistance. Les services appliquent actuellement une compensation en cas d’échec de sauvegarde, mais cela ne garantit pas l’atomicité et la compensation elle-même peut échouer.
- [x] Corriger la progression d’expérience pour traiter un gain qui franchit plusieurs seuils et publier un événement par niveau atteint.
- [ ] Définir et garantir l’invariant « un joueur appartient à au plus une guilde », y compris invitations concurrentes et adhésion simultanée.
- [ ] Vérifier cohérence du transfert de chef, des rôles de membres et des invitations/demandes en cours après départ, exclusion ou dissolution.
- [ ] Définir la cohérence entre sauvegarde/suppression du territoire, revendications de chunks, événements domaine et effets monnaie. Les événements de claim/unclaim et level-up sont émis après sauvegarde ; sans transaction, une sauvegarde et ses effets externes ne sont pas atomiques.
- [ ] Valider l’intégrité des points de spawn : monde/chunk revendiqué, type privé/visiteur, et comportement lors de la libération du chunk.
- [ ] Définir les règles exactes de confiance et d’interaction pour chaque type de territoire et tous les résultats `InteractionAllowResult`.
- [ ] Définir les règles de création/extension (distance au spawn et aux territoires, adjacency, limites de chunks) comme contrats vérifiables.

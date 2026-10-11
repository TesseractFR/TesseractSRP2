# Reste à faire — `territory-domain`

## Corrections fonctionnelles identifiées

- [ ] Corriger le retrait bancaire : après transfert au joueur, diminuer le solde de guilde au lieu d’appeler `Guild.addMoney`.
- [ ] Valider que dépôts, retraits, coût de création et transferts sont strictement positifs (ou spécifier le traitement de zéro) et atomiques entre joueur, banque et persistance.
- [ ] Corriger la progression d’expérience pour traiter un gain qui franchit plusieurs seuils et publier le bon nombre d’événements de niveau.
- [ ] Définir et garantir l’invariant « un joueur appartient à au plus une guilde », y compris invitations concurrentes et adhésion simultanée.
- [ ] Vérifier cohérence du transfert de chef, des rôles de membres et des invitations/demandes en cours après départ, exclusion ou dissolution.
- [ ] Garantir la cohérence transactionnelle entre sauvegarde/suppression du territoire, revendications de chunks, événements domaine et effets monnaie.
- [ ] Valider l’intégrité des points de spawn : monde/chunk revendiqué, type privé/visiteur, et comportement lors de la libération du chunk.
- [ ] Définir les règles exactes de confiance et d’interaction pour chaque type de territoire et tous les résultats `InteractionAllowResult`.
- [ ] Définir les règles de création/extension (distance au spawn et aux territoires, adjacency, limites de chunks) comme contrats vérifiables.

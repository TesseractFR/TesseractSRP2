# Reste à faire — `job-domain`

## Fonctionnalités incomplètes

- [ ] Implémenter `upgradeSkill` : vérifier disponibilité, niveau maximal et points, mettre à jour le niveau/solde et persister le résultat.
- [ ] Ajouter au service une opération de dépôt de mission qui valide le métier, le matériau, la quantité et la qualité minimale, puis met à jour le livré/restant et remet la récompense à l’achèvement.
- [ ] Définir le cycle de vie des missions (création, slots par rang, expiration/annulation, répétition et suppression) et ses règles de récompense.
- [ ] Définir les bornes des bonus de chance et qualité (bornage à 0–1 ou autre règle) et le comportement lorsque les valeurs configurées dépassent ces limites.
- [ ] Préciser l’indexation des métiers et des sources après modification de configuration; prévoir un rechargement ou un cache explicitement figé.
- [ ] Vérifier le traitement de plusieurs métiers qui déclareraient une même `Source` : l’index actuel ne conserve qu’un métier par source.
- [ ] Valider cohérence `maxLevel` / `values` / `pricePerLevel` pour chaque talent et fournir des erreurs de configuration explicites.

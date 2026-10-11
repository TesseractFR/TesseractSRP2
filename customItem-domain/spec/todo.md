# Reste à faire — `customItem-domain`

## Complétions identifiées

- [ ] Retourner une vue immuable ou une copie depuis `getCustomMaterials` pour protéger l’index interne.
- [ ] Définir et valider les quantités autorisées pour ajout/retrait, y compris zéro et les nombres négatifs.
- [ ] Définir le contrat de retrait : quantité réellement retirée, échec si stock insuffisant ou consommation partielle.
- [ ] Définir le comportement de `getItemStack` quand un matériau déclaré ne peut pas être construit par le dépôt.
- [ ] Documenter explicitement que la qualité ne s’applique qu’aux items personnalisés, ou revoir l’API si la qualité doit aussi qualifier les matériaux vanilla.
- [ ] Ajouter une politique de collision si plusieurs matériaux chargés partagent le même `MaterialName`.

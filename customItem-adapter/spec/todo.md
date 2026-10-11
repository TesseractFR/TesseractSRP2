# Reste à faire — `customItem-adapter`

## Corrections / complétions identifiées

- [ ] Traiter le résultat `Map` de `PlayerInventory.addItem` et définir quoi faire des objets excédentaires lorsque l’inventaire est plein.
- [ ] Définir le résultat d’un retrait lorsque l’inventaire ne contient pas la quantité demandée (échec explicite, quantité retirée ou comportement actuel documenté).
- [ ] Valider les noms Bukkit et les identifiants ItemAdder avant conversion; remplacer les retours `null`/exceptions ambigus par un résultat uniforme.
- [ ] Valider la présence et la valeur des champs YAML (`display_name`, `item_tag`, `rarity`) et fournir une erreur qui nomme le fichier et la clé fautive.
- [ ] Vérifier `getCustomItemData` quand la clé de qualité manque ou contient une valeur inconnue.

## Décisions / vérifications

- [ ] Fixer une politique unique pour les quantités négatives et nulles dans les opérations d’inventaire.
- [ ] Vérifier l’exécution sur le thread principal pour tous les accès Bukkit aux joueurs/inventaires.
- [ ] Déterminer si le fichier YAML doit être surveillé/rechargé à chaud et documenter le format attendu.

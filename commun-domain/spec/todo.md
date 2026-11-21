# Reste à faire — `commun-domain`

## Décisions métier à prendre

- [ ] Décider si `WorldService` doit rester une classe concrète dépendant des types Bukkit ou devenir un port indépendant de la plateforme.
- [ ] Spécifier l’annulation d’une téléportation lorsque le joueur se déconnecte avant le premier tick de contrôle et le comportement en cas de durée non multiple de 10 ticks.

## Complétions techniques

- [ ] Ajouter une couverture de vérification pour la résolution des mondes et le flux de téléportation selon les règles décidées.

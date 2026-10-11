# Reste à faire — `commun-domain`

## Décisions métier à prendre

- [ ] Définir et appliquer les règles sur les ajustements négatifs ou nuls, les bornes maximales de soldes et les débordements entiers.
- [ ] Définir si un rang assigné manuellement doit toujours synchroniser le titre et comment traiter un titre incohérent chargé depuis la persistance.
- [ ] Décider si `WorldService` doit rester une classe concrète dépendant des types Bukkit ou devenir un port indépendant de la plateforme.
- [ ] Spécifier l’annulation d’une téléportation lorsque le joueur se déconnecte avant le premier tick de contrôle et le comportement en cas de durée non multiple de 10 ticks.

## Complétions techniques

- [ ] Ajouter une couverture de vérification pour progression de rang, soldes, résolution des mondes et flux de téléportation selon les règles décidées.

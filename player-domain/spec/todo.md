# Reste à faire — `player-domain`

## Décisions métier à prendre

- [ ] Définir et appliquer les règles sur les ajustements négatifs ou nuls, les bornes maximales de soldes et les débordements entiers.
- [ ] Définir comment traiter un titre incohérent avec le grade chargé depuis la persistance.

## Complétions techniques

- [ ] Décider où placer la frontière transactionnelle pour que la sauvegarde du joueur et l’écriture du journal soient atomiques (achat de grade, ajustement du staff).

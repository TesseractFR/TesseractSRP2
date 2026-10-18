# Contrats et économie

[← Retour à la vision globale](./main.md)

## Contrats et économie

Les contrats, au spawn et dans les guildes, sont la seule source d'argent du jeu. Tout le reste déplace ou détruit de l'argent déjà créé.

**Contrats du spawn (proposition à confirmer).**

- Un tableau personnel de 6 contrats au plus. Quand il en manque, un nouveau arrive toutes les 2 à 3 heures. Les contrats s'accumulent pendant l'absence, il n'y a donc pas de corvée quotidienne.
- Chaque contrat indique un client (PNJ nommé), un item, une quantité, une qualité minimale, une difficulté de 1 à 3 étoiles (la réputation), une récompense en lys et en XP.
- Les contrats sont pondérés par les métiers que le joueur pratique, avec un contrat de découverte. Certains contrats demandent des qualités faibles pour écouler les items de rebut.
- Un slot libre est nécessaire pour accepter : 2 slots au départ, jusqu'à 5 avec les grades. Un contrat accepté devient un parchemin physique, qu'on livre au PNJ. Les livraisons partielles sont permises.

**Contrats de guilde (tableau de ville).**

- Ils sont générés automatiquement, comme au spawn : les guildes ne définissent pas les contrats. Même interface, même type de tableau, récompenses plus élevées.
- Ils se renouvellent. Le rythme n'est pas fixé (2 à 3 heures comme au spawn, ou autre) et dépendra du nombre de joueurs.
- Présentation à trancher : individuelle (chacun voit ses contrats), commune (une liste partagée par la guilde, premier arrivé premier servi) ou mixte (contrats personnels et « chantiers » communs où chacun contribue).
- La récompense est partagée entre le joueur et la banque de sa guilde. La part de la banque est prélevée sur la récompense, pas ajoutée, pour ne pas créer plus d'argent. Elle serait plus forte pour les membres de la guilde, avec une taxe plus lourde sur un contrat fait pour une autre ville (à définir).
- Chaque contrat livré donne de l'XP à la guilde.

**Siphons.** Les grades, les kits d'amélioration des ailes, les structures de guilde (payées en items craftés), les catalyseurs, l'assurance contre la perte de stuff, la taxe de l'hôtel des ventes.

&#91;embedded content: flux d'argent · 1 robinet, 2 dépenses, 1 banque\]

Le système crée de l'argent uniquement par les contrats, au spawn et dans les guildes. La banque de guilde reçoit une part de ces récompenses et les ventes des shops de ville.

**Valeur d'un item.** Tous les métiers doivent rapporter à peu près autant à l'heure. La valeur de référence d'un item se calcule ainsi, avec les cadences mesurées pendant la bêta :

```latex
valeur = \frac{1}{\text{chance de loot} \times \text{récoltes par heure}} \times \text{taux horaire cible} \times \text{coef. qualité} \times \text{coef. rareté}
```

La récompense d'un contrat est la somme des valeurs demandées, multipliée par un coefficient de réputation et un bonus de qualité. Prévoir dès maintenant un champ de multiplicateur de demande (bonus sur ce dont la cité a besoin, malus sur ce qui est saturé), même inactif au début.

**Anti-comptes secondaires.** Pas de revente de parchemins et pas de transfert d'argent avant un certain temps de jeu.

# Grades

[← Retour à la vision globale](./main.md)

## Les grades

Un grade est une ligne de configuration : un rang, un titre, un coût en lys, une condition d'accès et une liste d'avantages. Chaque grade doit apporter quelque chose de nouveau, pas seulement un chunk de plus ou une téléportation plus rapide.

**Cinq familles d'avantages.**

- Capacité : chunks du campement, tickets de contrat par jour, étals de shop, annonces à l'hôtel des ventes, crafts en file d'attente, pages de stockage (sac).
- Confort : délais de téléportation et de TPA, stèles, commandes (`/back`, `/ec`, `/craft`, `/heads`).
- Accès : rejoindre ou créer une guilde, arbre des ailes, plafond d'amélioration des ailes, donjons par niveau.
- Économie douce : tickets de contrat supplémentaires par jour, taxe réduite à l'hôtel des ventes, rachat à prix réduit des items perdus.
- Prestige : titre, couleur de chat, skins et traînées d'ailes, bannière.

Chaque avantage indique s'il s'ajoute (par exemple +5 chunks) ou s'il remplace la valeur précédente (par exemple le délai de téléportation).

**Condition d'accès (proposition).** À partir d'Explorateur, l'achat demande aussi une courte chaîne de contrats. Un grade ne s'achète donc pas uniquement avec de l'argent reçu d'un autre compte.

**Répartition proposée (exemples, à équilibrer).** Chaque grade a un thème, pour que la progression ne soit pas répétitive.

| Grade | Coût (lys) | Thème | Nouveautés |
| --- | --- | --- | --- |
| 1. Survivant | Gratuit | Débuter | Campement de 5 chunks, stock de départ de tickets de contrat par jour, 1 craft en file, 1 page de sac, téléportation 4,5 s |
| 2. Explorateur | 500 | Rejoindre les autres | Rejoindre une guilde, 1 étal de shop de campement, 1 annonce à l'hôtel des ventes, +5 chunks |
| 3. Aventurier | 2 000 | Voler | PNJ de l'arbre des ailes, bottes d'Hermès, ailes jusqu'au niveau 3, 2 crafts en file, téléportation 4 s |
| 4. Noble | 10 000 | Gagner plus | + tickets de contrat par jour, 1 stèle temporaire, 2 étals de shop, +5 chunks |
| 5. Baron | 50 000 | Bâtir | Créer une guilde, grappin, +1 page de sac, téléportation 3,5 s |
| 6. Seigneur | 250 000 | Se déplacer | TPA, 2 stèles temporaires, ailes jusqu'au niveau 5, 3 crafts en file, +5 chunks |
| 7. Vicomte | 1 million | Commercer | + tickets de contrat par jour, 3 étals de shop, 3 annonces, taxe réduite à l'hôtel des ventes, PNJ de l'arbre jetpack, `/heads` |
| 8. Comte | 5 millions | S'équiper | Shulker portable, 3 stèles temporaires, assurance (rachat des items perdus à prix réduit), +1 page de sac |
| 9. Duc | 15 millions (à confirmer) | Durer | 1 stèle permanente, + tickets de contrat par jour, téléportation 2,5 s, +5 chunks |
| 10. Roi | 50 millions (à confirmer) | Maîtriser | `/craft`, `/ec`, + tickets de contrat par jour, 4 crafts en file |
| 11. Empereur | 100 millions (à confirmer) | Régner | `/back`, balise portable, 2 stèles permanentes, TPA instantané, titre et skin d'ailes exclusifs |

Le nombre de **tickets de contrat par jour** augmente avec le grade (valeurs à fixer en configuration, voir `08-contrats-et-economie.md`). Il n'y a plus de slots ni de relances de contrat. Une stèle est un point de téléportation que le joueur pose (à confirmer).

**Grades VIP (payants).** Ils donnent du confort et du cosmétique, sans effet sur l'argent, le loot ou l'XP : skins et traînées d'ailes, couleurs de chat et de panneaux, armor stands modifiables, plus de homes. Le contenu exact reste à définir. Vérifier les règles de monétisation de Mojang.

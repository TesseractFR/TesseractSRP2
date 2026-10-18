# Guildes

[← Retour à la vision globale](./main.md)

## Guildes

Une guilde est une ville sur Terre avec sa banque, ses structures et ses colonies de ressources. Le joueur qui la crée en devient le maire.

**Création.** Il faut un campement et le grade Baron, puis se placer sur un chunk libre du monde des guildes. Le coût n'est pas fixé : 10 000 lys dans le code actuel, ou des lys plus des ressources dont la moitié est rendue dans le brainstorming initial. À trancher.

**Rôles.** Maire, Adjoint, Bâtisseur, Citoyen. Un joueur appartient à une seule guilde. Quitter est refusé au maire, qui doit supprimer la guilde ou transférer le rôle.

**Ville.** Territoire permanent du monde des guildes. Une guilde peut s'installer dans l'overworld, le Nether ou l'End, et y placer ses villes et ses avant-postes, au choix des joueurs. La ville est agrandie par claim de chunks adjacents. Elle contient les tables d'artisanat, les structures, le tableau de contrats et les shops de ville.

**Colonie.** Créée par le maire avec le drapeau de la guilde, posé en monde ressource à distance du spawn et des autres colonies. Le drapeau protège environ 9 chunks réservés aux membres. La colonie est réinitialisée avec le monde ressource. Des mineurs automatiques y extraient des items selon le biome, dont des items custom. Piste : certains items ne seraient obtenables que par ces mineurs, jamais à la main, ou seraient propres aux colonies. Leur rendement reste à équilibrer. L'inventaire étant unique, il n'y a ni import ni export.

**Banque.** Les membres déposent, les rôles autorisés retirent. Revenus : ventes des shops de ville, taxes sur les contrats, dons. Dépenses : récompenses des contrats publiés, améliorations.

**Progression.**

- L'XP de guilde vient des contrats de guilde livrés par les membres. Le seuil du niveau suivant est de 1 000 × le niveau actuel.
- Chaque niveau donne un point de guilde, à dépenser dans une nouvelle structure ou une amélioration.
- Le rang de guilde (de Hameau à Capitale) dépend d'un niveau minimal et d'un coût. Il limite le nombre de chunks, la création de shops, l'ouverture au public et les plaques de téléportation.

**Structures.** Tables d'artisanat (chacune débloque ses recettes et ses tiers), shops, tableau de contrats, structures décoratives de 2 à 3 blocs. Les structures de guilde se paient en points de guilde et en items craftés.

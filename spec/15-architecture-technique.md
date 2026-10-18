# Architecture technique

[← Retour à la vision globale](./main.md)

## Structure technique

Le code suit une architecture par module : un domaine, qui porte les règles sans dépendre de Bukkit, et un adaptateur, qui branche Bukkit, Spring et la base de données. Six modules existent déjà, et six sont à créer.

&#91;embedded content: architecture · 6 modules existants, 6 à créer\]

Chaque couche s'appuie sur celle du dessous. Les modules à créer sont teintés.

**Modules existants.**

| Module | Rôle | Reste à faire pour la v1 |
| --- | --- | --- |
| commun | Joueur (grade, argent, illumination), coordonnées, téléportation, menus partagés | Règles sur les montants, monde absent |
| customItem | Items custom, rareté, qualité, comptage dans l'inventaire | Inventaire plein, retrait en quantité insuffisante |
| job | Métiers, loot, talents, modèle de mission de livraison | Achat de talents, sources « tuer » et « interagir », dépôt des missions |
| skill | Tables, recettes, crafts chronométrés, améliorations de table | Sauvegarde du butin, récupération et craft double, qualité des composants |
| territory | Campements, guildes, banque, XP | Sauvegarde des chunks, branchement à la monnaie |
| plugin | Assemblage, commandes, listeners | Migrer les listeners (drops custom, protection, chat) |

**Modules à créer.**

| Module | Rôle |
| --- | --- |
| économie | Service monétaire unique, journal des mouvements, taxes, plafonds de transfert |
| contrat | Tableau personnel, parchemins, contrats entre villes, délais, récompenses |
| marché | Shops physiques, de campement et de ville, hôtel des ventes |
| ailes | Boosts, 4 améliorations, points d'illumination (aujourd'hui dans le plugin) |
| events | Jauge communautaire, events, donjons |
| succès | Hauts faits, tuto, titres, cosmétiques |

**Principes.**

- Une seule porte pour l'argent : toute modification de solde passe par le service monétaire, avec sa raison, et laisse une trace dans le journal.
- Les modules réagissent à des événements plutôt que de s'appeler entre eux : item récolté, craft récupéré, contrat livré, argent déplacé, montée de grade, niveau de guilde, jauge pleine. Par exemple, un contrat livré donne de l'XP de guilde, remplit la jauge et fait avancer des hauts faits.
- Tout ce qui s'équilibre est en configuration, rechargeable à chaud : grades, métiers, items, recettes, modèles de contrats, events, taux.
- Chaque donnée appartient à un seul module. Les autres la lisent par des ports.

**Ordre de construction.**

1. Débloquer la base : achat de talents, sauvegarde des chunks, monnaie réelle, butin de craft sauvegardé, drops custom.
2. Économie et contrats : la boucle gagner, dépenser.
3. Grades en configuration, avec leurs conditions.
4. Guildes complètes : rangs, structures, XP, banque, contrats entre villes.
5. Marché : shops et hôtel des ventes.
6. Ailes, events, succès.

Puis une bêta d'un mois sur le tier 1.

### Entités à créer (TODO par module)

Pour chaque module à créer : les entités, les commandes et le principe. Les détails iront dans les specs.

**économie**

- Entités : `Transaction` (joueur, montant, raison, date), `CompteGuilde` (banque).
- Commandes : `/balance`, `/pay`, admin `/eco give|take`.
- Principe : seule porte pour l'argent, journal obligatoire, plafond de transfert pour les comptes neufs.
- À faire : raisons de mouvement (enum), taxes, plafonds.

**contrat**

- Entités : `ContratModele` (config), `ContratJoueur` (offre, statut, échéance), `ContratGuilde` (automatique, renouvelé, partagé ou non).
- Commandes : `/contrats` (ouvre le tableau), admin `/contrat reroll`.
- Principe : même interface au spawn et en guilde, la guilde paie plus, tirage automatique sans intervention d'un joueur.
- À faire : modèles, rythme de renouvellement, contrat de guilde individuel ou commun.

**marché**

- Entités : `Shop` (type, propriétaire, stock, prix), `AnnonceHV` (item custom, prix, vendeur).
- Commandes : `/shop`, `/hv`, `/hv vendre <prix>`.
- Principe : shop du spawn = admin shop, shops de campement et de ville distincts, hôtel des ventes limité aux items custom.
- À faire : taxes, plafonds, durée des annonces.

**ailes**

- Entités : `AilesJoueur` (niveaux des 4 améliorations, illumination).
- Commandes : `/ailes` (menu), admin `/ailes illumination give`.
- Principe : 5 boosts, régénération 30 à 60 s, 4 améliorations, rien d'autre en v1.
- À faire : coûts par niveau, sources d'illumination.

**events**

- Entités : `Jauge` (valeur, palier), `Event` (type, état, récompenses), `Donjon`.
- Commandes : `/jauge`, admin `/event start|stop`.
- Principe : jauge permanente alimentée par les contrats livrés, déclenche un event ou un donjon.
- À faire : les 2 ou 3 premiers events, récompenses.

**succès**

- Entités : `Succes` (config), `SuccesJoueur` (progression), `Titre`.
- Commandes : `/succes`, `/titre`.
- Principe : le livre des hauts faits est une interface, il ne crée aucun item.
- À faire : liste des succès, tutoriel.

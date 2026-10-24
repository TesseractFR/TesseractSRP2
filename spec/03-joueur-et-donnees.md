# Joueur et données

[← Retour à la vision globale](./main.md)

## Le joueur : modèle de données

Un joueur est identifié par son UUID. En base, une table centrale légère contient son profil, et chaque module possède ses propres tables, qui référencent le joueur par `player_uuid`. Un profil est créé à la première connexion : grade Survivant, 0 lys, 0 point d'illumination.

| Table | Clé | Contenu | Module | État |
| --- | --- | --- | --- | --- |
| t\_srp\_player | uuid | Grade, lys, titre, points d'illumination | commun | existe |
| Préférences | player\_uuid | PvP activé ou non dans le monde des guildes, animation de téléportation | commun | à créer (ou colonnes de t\_srp\_player) |
| Progression de métier | player\_uuid + nom du métier | Points de talent disponibles, niveau de chaque talent, XP et niveau | job | existe, XP et niveau à confirmer |
| Contrats | id du contrat | Acheteur d'origine (issued\_to), type, métier, matériau, quantité, qualité minimale, tier, récompense, dates d'émission et d'expiration | contrat | à créer |
| Tickets de contrat | player\_uuid | Tickets restants, jour du dernier renouvellement, nombre de contrats livrés | contrat | à créer |
| Crafts | player\_uuid + table | Craft en cours, file d'attente, butin à récupérer | skill | existe |
| Ailes | player\_uuid | Niveau des 4 améliorations, niveau global | ailes | à créer (aujourd'hui dans le plugin) |
| Hauts faits et titres | player\_uuid + id | Débloqués, progression, date | succès | à créer |
| Cosmétiques | player\_uuid + id | Possédés, équipés | succès | à créer |
| Membre de guilde | player\_uuid | Guilde, rôle | territory | existe |
| Campement | propriétaire (player\_uuid) | Nom, spawn, chunks, membres de confiance | territory | existe |
| Journal des transactions | id | Date, joueur ou guilde, montant, raison, solde après | économie | à créer |

**Pas de clé étrangère vers le métier.** Les métiers sont définis dans des fichiers de configuration, pas en base. La ligne de progression porte donc le nom du métier (un texte), sans table « métier ».

**Un contrat n'a pas de propriétaire.** Il est porté par un parchemin physique : celui qui le livre touche la récompense. La table garde seulement l'acheteur d'origine (`issued_to`). Un contrat livré ou expiré est supprimé. Les tickets de chaque joueur sont dans une table à part.

**Préférences et cosmétiques** sont de petites tables, car elles doivent survivre à un redémarrage. **Les limites anti-abus ne sont pas en base** : ce sont des compteurs en mémoire (drops par heure, transferts récents) qui décroissent avec le temps. Seules les alertes pour le staff sont écrites dans les journaux.

**Convention de code.** Ajouter une donnée joueur, c'est une table, une entité JPA, un port (interface de dépôt) et un convertisseur, dans le module concerné. Jamais dans le profil commun, et sans relation JPA entre modules : on se réfère à l'autre module par son identifiant (UUID du joueur, id de guilde).

Le grade pilote les plafonds du joueur (taille du campement, tickets de contrat par jour, étals de shop, file de craft, délais). Ils se lisent dans la configuration des grades, pas en base.

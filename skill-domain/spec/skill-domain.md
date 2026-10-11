# Spec — `skill-domain`

## Objectif

Modéliser les compétences, recettes, stations et tâches de fabrication. Le domaine expose les services utilisateur et les ports nécessaires pour lire la configuration, consulter les territoires et objets, gérer les résultats et planifier le traitement asynchrone.

## Modèle et règles

- Une `Skill` possède un nom, une structure et des tiers. Chaque `SkillTier` référence les recettes par identifiant numérique et par nom.
- Une `Recipe` associe nom, emplacement de recette, composants indexés par `IngredientSlot`, résultat, tier et durée. Les quantités sont portées par `RecipeComponent`.
- Les valeurs `RecipeName`, `Material`, `SkillName`, `Slot`, `IngredientSlot` et `Tier` encapsulent les identifiants. Les noms de recette, matériau et compétence ne peuvent pas être blancs ; slot et index d’ingrédient sont positifs ou nuls ; le tier commence à 1.
- Une station est identifiée par territoire et compétence (`StationKey`). Ses statistiques comprennent tier, qualité, réussite et réduction de durée. À sa création, le tier vaut 1 et les autres niveaux valent 0.
- Une amélioration produit de nouvelles `StationStats`/`Station` avec le niveau correspondant augmenté de 1. Les bonus actuellement calculés sont qualité = 2 % par niveau, réussite = 1,5 % et réduction de durée = 1 %.
- Le domaine contient aussi les tâches de craft, files par compétence, recettes mises en attente et caches de butin joueur, qui constituent les données de suivi d’une fabrication.

## Interfaces et dépendances

Les ports serveur fournissent les recettes/configurations, objets, stations, territoires, messages et cache de résultats ; `CraftTaskScheduler` planifie les tâches. Les services utilisateur sont `CraftingService`, `SkillService` et `StationService`.

## Critères d’acceptation

1. Les identifiants invalides indiqués ci-dessus sont rejetés à la construction.
2. Une recette conserve le lien entre chaque emplacement d’ingrédient, composant requis, résultat, tier et durée.
3. Une station nouvellement créée possède les niveaux initiaux indiqués.
4. Une amélioration d’une statistique augmente uniquement cette statistique et conserve les autres.
5. Les bonus calculés suivent les coefficients définis dans le modèle de station.

## Limites constatées

Le service appelle les ports de sauvegarde après le pas de fabrication, mais l’adaptateur JPA actuel de `SkillResultCacheRepository.save` est vide : les caches de résultat ne sont pas persistés par cette implémentation. L’adaptateur de fabrication enregistre les tâches et recettes en attente, mais l’enregistrement d’une file vide ne supprime pas les anciennes entrées; la présence de données JPA ne garantit donc pas la reprise exacte du travail après redémarrage.

Plusieurs types de tâche/recette sont de simples structures de données ; leurs règles de validation (quantités, durée, cohérence des cartes) ne sont pas uniformément imposées par les modèles. Les règles complètes de calcul d’issue du craft sont réparties dans les services et doivent être confirmées par leurs implémentations avant d’être contractualisées ici.

## Détails d’exécution de la fabrication

`CraftingService` charge les caches de butin persistés au démarrage et conserve en mémoire caches, tâches actives et files indexés par joueur et compétence. `startCraft` vérifie d’abord le tier de la station, puis la disponibilité de tous les composants à qualité `POOR`; si un seul manque, le craft s’arrête et un message est envoyé. Une fois tous validés, les ingrédients sont retirés, un bonus de station est copié dans le craft, puis la recette est démarrée ou mise en file. La quantité d’items par lot est bornée par la taille max des piles des ingrédients et du résultat via `getMaxStackSize`.

À chaque pas, le succès est tiré avec probabilité `0,75 + bonus de réussite`. En cas de succès, la qualité du composant est augmentée d’un rang avec probabilité `qualityBonus`, puis le résultat est ajouté au cache `done`; un échec ne produit pas d’item. Le nombre restant est décrémenté et tâche/cache sont sauvegardés. À fin de lot, l’ordonnanceur est annulé, la tâche active est supprimée et la prochaine recette de la file démarre.

`collectCraftResults` remet au joueur les listes `done` et `garbage`, les vide et supprime le cache persistant. `hasLootCache` signale l’une ou l’autre liste non vide. `getQueue` et `getLootCache` créent les conteneurs à la demande. `SkillService.getSkillFromStructureID` cherche la compétence associée à un identifiant de structure. `StationService` résout une station par clé ou chunk, puis retourne une station où une amélioration incrémente une statistique ciblée.

## Réserves comportementales

Dans `processCraftStep`, le premier test de quantité nulle appelle `finishTask` sans retourner ensuite; le code poursuit tout de même le pas. La qualité initiale du craft est fixée à `POOR`; les bonuses génériques du craft sont instanciés à zéro. Le cache est chargé au démarrage, mais l’implémentation de restauration des tâches/files n’est pas visible dans le constructeur actuel. Les règles de validation des quantités de recette et de durée restent limitées par les modèles et le chargeur YAML.

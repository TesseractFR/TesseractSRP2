# Divergences entre la vision générale et les specs des modules

> Comparaison des specs générales de `spec/` avec les specs présentes dans `*-domain/spec/` et `*-adapter/spec/`.
> Ce document relève les contradictions explicites et les écarts de couverture. Une fonctionnalité absente des specs de module est signalée comme **non spécifiée**, pas comme une contradiction ni comme une preuve d’absence dans le code.

## Contradictions ou différences de comportement

### Progression des grades : configuration générale, rangs codés dans le domaine

La vision indique que les grades et leurs avantages sont définis dans la configuration, et que les plafonds du joueur se lisent dans cette configuration ([grades](./04-grades.md), [joueur et données](./03-joueur-et-donnees.md)). Pourtant, `commun-domain` décrit `PlayerRank` comme référentiel de onze rangs portant directement coût, délai de téléportation, slots de missions et niveau de campement, et `buyNextRank` achète le rang suivant à son coût ([commun-domain](../commun-domain/spec/commun-domain.md)).

**À clarifier :** quelles propriétés restent codées dans `PlayerRank`, lesquelles viennent des fichiers de configuration, et comment la condition d’achat par contrats s’intègre à `buyNextRank`.

### Monnaie : service central prévu, mutations exposées dans le profil joueur

La vision pose une porte unique pour toute modification monétaire, avec raison et journal des transactions ([architecture technique](./15-architecture-technique.md), [contrats et économie](./08-contrats-et-economie.md)). `SrpPlayer` expose cependant `addMoney` et `PlayerService` expose un ajustement de solde qui persiste directement le joueur ([commun-domain](../commun-domain/spec/commun-domain.md)). En parallèle, l’adaptateur territorial injecte un `MoneyServiceImpl` provisoire dont les opérations sont des bouchons ([territory-adapter](../territory-adapter/spec/territory-adapter.md)).

**À clarifier :** faire de l’ajustement de solde une opération interne au service économique, ou documenter explicitement les cas autorisés hors de celui-ci. La vision prévoit un ledger, mais aucun module économie ni sa spec n’existe encore.

### Sources de récolte : la vision inclut tuer et interagir, l’adaptateur métier ne traite que la casse

La vision définit les sources comme une action et un bloc ou mob, avec les actions casser, tuer et interagir ([métiers, items custom et talents](./06-metiers-items-et-talents.md)). La spec de `job-adapter` indique que la configuration associe les sorties à une source de type `BLOCK` et que seul `BlockBreakListener` transmet les récoltes ([job-adapter](../job-adapter/spec/job-adapter.md)). Elle identifie elle-même les sources « tuer » et « interagir » comme reste à faire.

**À clarifier :** préciser si les specs générales décrivent la cible finale ou le périmètre v1, et formaliser les sources prévues pour la bêta.

### Résultat des crafts : taux de réussite chiffré dans le module, valeur absente de la vision

La vision prévoit un tirage de succès et une perte des ingrédients en cas d’échec, sans donner de taux de base ([artisanat](./07-artisanat.md)). La spec de `skill-domain` fixe le succès à `0,75 + bonus de réussite` par étape de craft ([skill-domain](../skill-domain/spec/skill-domain.md)). Cette valeur de 75 % est donc une règle de module absente de la vision et qui peut influer fortement sur l’économie et l’équilibrage.

**À clarifier :** confirmer si 75 % est une valeur provisoire de code, une valeur de v1 ou une règle de design, puis indiquer où elle est configurable.

### Améliorations de table : bénéfices de récupération et craft multiple annoncés, mais non réalisés dans le module

La vision compte parmi les améliorations les bénéfices de récupération de composants et de craft double ([artisanat](./07-artisanat.md)). Dans `skill-adapter`, ces statistiques apparaissent au menu mais restent à zéro et sont marquées TODO; la spec du domaine ne les inclut pas parmi les statistiques effectivement calculées ([skill-adapter](../skill-adapter/spec/skill-adapter.md), [skill-domain](../skill-domain/spec/skill-domain.md)).

**À clarifier :** marquer ces bonus comme hors périmètre bêta ou les ajouter au contrat fonctionnel du module avant de considérer les améliorations de table comme couvertes.

### Progression des talents : achat attendu par la vision, achat inopérant dans le domaine

La vision dépend du déblocage par talents pour l’accès aux items de palier supérieur ([métiers, items custom et talents](./06-metiers-items-et-talents.md)). Or `JobPlayerProgressionService.upgradeSkill` retourne toujours `false`; l’achat effectif et sa persistance ne sont pas implémentés ([job-domain](../job-domain/spec/job-domain.md), [job-adapter](../job-adapter/spec/job-adapter.md)). La spec générale le classe bien parmi les travaux bloquants, mais décrit aussi la progression comme une composante de la boucle cible.

**À clarifier :** conserver explicitement ce statut bloquant dans les critères v1 et ne pas présenter les déblocages de paliers comme disponibles avant l’implémentation.

## Concepts généraux sans spec de module correspondante

Les points ci-dessous sont des écarts de couverture documentaire. Les specs générales les décrivent, mais les modules prévus n’ont pas encore de spec dédiée dans le dépôt.

| Concept général | Référence générale | État de la documentation module |
|---|---|---|
| Contrats de joueur et de guilde, renouvellement, livraison, récompenses et expiration | [Contrats et économie](./08-contrats-et-economie.md) | Aucun module `contrat` ; seuls les modèles de `JobMission` (mission de livraison métier) sont décrits dans [job-domain](../job-domain/spec/job-domain.md). Leur relation avec les contrats généraux n’est pas définie. |
| Journal monétaire, plafonds et transferts joueur à joueur | [Architecture technique](./15-architecture-technique.md), [contrats et économie](./08-contrats-et-economie.md) | Aucun module `économie`; `MoneyServiceImpl` territorial est documenté comme bouchon dans [territory-adapter](../territory-adapter/spec/territory-adapter.md). |
| Boutiques de campement et de ville, shop du spawn et hôtel des ventes | [Boutiques et hôtel des ventes](./13-boutiques-et-hotel-des-ventes.md) | Aucun module `marché`. |
| Boosts et améliorations des ailes, illumination | [Ailes](./12-ailes.md) | Aucun module `ailes`; la vision précise que l’implémentation actuelle est encore dans le plugin. |
| Jauge communautaire, événements et donjons | [Events et jauge](./11-events-et-jauge.md) | Aucun module `events`. |
| Succès, titres et cosmétiques | [Joueur et données](./03-joueur-et-donnees.md), [campement](./05-campement.md) | Aucun module `succès`. |
| Anti-abus : plafonds de drops, contrôle des fermes et seuils d’alerte | [Anti-abus](./14-anti-abus.md) | Aucun contrat de module dédié ; les specs présentes ne décrivent pas ces mécanismes. |
| Colonies, mineurs automatiques et avant-postes | [Guildes](./09-guildes.md), [rivalité et recrutement](./10-rivalite-et-recrutement.md) | `territory-domain` couvre guildes et territoires, mais sa spec ne décrit pas colonies, mineurs ni compétition d’avant-postes ([territory-domain](../territory-domain/spec/territory-domain.md)). |

## Points à harmoniser dans les specs existantes

- **Qualité et rareté :** les specs `customItem-domain` confirment que la rareté du matériau et la qualité individuelle sont deux notions distinctes, en accord avec la vision. En revanche, le modèle décrit ne porte pas de palier d’item ; les paliers qui relient récolte, talents et tiers de craft restent à documenter ([customItem-domain](../customItem-domain/spec/customItem-domain.md), [métiers, items custom et talents](./06-metiers-items-et-talents.md), [artisanat](./07-artisanat.md)).
- **Persistance du butin de craft :** la vision demande que le butin soit sauvegardé. La spec d’adaptateur indique que `SkillResultCacheRepositoryJpaAdapter.save` est vide, donc cette garantie n’est pas satisfaite actuellement ([skill-adapter](../skill-adapter/spec/skill-adapter.md), [architecture technique](./15-architecture-technique.md)).
- **Territoires :** la vision présente les chunks revendiqués et les campements comme des données persistées. La spec de l’adaptateur territorial relève que la sauvegarde/recherche de chunks et la recherche d’un campement par joueur ou chunk sont encore des bouchons ([territory-adapter](../territory-adapter/spec/territory-adapter.md), [joueur et données](./03-joueur-et-donnees.md)).
- **Guildes et monnaie :** le coût actuel de création de guilde est de 10 000 dans `territory-domain`, ce qui correspond à une des options encore ouvertes dans la vision. La vision doit conserver cette décision comme non tranchée tant que le coût n’est pas validé ([territory-domain](../territory-domain/spec/territory-domain.md), [guildes](./09-guildes.md), [décisions ouvertes et TODO](./16-decisions-et-todo.md)).

## Décisions proposées pour fermer les écarts

1. Étiqueter chaque règle générale comme **décidée**, **proposée**, **hors v1** ou **à confirmer**; les modules peuvent alors distinguer règle cible et comportement actuel.
2. Définir une source de vérité pour les rangs et la monnaie, puis aligner `commun-domain` et la future spec économie dessus.
3. Ajouter les specs de modules manquantes ou marquer explicitement leurs concepts comme non spécifiés à ce stade.
4. Ajouter dans les specs concernées les règles chiffrées qui affectent l’équilibrage, notamment le taux de réussite du craft et les mécanismes de progression de talents.

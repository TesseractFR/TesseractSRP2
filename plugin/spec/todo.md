# Reste à faire — `plugin`

Cette liste rassemble les TODO explicites et les opérations manifestement incomplètes du module d’assemblage Bukkit/Spring.

## Fonctionnalités encore en attente de migration

- [ ] Migrer/réimplémenter le rendu de chat et le préfixe de rang dans `PlayerChatPrefixListener`.
- [ ] Migrer/réimplémenter le traitement d’utilisation des bâtons d’annexion dans `AnnexionStickUseListener`.
- [ ] Migrer/réimplémenter les drops d’objets personnalisés lors de casse de bloc et de mort d’entité dans `CustomItemDropListener`.
- [ ] Migrer/réimplémenter la protection des objets personnalisés lors de pose, consommation et fabrication dans `CustomItemProtectionListener`.
- [ ] Migrer/réimplémenter le listener de récolte de métiers et sa planification dans `CustomItemHarvestListener`.
- [ ] Migrer/réimplémenter les réactions aux événements de progression métier dans `JobsEventListener`.
- [ ] Migrer/réimplémenter la création/initialisation du ledger au chargement serveur dans `LedgerCreationListener`.
- [ ] Migrer/réimplémenter la notification de distribution d’un bâton d’annexion dans `AnnexionStickGivenListener`.

## Planificateur Spring/Bukkit

`BukkitTaskScheduler` n’implémente actuellement qu’une partie de `TaskScheduler`.

- [ ] Implémenter `schedule(Runnable, Trigger)` et `schedule(Runnable, Instant)`.
- [ ] Implémenter les deux variantes de `scheduleWithFixedDelay` et `scheduleAtFixedRate` avec une date de départ.
- [ ] Remplacer les `UnsupportedOperationException` de `ScheduledFuture.get()` par un contrat de complétion/erreur cohérent, ou documenter pourquoi ces opérations ne sont pas prises en charge.
- [ ] Corriger `getDelay` : le calcul mélange `getEpochSecond()` et `toEpochMilli()`, et doit retourner un délai positif cohérent avec l’unité demandée.
- [ ] Convertir les périodes en ticks avec précision suffisante (actuellement `period.getSeconds() * 20`, qui tronque les sous-secondes) et valider les périodes nulles/négatives.
- [ ] Définir la sémantique de `isDone`, du résultat, de l’exception et de l’annulation pour une tâche périodique Bukkit.

## Chargement de classes et ressources

- [ ] Vérifier le modèle parent/enfants de `CompoundClassLoader` afin de respecter la délégation standard et les contraintes de sécurité des packages Java.
- [ ] Définir le comportement de `getResource(s)` en cas d’échec d’un chargeur enfant; les retours `null` sont normaux pour une ressource absente, mais les erreurs d’énumération sont actuellement ignorées silencieusement.
- [ ] Vérifier les doublons de ressources/class loaders entre le plugin, son parent et TesseractLib.

## Démarrage, configuration et persistance

- [ ] Rendre explicite le contrôle `checkWorldsExist` : il appelle `Bukkit.getWorld` sans signaler les mondes SRP absents.
- [ ] Définir une stratégie de copie de configuration sûre et versionnée. Le mode `forceUpdateConfig` supprime les fichiers existants avant copie et peut écraser les personnalisations serveur.
- [ ] Garantir la fermeture propre du contexte Spring et l’arrêt des tâches dans `onDisable`.
- [ ] Confirmer que les paramètres Hibernate/JPA et les propriétés SQL/logging configurés sont ceux réellement attendus en production.
- [ ] Vérifier les transactions des commandes staff et la cohérence des écritures entre service joueur, équipement, guilde et ledger.

## Couverture fonctionnelle

- [ ] Compléter l’implémentation du profil de progression métier restée dans le plugin ou migrer ses fonctionnalités vers `job-domain`; des méthodes de disponibilité/achat de compétence retournent actuellement `false` dans cette ancienne implémentation.
- [ ] Examiner les chemins `false`/`null` dans le service et les menus d’élytra et déterminer s’ils représentent des cas normaux ou des fonctionnalités manquantes.
- [ ] Clarifier la coexistence des anciennes classes métier/objets de ce module avec les nouveaux modules `job-*`, `customItem-*`, `territory-*` et `commun-domain`, puis retirer ou adapter les implémentations redondantes.
- [ ] Ajouter une procédure de validation du démarrage couvrant l’enregistrement des commandes, listeners, serializers, titres, mondes et configurations.

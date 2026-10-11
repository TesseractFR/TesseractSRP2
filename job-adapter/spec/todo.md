# Reste à faire — `job-adapter`

## Implémentation à compléter

- [ ] Implémenter l’achat et la persistance des améliorations de talents : `JobPlayerProgressionService.upgradeSkill` retourne actuellement toujours `false`.
- [ ] Faire respecter les prérequis lors de l’achat, et distinguer talent indisponible, niveau maximal et points insuffisants.
- [ ] Décider si les fichiers métier et arbres doivent être rechargés à chaud; corriger le pseudo-cache de `JobSkillTreeYamlRepository` (`&& false`) si le cache est voulu.
- [ ] Valider les fichiers YAML (champs requis, enum `Bonus`, matériaux, nombre de prix/valeurs par niveau) avant de construire les modèles.
- [ ] Vérifier la conversion et la persistance JPA des clés composées de progression ainsi que les champs de mission.
- [ ] Vérifier si le listener de casse doit ignorer les événements annulés et les mondes/blocs non concernés avant d’appeler le domaine.

## Interface

- [ ] Afficher des messages d’erreur ou de succès lors d’un clic de talent selon le résultat réel de l’achat.

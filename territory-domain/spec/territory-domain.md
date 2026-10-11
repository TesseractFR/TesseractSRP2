# Spec — `territory-domain`

## Objectif

Gérer les territoires associés aux chunks, leurs règles d’interaction et deux types d’espaces détenus : les campements personnels et les guildes. Le domaine couvre les revendications, adhésions, permissions, points de spawn, progression et événements associés.

## Modèle et règles

- `Territory` et `TerritoryChunk` représentent le propriétaire territorial et le chunk revendiqué. Les implémentations distinguent les chunks de campement et de guilde.
- `TerritoryService` fournit les opérations partagées de consultation, de revendication/libération et de décision d’interaction. Les services vérifient le monde attendu par sous-domaine.
- Le campement appartient à un joueur. Sa création vérifie la disponibilité du chunk, récupère le niveau de camp, crée le campement et revendique ses chunks initiaux. Le campement expose son spawn et peut être supprimé ou changer de niveau.
- La guilde possède un chef, des membres avec rôles, des invitations et demandes d’adhésion, des chunks, des points de spawn privé/visiteur, une banque, de l’expérience, un niveau et un rang.
- Créer une guilde vérifie le rang du joueur, son argent, l’unicité du nom et la disponibilité du territoire ; le coût actuel est de 10 000. À succès, le coût est payé, les chunks initiaux sont revendiqués et la guilde est sauvegardée.
- Les invitations et adhésions vérifient l’appartenance existante et les droits de l’émetteur. Les événements d’invitation et de montée de niveau sont publiés via `EventPublisher`.
- Quitter une guilde est refusé au chef, qui doit supprimer la guilde. Les expulsions ne peuvent pas cibler le chef.
- Les dépôts bancaires exigent que le joueur soit membre et possède les fonds. Les retraits sont réservés aux rôles autorisés et limités au solde disponible.
- L’expérience ajoutée ne peut pas être négative. Le seuil du niveau suivant est `1 000 × niveau courant` ; au franchissement, le niveau monte et l’événement correspondant est publié. Le rang de guilde dépend d’un niveau minimal et d’un coût définis par `GuildRank`.
- Les conteneurs de confiance et de spawn représentent les politiques de confiance et les emplacements de spawn par défaut ou personnalisés.

## Interfaces et dépendances

Les dépôts serveur gèrent campements, guildes, joueurs et chunks ; `TerritoryBorderTaskScheduler` permet l’actualisation des bordures. Les services utilisateur incluent les services territoriaux, de campement, de guilde et leurs services de bordure. La monnaie et les événements sont délégués à `MoneyService` et `EventPublisher`.

## Critères d’acceptation

1. Une opération territoriale est limitée au monde prévu par le service concerné.
2. Une création de campement ou guilde n’écrit pas de territoire si ses contrôles préalables échouent.
3. Les adhésions maintiennent un joueur dans une seule guilde selon les contrôles du service ; le rôle et l’identité du chef restent cohérents lors d’un transfert de leadership.
4. Un dépôt bancaire met à jour les soldes via le port monnaie et ajoute la somme à la banque de guilde.
5. Une montée de niveau consomme le seuil d’expérience et publie l’événement de niveau.
6. Une revendication ou libération réussie persiste le changement et publie l’événement de chunk correspondant.

## Limites constatées

Le retrait bancaire appelle actuellement `Guild.addMoney(amount)` après le transfert vers le joueur, ce qui augmente le solde de guilde au lieu de le diminuer ; cette anomalie doit être corrigée avant de considérer les retraits conformes à la règle métier attendue. Les services et modèles ne garantissent pas uniformément les montants non positifs des opérations bancaires, les seuils de progression au cas de gains couvrant plusieurs niveaux, ou la cohérence transactionnelle entre le service monnaie et le dépôt de territoire.

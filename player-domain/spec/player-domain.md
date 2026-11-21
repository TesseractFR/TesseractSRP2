# Spec — `player-domain`

## Objectif

Porter le profil SRP du joueur : grade, solde d’argent, points d’illumination et titre affiché, ainsi que les cas d’usage qui les modifient. Le domaine dépend uniquement de `common-domain` (`PlayerId`, `NotEnoughMoneyException`) et de la classe `Title` de Tesseract Core pour le titre associé à chaque grade. Il ne dépend ni de Bukkit, ni de Spring, ni de JPA.

## Modèle et règles

- `SrpPlayer` est identifié par un `PlayerId` et porte un grade, un solde d’argent, des points d’illumination et un identifiant de titre.
- Un joueur créé avec son seul identifiant reçoit le grade `Survivant`, le titre associé et des soldes à zéro.
- `addMoney` et `addIlluminationPoints` acceptent les variations positives ou négatives, mais rejettent toute opération qui ferait passer le solde correspondant sous zéro (`NotEnoughMoneyException`, `NotEnoughIlluminationPointsException`). La valeur mise à jour est retournée.
- `buyNextRank` achète le grade suivant au coût défini par ce grade. L’opération retourne `false` si le grade est déjà maximal ou si le joueur n’a pas les fonds ; sinon le coût est débité et le grade est modifié.
- `PlayerRank` ordonne onze grades, de `Survivant` à `Empereur`. Chaque grade associe un titre, un coût d’accès, un délai de téléportation et un niveau de campement. `next()` retourne le grade suivant ou `null` au sommet.
- `PlayerRankUpEvent` est l’événement de montée de grade : il transporte l’UUID du joueur et le nouveau grade.

## Cas d’usage

Les opérations sur le joueur sont réparties en quatre services, un par cas d’usage, qui prennent tous un `PlayerId` :

- `SrpPlayerProfileService` lit et sauvegarde le joueur via `PlayerRepository`. Une lecture absente renvoie un joueur neuf avec grade survivant, titre associé et soldes à zéro, sans sauvegarde immédiate.
- `PlayerRankService` :
  - `changeRank` synchronise le titre au grade, persiste et publie `PlayerRankUpEvent`, seulement si le grade change.
  - `buyNextRank` appelle la logique du modèle, synchronise le titre et persiste uniquement en cas d’achat réussi. Il journalise alors le paiement du grade acheté via `PlayerLedgerPort` et publie `PlayerRankUpEvent` avec ce nouveau grade.
- `PlayerMoneyService` et `PlayerIlluminationService` : les ajustements d’argent et de points d’illumination refusent un solde final négatif (retour `false`) et sauvegardent toute modification acceptée ; l’ajustement peut être positif, nul ou négatif. `adjustMoney` ne journalise rien : c’est à l’appelant de tracer le mouvement s’il le faut. `adjustMoneyAsStaff` applique la même règle et journalise l’ajustement accepté via `PlayerLedgerPort`.

## Ports

| Port | Rôle | Implémentation |
| --- | --- | --- |
| `PlayerRepository` (serverside) | lecture (`findById`, vide si le joueur n’a rien enregistré) et sauvegarde du profil | `JpaPlayerRepository` dans `player-adapter` |
| `PlayerLedgerPort` (serverside) | journal des achats de grade et des ajustements de solde du staff | `MoneyLedgerPlayerLedgerPort` dans `plugin`, au-dessus de `MoneyLedgerService` |
| `EventPublisher` (de `common-domain`) | publication de `PlayerRankUpEvent` | `DomainEventPublisher` dans `common-adapter` |

## Critères d’acceptation

1. Un joueur neuf possède les valeurs initiales décrites ci-dessus.
2. Un débit d’argent ou de points d’illumination qui laisserait un solde négatif échoue et ne modifie pas le solde.
3. L’achat d’un grade disponible débite exactement son coût ; l’achat impossible ne modifie ni le grade ni le solde.
4. Après un achat réussi, le journal et `PlayerRankUpEvent` reçoivent le grade acheté, pas l’ancien.
5. Un ajustement de solde du staff refusé n’est pas journalisé.

## Limites constatées

La validation des montants négatifs en entrée, les bornes supérieures de solde et les règles de cohérence titre/grade ne sont pas spécifiées par les modèles actuels. Les services du domaine n’ouvrent pas de transaction : la sauvegarde du joueur et l’écriture du journal ne sont pas atomiques.

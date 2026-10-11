# Spécification — `customItem-domain`

## Périmètre

Ce module décrit les objets personnalisés et expose un service qui unifie les opérations sur matériaux personnalisés et matériaux Bukkit ordinaires. Les interfaces de dépôt séparent le modèle de la plateforme : elles couvrent les définitions de matériaux, la représentation d’objet, et la lecture/mutation des inventaires.

## Modèle

- `MaterialName` encapsule le nom du matériau.
- `CustomMaterial` regroupe le nom logique, le nom d’affichage, le tag d’objet et la rareté (`Rarity`).
- `CustomItem` représente un matériau personnalisé combiné à une qualité commune `Quality`. La qualité fait partie de l’identité utilisée pour compter ou retirer un objet personnalisé.
- `Rarity` est le référentiel des raretés d’un matériau (distinct de la qualité individuelle d’un item).

## Service et règles de routage

`CustomItemService` reçoit `CustumMaterialRepository`, `InventoryRepository` et `CustomItemRepository`. Son constructeur appelle immédiatement `loadAll`.

`loadAll` vide la map interne, lit l’ensemble courant des définitions depuis le dépôt, puis l’indexe par `MaterialName`. Chaque nouvelle invocation remplace donc le cache précédent; en cas de noms dupliqués, la dernière insertion l’emporte.

Les méthodes de service routent selon la présence du nom dans cette map :

| Opération | Matériau personnalisé connu | Nom non déclaré |
|---|---|---|
| `getItemQuantity(player, name, quality)` | Recherche le `CustomItem(material, quality)` dans l’inventaire | Compte le matériau vanilla par nom; la qualité est ignorée |
| `addItem(player, name, quality, amount)` | Ajoute le `CustomItem` de qualité demandée | Ajoute le matériau vanilla |
| `removeItem(player, name, quality, amount)` | Retire le `CustomItem` de qualité demandée | Retire le matériau vanilla |
| `getItemStack(name)` | Demande au dépôt de construire l’item du `CustomMaterial` | Demande au dépôt de construire le matériau vanilla |

`isCustomMaterial` et `getCustomMaterial` interrogent le cache. `getCustomMaterials` retourne actuellement la map modifiable elle-même, pas une copie immuable.

## Ports

- `CustumMaterialRepository.findAll()` fournit les définitions disponibles (le nom d’interface conserve cette orthographe historique).
- `InventoryRepository` expose les quantités et opérations d’ajout/retrait distinctes pour objets `CustomItem` et matériaux vanilla.
- `CustomItemRepository` construit les `ItemStack` personnalisés ou vanilla.

## Limites

Le service ne valide pas les quantités positives, ne vérifie pas le résultat des dépôts de retrait, ni ne protège le cache exposé. La qualité est sans effet pour les matériaux vanilla. Le comportement quand un nom personnalisé est déclaré mais ne peut pas être matérialisé dépend des implémentations des dépôts.

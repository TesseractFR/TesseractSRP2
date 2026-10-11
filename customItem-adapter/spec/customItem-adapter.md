# Spécification — `customItem-adapter`

## Périmètre

Ce module implémente les ports de `customItem-domain` avec Bukkit, ItemAdder et le fichier de configuration du serveur. `CustomItemServiceProducer` expose le service domaine à Spring avec les dépôts de ce module.

## Chargement des matériaux

`FileSystemCustumMaterialRepository` lit `plugins/Tesseract/customitem.yml`. Si le fichier manque, il retourne un ensemble vide. Sinon, chaque clé racine disposant d’une section devient un `CustomMaterial` : la clé est mise en majuscules pour `MaterialName`, et les champs `display_name`, `item_tag`, `rarity` construisent respectivement le libellé (également `MaterialName` dans le modèle), le tag et l’enum `Rarity`. Une rareté inconnue ou une configuration incohérente peut provoquer une erreur pendant le chargement.

## Création et identification d’items

`ItemAdderGateway` utilise `CustomStack.getInstance(namespaceId)` pour charger un item ItemAdder. Si aucun stack ne correspond, il lève `IllegalArgumentException`. Sa méthode `isCustomItem` délègue à `CustomStack.byItemStack`.

`CustomItemstackMapper` implémente `CustomItemRepository` :

- Pour un `CustomMaterial`, charge dans ItemAdder l’identifiant contenu dans `itemTag`.
- Pour un `MaterialName` vanilla, utilise `Material.getMaterial`; un matériau Bukkit invalide retourne `null`, sinon crée un `ItemStack`.
- Pour un `CustomItem`, ajoute au `PersistentDataContainer` les clés namespacées `customMaterial` et `quality`, puis ajoute une ligne de lore `Quality: <qualité>` à l’item ItemAdder.
- `isCustom` classe comme personnalisé un item portant la clé PDC `customMaterial`.
- `getCustomItemData` retourne le couple nom/qualité lu dans les données persistantes, ou `null` si la clé de matériau manque. Une qualité manquante ou invalide échoue lors de `Quality.valueOf`.

## Inventaires Bukkit

`BukkitInventoryRepository` récupère le joueur connecté par UUID; l’absence du joueur déclenche `IllegalArgumentException("Player not found")`.

- Le comptage personnalisé parcourt l’inventaire du joueur et additionne les piles `isSimilar` à l’item de référence (matériau et qualité compris par les métadonnées).
- Le comptage vanilla compare le type Bukkit correspondant au nom et exclut les items portant le marqueur `customMaterial`, pour ne pas les compter comme matériaux vanilla.
- L’ajout vanilla ou personnalisé divise la quantité en piles ne dépassant pas la taille maximale de l’item et appelle `addItem` pour chaque pile. Les surplus retournés par Bukkit ne sont pas traités explicitement.
- Le retrait parcourt les slots, réduit les piles similaires jusqu’à la quantité demandée ou épuisement de l’inventaire, puis vide les slots dont la quantité atteint zéro. Aucune erreur n’est signalée si la quantité disponible est insuffisante.

## Limites

Le chemin du YAML est relatif au répertoire de travail du serveur. Les appels d’inventaire nécessitent un joueur connecté et doivent respecter les contraintes de thread Bukkit. Les quantités négatives/non positives ne sont pas validées uniformément. Les items ItemAdder manquants et les noms Bukkit invalides peuvent produire des exceptions ou des valeurs nulles selon la méthode concernée.

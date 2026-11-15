# Normes de développement

Ce document fixe les règles à appliquer sur tout nouveau code et sur tout code modifié. Le code existant qui ne les respecte pas est corrigé quand on le touche, pas dans un grand ménage à part.

Les trois principes de base :

1. **SOLID** : chaque classe a une seule raison de changer, et le domaine ne dépend d'aucune technique.
2. **Des noms explicites** : on doit comprendre ce que fait une classe ou une méthode sans l'ouvrir.
3. **`Optional<T>` plutôt que `null`** : une méthode qui peut ne rien renvoyer le dit dans sa signature.

---

## 1. Langue

| Élément | Langue |
| --- | --- |
| Classes, méthodes, variables, packages | Anglais |
| Commentaires, Javadoc | Français |
| Messages de commit | Français |
| Messages affichés aux joueurs | Français |

Les noms de concepts métier déjà établis dans le code restent tels quels, même en français (`Campement`, `Annexion`). On ne les traduit pas, et on ne crée pas de doublon anglais.

---

## 2. Architecture

Le projet suit une architecture hexagonale. Chaque fonctionnalité est un couple de modules :

```
<feature>-domain    règles métier, sans Bukkit, Spring ni JPA
<feature>-adapter   branche le domaine sur Bukkit, Spring, la base de données et la config
plugin              assemblage, démarrage
```

### 2.1 Packages du domaine

```
onl.tesseract.srp.<feature>.domain
├── model              agrégats, value objects, enums
│   ├── event          événements du domaine
│   └── exception      exceptions métier
└── port
    ├── userside       services appelés par les adapters (commandes, menus, listeners)
    └── serverside     interfaces implémentées par les adapters (persistance, Bukkit, config)
```

### 2.2 Règles de dépendance

- **Un domaine ne dépend que de `common-domain`.** Il n'importe jamais `org.bukkit`, `org.springframework`, `jakarta.persistence`, ni un autre `*-domain`.
- **Un adapter peut dépendre du service (port userside) d'un autre domaine.** Exemple : `skill-adapter` utilise un service de `customItem-domain`. C'est l'adapter qui fait le lien entre deux domaines, jamais le domaine lui-même.
- **Un adapter n'utilise jamais le repository (port serverside) d'un autre domaine.** Chaque donnée appartient à un seul module, et les autres passent par son service.
- **Les adapters userside restent minces.** Une commande, un menu ou un listener traduit l'entrée, appelle un service, puis affiche le résultat. Aucune règle métier n'y vit.

### 2.3 SOLID appliqué au projet

| Principe | Ce qu'on attend concrètement |
| --- | --- |
| **S**: responsabilité unique | Un service par agrégat (`GuildService`, `CampementService`). Quand un service mélange plusieurs sujets, on le découpe par responsabilité : `GuildMembershipService`, `GuildBankService`. Un listener ne traite qu'un type de sujet. |
| **O**: ouvert/fermé | On ajoute un comportement par une nouvelle implémentation ou une entrée de config, pas par un `switch` de plus dans une classe existante. Ce qui s'équilibre (taux, prix, niveaux) est en configuration. |
| **L**: substitution de Liskov | Toute implémentation d'un port respecte son contrat, y compris sur `Optional` et les exceptions. Si le port dit `find…` renvoie `Optional.empty()`, l'adapter ne lève pas d'exception à la place. |
| **I**: ségrégation des interfaces | Des ports petits et ciblés. On ne crée pas un `PlayerPort` fourre-tout, mais `TeleportationPort` ou `PlayerInventoryPort`. |
| **D**: inversion des dépendances | Le domaine définit ses ports serverside, et les adapters les implémentent. Les dépendances sont injectées par le constructeur, jamais par `@Autowired` sur un champ. |

---

## 3. Nommage

### 3.1 Règle générale

Un nom dit **ce que fait** l'élément, pas comment il le fait. Pas d'abréviation (`mgr`, `cfg`, `tmp`, `p` pour un joueur), sauf `id`. Un nom long et clair vaut mieux qu'un nom court et ambigu.

```java
// Non
void handle(Player p, int a);
class GuildUtils { }

// Oui
void depositToGuildBank(PlayerId depositor, MoneyValue amount);
class GuildRankPermissionChecker { }
```

Les classes fourre-tout `*Utils` et `*Helper` sont à éviter : leur nom ne dit rien de leur responsabilité.

### 3.2 Classes

| Rôle | Convention | Exemple |
| --- | --- | --- |
| Agrégat, entité métier | Nom métier | `Guild`, `Campement` |
| Value object | Nom métier, `record` | `PlayerId`, `MoneyValue` |
| Service (port userside) | `<Agrégat>Service` ou `<Agrégat><Responsabilité>Service` | `GuildService`, `GuildBankService` |
| Port serverside de persistance | `<Agrégat>Repository` | `CraftingRepository` |
| Autre port serverside | `<Capacité>Port` | `TeleportationPort` |
| Implémentation d'un port | `<Techno><Port>`, **jamais `Impl`** | `JpaCraftingRepository`, `YamlMoneyRepository`, `BukkitTeleportationPort` |
| Repository Spring Data | `<Agrégat>JpaRepository` | `CraftingJpaRepository` |
| Entité JPA | `<Agrégat>Entity` | `CraftingEntity` |
| Mapper domaine ↔ technique | `<Agrégat>Mapper` | `CraftingMapper` |
| Événement | `<Agrégat><ActionAuPassé>Event` | `PlayerRankUpEvent`, `GuildLevelUpEvent` |
| Exception métier | `<Problème>Exception` | `NotEnoughMoneyException` |
| Commande Bukkit | `<Sujet>Command` | `GuildCommand` |
| Menu | `<Sujet>Menu` | `GuildMembersMenu` |
| Listener | `<Sujet><Événement>Listener` | `CampementProtectionListener` |

Exemple complet pour un même agrégat :

```
port      : CraftingRepository
adapter   : JpaCraftingRepository
Spring    : CraftingJpaRepository (extends JpaRepository)
entité    : CraftingEntity
mapper    : CraftingMapper
```

### 3.3 Méthodes

Une méthode commence par un verbe. Le verbe indique le contrat :

| Préfixe | Retour | Contrat |
| --- | --- | --- |
| `find…` | `Optional<T>` | Peut ne rien trouver |
| `findAll…` | `List<T>` / `Set<T>` | Peut renvoyer une collection vide, jamais `null` |
| `get…` | `T` | Ne renvoie jamais vide. Lève une exception métier si l'objet manque |
| `exists…` | `boolean` | Test d'existence |
| `is…` / `has…` / `can…` | `boolean` | Test d'état ou de droit |
| `create…` / `add…` / `remove…` / `update…` | — | Modifie l'état |
| `to…` | `T` | Conversion (mappers) |

```java
Optional<Guild> findByMember(PlayerId memberId);
Guild getById(GuildId id);                 // lève GuildNotFoundException
List<Guild> findAllByLevel(int level);
boolean existsByName(String name);
boolean canInvite(PlayerId inviterId);
```

Les getters de champ (`getName()`, `getLevel()`) gardent la forme `get…` habituelle : ils ne sont jamais vides par construction.

Les méthodes métier portent le nom du geste métier, pas celui de la technique : `guild.promote(member)` plutôt que `guild.setRank(member, rank + 1)`.

---

## 4. `Optional` et `null`

### 4.1 Règles

1. **Une méthode qui peut ne rien renvoyer renvoie `Optional<T>`.** Elle ne renvoie jamais `null`.
2. **`Optional` est réservé aux types de retour.** On ne l'utilise ni en paramètre, ni en champ, ni dans une collection.
3. **Une collection n'est jamais `null` et jamais `Optional`.** On renvoie une collection vide.
4. **Un paramètre facultatif** se traite par une surcharge de méthode, pas par un `null` ou un `Optional`.
5. **À l'intérieur d'une méthode**, une variable locale `null` est tolérée si elle ne sort pas de la méthode.
6. **À la frontière avec une API externe** (Bukkit, Spring Data, config) qui renvoie `null`, on enveloppe le résultat immédiatement : `Optional.ofNullable(Bukkit.getPlayer(uuid))`.

### 4.2 Utilisation

```java
// Non
Optional<Guild> guild = guildRepository.findByMember(playerId);
if (guild.isPresent()) {
    guild.get().addXp(amount);
}

// Oui
guildRepository.findByMember(playerId)
        .ifPresent(guild -> guild.addXp(amount));

// Absence anormale : on transforme en exception métier
Guild guild = guildRepository.findByMember(playerId)
        .orElseThrow(() -> new PlayerNotInGuildException(playerId));
```

- `Optional.get()` sans vérification est interdit. On utilise `orElseThrow`, `orElse`, `map` ou `ifPresent`.
- `orElse(null)` est interdit hors frontière avec une API externe qui attend `null`.

---

## 5. Erreurs métier

- **Optional exprime « absent », l'exception exprime « interdit » ou « impossible ».** Un joueur sans guilde : `Optional.empty()`. Un joueur qui paie sans assez d'argent : `NotEnoughMoneyException`.
- Les exceptions métier vivent dans `domain/model/exception`, héritent de `RuntimeException` et portent un nom qui décrit le problème.
- Une exception métier transporte les données utiles au diagnostic (identifiant, montant), pas seulement un texte.
- Les adapters userside (commandes, menus) attrapent les exceptions métier et les traduisent en message pour le joueur. Le domaine ne formate jamais de message pour le joueur.
- On n'attrape jamais `Exception` ou `RuntimeException` de façon générique pour l'ignorer.

---

## 6. Modèles du domaine

- **Les value objects sont des `record` immuables** (`PlayerId`, `MoneyValue`, `Coordinate`). Leur constructeur compact valide les invariants :

  ```java
  public record MoneyValue(long amount) {
      public MoneyValue {
          if (amount < 0) {
              throw new IllegalArgumentException("Un montant ne peut pas être négatif : " + amount);
          }
      }
  }
  ```

- **On préfère un value object à un type primitif** pour les identifiants et les grandeurs métier : `PlayerId` plutôt que `UUID`, `MoneyValue` plutôt que `long`.
- **Les agrégats sont mutables, mais seulement par des méthodes métier.** Pas de setter public qui permettrait de contourner une règle. Les collections exposées sont non modifiables (`List.copyOf`, `Collections.unmodifiableList`).
- Le modèle du domaine n'est jamais une entité JPA. La conversion passe par un mapper dans l'adapter.

---

## 7. Lombok

Lombok est autorisé sans restriction. Les règles des sections précédentes restent prioritaires. Par exemple, un `@Setter` sur un agrégat du domaine contourne la règle des méthodes métier (section 6).

---

## 8. Tests

- **Toute règle métier du domaine a un test unitaire** JUnit 5. Les ports serverside sont mockés avec Mockito.
- Les tests des adapters sont encouragés, pas obligatoires.
- Le nom d'un test décrit le comportement attendu :

  ```java
  @Test
  void depositToGuildBank_throwsWhenPlayerHasNotEnoughMoney() { }
  ```

- Un test suit la structure *given / when / then*.

---

## 9. Commentaires

- Un bon nom remplace un commentaire. On commente le **pourquoi** (règle métier, contrainte, choix non évident), jamais le **quoi**.
- La Javadoc est attendue sur les ports (userside et serverside), car elle décrit leur contrat : ce qui est renvoyé quand rien n'est trouvé, et les exceptions possibles.
- Pas de code commenté laissé en place : l'historique Git le conserve.

---

## 10. Checklist de revue

- [ ] Le domaine n'importe ni Bukkit, ni Spring, ni JPA, ni un autre domaine que `common-domain`.
- [ ] Aucun adapter n'utilise le repository d'un autre domaine.
- [ ] Les noms de classes et de méthodes disent ce qu'elles font, sans abréviation.
- [ ] Les implémentations de ports suivent `<Techno><Port>`, sans `Impl`.
- [ ] Aucune méthode ne renvoie `null` : `Optional` pour un objet, une collection vide pour une liste.
- [ ] `find…` renvoie `Optional`, `get…` ne renvoie jamais vide.
- [ ] Les nouveaux identifiants et grandeurs métier sont des `record`.
- [ ] Les nouvelles règles métier sont testées.

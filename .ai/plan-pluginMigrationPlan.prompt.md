# Plan de Migration du Module Plugin vers Java

## Contexte

Le module `plugin` est actuellement écrit en Kotlin et doit être migré vers Java 21. Les modules `domain` (commun-domain, customItem-domain, skill-domain, job-domain) sont déjà en Java et serviront de référence pour les contrats.

**Dépendances du module plugin :**
- commun-domain (Java)
- customItem-domain (Java)
- skill-domain (Java)
- job-domain (Java)
- customItem-adapter (Kotlin)
- job-adapter (Kotlin)
- skill-adapter (Kotlin)

**Fichiers à migrer :** ~70 fichiers Kotlin dans `plugin/src/main/kotlin/onl/tesseract/srp/`

---

## Étape 1 : Préparer la structure Java du module plugin

### Actions sur `plugin/pom.xml`
- Changer `<sourceDirectory>` de `${project.basedir}/src/main/kotlin` vers `${project.basedir}/src/main/java`
- Retirer le plugin `kotlin-maven-plugin` de la section `<plugins>`
- S'assurer que `lombok` est configuré comme annotation processor dans `maven-compiler-plugin`
- Retirer les dépendances Kotlin (`kotlin-stdlib-jdk8`, `kotlin-reflect`) si non utilisées par d'autres modules
- Conserver les dépendments Lombok, Spring Boot, Paper API, Tesseract Core, etc.

### Actions sur la structure de fichiers
- Créer `src/main/java/onl/tesseract/srp/` avec les sous-dossiers correspondant aux packages Kotlin existants :
  - `controller/command/staff/`
  - `controller/command/elytra/`
  - `controller/command/guild/`
  - `controller/command/job/`
  - `controller/command/rank/`
  - `controller/command/campement/`
  - `controller/command/argument/`
  - `controller/command/argument/guild/`
  - `controller/event/territory/`
  - `controller/event/chat/`
  - `controller/event/equipment/`
  - `controller/event/global/listener/`
  - `controller/event/guild/`
  - `controller/event/item/`
  - `controller/event/job/`
  - `controller/event/money/`
  - `controller/event/structure/`
  - `controller/menu/elytra/`
  - `controller/menu/guild/`
  - `controller/menu/job/`
  - `controller/menu/player/`
  - `domain/item/`
  - `domain/player/`
  - `domain/player/event/`
  - `domain/job/`
  - `domain/money/ledger/`
  - `infrastructure/`
  - `infrastructure/inventory/`
  - `infrastructure/runnable/territory/`
  - `repository/hibernate/`
  - `repository/hibernate/job/`
  - `repository/hibernate/player/`
  - `repository/hibernate/territory/`
  - `repository/yaml/job/`
  - `repository/yaml/skill/`
  - `repository/yaml/equipment/`
  - `repository/yaml/equipment/elytra/`
  - `repository/yaml/equipment/annexionStick/`
  - `repository/generic/player/`
  - `repository/generic/job/`
  - `repository/generic/territory/`
  - `service/job/`
  - `service/player/`
  - `service/money/`
  - `service/world/`
  - `service/equipment/elytra/`
  - `service/equipment/annexionStick/`
  - `service/item/`
  - `util/`
  - `util/menu/`
  - `util/equipment/annexionStick/`

---

## Étape 2 : Migrer les objets domain et sealed interfaces

### Règles de conversion Kotlin → Java

| Kotlin | Java |
|--------|------|
| `data class` | `@Data @NoArgsConstructor @AllArgsConstructor` (Lombok) ou `record` |
| `object singleton` | `public static final` instance + private constructor |
| `enum class` avec companion | `enum` avec méthodes statiques |
| `sealed interface` | `sealed interface` Java 17+ avec `permits` |
| `companion object` | `static` methods/fields |
| `inline fun` | méthodes normales (pas d'équivalent inline en Java) |
| `TODO("Not yet implemented")` | `throw new UnsupportedOperationException()` |

### Fichiers à migrer

#### `domain/item/CustomItem.kt`
```kotlin
data class CustomItem(
    val material: CustomMaterial,
    val quality: Quality,
    val quantity: Int
)
```
→ Java : `@Data @NoArgsConstructor @AllArgsConstructor public class CustomItem` ou `record`

#### `domain/item/CustomMaterial.kt`, `CustomItemIds.kt`, `Rarity.kt`
→ Enums ou classes constantes selon le contenu

#### `domain/player/SrpPlayer.kt`
```kotlin
class SrpPlayer(
    val uniqueId: UUID,
    var rank: PlayerRank = PlayerRank.Survivant,
    money: Int = 0,
    var titleID: String = rank.title.id,
    illuminationPoints: Int = 0
) {
    var money: Int = money private set
    var illuminationPoints: Int = illuminationPoints private set
    // ... méthodes avec logique
}
```
→ Java : Class avec constructeur principal, champs `private` pour money/illuminationPoints, setters publics restreints

#### `domain/player/PlayerRank.kt`
```kotlin
enum class PlayerRank(...) {
    Survivant(...),
    // ...
    ;
    fun next(): PlayerRank? { ... }
    companion object {
        fun getRequiredRankForMissionSlot(slot: Int): PlayerRank { ... }
    }
}
```
→ Java : `enum` avec méthodes d'instance et statiques

#### `domain/job/BaseStat.kt`, `EnumJob.kt`, `Job.kt`, `JobBonus.kt`, `JobSkill.kt`, `QualityDistribution.kt`
→ Classes/records selon la complexité. Les objets simples (sans logique) → `record`. Les objets complexes → class Lombok.

#### `domain/money/ledger/Ledger.kt`, `MoneyTransaction.kt`
→ Classes avec Lombok

---

## Étape 3 : Migrer la configuration et les utilitaires core

### `Config.kt`
```kotlin
data class Config(
    val srpDbHost: String, ...
) {
    companion object {
        private lateinit var instance: Config
        operator fun invoke(): Config { ... }
        fun load(): Config { ... }
    }
}
```
→ Java : Class avec constructeur, méthode statique `load()`, et pattern singleton via `getInstance()`

### `DaoUtils.kt` (package `onl.tesseract.srp.config`)
```kotlin
object DaoUtils {
    val log: Logger = LoggerFactory.getLogger(DaoUtils::class.java)
    inline fun executeInsideTransaction(action: (Session) -> Unit) { ... }
    fun <T> loadAll(type: Class<T?>?, session: Session): MutableList<T?>? { ... }
}
```
→ Java : `public final class DaoUtils` avec méthodes statiques, logger static final

### `HibernateConfig.kt` (package `onl.tesseract.srp.config`)
```kotlin
object HibernateConfig {
    val sessionFactory: SessionFactory = buildSessionFactory()
    fun buildSessionFactory(): SessionFactory { ... }
}
```
→ Java : Class avec static field `sessionFactory` initialisé statiquement

### `SrpCommandInstanceProvider.kt`
→ Java class implémentant `CommandInstanceProvider` (interface externe)

### `CompoundClassLoader.kt`
→ Java class (conversion 1:1, presque déjà Java-like)

### `BukkitTaskScheduler.kt`
```kotlin
class BukkitTaskScheduler(val plugin: Plugin, val bukkitScheduler: BukkitScheduler) : TaskScheduler {
    override fun scheduleAtFixedRate(task: Runnable, period: Duration): ScheduledFuture<*> { ... }
    // TODO pour les autres méthodes
}
```
→ Java class implémentant `TaskScheduler` (interface Spring)

---

## Étape 4 : Migrer les utilitaires et infrastructure

### Utilitaires (`util/`)
- `PlayerUtils.kt`, `NumberUtils.kt`, `EntityUtils.kt`, `SrpChatFormats.kt` → Java utilities classes avec constructeur privé
- `InteractionAllowResult.kt` → sealed interface ou enum selon le cas
- `menu/BiMenu.kt` → Java class

### Infrastructure (`infrastructure/`)
- `CustomInventoryView.kt` → Java class implémentant `InventoryView` (conversion directe)
- `inventory/BukkitPlayerInventoryGateway.kt` → Java class
- `runnable/territory/TerritoryBorderTask.kt` → Java Runnable ou classe avec méthode run()

### `JobSkillMenuConfigParser.kt` + `JobSkillMenuConfig`
```kotlin
@Component
class JobSkillMenuConfigParser { ... }

data class JobSkillMenuConfig(val cells: Array<Array<CellType>>) {
    sealed interface CellType
    data object EmptyCell : CellType
    data class Arrow(val type: ArrowType) : CellType
    // ...
}
```
→ Java : Class avec `@Component`, inner classes statiques pour les types de cellules

---

## Étape 5 : Migrer les repositories

### Hibernate (`repository/hibernate/`)
- `MoneyLedgerRepository.kt` → `@Repository` interface étendant `JpaRepository`
- `MoneyLedgerEntities.kt` → Classes avec annotations JPA (`@Entity`, `@Id`, etc.)
- `SrpPlayerEntity.kt` → Entity JPA
- Repositories dans `job/`, `player/`, `territory/` → Interfaces Spring Data JPA

### YAML (`repository/yaml/`)
- `job/JobsConfigRepository.kt`, `JobConfig.kt` → Java avec lecture YAML (Jackson ou similaire)
- `skill/SkillConfigRepository.kt` → Java config reader
- `equipment/elytra/ElytraSrpSerializer.kt` → Classes de sérialisation
- `equipment/annexionStick/*Serializer.kt` → Sérialiseurs

### Generic (`repository/generic/`)
- `MoneyLedgerRepository.kt`, `player/`, `job/`, `territory/` → Repositories Java

---

## Étape 6 : Migrer les services

Tous les fichiers annotés `@Service` deviennent des classes Java avec `@Service`.

### `service/job/JobService.kt`
```kotlin
@Service
class JobServicee(  // Note: typo dans le nom original
    private val jobConfigRepository: JobsConfigRepository,
    private val playerJobService: PlayerJobService,
    private val eventService: EventService,
) { ... }
```
→ Java : Class avec constructeur par injection de dépendances (final fields)

### `service/job/PlayerJobService.kt`, `PlayerLevelUpEvent.kt`, `PlayerJobSkillUnlockedEvent.kt`, `JobLootItemEvent.kt`
→ Services et events Java

### `service/player/SrpPlayerService.kt`
→ Service avec méthodes `@Transactional`

### `service/money/MoneyLedgerService.kt`, `TransferService.kt`
→ Services avec logique métier

### `service/world/WorldService.kt`, `TeleportationService.kt`
→ Services d'accès au monde Bukkit

### `service/equipment/elytra/ElytraService.kt`
→ Service pour les élytres

### `service/item/CustomItemService.kt`
→ Service pour les items custom

---

## Étape 7 : Migrer les controllers, commands et listeners

### Commands (`controller/command/`)
- `staff/SrpStaffCommand.kt` + sous-commandes (8 fichiers) → Classes Java avec annotations de commande
- `elytra/ElytraMenuCommand.kt`, `ElytraUpgradeMenuCommand.kt` → Commands Bukkit
- `guild/GuildCommand.kt` → Commande guild
- `job/JobSkillCommand.kt` → Commande job
- `rank/RankCommand.kt` → Commande rank
- `campement/CampementCommands.kt` → Commands campement
- `argument/*` → Parsers d'arguments Java

### Events (`controller/event/`)
Tous les listeners deviennent des classes Java avec `@Component` implémentant `Listener`.

- `territory/TerritoryDisplayListener.kt`, `TerritoryBorderDisplayListener.kt`
- `chat/PlayerChatPrefixListener.kt`
- `equipment/*`
- `global/listener/NatureProtectionListener.kt`, `ChunkProtectionListener.kt`
- `guild/*`
- `item/CustomItemProtectionListener.kt`, `CustomItemDropListener.kt`
- `job/CustomItemHarvestListener.kt`
- `money/LedgerCreationListener.kt`
- `structure/CustomStructureListener.kt`

### Menus (`controller/menu/`)
- `elytra/ElytraBaseMenu.kt`, `ElytraMenu.kt`, `ElytraUpgradeMenu.kt` → Classes de menu Java
- `guild/GuildMenu.kt` → Menu guild
- `job/JobSkillMenu.kt`, `JobSelectionMenu.kt` → Menus job
- `player/PlayerRankProgressMenu.kt` → Menu de progression
- `ItemAdderMenu.kt` → Classes abstraites pour les menus ItemsAdder

---

## Étape 8 : Migrer les fichiers restants et le point d'entrée

### `SrpServiceBeans.kt`
→ Java `@Configuration` class avec méthodes `@Bean`

### `TesseractSRP.kt` (point d'entrée principal)
```kotlin
class TesseractSRP : JavaPlugin() {
    lateinit var springContext: ApplicationContext
    override fun onEnable() { ... }
    private fun registerListeners() { ... }
    fun registerCommands() { ... }
    // ...
}
```
→ Java class étendant `JavaPlugin` (Paper API) avec méthode `onEnable()`

### `TesseractSRPSpringApp.kt` (@SpringBootApplication)
→ Java `@Configuration` class avec `@Bean` methods pour la configuration Spring

---

## Validation

Après chaque étape :
1. Exécuter `mvn clean compile -pl plugin -am` pour valider la compilation
2. Vérifier les erreurs de type et les imports manquants
3. S'assurer que les modules consommateurs (autres modules) compilent toujours

---

## Considérations techniques

### Lombok vs Records
- **Lombok** (`@Data`, `@Value`, `@NoArgsConstructor`, `@AllArgsConstructor`) : pour les POJOs avec Spring Boot et les dépendances complexes
- **Java records** : pour les DTOs purs sans logique (ex: `CustomItem` si pas de besoin Spring)

### Sealed interfaces
Java 17+ supporte les interfaces sealed avec `permits`. Exemple :
```java
public sealed interface CellType permits EmptyCell, Arrow, RootCell, SkillCell { }
```

### Coroutines vs CompletableFuture
Si le code Kotlin utilise des coroutines, les remplacer par `CompletableFuture` ou `java.util.concurrent`.

### Pattern singleton Kotlin `object` → Java
```kotlin
object DaoUtils { ... }
```
→ Java :
```java
public final class DaoUtils {
    private DaoUtils() {} // non-instantiable
    public static final DaoUtils INSTANCE = new DaoUtils();
    // ... membres statiques
}
```

### TODO("Not yet implemented") → UnsupportedOperationException
Toutes les méthodes Kotlin avec `TODO()` doivent devenir :
```java
throw new UnsupportedOperationException("Not yet implemented");
```

---

## Ordre de migration recommandé

1. **Couche 1** : Domain objects + sealed interfaces (sans dépendances externes)
2. **Couche 2** : Configuration + utilitaires core (DaoUtils, HibernateConfig, Config)
3. **Couche 3** : Infrastructure (CustomInventoryView, CompoundClassLoader, BukkitTaskScheduler)
4. **Couche 4** : Repositories (Hibernate + YAML + Generic)
5. **Couche 5** : Services (tous les @Service)
6. **Couche 6** : Commands et listeners
7. **Couche 7** : Menus
8. **Couche 8** : Point d'entrée (TesseractSRP, TesseractSRPSpringApp, SrpServiceBeans)

---

## Fichiers de test à migrer

- `plugin/test/` → fichiers de tests JUnit/Mockito existants
- Adapter les tests pour Java si nécessaire


# TesseractSRP2

Plugin Paper (Java 25, Maven, Spring, JPA), en architecture hexagonale : modules `<feature>-domain` et `<feature>-adapter`, plus `plugin` pour l'assemblage. Les specs fonctionnelles sont dans `spec/`.

Les normes complètes sont dans [CONTRIBUTING.md](CONTRIBUTING.md). Elles s'appliquent à tout code écrit ou modifié. L'essentiel :

- **Langue** : code en anglais, commentaires, Javadoc et commits en français.
- **Dépendances** : un domaine ne dépend que de `common-domain`, sans Bukkit, Spring ni JPA. Un adapter peut utiliser le service d'un autre domaine, jamais son repository.
- **SOLID** : un seul service par agrégat ; quand il grossit, il implémente une interface `<Agrégat><Responsabilité>UseCase` par responsabilité. Injection par constructeur. Ports petits et ciblés.
- **Nommage** : noms explicites, sans abréviation, sans `*Utils` ni `*Helper`. L'implémentation d'un port suit `<Techno><Port>` (`JpaCraftingRepository`), jamais `Impl`.
- **Méthodes** : `find…` renvoie `Optional<T>`, `findAll…` une collection (vide si besoin), `get…` ne renvoie jamais vide et lève une exception métier si l'objet manque. `is/has/can/exists` renvoient un booléen.
- **Optional** : seulement en type de retour, jamais en paramètre ni en champ. Aucune méthode ne renvoie `null`. On enveloppe les `null` des API externes avec `Optional.ofNullable`. `Optional.get()` est interdit.
- **Erreurs** : `Optional` pour « absent », exception métier (`domain/model/exception`, extends `RuntimeException`) pour « interdit ». Les adapters userside traduisent l'exception en message pour le joueur.
- **Modèle** : value objects en `record` validés dans le constructeur. Agrégats modifiés seulement par des méthodes métier, sans setter public.
- **Tests** : toute règle métier du domaine a un test JUnit 5 + Mockito, nommé `methode_comportementAttendu`.

# 2 - Fonctionnalités avancées de Java

Cinq mécanismes avancés de Java, chacun sur son propre exemple, sans rien qui les relie
entre eux.

| Mécanisme                      | Package                      | Ce qu'il illustre                                                     |
| ------------------------------ | ---------------------------- | --------------------------------------------------------------------- |
| Classloaders                   | `ch.heigvd.amt.classloaders` | délégation, `CustomClassLoader`, un plugin chargé par `ServiceLoader` |
| Reflection                     | `ch.heigvd.amt.reflection`   | `Describer.describe`, et le piège des modules fermés (JDK 17+)        |
| Annotations à l'exécution      | `ch.heigvd.amt.annotations`  | `@Hidden`, lue par `isAnnotationPresent`                              |
| Proxies                        | `ch.heigvd.amt.proxies`      | `LoggingHandler`, ce qu'un proxy ne voit pas (appel interne)          |
| Annotations à la compilation   | `ch.heigvd.amt.describe`     | `@Describe`, et `DescriberProcessor` (module à part, voir ci-dessous) |

`ch.heigvd.amt.describe` vit dans deux modules : les annotations et le processor dans
`2-advanced-java-features-processor`, `Config` (qui les utilise) ici, parce que javac doit
avoir compilé le processor avant le code qu'il traite.

Le plugin d'export (`src/plugin/`) se compile à part et se package dans un JAR séparé
(`-plugin.jar`), jamais sur le classpath de l'application : `PluginIT` le charge par
`URLClassLoader` et `ServiceLoader`, comme une application charge un plugin qu'elle ne
connaît pas à la compilation.

## Exécuter

```bash
./mvnw -pl 2-advanced-java-features -am verify
```

## Limites voulues

- `CustomClassLoader` lit ses classes dans un répertoire donné au constructeur.
- `Describer.describe` et `DescriberProcessor` ne gèrent qu'un affichage `Nom[champ=valeur, ...]`
  à plat, sans récursion sur les champs eux-mêmes.
- `DescriberProcessor` ne vérifie que les champs privés ; il ne vérifie pas, par exemple,
  qu'une classe `@Describe` a un constructeur accessible.
- `LoggingHandler` écrit sur `System.out` ; un intercepteur de production passerait par un
  logger.

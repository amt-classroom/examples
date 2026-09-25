# 2 - Annotation processor @Describe

L'exemple de la section « Annotations à la compilation » du deck
`site/apps/web/src/decks/2-advanced-java-features.tsx`. Un module à part de
`2-advanced-java-features` : javac doit avoir compilé le processor avant le code qu'il
traite, comme dans `../../examples/01-command-line-processor`.

`@Describe` marque une classe ; `DescriberProcessor` lui génère `<Type>_Describer`, une
classe dont `describe` lit chaque champ directement, en masquant ceux marqués `@Hidden`. Le
processor refuse aussi les champs privés, que le code généré ne saurait pas lire.

`DescriberProcessorTest` compile une classe `@Describe` avec `javax.tools` et lit la sortie
de javac ; `2-advanced-java-features/src/test/java/ch/heigvd/amt/describe/ConfigDescriberTest.java`
appelle le describer généré.

`javac` trouve le processor par `META-INF/services/javax.annotation.processing.Processor`.
Ce module compile avec `<proc>none</proc>` : sans cela, javac lancerait le processor qu'il
est en train de compiler.

## Limites voulues

- un seul niveau de champs, pas de récursion sur un champ qui serait lui-même `@Describe` ;
- aucune vérification qu'une classe `@Describe` a un constructeur accessible.

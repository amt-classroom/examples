# Illustrations AMT

Les exemples que les slides du cours AMT montrent, un module Maven par deck. Chaque module
correspond à un fichier de `../site/apps/web/src/decks/` et vérifie, par des tests, que ce
que la slide affiche est bien ce que le code produit. Java 21 ou plus récent.

Ce dépôt n'est pas la correction des laboratoires : c'est `../examples/`, distribué aux
étudiants après le laboratoire. Ici, chaque exemple est publié avec le deck : voir
[`AGENTS.md`](AGENTS.md) pour ce que ça change dans la conception des modules.

| Module                                                                         | Deck                              | Contenu                                                               |
| ------------------------------------------------------------------------------- | ---------------------------------- | ----------------------------------------------------------------------- |
| [`2-advanced-java-features`](2-advanced-java-features/)                         | 2 - Fonctionnalités avancées de Java | classloaders, reflection, annotations à l'exécution, proxies          |
| [`2-advanced-java-features-processor`](2-advanced-java-features-processor/)     | 2 - Fonctionnalités avancées de Java | `@Describe` et l'annotation processor, compilés avant le reste         |

## Compiler et exécuter

```bash
./mvnw verify
```

`./mvnw` télécharge la version de Maven fixée au premier lancement. Les conventions et les
étapes pour ajouter un exemple sont dans [`AGENTS.md`](AGENTS.md).

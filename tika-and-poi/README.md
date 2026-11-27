# Apache Tika et Apache POI

Un rapport de ventes en PDF, lu avec Apache Tika et réécrit en classeur Excel avec Apache POI.
Le rapport tient sur deux pages : un titre, du texte, un tableau de 42 concerts coupé par le
saut de page, puis un tableau de trois scènes.

Tika lit un document sans que le code connaisse son format : il le reconnaît, choisit le
parseur, et décrit le contenu comme du XHTML. POI écrit les formats de Microsoft Office.

## Exécuter

```bash
cd tika-and-poi
```

```bash
../mvnw compile exec:java
```

`Main` lit `src/main/resources/report.pdf` et écrit `target/report.xlsx`, une feuille par
tableau :

```
Ventes par concert: 42 rows
Ventes par scène: 3 rows
```

## Les exemples

Les classes sont dans `ch.heigvd.amt.tables`, et `ReportTest` les teste.

| Notion                                   | Code            | Test                              |
| ---------------------------------------- | --------------- | --------------------------------- |
| Lire les tableaux d'un PDF avec Tika     | `PdfTables`     | `tikaFindsTheTwoTablesOfTheReport` |
| Recoller un tableau coupé par une page   | `PdfTables`     | `aTableCutByAPageBreakStaysOneTable` |
| Écrire des cellules typées avec POI      | `ExcelWorkbook` | `poiWritesTypedCells`             |

`PdfTables` est un handler SAX : Tika l'appelle à chaque balise, `h2` pour un titre, `table`,
`tr`, `th` et `td` pour un tableau. Le PDF coupe le premier tableau en deux et répète ses
en-têtes en haut de la deuxième page. Le handler recolle les deux moitiés : un tableau que
n'annonce aucun titre continue le précédent.

`ExcelWorkbook` range une date dans une cellule de date et un nombre dans une cellule
numérique, pour qu'Excel sache les trier et les additionner. `1'440` devient le nombre 1440.

## Ce que l'exemple laisse de côté

- **Les PDF sans structure.** Tika trouve les tableaux parce que ce PDF est balisé (*tagged
  PDF*) : il décrit sa structure en plus de son apparence. `report.pdf` est la page
  `report.html` imprimée par Chrome, qui écrit ces balises. Un PDF scanné, ou produit sans
  balises, ne donne que des paragraphes : il faut alors deviner les colonnes d'après la
  position du texte, ce qui est un autre problème.
- **Les cellules vides.** Tika ne les transmet pas : une ligne qui en contient arrive plus
  courte, et ses valeurs se décalent. Le rapport n'en a aucune.
- **La mise en forme.** Le classeur ne reprend ni les couleurs ni les en-têtes en gras.

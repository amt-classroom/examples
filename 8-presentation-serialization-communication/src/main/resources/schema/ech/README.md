# Schémas eCH

Les treize fichiers `.xsd` de ce dossier sont des copies non modifiées de schémas publiés par
l'association [eCH](https://www.ech.ch), qui normalise la cyberadministration suisse. eCH en reste
l'auteur. Ses normes s'utilisent et se diffusent gratuitement et sans restriction, à condition de
la mentionner.

- `eCH-0119-4-0-0.xsd` est le schéma de la norme
  [eCH-0119, E-Tax Filing](https://www.ech.ch/fr/ech/ech-0119), version 4.0.0 : la déclaration
  d'impôt des personnes physiques.
- Les douze autres sont ceux qu'il importe, directement ou non. Chacun vient de l'adresse que son
  espace de noms indique, sous `https://www.ech.ch/xmlns/`.

Copiés le 27 novembre 2026.

`catalog.xml` n'est pas d'eCH : il fait correspondre ces adresses aux copies du dossier, pour que
XJC ne les cherche pas sur le réseau.

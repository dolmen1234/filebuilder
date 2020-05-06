# filebuilder

Pour les concours CodinGame.
Surveille un répertoire, et si un des fichiers *.java est modifié, compacte tout dans un seul fichier, en enlevant espaces, commentaires ...
A combiner avec le plugin cg-sync qui enverra automatiquement le fichier généré sur le site de CodinGame.

https://chrome.google.com/webstore/detail/codingame-sync-ext/ldjnbdgcceengbjkalemckffhaajkehd

Syntaxe : 
`java -jar filebuilder.jar <fichier source principal> <répertoire destination>`

Exemple : 
`java -jar D:\tools\git\filebuilder\target\filebuilder.jar d:\tools\git\filebuilder\src\test\resources\in\Player.java d:\tmp`

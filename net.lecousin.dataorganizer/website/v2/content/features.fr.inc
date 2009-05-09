<h1>Ecran principal</h1>
<p class="text_1">
Plusieurs mode d'affichage sont disponible.<br>
L'affichage classique:<br>
<center><img src="screenshots/main_screen.gif"/></center><br/>
Ou l'affichage détaillé:<br>
<center><img src="screenshots/main_screen_2.gif"/></center><br/>
L'application présente une fenêtre principalement composée de 3 parties:
<ul style="margin-top: 0px; margin-bottom: 0px;">
 <li>Un ensemble de critères de recherche vous permet de filtrer les données affichées</li>
 <li>La liste de vos données, filtrée selon les critères de recherche</li>
 <li>Les informations concernant la donnée sélectionnée</li>
</ul>
</p>

<h2>Recherche</h2>
<p class="text_2">
La fenêtre de recherche vous permet de filtrer les données affichées.<br>
Pour chacun des critères, vous pouvez soit demander d'inclure les données répondant à ce critère, 
ou vous pouvez inverser le critère et ainsi demander d'exlure les données répondant à ce critère.
</p>
<h3>Par type de donnée</h3>
<p class="text_3">
Vous pouvez filtrer par type de donnée et ainsi par exemple n'afficher que vos vidéos
</p>
<h3>Par étiquettes</h3>
<p class="text_3">
Vous pouvez attribuer une liste d'étiquettes à chacune de vos données. Par exemple "Famille", "Film", "Humour"...<br/>
Les étiquettes sont hiérarchique: une étiquette peut contenir des sous-étiquettes, pouvant elles-même contenir des sous-étiquettes et ainsi de suite.<br/>
Les étiquettes vous permettent ainsi de trier vos données de manière efficace: à la fois de manière hiérarchique, mais vous avez également la possibilité pour une même donnée de lui attribuer des étiquettes de différents niveaux de la hiérarchie.
</p> 
<h3>Critères principaux</h3>
<p class="text_3">
Une série de critères utilisables pour n'importe quel type de donnée vous permet de rapidement chercher vos données:
<ul style="margin-top: 0px; margin-bottom: 0px;">
 <li>Nom: pour une recherche rapide d'une ou de données dont vous connaissez au moins une partie du nom</li>
 <li>Note: pour rechercher les données ayant une note comprise dans un intervalle. Par exemple les données ayant une note entre 15 et 20.</li>
 <li>Ajouté: pour rechercher les données ayant été ajoutées dans un intervalle. Par exemple les données que vous avez ajouté les 2 dernières semaines.</li>
 <li>Ouvert: pour rechercher les données que vous avez ouvert dans un intervalle. Par exemple les données que vous avez ouvert le mois précédent.</li> 
</ul>
</p> 
<h3>Critères spécifiques par type de donnée</h3>
<p class="text_3">
Enfin, selon les types de donnée sélectionnées, des critères supplémentaires vont s'afficher:
<ul style="margin-top: 0px; margin-bottom: 0px;">
 <li>Vidéo: durée de la vidéo, casting (recherche par acteur, producteur, réalisateur...)</li>
 <li>Audio: nom de l'album, de l'artiste ou nom de chanson/piste</li>
</ul>
</p>

<h2>Liste de données</h2>
<p class="text_2">
La liste de données affiche les données correspondant aux critères de recherche que vous avez spécifié.<br/>
Cette liste peut être affichée sous forme de table (comme ci-dessus) ou sous forme de mosaïque:<br/>
<center><img src="screenshots/mosaic.gif"/></center>
</p>

<h2>Détail d'une donnée</h2>
<p class="text_2">
Lorsque vous sélectionnez une donnée dans la liste, les détails de cette donnée s'affichent.<br/>
Cet écran vous permet d'accéder par exemple au résumé du film, aux commentaires récupérés d'Internet, et 
divers autres informations. Cet écran vous permet également d'avoir des détails sur les acteurs, réalisateurs, 
producteurs, et scénaristes. En effet, lorsque vous cliquez sur un acteur, vous pouvez créer une donnée
pour cet acteur et ainsi récupérer ses informations depuis Internet. L'acteur est ainsi ajouté à votre liste
et vous pouvez voir ses informations:<br/>
<center><img src="screenshots/people.gif"/></center><br/>
Via cet écran de détail, vous pouvez ainsi naviguer à travers les liens d'un film sur acteur, sur un autre film de cet acteur...
</p>

<h1>Ouvrir une donnée</h1>
<p class="text_1">
Pour ouvrir une donnée, plusieurs choix s'offrent à vous:
<ul style="margin-top: 0px; margin-bottom: 0px;">
 <li>Utiliser une autre application installée sur votre ordinateur</li>
 <li>Utiliser le Media Player intégré à DataOrganizer:<br/>
 <center><img src="screenshots/mediaplayer.gif"/></center></li>
 <li>Utiliser votre TV:<br/>
 <center><img src="screenshots/sfr_mediacenter.gif"/></center><br/>
 Pour l'instant uniquement la NeufBox/SFRBox est supportée (toute participation pour supporter d'autres boxes est bien sûr la bienvenue ;-)</li>
</ul>
</p>

<h1>Ajouter des données</h1>
<p class="text_1">
Pour ajouter une ou des données, rien de plus facile: soit vous sélectionnez vous même une à une les données à ajouter, soit
vous demandez d'analyser tout un répertoire (et ses sous-répertoires) pour trouver automatiquement vos données:
<ul style="margin-top: 0px; margin-bottom: 0px;">
 <li>Vidéo: DataOrganizer va automatiquement trouver vos fichiers vidéos, et pour chacun d'eux créer une donnée</li>
 <li>Audio: DataOrgniazer est capable de trouver automatiquement vos albums audio et leur informations:
  <ul style="margin-top: 0px; margin-bottom: 0px;">
   <li>Analyse les informations de chaque fichier audio (nom d'album, artiste, numéro de piste...)</li>
   <li>Analyse les playlist présentes pour déterminer l'ordre des pistes</li>
   <li>Si l'ordre des pistes ne peut pas être déterminé par les informations contenues dans les fichiers ou les playlists, DataOrganizer essayes de les trouver grâce au nom des fichiers</li>
   <li>Si des images sont présentes, DataOrganizer peut les attacher à l'album (par exemple couverture du CD, photos de concerts...)</li>
  </ul>
 </li>
</ul>
Vous pouvez également ajouter des données que vous ne possédez pas en récupérant les informations sur Internet. Ceci est particulièrement utile si vous souhaitez avoir des fiches sur des acteurs (qui ne correspond pas à un ou des fichiers présents sur votre ordinateur).
</p>

<h1>Il est temps de tester !</h1>
<p class="text_1">
Si vous pensez que ce logiciel vous convient, ou que vous souhaitez le tester, rien de plus simple:<br/>
<a href="download.html">Vous pouvez le télecharger ici</a><br/>
Ce logiciel est <b>totallement gratuit</b>, sans aucune restriction d'utilisation ni publicité.<br/>
L'installation ne copie aucun fichier dans vos répertoire systèmes, ni n'utilise votre base de registres. En somme il est totallement indépendant et n'impactera aucune donnée de votre ordinateur, vous pouvez donc le tester en toute sécurité, puis le désinstaller s'il ne vous plaît pas sans risquer quoi que ce soit.<br/>
N'hésitez pas à <a href="mailto:support.dataorganizer@gmail.com">nous contacter</a> pour nous indiquer ce qui vous convient comme ce qui ne vous convient pas afin que nous puissions améliorer petit à petit ce logiciel.
</p>

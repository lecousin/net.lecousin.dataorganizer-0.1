<html>
<head>
 <title>DataOrganizer News</title>
 <link rel="stylesheet" type="text/css" href="../../css/common.css"/>
 <link rel="stylesheet" type="text/css" href="../../css/content.css"/>
</head>
<body>
<?php
$ids = explode(",", $_GET['ids']);

function isShown($id, $ids) {
	$count = count($ids);
	for ($i = 0; $i < $count; $i++) {
		if ($ids[$i] == $id) {
			return true;
		}
	}
	return false;	
}
?>
<table border="0" width="80%" align="center">
<tr><td><div style="font-size: 16pt;color: #303050;">DataOrganizer Infos</div></td></tr>
<tr><td>
<div class="cadre">

<?php
if (isShown("1", $ids)) {
?>
<h1>Nous avons besoin de vos remarques !</h1>
<p class="text_1">
Tout d'abord, merci d'utiliser DataOrganizer.<br/>
<br/>
Comme vous pouvez le constater, nous en sommes aux toutes premi�res versions de DataOrganizer, et malgr� que nous faisons de notre mieux pour vous fournir le meilleur logiciel, il est �vident qu'il manque encore plein de fonctionnalit�s sympas!<br/>
Mais le meilleur moyen, pour vous d'avoir le logiciel correspondant au mieux � vos souhaits, et pour nous de fournir le meilleur logiciel � la communaut�, est que nous collections vos remarques, id�es, et contributions !!<br/>
Donc n'h�sitez surtout pas � nous contacter, et � r�flechir � comment am�liorer ce logiciel et comment vous souhaiteriez le voir �voluer.<br/> 
<br/>
Pour nous contacter, vous pouvez utiliser une des mani�res suivantes:
<ul style="margin-top:0pt;">
<li>Vous inscrire et ecrire sur <a href="/xmb" target="_blank">notre forum</a></li>
<li>Nous envoyer directement votre message par mail � l'adresse <a href="mailto:support.dataorganizer@gmail.com">support.dataorganizer@gmail.com</a></li>
</ul>
Et m�me si vous pensez que DataOrganizer est d�j� tr�s bien, n'h�sitez pas � nous le faire savoir �galement ;-)<br/>
Bonne utilisation !<br/>
<i>L'�quipe de DataOrganizer.</i>
</p>
<?php
}
?>

<?php
if (isShown("2", $ids)) {
?>
<h1>Nouvelle version disponible mais erreur de mise � jour</h1>
<p class="text_1">
Tout d'abord la bonne nouvelle: la version 0.4 est disponible.<br>
La mauvaise nouvelle, c'est que la version 0.3 contient un bug dans son syst�me de mise � jour.<br>
<br>
Pour installer la derni�re version, il vous faut donc:<ul>
<li>D�sinstaller votre version actuelle</li>
<li>T�l�charger la derni�re version en <a href="http://cloud.github.com/downloads/lecousin/net.lecousin.dataorganizer/DataOrganizer-Install.exe">cliquant ici</a></li>
<li>Installer cette nouvelle version</li>
</ul>
<br>
Bien s�r, l'installation de cette nouvelle version ne supprimera aucune de vos donn�es. En effet la d�sinstallation ne supprime que les ex�cutables et non votre travail d�j� effectu� dans le logiciel!<br>
<br>
Nous vous prions ne bien vouloir nous excuser pour ce petit probl�me.
<br>
Si vous avez un probl�me ou une question, n'h�sitez pas � nous contacter sur notre adresse mail <a href="mailto:support.dataorganizer@gmail.com">support.dataorganizer@gmail.com</a>.
<i>L'�quipe de DataOrganizer.</i>
</p>
<?php
}
?>

</div>
</td></tr></table>
<script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>
<script type="text/javascript">
try {
var pageTracker = _gat._getTracker("UA-7574390-3");
pageTracker._trackPageview();
} catch(err) {}</script>
</body>
</html>
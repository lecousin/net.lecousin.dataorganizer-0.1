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
Comme vous pouvez le constater, nous en sommes aux toutes premières versions de DataOrganizer, et malgré que nous faisons de notre mieux pour vous fournir le meilleur logiciel, il est évident qu'il manque encore plein de fonctionnalités sympas!<br/>
Mais le meilleur moyen, pour vous d'avoir le logiciel correspondant au mieux à vos souhaits, et pour nous de fournir le meilleur logiciel à la communauté, est que nous collections vos remarques, idées, et contributions !!<br/>
Donc n'hésitez surtout pas à nous contacter, et à réflechir à comment améliorer ce logiciel et comment vous souhaiteriez le voir évoluer.<br/> 
<br/>
Pour nous contacter, vous pouvez utiliser une des manières suivantes:
<ul style="margin-top:0pt;">
<li>Vous inscrire et ecrire sur <a href="/xmb" target="_blank">notre forum</a></li>
<li>Nous envoyer directement votre message par mail à l'adresse <a href="mailto:support.dataorganizer@gmail.com">support.dataorganizer@gmail.com</a></li>
</ul>
Et même si vous pensez que DataOrganizer est déjà très bien, n'hésitez pas à nous le faire savoir également ;-)<br/>
Bonne utilisation !<br/>
<i>L'équipe de DataOrganizer.</i>
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
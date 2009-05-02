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
<tr><td><div style="font-size: 16pt;color: #303050;">DataOrganizer News</div></td></tr>
<tr><td>
<div class="cadre">

<?php
if (isShown("1", $ids)) {
?>
<h1>We need your feed back !</h1>
<p class="text_1">
First, thanks for using DataOrganizer.<br/>
<br/>
As you can see, we are at the beginning of our releases, and even we are doing our best to provide you with the best software, for sure it still misses a lot of nice features.<br/>
But the best way, for you to have the software you are waiting for, and for us to provide the best software to the community, is that we collect your feedback, ideas, and contributions !!<br/>
So do not hesitate to contact us, and to think about what you would like this software to be.<br/>
<br/>
To contact us, you can use one of the following ways:
<ul style="margin-top:0pt;">
<li>Post a thread in <a href="/xmb" target="_blank">our forum</a></li>
<li>Send us an email to <a href="mailto:support.dataorganizer@gmail.com">support.dataorganizer@gmail.com</a></li>
</ul>
And even you think DataOrganizer is good enough, do not hesitate to tell us ;-)<br/>
Enjoy using DataOrganizer!<br/>
<i>The DataOrganizer Team</i>
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
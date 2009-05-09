<?php
// initialize the page to display
if (!isset($page) || $page == '') {
	if (isset($_GET['page']) && $_GET['page'] <> '') {
		$page = $_GET['page'];
	} else {
		$page = 'home';
	}
}

// intitialize the language
if (!isset($lang) || $lang == '') {
	if (isset($_GET['lang']) && $_GET['lang'] <> '') {
		$lang = $_GET['lang'];
	} else {
		include('detect_language.inc');
		$lang = detect_language($_SERVER['HTTP_ACCEPT_LANGUAGE']);
	}
}
if ($lang == 'fr') {
	include('lang.fr.inc');
} else {
	include('lang.en.inc');
}

function pagelink($pagename,$pagelang) {
	echo "<a href='".$pagename.".".$pagelang.".php'>";
}

?>
<html>
<head>
<?php include('head.inc'); ?>
<link rel="stylesheet" type="text/css" href="style.css"/>
</head>
<body>
<table width=100%>
<tr><td colspan=3 height="70">
<?php include('header.inc') ?>
</td></tr>
<tr><td width="175" valign="top">
<?php include('menu.inc') ?>
</td><td valign="top">
<?php include('content.inc') ?>
</td><td width="150" valign="top">
<?php include('right.inc') ?>
</td></tr>
</table>
</body>
</html>
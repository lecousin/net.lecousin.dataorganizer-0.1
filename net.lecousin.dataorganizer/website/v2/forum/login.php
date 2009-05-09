<?php

include('settings.inc');
include('functions.inc');

if (isset($_POST['login'])) {
	db_connect();
	$admin = adminLogin($_POST['login'], $_POST['pass']);
	db_close();
	if ($admin == false) {
		echo "Login failed.";
	} else {
		echo "Login succeed.<br>";
		echo "<a href='".$_POST['forum_path']."?forumpage=home'>";
		echo "Go to forum home page</a>";
	}
}

?>

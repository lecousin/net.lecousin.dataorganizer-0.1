<br>
<?php
if (!isAdmin()) {
	linkforum('login','','');
	echo "Login as admin</a>";
} else {
	$admin = getAdmin();
	echo "Logged as ".$admin['login']." (";
	linkforum('logout','','');
	echo "logout</a>)";
}
?>
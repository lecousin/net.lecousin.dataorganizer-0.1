<?php
$forum_dir = 'forum';
$forum_path = $_SERVER['SCRIPT_NAME'];

// initialize the page to display
if (!isset($forumpage) || $forumpage == '') {
	if (isset($_GET['forumpage']) && $_GET['forumpage'] <> '') {
		$forumpage = $_GET['forumpage'];
	} else if (isset($_POST['forumpage']) && $_POST['forumpage'] <> '') {
		$forumpage = $_POST['forumpage'];
	} else {
		$forumpage = 'home';
	}
}

echo "<link rel='stylesheet' type='text/css' href='".$forum_dir."/forum.css'/>";


include($forum_dir.'/settings.inc');
include($forum_dir.'/functions.inc');
include($forum_dir.'/lang.'.$lang.'.inc');
db_connect();
include($forum_dir.'/'.$forumpage.'.inc');
db_close();
?>

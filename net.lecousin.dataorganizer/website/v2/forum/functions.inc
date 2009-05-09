<?php

function db_connect() {
	global $db_settings;
	$GLOBALS['db'] = mysql_connect($db_settings['host'], $db_settings['user'], $db_settings['pass']);
	mysql_select_db($db_settings['db'], $GLOBALS['db']);
}
function db_close() {
	mysql_close($GLOBALS['db']);
}

function getAdmin() {
	if (isset($GLOBALS['admin'])) return $GLOBALS['admin'];
	$admin = internalGetAdmin();
	$GLOBALS['admin'] = $admin;
	return $admin;
}
function internalGetAdmin() {
	global $db_settings;
	if (!isset($_COOKIE['forum_admin_session'])) return false;
	$session = $_COOKIE['forum_admin_session'];
	if (!isset($session) || $session == '') return false;
	$query = "SELECT * FROM `".$db_settings['db']."`.`".$db_settings['forum_admin_table']."` WHERE `session`='".$session."';";
	$result = @mysql_query($query, $GLOBALS['db']);
	if ($result == false) return false;
	$admin = mysql_fetch_array($result);
	if ($admin['expire'] < time()) return false;
	return $admin;
}

function isAdmin() {
	$admin = getAdmin();
	if ($admin == false) return false;
	return true;
}

function generateSessionID() {
	$id = rand().rand().rand();
	return $id;
}
function adminLogin($login, $pass) {
	global $db_settings;
	$query = "SELECT * FROM `".$db_settings['db']."`.`".$db_settings['forum_admin_table']."` WHERE `login`='".$login."' AND `pass`='".$pass."';";
	$result = @mysql_query($query, $GLOBALS['db']);
	if ($result == false) { echo "Invalid login or password (1).<br>"; return false; }
	$result = mysql_fetch_array($result);
	if ($result == false) { echo "Invalid login or password (2).<br>"; return false; }
	$session = generateSessionID();
	$expire = time()+1000*60*60;
	$query = "UPDATE `".$db_settings['db']."`.`".$db_settings['forum_admin_table']."` SET `session` = '".$session."', `expire` = '".$expire."' WHERE `login`='".$login."' AND `pass`='".$pass."';";
	$result = @mysql_query($query, $GLOBALS['db']);
	setcookie('forum_admin_session', $session, $expire, '/');
	return true;
}

function adminLogout() {
	$admin = getAdmin();
	if ($admin == false) return false;
	$session = $admin['session'];
	global $db_settings;
	$query = "UPDATE `".$db_settings['db']."`.`".$db_settings['forum_admin_table']."` SET `session` = NULL, `expire` = NULL WHERE `session`='".$session."';";
	$result = @mysql_query($query, $GLOBALS['db']);
	return true;
}

function linkforum($page,$attrs,$cl) {
	global $forum_path;
	echo "<a href='".$forum_path."?forumpage=".$page.$attrs."' class='".$cl."'>";
}

function getRootCategories() {
	global $db_settings;
	$query = "SELECT * FROM `".$db_settings['db']."`.`".$db_settings['forum_categories_table']."` WHERE `parent_id` IS NULL;";
	$result = @mysql_query($query, $GLOBALS['db']);
	$arr = array();
	if ($result == false) { return $arr; }
	$i = 0;
	while ($row=mysql_fetch_array($result)) {
		$arr[$i] = $row;
		$i = $i+1;
	}
	return $arr;
}

function getCategoryDetail($cat_id) {
	global $db_settings, $lang;
	$query = "SELECT * FROM `".$db_settings['db']."`.`".$db_settings['forum_category_names_table']."` WHERE `cat_id`=".$cat_id." AND `lang`='".$lang."';";
	$result = @mysql_query($query, $GLOBALS['db']);
	return mysql_fetch_array($result);
}

function getCategoryName($cat_id) {
	$det = getCategoryDetail($cat_id);
	if ($det == false) return "";
	return $det['name'];
}
function getCategoryDescription($cat_id) {
	$det = getCategoryDetail($cat_id);
	if ($det == false) return "";
	return $det['description'];
}

function getSubCategories($cat_id) {
	global $db_settings;
	$query = "SELECT * FROM `".$db_settings['db']."`.`".$db_settings['forum_categories_table']."` WHERE `parent_id`='".$cat_id."';";
	$result = @mysql_query($query, $GLOBALS['db']);
	$arr = array();
	if ($result == false) { return $arr; }
	$i = 0;
	while ($row=mysql_fetch_array($result)) {
		$arr[$i] = $row;
		$i = $i+1;
	}
	return $arr;
}

function getParentCategoryID($cat_id) {
	global $db_settings, $lang;
	$query = "SELECT * FROM `".$db_settings['db']."`.`".$db_settings['forum_categories_table']."` WHERE `id`='".$cat_id."';";
	$result = @mysql_query($query, $GLOBALS['db']);
	$cat = mysql_fetch_array($result);
	return $cat['parent_id'];
}

function createCategory($parent) {
	global $db_settings;
	$query = "INSERT INTO `".$db_settings['db']."`.`".$db_settings['forum_categories_table']."` (`parent_id`) VALUES ( ".(isset($parent)?"'".$parent."'":"NULL")." );";
	$result = @mysql_query($query, $GLOBALS['db']);
	if ($result == false) { echo "Failure: ".$query; return false; }
	$id = mysql_insert_id();
	return $id;
}
function addCategoryName($cat_id, $lang, $name, $description) {
	global $db_settings;
	$query = "INSERT INTO `".$db_settings['db']."`.`".$db_settings['forum_category_names_table']."` (`cat_id`, `lang`, `name`, `description` ) VALUES ( '".$cat_id."', '".$lang."', '".$name."', '".$description."' );";
	$result = @mysql_query($query, $GLOBALS['db']);
	if ($result == false) { echo "Failure."; } else { echo "Success."; }
}
function removeCategory($cat_id) {
	global $db_settings;
	$query = "DELETE FROM `".$db_settings['db']."`.`".$db_settings['forum_categories_table']."` WHERE `id`='".$cat_id."';";
	$result = @mysql_query($query, $GLOBALS['db']);
	$query = "DELETE FROM `".$db_settings['db']."`.`".$db_settings['forum_category_names_table']."` WHERE `cat_id`='".$cat_id."';";
	$result = @mysql_query($query, $GLOBALS['db']);
	$query = "DELETE FROM `".$db_settings['db']."`.`".$db_settings['forum_posts_table']."` WHERE `cat_id`='".$cat_id."';";
	$result = @mysql_query($query, $GLOBALS['db']);
}


function getCategoryNbPosts($cat_id) {
	global $db_settings;
	$query = "SELECT COUNT(*) FROM `".$db_settings['db']."`.`".$db_settings['forum_posts_table']."` WHERE cat_id='".$cat_id."';";
	$result = @mysql_query($query, $GLOBALS['db']);
	$row=mysql_fetch_array($result);
	return $row['COUNT(*)'];
}

function getCategoryRootPosts($cat_id) {
	global $db_settings;
	$query = "SELECT * FROM `".$db_settings['db']."`.`".$db_settings['forum_posts_table']."` WHERE `reply_id` IS NULL AND cat_id='".$cat_id."' ORDER BY `date`;";
	$result = @mysql_query($query, $GLOBALS['db']);
	$arr = array();
	if ($result == false) { return $arr; }
	$i = 0;
	while ($row=mysql_fetch_array($result)) {
		$arr[$i] = $row;
		$i = $i+1;
	}
	return $arr;
}

function getLatestPosts() {
	global $db_settings;
	$query = "SELECT * FROM `".$db_settings['db']."`.`".$db_settings['forum_posts_table']."` ORDER BY `date` DESC LIMIT 5;";
	return @mysql_query($query, $GLOBALS['db']);
}

function getPost($post_id) {
	global $db_settings;
	$query = "SELECT * FROM `".$db_settings['db']."`.`".$db_settings['forum_posts_table']."` WHERE `id`='".$post_id."';";
	$result = @mysql_query($query, $GLOBALS['db']);
	if ($result == false) return false;
	return mysql_fetch_array($result);
}

function getSubPosts($post_id) {
	global $db_settings;
	$query = "SELECT * FROM `".$db_settings['db']."`.`".$db_settings['forum_posts_table']."` WHERE `reply_id`='".$post_id."' ORDER BY `date`;";
	$result = @mysql_query($query, $GLOBALS['db']);
	$arr = array();
	if ($result == false) { return $arr; }
	$i = 0;
	while ($row=mysql_fetch_array($result)) {
		$arr[$i] = $row;
		$i = $i+1;
	}
	return $arr;
}

function getNbReplies($post_id) {
	global $db_settings;
	$query = "SELECT COUNT(*) FROM `".$db_settings['db']."`.`".$db_settings['forum_posts_table']."` WHERE reply_id='".$post_id."';";
	$result = @mysql_query($query, $GLOBALS['db']);
	$row=mysql_fetch_array($result);
	return $row['COUNT(*)'];
}

function getRootPost($post_id) {
	$post = getPost($post_id);
	if (!isset($post['reply_id']) || $post['reply_id'] == '') return $post;
	return getRootPost($post['reply_id']);
}

function createPost($cat_id, $name, $lang, $title, $message, $reply_id) {
	global $db_settings;
	$query = "INSERT INTO `".$db_settings['db']."`.`".$db_settings['forum_posts_table']."` (`cat_id`, `user`, `lang`, `title`, `message`, `date`, `state`, `reply_id` ) VALUES ( ";
	$query = $query."'".$cat_id."', '".$name."', '".$lang."', '".$title."', '".$message."', CURRENT_TIMESTAMP(), 'open', ".(isset($reply_id)?"'".$reply_id."'":"NULL")." );";
	$result = @mysql_query($query, $GLOBALS['db']);
	if ($result == false) return false;
	$id = mysql_insert_id();
	return $id;
}

function removePost($post_id) {
	$sub = getSubPosts($post_id);
	foreach ($sub as $p) {
		removePost($p['id']);
	}
	global $db_settings;
	$query = "DELETE FROM `".$db_settings['db']."`.`".$db_settings['forum_posts_table']."` WHERE `id`='".$post_id."';";
	$result = @mysql_query($query, $GLOBALS['db']);
	echo "Post ".$post_id." removed.<br>";
}

function putFlag($lang) {
	global $forum_dir;
	if ($lang=='en') {
		echo "<img src='".$forum_dir."/images/en.gif'>";
	} elseif ($lang == 'fr') {
		echo "<img src='".$forum_dir."/images/fr.jpg'>";
	}
}

function printCategoryPath($cat_id) {
	$parent = getParentCategoryID($cat_id);
	if (isset($parent) && $parent <> '') {
		printCategoryPath($parent);
	} else {
		linkforum('home','','');
		lang_forum_homepage();
		echo "</a>";
	}
	echo " &gt; ";
	linkforum('category','&cat_id='.$cat_id,'');
	echo getCategoryName($cat_id);
	echo "</a>";
}


?>

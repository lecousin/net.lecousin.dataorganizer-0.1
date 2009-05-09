<?php
$post_id = $_GET['post_id'];
$post = getRootPost($post_id);
$post_id = $post['id'];

printCategoryPath($post['cat_id']);
?>
<h1>DataOrganizer Forum - <?php echo getCategoryName($post['cat_id']); ?></h1>

<?php
function printPost($post, $margin) {
	echo "<div style='margin-left: ".$margin.";'>";
	echo "<table class='forum_post' width=100%>";
	echo "<tr class='forum_post_header'>";
	echo "<td>";
	if (isSet($post['title'])) {
		echo "<table width=100% border=0 style='margin: 0;' cellspacing=0 cellpadding=0><tr><td><div class='forum_post_title'>";
		putFlag($post['lang']);
		echo " ".$post['title']."</div>";
		echo "</td><td><div class='forum_post_info'>";
		lang_forum_by();
		echo " ".$post['user']." ";
		lang_forum_on();
		echo " ".$post['date']." ";
		if (isAdmin()) {
			global $forum_dir;
			echo "<a href='".$forum_dir."/admin.php?action=remove_post&post_id=".$post['id']."' onclick='javascript:return confirm(\"Are you sure you want to remove the post ?\");'>";
			echo "<img src='".$forum_dir."/images/del.gif' border=0>";
			echo "</a>";
		}
		echo "</div></td></tr></table>";
	} else {
		echo "<div class='forum_post_info'>";
		lang_forum_answer_by();
		echo " ".$post['user']." ";
		lang_forum_on();
		echo " ".$post['date']." ";
		echo "</div>";
	}
	echo "</td>";
	echo "</tr><tr class='forum_post_body'>";
	echo "<td>";
	echo str_replace("\n","<br>",$post['message']);
	echo "<br>";
	linkforum('newpost','&post_id='.$post['id'].'&cat_id='.$post['cat_id']."&title=".$post['title'], '');
	lang_forum_reply();
	echo "</a>";
	echo "</td>";
	echo "</tr>";
	echo "</table>";
	echo "</div>";
	$posts = getSubPosts($post['id']);
	foreach ($posts as $p) {
		printPost($p, $margin+20);
	}
}

printPost($post, 0);
?>

<?php include($forum_dir.'/login_footer.inc'); ?>

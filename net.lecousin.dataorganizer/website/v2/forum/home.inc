<h1>DataOrganizer Forum</h1>

<table class="forum" cellspacing="0" cellpadding="3">
<tr class='forum_header'><th class="forum"><?php lang_forum_LatestPosts(); ?></th></tr>
<?php
$posts = getLatestPosts();
while ($post=mysql_fetch_array($posts)) {
	echo "<tr><td class='forum'>";
	putFlag($post['lang']);
	echo " ".$post['user']." ";
	linkforum('post','&post_id='.$post['id'], 'forum_title');
	echo $post['title'];
	echo "</a>";
	echo " (".$post['date'].")";
	echo "</td></tr>";
}
?>
</table>
<br>
<?php 
$cats = getRootCategories();
include($forum_dir.'/cat_list.inc');
?>

<?php include($forum_dir.'/login_footer.inc'); ?>

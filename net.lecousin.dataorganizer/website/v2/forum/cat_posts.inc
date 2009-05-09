<?php linkforum('newpost', (isset($cat_id)?"&cat_id=".$cat_id:""), ''); lang_forum_new_post(); echo "</a><br>"; ?>
<table class="forum" cellspacing="0" cellpadding="3">
<tr class='forum_header'><th colspan=4 class="forum"><?php lang_forum_Posts(); ?></th></tr>
<tr class='forum_header'>
 <td class='forum_header'><?php lang_forum_user(); ?></td>
 <td class='forum_header'><?php lang_forum_topic(); ?></td>
 <td class='forum_header'><?php lang_forum_replies(); ?></td>
 <td class='forum_header'><?php lang_forum_state(); ?></td>
</tr>
<?php
foreach ($posts as $post) {
	echo "<tr>";
	echo "<td class='forum'>";
	echo $post['user'];
	echo "</td>";
	echo "<td class='forum'>";
	putFlag($post['lang']);
	echo " ";
	linkforum('post','&post_id='.$post['id'], 'forum_title');
	echo $post['title'];
	echo "</a>";
	echo "</td>";
	echo "<td class='forum' align=center>";
	echo getNbReplies($post['id']);
	echo "</td>";
	echo "<td class='forum' align=center>";
	echo lang_forum_poststate($post['state']);
	echo "</td>";
	if (isAdmin()) {
		echo "<td>";
		echo "<a href='".$forum_dir."/admin.php?action=remove_post&post_id=".$post['id']."' onclick='javascript:return confirm(\"Are you sure you want to remove the post ?\");'>";
		echo "<img src='".$forum_dir."/images/del.gif' border=0>";
		echo "</a>";
		echo "</td>";
	}
	echo "</tr>";
}
?>
</table>
<?php linkforum('newpost', (isset($cat_id)?"&cat_id=".$cat_id:""), ''); lang_forum_new_post(); echo "</a><br>"; ?>
<br>

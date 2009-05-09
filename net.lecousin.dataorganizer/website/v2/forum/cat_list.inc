<?php if (count($cats)>0) { ?>
<table class="forum" cellspacing="0" cellpadding="3">
<tr class='forum_header'><th colspan=2 class="forum"><?php lang_forum_Categories(); ?></th></tr>
<?php
foreach ($cats as $cat) {
	echo "<tr><td class='forum'>";
	$cat_detail = getCategoryDetail($cat['id']);
	linkforum('category','&cat_id='.$cat['id'], 'forum_title');
	echo $cat_detail['name'];
	echo "</a><br>";
	linkforum('category','&cat_id='.$cat['id'], 'forum_subtitle');
	echo $cat_detail['description'];
	echo "</a>";
	echo "</td><td class='forum'>";
	$subcats = getSubCategories($cat['id']);
	echo count($subcats)." ";
	lang_forum_sub_cats();
	echo "<br>";
	$cat_msg = getCategoryRootPosts($cat['id']);
	echo getCategoryNbPosts($cat['id'])." ";
	lang_forum_topic_s_();
	echo "</td>";
	if (isAdmin()) {
		echo "<td>";
		echo "<a href='".$forum_dir."/admin.php?action=remove_category&cat_id=".$cat['id']."' onclick='javascript:return confirm(\"Are you sure you want to remove the category ?\");'>";
		echo "<img src='".$forum_dir."/images/del.gif' border=0>";
		echo "</a>";
		echo "</td>";
	}
	echo "</tr>";
}
?>
</table>
<?php } ?>
<?php
if (isAdmin()) {
	echo "<a href='".$forum_dir."/admin.php?action=add_category";
	if (isset($cat_id))
		echo "&cat_id=".$cat_id;
	echo "'>";
	echo "Add new category</a><br>";
}
?>
<br>

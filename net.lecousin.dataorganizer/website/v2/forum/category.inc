<?php
$cat_id = $_GET['cat_id'];
$cats = getSubCategories($cat_id);
$posts = getCategoryRootPosts($cat_id);

printCategoryPath($cat_id);
?>
<h1>DataOrganizer Forum - <?php echo getCategoryName($cat_id); ?></h1>
<p class="content_1">
<?php echo getCategoryDescription($cat_id); ?>
</p>
<br>

<?php include($forum_dir.'/cat_list.inc'); ?>
<?php include($forum_dir.'/cat_posts.inc'); ?>

<?php include($forum_dir.'/login_footer.inc'); ?>

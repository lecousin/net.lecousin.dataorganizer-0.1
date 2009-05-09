<h1><?php lang_forum_new_post(); ?></h1>

<?php if (!isset($_POST['message'])) { ?>
<form action=<?php echo "'".$forum_path."'"; ?> method='post'>
<input type='hidden' name='forumpage' value='newpost'>
<?php echo "<input type='hidden' name='cat_id' value='".$_GET['cat_id']."'>"; ?>
<?php if (isset($_GET['post_id'])) echo "<input type='hidden' name='post_id' value='".$_GET['post_id']."'>"; ?>
<table border=0>
<tr><td><?php lang_forum_your_name(); ?></td><td><input type='text' size=30 name='user'></td></tr>
<tr><td><?php lang_forum_language(); ?></td>
<td>
 <select id='lang' name='lang'>
  <option value='en'>English</option>
  <option value='fr'>Français</option>
 </select>
 <?php lang_forum_try_to_write_in_english(); ?>
</td></tr>
<tr><td><?php lang_forum_your_topic(); ?></td><td><input type='text' size=100 name='title' value=<?php echo "'".(isset($_GET['title'])?"Re: ".$_GET['title']:"")."'"; ?>></td></tr>
<tr><td><?php lang_forum_message(); ?></td><td><textarea cols=80 rows=10 name='message'></textarea></td></tr>
<tr><td colspan=2 align=center><input type='submit' value=<?php echo "'";lang_forum_send();echo "'"; ?></td></tr>
</table>
</form>
<?php 
} else {
	lang_forum_post_in_progress();
	$post_id = isset($_POST['post_id'])?$_POST['post_id']:null;
	$id = createPost($_POST['cat_id'], $_POST['user'], $_POST['lang'], $_POST['title'], $_POST['message'], $post_id);
	if ($id == false) {
		lang_forum_failure();
	} else {
	?>
	<script type='text/javascript'>
	  window.location = <?php echo "'".$forum_path."?forumpage=post&post_id=".$id."'"; ?>
	</script>
	<?php }
}
?>
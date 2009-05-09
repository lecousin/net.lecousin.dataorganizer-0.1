<?php
echo "<form action='".$forum_dir."/login.php' method='post'>";
?>
<?php echo "<input type='hidden' name='forum_path' value='".$forum_path."'>"; ?>
Login:<input type="text" name="login"><br>
Pass:<input type="password" name="pass"><br>
<input type="submit" value="Login">
</form>

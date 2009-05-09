<?php

include ('settings.inc');
include ('functions.inc');

db_connect();
if (!isAdmin()) die("Access denied.");

if ($_GET['action'] == 'remove_category') {
	$cat_id = $_GET['cat_id'];
	removeCategory($cat_id);
	echo "Category removed.";
} elseif ($_GET['action'] == 'add_category') {
	if (isset($_GET['cat_id']))
		$parent = $_GET['cat_id'];
	else
		$parent = null;
	$id = createCategory($parent);
	echo "Category ID ".$id." added.<br>";
	if ($id <> false) {
		echo "<br><a href='admin.php?action=add_category_name&cat_id=".$id."'>Add names</a>";
	}

} elseif ($_GET['action'] == 'add_category_name') {
	if (isset($_GET['description'])) {
		addCategoryName($_GET['cat_id'], $_GET['lang'], $_GET['name'], $_GET['description']);
	}
	echo "Add a name for the category:<br>";
	echo "<form action='admin.php' method='get'>";
	echo "<input type='hidden' name='action' value='add_category_name'>";
	echo "<input type='hidden' name='cat_id' value='".$_GET['cat_id']."'>";
	echo "<select name='lang'>";
	echo "<option id='fr' name='fr' value='fr'>Francais</option>";
	echo "<option id='en' name='en' value='en'>English</option>";
	echo "</select><br>";
	echo "Name:<input type='text' name='name'><br>";
	echo "Description:<input type='text' name='description'><br>";
	echo "<input type='submit' value='Ok'>";
	echo "</form>";
} elseif ($_GET['action'] == 'remove_post') {
	removePost($_GET['post_id']);
}

db_close();

?>

<?php

function open_db() {
	$db = mysql_connect("localhost", "dataorga_me", "5altefj7");
	mysql_select_db("dataorga_me", $db);
	return $db;
}

function create_table($ref, $name, $result) {
	echo "<a name='" . $ref . "'>" . $name . "</a>";
	echo "<table border=1>";
	echo "<tr><th>Version</th><th>Date</th><th>IP</th>";
        if ($ref == 'launch') {
        	echo "<th>Language</th>";
        }
	echo "</tr>";
	while ($row=mysql_fetch_array($result))
    {
      echo "<tr>";
        echo "<td>".$row['version']."</td>";
        echo "<td>".$row['date']."</td>";
        echo "<td>".$row['ip']."</td>";
        if ($ref == 'launch') {
        	echo "<td>".$row['language']."</td>";
        }
      echo '</tr>';
    }
    echo "</table>"; 
}

function doit($db) {
	$do_ver = $_GET['version'];
	$do_type = $_GET['type'];
	$do_lang = $_GET['lang'];
	$user_ip = $_SERVER['REMOTE_ADDR'];
	$date = time();

	if ($do_type == 'install') {
		$query = "INSERT INTO `dataorga_me`.`install` ( `version`, `date`, `ip` ) VALUES ( '" . $do_ver . "', CURRENT_TIMESTAMP, '" . $user_ip . "');";
		$result = mysql_query($query, $db);
		echo 'Install track successfully added';
	} else if ($do_type == 'update') {
		$query = "INSERT INTO `dataorga_me`.`update` ( `version`, `date`, `ip` ) VALUES ( '" . $do_ver . "', CURRENT_TIMESTAMP, '" . $user_ip . "');";
		$result = mysql_query($query, $db);
		echo 'Update track successfully added';
	} else if ($do_type == 'launch') {
		$query = "INSERT INTO `dataorga_me`.`launch` ( `version`, `date`, `ip`, `language` ) VALUES ( '" . $do_ver . "', CURRENT_TIMESTAMP, '" . $user_ip . "', '" . $do_lang . "');";
		$result = mysql_query($query, $db);
		echo 'Launch track successfully added';
	}  else if ($do_type == 'view') {
		$query = "SELECT * FROM `dataorga_me`.`install`";
		$result_install = @mysql_query($query, $db);
		$query = "SELECT * FROM `dataorga_me`.`update`";
		$result_update = @mysql_query($query, $db);
		$query = "SELECT * FROM `dataorga_me`.`launch`";
		$result_launch = @mysql_query($query, $db);
		echo "<a href='#install'>Install Logs</a><br><a href='#update'>Update Logs</a><br><a href='#launch'>Launch Logs</a><br>";
		create_table('install','Install Logs',$result_install);
		create_table('update','Update Logs',$result_update);
		create_table('launch','Launch Logs',$result_launch);
	} else {
		echo 'invalid request type ' . $do_type;
	}
}

$db = open_db();
doit($db);
mysql_close($db);
?>

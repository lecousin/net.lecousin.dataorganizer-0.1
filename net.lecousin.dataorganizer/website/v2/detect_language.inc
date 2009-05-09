<?php
function detect_language($s) {
	$i = strpos($s, ';');
	if ($i <> false) {
		$s = substr($s, 0, $i);
	}
	$values = explode(',', $s);
	$count = count($values);
	for ($i = 0; $i < $count; $i++) {
		$s = strtolower($values[$i]);
		if ($s == 'en') return 'en';
		if (substr($s, 0, 2) == 'en') return 'en';
		if ($s == 'fr') return 'fr';
		if (substr($s, 0, 2) == 'fr') return 'fr';
	}
	return 'en';
}
?>
<div class="cadre_nopadding">
<table class="section_header" width=100% border=0>
<tr><td class="section_header"><center><?php lang_menu(); ?></center></td></tr>
</table>
<table style="padding: 6pt 3pt 3pt 3pt;" width=100% border=0 class="menu"><tr><td>

<?php
if ($page <> 'home') { pagelink('home', $lang); } else { echo "<b>"; }
echo "<img src='images/home.png' border=0>";
lang_home();
if ($page <> 'home') { echo "</a>"; } else { echo "</b>"; }
echo "<br>";

if ($page <> 'download') { pagelink('download', $lang); } else { echo "<b>"; }
echo "<img src='images/download.gif' border=0>";
lang_download();
if ($page <> 'download') { echo "</a>"; } else { echo "</b>"; }
echo "<br>";

if ($page <> 'features') { pagelink('features', $lang); } else { echo "<b>"; }
echo "<img src='images/features.gif' border=0>";
lang_features();
if ($page <> 'features') { echo "</a>"; } else { echo "</b>"; }
echo "<br>";

if ($page <> 'forum') { pagelink('forum', $lang); } else { echo "<b>"; }
echo "<img src='images/forum.png' border=0>";
lang_forum();
if ($page <> 'forum') { echo "</a>"; } else { echo "</b>"; }
echo "<br>";

if ($page <> 'contact') { pagelink('contact', $lang); } else { echo "<b>"; }
echo "<img src='images/mail.gif' border=0>";
lang_contact();
if ($page <> 'contact') { echo "</a>"; } else { echo "</b>"; }
echo "<br>";

?>
</td></tr></table>
</div>
<br>
<center>
<?php pagelink($page,'en'); ?><img src="images/english.gif" border=0></a>
<?php pagelink($page,'fr'); ?><img src="images/french.gif" border=0></a>
</center>
<br>

<div class="cadre_nopadding">
<table class="section_header" width=100% border=0>
<tr><td class="section_header"><center><?php lang_info(); ?></center></td></tr>
</table>
<table style="padding: 6pt 3pt 3pt 3pt;" width=100% border=0 class="info"><tr><td>
<b>2009-05-09</b><br>new web site, new forum<br><br>
<b>2009-05-03</b><br>version 0.4.0 available<br><br>
<b>2009-04-30</b><br>version 0.3.0 available<br><br>
<b>2009-04-20</b><br>version 0.2.0 available<br><br>
<b>2009-04-04</b><br>version 0.1.0 release!
</td></tr></table>
</div>


<?php

?>

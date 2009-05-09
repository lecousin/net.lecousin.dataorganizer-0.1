<?php

function lang_forum_LatestPosts() { echo "Latest posts"; }
function lang_forum_Categories() { echo "Categories"; }
function lang_forum_Posts() { echo "Topics"; }
function lang_forum_topic_s_() { echo "topic(s)"; }
function lang_forum_sub_cats() { echo "sub category(ies)"; }
function lang_forum_homepage() { echo "Forum Home Page"; }

function lang_forum_user() { echo "user"; }
function lang_forum_topic() { echo "topic"; }
function lang_forum_replies() { echo "replies"; }
function lang_forum_state() { echo "state"; }

function lang_forum_new_post() { echo "Post a new topic"; }
function lang_forum_your_name() { echo "Your name"; }
function lang_forum_language() { echo "Language"; }
function lang_forum_try_to_write_in_english() { echo "(Try to write in english so more people can participate)"; }
function lang_forum_your_topic() { echo "Topic"; }
function lang_forum_message() { echo "Message"; }
function lang_forum_send() { echo "Send"; }
function lang_forum_post_in_progress() { echo "Sending topic... please wait..."; }
function lang_forum_failure() { echo "<br><br><div style='color: red;'><strong>Failure. Use back button to retry</strong></div><br>"; } 
function lang_forum_reply() { echo "Reply to this message"; }

function lang_forum_by() { echo "by"; }
function lang_forum_on() { echo "on"; }
function lang_forum_answer_by() { echo "answer by"; }

function lang_forum_poststate($state) {
	if ($state == 'open') return "Open";
	if ($state == 'resolved') return "Resolved";
	if ($state == 'ongoing') return "Ongoing"; 
	return "Unknown";
}

?>

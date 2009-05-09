<?php

function lang_forum_LatestPosts() { echo "Dernières discussions"; }
function lang_forum_Categories() { echo "Catégories"; }
function lang_forum_Posts() { echo "Discussions"; }
function lang_forum_topic_s_() { echo "discussion(s)"; }
function lang_forum_sub_cats() { echo "sous-catégorie(s)"; }
function lang_forum_homepage() { echo "Accueil Forum"; }

function lang_forum_user() { echo "utilisateur"; }
function lang_forum_topic() { echo "sujet"; }
function lang_forum_replies() { echo "réponses"; }
function lang_forum_last_post() { echo "dernier envoi"; }
function lang_forum_state() { echo "état"; }

function lang_forum_new_post() { echo "Créer une nouvelle discussion"; }
function lang_forum_your_name() { echo "Votre nom"; }
function lang_forum_language() { echo "Langue"; }
function lang_forum_try_to_write_in_english() { echo "(Essayez d'ecrire en anglais pour qu'un maximum de personne puisse participer)"; }
function lang_forum_your_topic() { echo "Sujet"; }
function lang_forum_message() { echo "Message"; }
function lang_forum_send() { echo "Envoyer"; }
function lang_forum_post_in_progress() { echo "Envoi du sujet en cours... veuillez patienter..."; }
function lang_forum_failure() { echo "<br><br><div style='color: red;'><strong>Echec. Utilisez le bouton Retour pour réessayer</strong></div><br>"; } 
function lang_forum_reply() { echo "Répondre à ce message"; }

function lang_forum_by() { echo "par"; }
function lang_forum_on() { echo "le"; }
function lang_forum_answer_by() { echo "réponse par"; }

function lang_forum_poststate($state) {
	if ($state == 'open') return "Ouvert";
	if ($state == 'resolved') return "Résolu";
	if ($state == 'ongoing') return "En cours"; 
	return "Inconnu";
}

?>

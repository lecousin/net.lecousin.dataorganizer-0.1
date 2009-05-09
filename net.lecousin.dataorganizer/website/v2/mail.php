<html>
<body>
<?php
$succ = mail("support.dataorganizer@gmail.com", $_POST['subject'], $_POST['from']."\r\n\r\n".$_POST['message']);

if ($_POST['lang'] == 'fr') {
	if ($succ) {
		echo "Votre mail a bien été envoyé. Vous recevrez une réponse dans les meilleurs délais.";
	} else {
		echo "Votre mail n'a pas pu être envoyé. Veuillez réessayer ou utiliser directement votre outil mail habituel et adressez nous votre message à <a href='mailto:support.dataorganizer@gmail.com'>support.dataorganizer@gmail.com</a>";
	}
} else {
	if ($succ) {
		echo "Your mail has been sent successfully. You will receive an answer as soon as possible.";
	} else {
		echo "Your mail cannot be sent. Please retry or use directly your mail client and send us your message to <a href='mailto:support.dataorganizer@gmail.com'>support.dataorganizer@gmail.com</a>";
	}
}
?>
</body>
</html>

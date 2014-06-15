<HTML>
<HEAD>
<TITLE>We're sorry, we could not locate the page you're looking for</TITLE>
<LINK REL=StyleSheet HREF="/404.css" TYPE="text/css">
</HEAD>

<BODY>

<DIV ID="outer">
	<DIV ID="inner">
		<DIV ID="image">
			<A HREF="http://www.mafiascum.net">
			<IMG SRC="/tigerfound.jpg" border="0"></IMG>
			</A>
		</DIV>
	</DIV>
</DIV>

<?php
$to = "mafiascum@gmail.com";
$cc = "dcorbe@gmail.com";
$subject = "404 Generated";
$body = "A 404 error has been generated.\n\n";

$body = $body . "URL: " . $_SERVER['REQUEST_URI'] . "\n";

if ($_SERVER['HTTP_REFERER'] != "")
{
	$body = $body . "REFERER: " . $_SERVER['HTTP_REFERER'] . "\n";
}

#mail($to, $subject, $body);
#mail($cc, $subject, $body);

?>

</BODY>
</HTML>

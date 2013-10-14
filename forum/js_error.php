<?php

if(!isset($_POST['Error']))
	exit;
if(!isset($_POST['URL']))
	exit;
if(!isset($_POST['LineNumber']))
	exit;

$error = $_POST['Error'];
$url = $_POST['URL'];
$lineNumber = $_POST['LineNumber'];
$ipAddress = $_SERVER['REMOTE_ADDR'];
$timestamp = strftime("%Y-%m-%d %H:%M:%S", time());

$file = fopen("js_errors.txt", "a");
if(!$file)
{
	echo("Could not open file.");
	exit;
}
fwrite($file, "$timestamp :: $ipAddress :: $url:$lineNumber :: $error\n");
fclose($file);

?>

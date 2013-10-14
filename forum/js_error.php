<?php

if(!isset($_POST['Error']))
	exit;
if(!isset($_POST['URL']))
	exit;
if(!isset($_POST['LineNumber']))
	exit;
if(!isset($_POST['UserID']))
	exit;
if(!isset($_POST['UserName']))
	exit;
if(!isset($_POST['ScriptName']))
	exit;

$error = $_POST['Error'];
$url = $_POST['URL'];
$userName = $_POST['UserName'];
$userID = $_POST['UserID'];
$lineNumber = $_POST['LineNumber'];
$scriptName = $_POST['ScriptName'];
$ipAddress = $_SERVER['REMOTE_ADDR'];
$timestamp = strftime("%Y-%m-%d %H:%M:%S", time());

$file = fopen("js_errors.txt", "a");
if(!$file)
{
	echo("Could not open file.");
	exit;
}
fwrite($file, "$timestamp :: $ipAddress :: $userID :: $userName :: $scriptName :: $url:$lineNumber :: $error\n");
fclose($file);

?>

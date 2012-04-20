<?php

define('IN_PHPBB', true);
$phpbb_root_path = (defined('PHPBB_ROOT_PATH')) ? PHPBB_ROOT_PATH : './';
$phpEx = substr(strrchr(__FILE__, '.'), 1);
include($phpbb_root_path . 'common.' . $phpEx);
include($phpbb_root_path . 'includes/functions_posting.' . $phpEx);
include($phpbb_root_path . 'includes/functions_display.' . $phpEx);
include($phpbb_root_path . 'includes/message_parser.' . $phpEx);

$user->session_begin();
$auth->acl($user->data);

$post_id	= request_var('p', 0);
$password 	= request_var('password', '');
$offset		= request_var('offset', 0);
$real		= request_var('real', 0);
$fetchSize	= request_var('fetch_size', 0);
$post_id	= request_var('post_id', 0);

$bad_posts = array();

if(strcmp(sha1($password), "9f56e7c19b16a47d338cd658892dc1e1fab49b49")) {

	echo("No");
	exit;
}

if($fetchSize != 0) {
	$sql = " SELECT"
	     . "  post_id,"
	     . "  post_text,"
	     . "  bbcode_uid,"
	     . "  bbcode_bitfield,"
	     . "  enable_bbcode,"
	     . "  enable_smilies,"
	     . "  enable_magic_url,"
	     . "  topic_id,"
	     . "  forum_id"
	     . " FROM phpbb_posts"
	     . " LIMIT $offset,$fetchSize";
}
else {
	$sql = " SELECT"
	     . "  post_id,"
	     . "  post_text,"
	     . "  bbcode_uid,"
	     . "  bbcode_bitfield,"
	     . "  enable_bbcode,"
	     . "  enable_smilies,"
	     . "  enable_magic_url,"
	     . "  topic_id,"
	     . "  forum_id"
	     . " FROM phpbb_posts"
	     . " WHERE post_id=$post_id";
}
$result = $db->sql_query($sql);

while( ($row = $db->sql_fetchrow($result)) )
{
	$topic_id = $row['topic_id'];
	$forum_id = $row['forum_id'];
	$post_id = $row['post_id'];
	
	$message_parser = new parse_message();
	$flash_status = $auth->acl_get('f_flash', $forum_id);
	$img_status   = ($auth->acl_get('f_img', $forum_id)) ? true : false;
	$quote_status	= true;
	
	$message_parser->message = $row['post_text'];

	decode_message($message_parser->message, $row['bbcode_uid']);
	
	$update_message = true;
	$mode = 'display';
	$txt = $message_parser->parse($row['enable_bbcode'], ($config['allow_post_links']) ? $row['enable_magic_url'] : false, $row['enable_smilies'], $img_status, $flash_status, $quote_status, $config['allow_post_links'], $update_message, $mode);
	
//	echo($message_parser->message);
	if(!strcmp($row['bbcode_bitfield'], $message_parser->bbcode_bitfield)) {
	
	}
	else {
	
		echo("$post_id," . $row['bbcode_bitfield'] . "," . $message_parser->bbcode_bitfield . "<><><><>");
	}
}
$db->sql_freeresult($result);


?>
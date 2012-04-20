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
	     . "  phpbb_posts.post_id,"
	     . "  phpbb_posts.post_text,"
	     . "  phpbb_posts.bbcode_uid,"
	     . "  phpbb_posts.bbcode_bitfield,"
	     . "  phpbb_posts.enable_bbcode,"
	     . "  phpbb_posts.enable_smilies,"
	     . "  phpbb_posts.enable_magic_url,"
	     . "  phpbb_posts.topic_id,"
	     . "  phpbb_posts.forum_id"
	     . " FROM (phpbb_posts, tempPostsWithBrokenBBCodeBitfields)"
	     . " LEFT JOIN tempPostsWithDiceTags ON tempPostsWithDiceTags.post_id=phpbb_posts.post_id"
	     . " WHERE phpbb_posts.post_id=tempPostsWithBrokenBBCodeBitfields.post_id"
	     . " AND tempPostsWithDiceTags.post_id IS NULL"
	     . " LIMIT $offset,$fetchSize";
}
else {
	$sql = " SELECT"
	     . "  phpbb_posts.post_id,"
	     . "  phpbb_posts.post_text,"
	     . "  phpbb_posts.bbcode_uid,"
	     . "  phpbb_posts.bbcode_bitfield,"
	     . "  phpbb_posts.enable_bbcode,"
	     . "  phpbb_posts.enable_smilies,"
	     . "  phpbb_posts.enable_magic_url,"
	     . "  phpbb_posts.topic_id,"
	     . "  phpbb_posts.forum_id"
	     . " FROM (phpbb_posts, tempPostsWithBrokenBBCodeBitfields)"
	     . " LEFT JOIN tempPostsWithDiceTags ON tempPostsWithDiceTags.post_id=phpbb_posts.post_id"
	     . " WHERE phpbb_posts.post_id=tempPostsWithBrokenBBCodeBitfields.post_id"
	     . " AND tempPostsWithDiceTags.post_id IS NULL"
	     . " AND phpbb_posts.post_id=$post_id";
}
$result = $db->sql_query($sql);

$sql = " INSERT INTO `tempPostsResubmitted`("
     . "  `post_id`,"
     . "  `post_text`,"
     . "  `post_checksum`,"
     . "  `bbcode_bitfield`,"
     . "  `bbcode_uid`"
     . ") VALUES";

$row_count = 0;
while( ($row = $db->sql_fetchrow($result)) )
{
	++$row_count;
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
	$message_parser->parse($row['enable_bbcode'], ($config['allow_post_links']) ? $row['enable_magic_url'] : false, $row['enable_smilies'], $img_status, $flash_status, $quote_status, $config['allow_post_links'], $update_message, $mode);
	
	$message_checksum = md5($message_parser->message);
	
	if($row_count != 1) {
		$sql .= ",";
	}
	$sql .= "(" . $row['post_id'] . ",'" . $db->sql_escape($message_parser->message) . "','" . $db->sql_escape($message_checksum) . "','" . $db->sql_escape($message_parser->bbcode_bitfield) . "','" . $db->sql_escape($message_parser->bbcode_uid) . "')";
}
$db->sql_freeresult($result);

//echo($sql);

$db->sql_query($sql);

?>
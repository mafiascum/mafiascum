<?php

$forumPath = dirname(__FILE__);
chdir($forumPath);

define('IN_PHPBB', true);
$phpbb_root_path = (defined('PHPBB_ROOT_PATH')) ? PHPBB_ROOT_PATH : './';
$phpEx = substr(strrchr(__FILE__, '.'), 1);
include($phpbb_root_path . 'common.' . $phpEx);
include($phpbb_root_path . 'includes/functions_user.' . $phpEx);

//ITEM_UNLOCKED

$update_arr = array(
	'topic_status'		=> ITEM_LOCKED,
	'autolock_time'		=> 0,
	'autolock_input'	=> ''
);

$sql = 'UPDATE ' . TOPICS_TABLE . '
		SET
			topic_status=IF(topic_status=' . ITEM_UNLOCKED . ',' . ITEM_LOCKED . ',topic_status),
			autolock_time=0,
			autolock_input=""
		WHERE autolock_time <= ' . time() . '
		AND autolock_time != 0';

$db->sql_query($sql);
?>

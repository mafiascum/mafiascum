<?php

$forumPath = dirname(__FILE__);
chdir($forumPath);

define('IN_PHPBB', true);
$phpbb_root_path = (defined('PHPBB_ROOT_PATH')) ? PHPBB_ROOT_PATH : './';
$phpEx = substr(strrchr(__FILE__, '.'), 1);
include($phpbb_root_path . 'common.' . $phpEx);
include($phpbb_root_path . 'includes/functions_user.' . $phpEx);

$sql = " SELECT"
     . "   group_id,"
     . "   user_id"
     . " FROM"
     . " " . USER_GROUP_TABLE
     . " WHERE auto_remove_time != 0"
     . " AND auto_remove_time <= UNIX_TIMESTAMP(NOW())";
     
	$result = $db->sql_query($sql);

	while ($row = $db->sql_fetchrow($result))
	{
		group_user_del($row['group_id'], array($row['user_id']));
	}
	$db->sql_freeresult($result);
?>

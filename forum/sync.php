<?php

define('IN_PHPBB', true);
$phpbb_root_path = (defined('PHPBB_ROOT_PATH')) ? PHPBB_ROOT_PATH : './';
$phpEx = substr(strrchr(__FILE__, '.'), 1);
include($phpbb_root_path . 'common.' . $phpEx);
include($phpbb_root_path . 'includes/functions_admin.' . $phpEx);

//sync("topic", "range", "topic_id BETWEEN 19001 AND 21000", true, true);
sync("forum", "range", "forum_id > 0", true, true);

?>
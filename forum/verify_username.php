<?php
define('IN_PHPBB', true);
$phpbb_root_path = (defined('PHPBB_ROOT_PATH')) ? PHPBB_ROOT_PATH : './';
$phpEx = substr(strrchr(__FILE__, '.'), 1);
require($phpbb_root_path . 'common.' . $phpEx);
require($phpbb_root_path . 'includes/functions_user.' . $phpEx);
$user->session_begin();
$auth->acl($user->data);
$username = $db->sql_escape($_POST['name']);
$field_name = $db->sql_escape($_POST['field_name']);
$private_id = (int)$_POST['id'];
$out = '';
if (!empty($username)){
	$private_users = array();
	$private_users[] = $username;
	$user_id_ary = array();
	user_get_id_name($user_id_ary, $private_users, array(USER_NORMAL, USER_FOUNDER));
	if(!sizeof($user_id_ary)){
		$out = 'false';
	}
	else {
		$out = get_username_string('full', $user_id_ary[0], $username) . '<input name="' . $field_name . '[' . $private_id . ']' . '" id="' . $field_name . '[' . $private_id . ']' . '" value="' . $username  . '" type = "hidden"/>  <button type="button" class="repeatable-remove">x</button>';
	}
	echo $out;
}
?>

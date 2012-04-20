<?php

/** 
*
* install script to set up permission options in the db for foo mod
* @license http://opensource.org/licenses/gpl-license.php GNU Public License 
*
*/

/**
* @ignore
*/

// initialize the page
define('IN_PHPBB', true);
define('IN_INSTALL', true);
$phpbb_root_path = (defined('PHPBB_ROOT_PATH')) ? PHPBB_ROOT_PATH : '';
$phpEx = substr(strrchr(__FILE__, '.'), 1);
include($phpbb_root_path . 'common.' . $phpEx);
include($phpbb_root_path . 'includes/acp/auth.' . $phpEx);
$auth_admin = new auth_admin();

$auth_admin->acl_add_option(array(
    'local'        => array(),
    'global'    => array('a_titlefairy')
));
//test

$message = 'good job';
trigger_error($message);

?>
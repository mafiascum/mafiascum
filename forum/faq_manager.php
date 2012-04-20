<?php
/**
*
* @package phpBB3 FAQ Manager
* @copyright (c) 2007 EXreaction, Lithium Studios
* @license http://opensource.org/licenses/gpl-license.php GNU Public License
*
*/

/**
* @ignore
*/
define('UMIL_AUTO', true);
define('IN_PHPBB', true);
$phpbb_root_path = (defined('PHPBB_ROOT_PATH')) ? PHPBB_ROOT_PATH : './';
$phpEx = substr(strrchr(__FILE__, '.'), 1);
include($phpbb_root_path . 'common.' . $phpEx);
$user->session_begin();
$auth->acl($user->data);
$user->setup();

if (!file_exists($phpbb_root_path . 'umil/umil_auto.' . $phpEx))
{
	trigger_error('Please download the latest UMIL (Unified MOD Install Library) from: <a href="http://www.phpbb.com/mods/umil/">phpBB.com/mods/umil</a>', E_USER_ERROR);
}

$mod_name = 'ACP_FAQ_MANAGER';

$version_config_name = 'faq_manager_version';

$language_file = 'mods/faq_manager';

$versions = array(
	'1.0.0'		=> array(
		'module_add' => array(
			array('acp', 'ACP_CAT_DOT_MODS', 'ACP_FAQ_MANAGER'),
			array('acp', 'ACP_FAQ_MANAGER', array(
					'module_basename'		=> 'faq_manager',
				),
			),
		),
	),
	'1.0.1' => array(),
	'1.0.2' => array(),
	'1.0.3' => array(),
	'1.1.0' => array(),
	'1.1.1' => array(),
	'1.1.2' => array(),
	'1.2.0' => array(),
	'1.2.1' => array(),
	'1.2.2' => array(),
	'1.2.3' => array(),
	'1.2.4' => array(),
	'1.2.5' => array(),
	'1.2.6' => array(),
);

// Include the UMIF Auto file and everything else will be handled automatically.
include($phpbb_root_path . 'umil/umil_auto.' . $phpEx);

?>
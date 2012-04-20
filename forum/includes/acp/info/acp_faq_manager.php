<?php
/**
 *
 * @package phpBB3 FAQ Manager
 * @copyright (c) 2007 EXreaction, Lithium Studios
 * @license http://opensource.org/licenses/gpl-license.php GNU Public License
 *
 */

/**
* @package module_install
*/
class acp_faq_manager_info
{
	function module()
	{
		return array(
			'filename'	=> 'acp_faq_manager',
			'title'		=> 'ACP_FAQ_MANAGER',
			'version'	=> '1.0.0',
			'modes'		=> array(
				'default'	=> array('title' => 'ACP_FAQ_MANAGER', 'auth' => 'acl_a_language', 'cat' => array('ACP_DOT_MODS')),
			),
		);
	}

	function install()
	{
	}

	function uninstall()
	{
	}
}

?>
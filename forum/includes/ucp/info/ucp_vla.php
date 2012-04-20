<?php
/**
*
* @package ucp
* @version $Id$
* @copyright (c) 2005 phpBB Group
* @license http://opensource.org/licenses/gpl-license.php GNU Public License
*
*/

/**
* @package module_install
*/
class ucp_vla_info
{
	function module()
	{
		return array(
			'filename'	=> 'ucp_vla',
			'title'		=> 'UCP_VLA',
			'version'	=> '1.0.0',
			'modes'		=> array(
				'settings'	=> array('title' => 'UCP_VLA_SETTINGS', 'auth' => '', 'cat' => array('UCP_VLA')),
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
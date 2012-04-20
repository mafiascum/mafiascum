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
class ucp_alts_info
{
	function module()
	{
		return array(
			'filename'	=> 'ucp_alts',
			'title'		=> 'UCP_ALTS',
			'version'	=> '1.0.0',
			'modes'		=> array(
				'overview'	=> array('title' => 'UCP_ALTS_OVERVIEW', 'auth' => '', 'cat' => array('UCP_ALTS')),
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
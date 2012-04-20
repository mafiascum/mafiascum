<?php
/**
*
* @package acp
* @version $Id$
* @copyright (c) 2005 phpBB Group
* @license http://opensource.org/licenses/gpl-license.php GNU Public License
*
*/

/**
* @package module_install
*/
class acp_assign_rank_info
{
	function module()
	{
		return array(
			'filename'	=> 'acp_assign_rank',
			'title'		=> 'ACP_TITLE_FAIRY',
			'version'	=> '1.0.0',
			'modes'		=> array(
				'rank'			=> array('title' => 'Title Fairy', 'auth' => 'acl_a_titlefairy', 'cat' => array('ACP_CAT_USERS')),
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
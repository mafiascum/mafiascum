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
* @ignore
*/
if (!defined('IN_PHPBB'))
{
	exit;
}

/**
* ucp_prefs
* Changing user preferences
* @package ucp
*/
class ucp_alts
{
	var $u_action;

	function main($id, $mode)
	{
		global $config, $db, $user, $auth, $template, $phpbb_root_path, $phpEx;

		$submit = (isset($_POST['submit'])) ? true : false;

		switch ($mode)
		{
			case 'settings':
			
				if ($submit)
				{
					

					//Grab submitted variables.
//					$data['vlastart_day'] = request_var('vlastart_day', 0);
					
					
		/*
					$validate_array = array(
						'vlastart_day'		=> array('num', true, 1, 31),
						'vlastart_month'	=> array('num', true, 1, 12),
						'vlastart_year'		=> array('num', true, 2011, gmdate('Y', time()) + 50),
						'vlatill_day'		=> array('num', true, 1, 31),
						'vlatill_month'	=> array('num', true, 1, 12),
						'vlatill_year'		=> array('num', true, 2011, gmdate('Y', time()) + 50),
						'user_vla_start' => array('date', true),
						'user_vla_till' => array('date', true),
					);
		*/
		
		/*
					$error = validate_data($data, $validate_array);

					$template->assign_vars(array(
						'S_VLATILL_DAY_OPTIONS'	=> $s_vlatill_day_options,
						'S_VLATILL_MONTH_OPTIONS'	=> $s_vlatill_month_options,
						'S_VLATILL_YEAR_OPTIONS'	=> $s_vlatill_year_options,
						'S_VLATILL_ENABLED'		=> true,
						'S_VLASTART_DAY_OPTIONS'	=> $s_vlastart_day_options,
						'S_VLASTART_MONTH_OPTIONS'	=> $s_vlastart_month_options,
						'S_VLASTART_YEAR_OPTIONS'	=> $s_vlastart_year_options,
						'S_VLASTART_ENABLED'		=> true,
					));
		*/
			}

			break;
		}

		$user_alt_data = UserAltData::getAlts($user->data['user_id']);
					
		$user_alt_data->loadAltUserData();
					
		$alt_user_id_array = $user_alt_data->getAllAlts();
					
		foreach($alt_user_id_array as $alt_user_id) {
						
			$alt_row = $user_alt_data->getAltUserData($alt_user_id);

			$type = 'Main';
						
			if($user_alt_data->hasAlt($alt_row['user_id'])) {
						
				if($alt_row['is_hydra']) {
								
					$type = 'Hydra';
				}
				else {
								
					$type = 'Alt';
				}
			}
						
			$template->assign_block_vars('user_alt', array(
						
				'USER_ID'		=>	$alt_row['user_id'],
				'USER_URL'		=>	get_username_string('full', $alt_row['user_id'],$alt_row['username'],$alt_row['user_colour']),
				'TYPE'			=>	$type,
				'IS_APPROVED'	=>	$alt_row['is_approved'] ? 1 : 0,
			));
			
			echo("Approved: " . $alt_row['is_approved'] . "</br>");
						
		}
		exit;

		$template->assign_vars(array(
			'L_TITLE'			=> $user->lang['UCP_ALTS_' . strtoupper($mode)],

			'S_UCP_ACTION'		=> $this->u_action)
		);

		$this->tpl_name = 'ucp_alts_' . $mode;
		$this->page_title = 'UCP_ALTS_' . strtoupper($mode);
	}
		
}

?>
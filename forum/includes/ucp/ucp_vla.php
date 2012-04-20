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
class ucp_vla
{
	var $u_action;

	function main($id, $mode)
	{
		global $config, $db, $user, $auth, $template, $phpbb_root_path, $phpEx;

		$submit = (isset($_POST['submit'])) ? true : false;
		$clearVLA = (isset($_POST['clearVLA'])) ? true : false;
		$error = $data = array();
		$s_hidden_fields = '';

		switch ($mode)
		{
			case 'settings':
				add_form_key('ucp_vla_settings');
				
				//Initial VLA status determination.
				$onVLA = false;
				//Static timestamp for date validations.
				$mkStatic = mktime(0,0,0,0,0,0);
				//Initial date variables.
				$data['vlastart_day'] = $data['vlastart_month'] = $data['vlastart_year'] = 0;
				$data['vlatill_day'] = $data['vlatill_month'] = $data['vlatill_year'] = 0;
				
				//Clear VLA if requested.	
				if($clearVLA)
				{
					$this->setVLA($user->data['user_id'], '', '');
					
					meta_refresh(3, $this->u_action);
					$message = $user->lang['PREFERENCES_UPDATED'] . '<br /><br />' . sprintf($user->lang['RETURN_UCP'], '<a href="' . $this->u_action . '">', '</a>');
					trigger_error($message);
				}
				//If a V/LA has been submitted, handle that.
				else if ($submit)
				{
					//Grab submitted variables.
					$data['vlastart_day'] = request_var('vlastart_day', 0);
					$data['vlastart_month'] = request_var('vlastart_month', 0);
					$data['vlastart_year'] = request_var('vlastart_year', 0);
					
					$data['user_vla_start'] = sprintf('%2d-%2d-%4d', $data['vlastart_day'], $data['vlastart_month'], $data['vlastart_year']);
					$data['vlatill_day'] = request_var('vlatill_day', 0);
					$data['vlatill_month'] = request_var('vlatill_month', 0);
					$data['vlatill_year'] = request_var('vlatill_year', 0);

					$data['user_vla_till'] = sprintf('%2d-%2d-%4d', $data['vlatill_day'], $data['vlatill_month'], $data['vlatill_year']);
					
					
					//Validate the submitted dates.
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
					
					$error = validate_data($data, $validate_array);
					
					//Make sure that the VLA dates are legal for our purposes.
					
					//Start Date timestamp for date comparison.
					$mkStart =  mktime(0,0,0,$data['vlastart_month'],$data['vlastart_day'],$data['vlastart_year']);
					//End Date timestamp for date comparison.
					$mkEnd =  mktime(23,59,59,$data['vlatill_month'],$data['vlatill_day'],$data['vlatill_year']);
					
					
					//Make sure that every variable is set properly.
					if(($data['vlastart_day'] === 0) || ($data['vlastart_month'] === 0) || ($data['vlastart_year'] === 0) || 
						($data['vlatill_day'] === 0) || ($data['vlatill_month'] === 0) || ($data['vlatill_year'] === 0))
					{
						$error[] = 'TOO_SMALL';
					}
					//Make sure both dates exist.
					else if($mkStart == $mkStatic || $mkEnd == $mkStatic)
					{
						$error[] = 'NO_VLA_DATA';
					}
					
					//Make sure that the end date is after the start date.
					else if($mkStart >= $mkEnd)
					{
						$error[] = 'MISMATCHED_VLA_DATE';
					}
					
					//Make sure that the end date is after the start date.
					//else if($mkStart < time() || $mkEnd < time())
					else if($mkEnd < time())
					{
						$error[] = 'VLA_DATE_PRIOR';
					}
					
					//Make sure that vla is at least three days.
					else if(($mkEnd - $mkStart) < 259000)
					{
						$error[] = 'VLA_TOO_SMALL';
					}
					//Make sure that vla is no longer than two months.
					else if(($mkEnd - $mkStart) > 5259487)
					{
						$error[] = 'VLA_TOO_LARGE';
					}
					
						
					//Check the form key.
					if (!check_form_key('ucp_vla_settings'))
					{
						$error[] = 'FORM_INVALID';
					}

					//If error-free, continue.
					if (!sizeof($error))
					{
						$this->setVLA($user->data['user_id'], $data['user_vla_start'], $data['user_vla_till']);

						meta_refresh(3, $this->u_action);
						$message = $user->lang['PREFERENCES_UPDATED'] . '<br /><br />' . sprintf($user->lang['RETURN_UCP'], '<a href="' . $this->u_action . '">', '</a>');
						trigger_error($message);
					}

					// Replace "error" strings with their real, localised form
					$error = preg_replace('#^([A-Z_]+)$#e', "(!empty(\$user->lang['\\1'])) ? \$user->lang['\\1'] : '\\1'", $error);
				}
				
				//Get display data for VLA dates.
				

				//Check if V/LA start is already defined, if so use that.
				if ($user->data['user_vla_start'])
				{
					list($data['vlastart_day'], $data['vlastart_month'], $data['vlastart_year']) = explode('-', $user->data['user_vla_start']);
				}
				//Check if V/LA end is already defined, if so use that.
				if ($user->data['user_vla_till'])
				{
					list($data['vlatill_day'], $data['vlatill_month'], $data['vlatill_year']) = explode('-', $user->data['user_vla_till']);
				}
				
				
				//Check if the V/LA is already finished. If so, reset the VLA dates.
				//End Date timestamp for date comparison.
				$mkEnd =  mktime(23,59,59,$data['vlatill_month'],$data['vlatill_day'],$data['vlatill_year']);
				
				if($mkEnd < time() && $mkEnd != $mkStatic)
				{
					$data['vlatill_day'] = $data['vlatill_month'] = $data['vlatill_year'] = 0;
					$data['vlastart_day'] = $data['vlastart_month'] = $data['vlastart_year'] = 0;
					$this->setVLA($user->data['user_id'], '', '');
				}
				else
				{
					$onVLA = ($mkEnd == $mkStatic) ? false : true;
				}

				//Assign the error messages and the V/LA status.
				$template->assign_vars(array(
					'ERROR'				=> (sizeof($error)) ? implode('<br />', $error) : '',
					'S_IS_VLA'			=> $onVLA,
				));
				
				//Assign the available V/LA dates for selection.

					//Start Dates.
					$s_vlastart_day_options = '<option value="0"' . ((!$data['vlastart_day']) ? ' selected="selected"' : '') . '>--</option>';
						for ($i = 1; $i < 32; $i++)
						{
							$selected = ($i == $data['vlastart_day']) ? ' selected="selected"' : '';
							$s_vlastart_day_options .= "<option value=\"$i\"$selected>$i</option>";
						}
	
						$s_vlastart_month_options = '<option value="0"' . ((!$data['vlastart_month']) ? ' selected="selected"' : '') . '>--</option>';
						for ($i = 1; $i < 13; $i++)
						{
							$selected = ($i == $data['vlastart_month']) ? ' selected="selected"' : '';
							$s_vlastart_month_options .= "<option value=\"$i\"$selected>$i</option>";
						}
						$s_vlastart_year_options = '';
	
						$now = getdate();
						$s_vlastart_year_options = '<option value="0"' . ((!$data['vlastart_year']) ? ' selected="selected"' : '') . '>--</option>';
						for ($i = $now['year']; $i <= ($now['year'] + 1); $i++)
						{
							$selected = ($i == $data['vlastart_year']) ? ' selected="selected"' : '';
							$s_vlastart_year_options .= "<option value=\"$i\"$selected>$i</option>";
						}
						unset($now);
						
					//End Dates.
					$s_vlatill_day_options = '<option value="0"' . ((!$data['vlatill_day']) ? ' selected="selected"' : '') . '>--</option>';
					for ($i = 1; $i < 32; $i++)
					{
						$selected = ($i == $data['vlatill_day']) ? ' selected="selected"' : '';
						$s_vlatill_day_options .= "<option value=\"$i\"$selected>$i</option>";
					}

					$s_vlatill_month_options = '<option value="0"' . ((!$data['vlatill_month']) ? ' selected="selected"' : '') . '>--</option>';
					for ($i = 1; $i < 13; $i++)
					{
						$selected = ($i == $data['vlatill_month']) ? ' selected="selected"' : '';
						$s_vlatill_month_options .= "<option value=\"$i\"$selected>$i</option>";
					}
					$s_vlatill_year_options = '';

					$now = getdate();
					$s_vlatill_year_options = '<option value="0"' . ((!$data['vlatill_year']) ? ' selected="selected"' : '') . '>--</option>';
					for ($i = $now['year'] ; $i <= ($now['year'] + 1); $i++)
					{
						$selected = ($i == $data['vlatill_year']) ? ' selected="selected"' : '';
						$s_vlatill_year_options .= "<option value=\"$i\"$selected>$i</option>";
					}
					unset($now);

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
				

			break;
		}
			

		$template->assign_vars(array(
			'L_TITLE'			=> $user->lang['UCP_VLA_' . strtoupper($mode)],

			'S_HIDDEN_FIELDS'	=> $s_hidden_fields,
			'S_UCP_ACTION'		=> $this->u_action)
		);

		$this->tpl_name = 'ucp_vla_' . $mode;
		$this->page_title = 'UCP_VLA_' . strtoupper($mode);
	}
	
	function setVLA($userid, $vlastart, $vlaend)
	{
		global $db;
		
		$sql_ary = array(
			'user_vla_start'		=> (string) $vlastart,
			'user_vla_till'		=> (string) $vlaend,
		);
	
		$sql = 'UPDATE ' . USERS_TABLE . '
			SET ' . $db->sql_build_array('UPDATE', $sql_ary) . '
			WHERE user_id = ' . $userid;
		$db->sql_query($sql);
	}
		
}

?>
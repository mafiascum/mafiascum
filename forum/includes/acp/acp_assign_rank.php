<?php
if (!defined('IN_PHPBB'))
{
	exit;
}

/**
* @package acp
*/
class acp_assign_rank
{
	var $u_action;
	var $p_master;

	function acp_users(&$p_master)
	{
		$this->p_master = &$p_master;
	}

	function main($id, $mode)
	{
		global $config, $db, $user, $auth, $template, $cache;
		global $phpbb_root_path, $phpbb_admin_path, $phpEx, $table_prefix, $file_uploads;

		$user->add_lang(array('posting', 'ucp', 'acp/assign_rank'));
		$this->tpl_name = 'acp_assign_rank';
		$this->page_title = 'ACP_ASSIGN_RANK';

		$error		= array();
		$username	= utf8_normalize_nfc(request_var('username', '', true));
		$user_id	= request_var('u', 0);
		$action		= request_var('action', '');

		$submit		= (isset($_POST['update']) && !isset($_POST['cancel'])) ? true : false;

		$form_name = 'acp_users';
		add_form_key($form_name);

		// Whois (special case)
		if ($action == 'whois')
		{
			include($phpbb_root_path . 'includes/functions_user.' . $phpEx);

			$this->page_title = 'WHOIS';
			$this->tpl_name = 'simple_body';

			$user_ip = request_var('user_ip', '');
			$domain = gethostbyaddr($user_ip);
			$ipwhois = user_ipwhois($user_ip);

			$template->assign_vars(array(
				'MESSAGE_TITLE'		=> sprintf($user->lang['IP_WHOIS_FOR'], $domain),
				'MESSAGE_TEXT'		=> nl2br($ipwhois))
			);

			return;
		}

		// Stop Forum Spam Check (special case)
		if ($action == 'sfs_check')
		{
			include($phpbb_root_path . 'includes/functions_user.' . $phpEx);
			include($phpbb_root_path . 'includes/functions_sfs.' . $phpEx);
			$this->page_title = 'STOP FORUM SPAM CHECK';
			$this->tpl_name = 'simple_body';

			$user_ip = request_var('user_ip', '');
			$ipResults = sfs_check($user_ip, 1);

			$template->assign_vars(array(
				'MESSAGE_TITLE'        => sprintf($user->lang['SFS_CHECK_FOR'], $user_ip),
				'MESSAGE_TEXT'        => ($ipResults) ? (($ipResults['ip']['appears']) ? 'The IP Address ' . $user_ip . ' appears ' . $ipResults['ip']['frequency'] . ' times.' : 'Does not appear.') : 'Check failed.')
			);

			return;
		}

		// Show user selection mask
		if (!$username && !$user_id)
		{
			$this->page_title = 'SELECT_USER';

			$template->assign_vars(array(
				'U_ACTION'			=> $this->u_action,
				'ANONYMOUS_USER_ID'	=> ANONYMOUS,

				'S_SELECT_USER'		=> true,
				'U_FIND_USERNAME'	=> append_sid("{$phpbb_root_path}memberlist.$phpEx", 'mode=searchuser&amp;form=select_user&amp;field=username&amp;select_single=true'),
			));

			return;
		}

		if (!$user_id)
		{
			$sql = 'SELECT user_id
				FROM ' . USERS_TABLE . "
				WHERE username_clean = '" . $db->sql_escape(utf8_clean_string($username)) . "'";
			$result = $db->sql_query($sql);
			$user_id = (int) $db->sql_fetchfield('user_id');
			$db->sql_freeresult($result);

			if (!$user_id)
			{
				trigger_error($user->lang['NO_USER'] . adm_back_link($this->u_action), E_USER_WARNING);
			}
		}

		// Generate content for all modes
		$sql = 'SELECT u.*, s.*
			FROM ' . USERS_TABLE . ' u
				LEFT JOIN ' . SESSIONS_TABLE . ' s ON (s.session_user_id = u.user_id)
			WHERE u.user_id = ' . $user_id . '
			ORDER BY s.session_time DESC';
		$result = $db->sql_query_limit($sql, 1);
		$user_row = $db->sql_fetchrow($result);
		$db->sql_freeresult($result);

		if (!$user_row)
		{
			trigger_error($user->lang['NO_USER'] . adm_back_link($this->u_action), E_USER_WARNING);
		}

		// Generate overall "header" for user admin
		$s_form_options = '';


		$template->assign_vars(array(
			'U_BACK'			=> $this->u_action,
			'U_ACTION'			=> $this->u_action . '&amp;u=' . $user_id,
			'S_FORM_OPTIONS'	=> $s_form_options,
		));

		// Prevent normal users/admins change/view founders if they are not a founder by themselves
		if ($user->data['user_type'] != USER_FOUNDER && $user_row['user_type'] == USER_FOUNDER)
		{
			trigger_error($user->lang['NOT_MANAGE_FOUNDER'] . adm_back_link($this->u_action), E_USER_WARNING);
		}
		switch ($mode)
		{			
			case 'rank' : 	
				if ($submit)
				{
					if (!check_form_key($form_name))
					{
						trigger_error($user->lang['FORM_INVALID'] . adm_back_link($this->u_action . '&amp;u=' . $user_id), E_USER_WARNING);
					}
	
					$rank_id = request_var('user_rank', 0);
	
					$sql = 'UPDATE ' . USERS_TABLE . "
						SET user_rank = $rank_id
						WHERE user_id = $user_id";
					$db->sql_query($sql);

					trigger_error($user->lang['USER_RANK_UPDATED'] . adm_back_link($this->u_action . '&amp;u=' . $user_id));
				}
	
				$sql = 'SELECT *
					FROM ' . RANKS_TABLE . '
					WHERE rank_special = 1
					ORDER BY rank_title';
				$result = $db->sql_query($sql);
	
				$s_rank_options = '<option value="0"' . ((!$user_row['user_rank']) ? ' selected="selected"' : '') . '>' .	$user->lang['NO_SPECIAL_RANK'] . '</option>';

				while ($row = $db->sql_fetchrow($result))				
				{
					$selected = ($user_row['user_rank'] && $row['rank_id'] == $user_row['user_rank']) ? ' selected="selected"' : '';
					$s_rank_options .= '<option value="' . $row['rank_id'] . '"' . $selected . '>' . $row['rank_title'] . '</option>';
				}
				$template->assign_vars(array(
					'S_RANK_OPTIONS'	=> $s_rank_options,
					'MANAGED_USERNAME'	=> $user_row['username'])
				);
				$db->sql_freeresult($result);


			break;
		}
		$template->assign_vars(array(
			'S_ERROR'			=> (sizeof($error)) ? true : false,
			'ERROR_MSG'			=> (sizeof($error)) ? implode('<br />', $error) : '')
		);
	}
}
?>
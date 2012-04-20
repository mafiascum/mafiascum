<?php
/**
*
* @package pm_search
* @version $Id: functions_pm_search.php 6 2009-11-15 08:32:26Z jari $
* @copyright (c) 2009 Jari Kanerva tumba25@gmail.com
* @license http://opensource.org/licenses/gpl-2.0.php GNU Public License version 2
*
* functions_pm_search.php
*
*/

/**
* DO NOT CHANGE
*/
if (!defined('IN_PHPBB'))
{
	exit;
}

include($phpbb_root_path . 'includes/pm_search/constants_pm_search.' . $phpEx);

/**
* pm_search_move_pm
*
* Moves PM's from search results. Handles multiple source folders
*/
function pm_search_move_pm($msg_ids, $dest_folder)
{
	global $db, $user, $phpbb_root_path, $phpEx;

	$user_id = $user->data['user_id'];
	$message_limit = $user->data['message_limit'];

	$folders = request_var('marked_msg_folder', array(0));
	$pm_search_return = utf8_normalize_nfc(request_var('pm_search_return', '', true));
	$redirect = append_sid("{$phpbb_root_path}ucp.$phpEx", $pm_search_return);

	if(!sizeof($folders) || !sizeof($msg_ids) || $dest_folder == PRIVMSGS_OUTBOX)
	{
		redirect($redirect);
	}

	foreach($msg_ids as $key => $value)
	{
		if($folders[$key] != $dest_folder && $folders[$key] != PRIVMSGS_OUTBOX && $folders[$key] != PRIVMSGS_OUTBOX)
		{
			move_pm($user_id, $message_limit, array($value), $dest_folder, $folders[$key]);
		}
	}
	redirect($redirect);
}

/**
* handle_pm_search_actions
*
* Handles actions with marked messages in search-results
* handle_mark_actions don't support multiple folders
*/
function handle_pm_search_actions($mark_action, $msg_ids)
{
	global $db, $user, $phpbb_root_path, $phpEx;

	$user_id = $user->data['user_id'];

	$folders = request_var('marked_msg_folder', array(0));
	$pm_search_return = utf8_normalize_nfc(request_var('pm_search_return', '', true));

	if(!sizeof($folders))
	{
		return false;
	}

	if($mark_action == 'mark_important')
	{
		$sql_where = '';
		foreach($msg_ids as $key => $value)
		{
			$sql_where .= (($sql_where == '') ? ' ' : ' OR') . ' (user_id = ' . $user_id . ' AND folder_id = ' . $folders[$key] . ' AND  msg_id = ' . $value . ')';
		}
		$sql = 'UPDATE ' . PRIVMSGS_TO_TABLE . ' SET pm_marked = 1 - pm_marked WHERE' . $sql_where;
		$db->sql_query($sql);
		redirect(append_sid("{$phpbb_root_path}ucp.$phpEx", $pm_search_return));
	}
	else if($mark_action == 'delete_marked')
	{
		if (confirm_box(true))
		{
			foreach($msg_ids as $key => $value)
			{
				delete_pm($user_id, array($value), $folders[$key]);
			}
			$success_msg = (sizeof($msg_ids) == 1) ? 'MESSAGE_DELETED' : 'MESSAGES_DELETED';

			$redirect = append_sid("{$phpbb_root_path}ucp.$phpEx", $pm_search_return);
			meta_refresh(3, $redirect);
			trigger_error($user->lang[$success_msg] . '<br /><br />' . sprintf($user->lang['RETURN_FOLDER'], '<a href="' . $redirect . '">', '</a>'));
		}
		else
		{
			$s_hidden_fields = array(
				'marked_msg_folder'	=> $folders,
				'mark_option'	=> 'delete_marked',
				'in_pm_search' => true,
				'submit_mark'	=> true,
				'marked_msg_id'	=> $msg_ids,
				'pm_search_return' => $pm_search_return,
			);
			confirm_box(false, 'DELETE_MARKED_PM', build_hidden_fields($s_hidden_fields));
		}
	}
	else
	{
		return(false);
	}
	return(true);
}

/**
* pm_search_hilit
*
* function for hiliting search keywords, mostly copied from viewtopic.php
*/
function pm_search_hilit($message)
{
	global $user, $db, $template, $config, $auth, $phpbb_root_path, $phpEx;

	$hilit_words = request_var('hilit', '', true);
	$match = request_var('match', PM_SEARCH_MATCH_ALL);

	$highlight_match = '';
	if($hilit_words)
	{
		if($match == PM_SEARCH_MATCH_EXACT)
		{
			$highlight_match = str_replace('%20', ' ', $hilit_words);
		}
		else
		{
			foreach(explode(' ', trim($hilit_words)) as $word)
			{
				if(trim($word))
				{
					$word = str_replace('\*', '\w+?', preg_quote($word, '#'));
					$word = preg_replace('#(^|\s)\\\\w\*\?(\s|$)#', '$1\w+?$2', $word);
					$highlight_match .= (($highlight_match != '') ? '|' : '') . $word;
				}
			}
		}
	}

	if($highlight_match)
	{
		if($match == PM_SEARCH_MATCH_WORD)
		{
			$message = preg_replace('#(?!<.*)(?<!\w)(' . $highlight_match . ')(?!\w|[^<>]*(?:</s(?:cript|tyle))?>)#is', '<span class="posthilit">\1</span>', $message);
		}
		else
		{
			$message = preg_replace('#(?!<.*)(?<!\.)(' . $highlight_match . ')(?!\.|[^<>]*(?:</s(?:cript|tyle))?>)#is', '<span class="posthilit">\1</span>', $message);
		}
	}

	return($message);
}

/**
* pm_search_viewfolder_setup
*
* Setup for includes/ucp/ucp_pm_viewfolder.php
*/
function pm_search_viewfolder_setup($folder_id, $start)
{
	global $user, $db, $template, $config, $auth, $phpbb_root_path, $phpEx;

	if(!$auth->acl_get('u_pm_search'))
	{
		return('');
	}

	$pm_search_enabled = is_pm_search();

	$user->add_lang(array('search', 'mods/pm_search'));
	$pm_advanced = request_var('pmsearch', '');

	$tpl_vars = array(
		'S_DISPLAY_SEARCHBOX'	=> ($auth->acl_get('u_search') && $config['load_search'] && $pm_search_enabled) ? true : false,
		'S_PM_SEARCHBOX_ACTION'	=> append_sid("{$phpbb_root_path}ucp.$phpEx", "mode=view&amp;action=view_folder&amp;f=$folder_id" . (($start !== 0) ? "&amp;start=$start" : '') . (($pm_advanced == '') ? '&amp;pmsearch=advanced' : '')),
		'SEARCH_KEYWORDS' => $user->lang['SEARCH_PM_FOLDERS'],
		'S_PM_SEARCH_ADVANCED' => append_sid("{$phpbb_root_path}ucp.$phpEx", "i=pm&amp;mode=view&amp;action=view_folder&amp;f=$folder_id" . (($start !== 0) ? "&amp;start=$start" : '') . (($pm_advanced == '') ? '&amp;pmsearch=advanced' : '')),
		'DISPLAY_ADVANCED' => ($pm_advanced == '') ? 'none' : 'block',

		'C_PM_SENDER' => PM_SENDER,
		'C_PM_RECIPIENT' => PM_RECIPIENT,
		'C_PM_BOTH' => PM_BOTH,
		'C_PM_SUBJECT' => PM_SUBJECT,
		'C_PM_MESSAGE' => PM_MESSAGE,

		'S_SEARCH_ALL_WORDS' => TRUE,
		'S_WITHIN_BOTH' => TRUE,
	);

	return($tpl_vars);
}

/**
* is_pm_search
*
* Check if pm_search module is active.
*/
function is_pm_search()
{
	global $db;

	// Check if the search-module is active
	$sql = 'SELECT module_enabled FROM ' . MODULES_TABLE . '
		WHERE module_basename = "pm"
		AND module_mode = "search"';
	$result = $db->sql_query($sql);
	$pm_search_enabled = (int) $db->sql_fetchfield('module_enabled');
	$db->sql_freeresult($result);

	return($pm_search_enabled);
}

?>
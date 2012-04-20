<?php
/**
*
* @package ucp
* @version $Id: pm_search.php 6 2009-11-15 08:32:26Z jari $
* @copyright (c) 2009 Jari Kanerva tumba25@gmail.com
* @license http://opensource.org/licenses/gpl-2.0.php GNU Public License version 2
*
* ucp_pm_search.php
*
*/

/**
* @ignore
*/

if(!defined('IN_PHPBB'))
{
	exit;
}

// Add the languagefiles,
$user->add_lang(array('viewforum', 'search', 'mods/pm_search'));

// Is the user allowed to perform searches?
if(!$auth->acl_get('u_pm_search'))
{
	trigger_error('NO_AUTH_PM_SEARCH');
}

// Get the functions file.
// Just check that it's not included already
if(!defined('IN_PM_SEARCH'))
{
	include($phpbb_root_path . 'includes/pm_search/functions_pm_search.' . $phpEx);
}

// Get the query so we'll have something to do
$keywords = utf8_normalize_nfc(trim(request_var('keywords', '', true)));

// Do we have a folder?
if($folder_specified)
{
	$folder_id = $folder_specified;
	$action = 'view_folder';
}
else
{
	$folder_id = request_var('f', PRIVMSGS_NO_BOX);
	$action = request_var('action', 'view_folder');
}

// If the user don't have JavaScript on, we need to know if the advanced search is activated
$advanced_search = request_var('pmsearch', '');

// Get the search-keys
$search_exact = request_var('exact', 0); // Exact phrase match
$search_case = request_var('case', 0); // Case sensitive searches
$search_current = request_var('current', 0); // Search in current or all folders
$search_author = request_var('author', 0); // Search for sender, recipient, both or message content
$search_all_words = request_var('all_words', 0); // Search for all words. Equals a + sign in front of every word
$search_word = request_var('word', 0); // Search only for whole words. Not partial words
$search_within = request_var('within', PM_BOTH); // Search in subject, message or both.

// Get the page to show
$start = request_var('start', 0);

// Get the sorting-keys
$sort_days	= request_var('st', 0);
$sort_key	= request_var('sk', 't');
$sort_dir	= request_var('sd', 'd');

$sort_by_author = ($sort_key == 'a') ? TRUE : FALSE;

// We do not continue if there are no keywords to find
if($keywords == '')
{
	redirect(append_sid("{$phpbb_root_path}ucp.$phpEx", "i=pm&amp;mode=view&amp;action=view_folder&amp;f=$folder_id" . (($start !== 0) ? "&amp;start=$start" : '')));
}

$limit_days = array(0 => $user->lang['ALL_MESSAGES'], 1 => $user->lang['1_DAY'], 7 => $user->lang['7_DAYS'], 14 => $user->lang['2_WEEKS'], 30 => $user->lang['1_MONTH'], 90 => $user->lang['3_MONTHS'], 180 => $user->lang['6_MONTHS'], 365 => $user->lang['1_YEAR']);

$sort_by_text = array('a' => $user->lang['AUTHOR'], 't' => $user->lang['POST_TIME'], 's' => $user->lang['SUBJECT']);
$sort_by_sql = array('a' => 'u.username_clean', 't' => 'p.msg_id', 's' => 'p.message_subject');

$s_limit_days = $s_sort_key = $s_sort_dir = $u_sort_param = '';
gen_sort_selects($limit_days, $sort_by_text, $sort_days, $sort_key, $sort_dir, $s_limit_days, $s_sort_key, $s_sort_dir, $u_sort_param);

if($sort_days)
{
	$sql_limit_time = ' AND p.message_time >= ' . (time() - ($sort_days * 86400));

	if(isset($_POST['sort']))
	{
		$start = 0;
	}
}
else
{
	$sql_limit_time = '';
}

global $cache;

// Grab icons
$icons = $cache->obtain_icons();

$color_rows = array('marked', 'replied');

// only show the friend/foe color rows if the module is enabled
$zebra_enabled = false;

$_module = new p_master();
$_module->list_modules('ucp');
$_module->set_active('zebra');

$zebra_enabled = ($_module->active_module === false) ? false : true;

unset($_module);

if($zebra_enabled)
{
	$color_rows = array_merge($color_rows, array('friend', 'foe'));
}

foreach($color_rows as $var)
{
	$template->assign_block_vars('pm_colour_info', array(
		'IMG'	=> $user->img("pm_{$var}", ''),
		'CLASS'	=> "pm_{$var}_colour",
		'LANG'	=> $user->lang[strtoupper($var) . '_MESSAGE'])
	);
}

$mark_options = array('mark_important', 'delete_marked');

$s_mark_options = '';
foreach ($mark_options as $mark_option)
{
	$s_mark_options .= '<option value="' . $mark_option . '">' . $user->lang[strtoupper($mark_option)] . '</option>';
}

$folder = get_folder($user->data['user_id'], $folder_id);

// We do the folder moving options here too, for template authors to use...
$s_folder_move_options = '';
if($folder_id != PRIVMSGS_NO_BOX && $folder_id != PRIVMSGS_OUTBOX)
{
	foreach($folder as $f_id => $folder_ary)
	{
		if($f_id == PRIVMSGS_OUTBOX || $f_id == PRIVMSGS_SENTBOX)
		{
			continue;
		}

		$s_folder_move_options .= '<option' . (($f_id != PRIVMSGS_INBOX) ? ' class="sep"' : '') . ' value="' . $f_id . '">';
		$s_folder_move_options .= sprintf($user->lang['MOVE_MARKED_TO_FOLDER'], $folder_ary['folder_name']);
		$s_folder_move_options .= (($folder_ary['unread_messages']) ? ' [' . $folder_ary['unread_messages'] . '] ' : '') . '</option>';
	}
}
$friend = $foe = array();

// Get friends and foes
$sql = 'SELECT *
	FROM ' . ZEBRA_TABLE . '
	WHERE user_id = ' . $user->data['user_id'];
$result = $db->sql_query($sql);
while($row = $db->sql_fetchrow($result))
{
	$friend[$row['zebra_id']] = $row['friend'];
	$foe[$row['zebra_id']] = $row['foe'];
}
$db->sql_freeresult($result);

$template->assign_vars(array(
	'S_MARK_OPTIONS'		=> $s_mark_options,
	'S_MOVE_MARKED_OPTIONS'	=> $s_folder_move_options)
);

// Don't sure if we need the PRIVMSGS_NO_BOX here because the folder for PM's in that folder changes to PRIVMSGS_INBOX when you enter any of your PM-folders. So you should not be able to access the pm-search with pm's still in that folder. hmm
$search_folder = ($search_current) ? ' AND t.folder_id = ' . $folder_id : 'AND t.folder_id != ' . PRIVMSGS_HOLD_BOX .' AND t.folder_id != ' . PRIVMSGS_NO_BOX;

// Assign some variables for later use.
$sql_search = $hilit = '';
$receiver_cache = $folder_name_cache = array();

// Create the query
if($search_exact && !$search_author)
{
	// Make the hilit string
	$hilit = $keywords;

	// Exact query-search. This is the easy one. :)
	$sql_user = 't.user_id = ' . $user->data['user_id'];
	if($search_within == PM_MESSAGE)
	{
		$sql_search = ($search_case) ? ' AND p.message_text REGEXP "[[:<:]](' . $keywords . ')[[:>:]]"' : ' AND UPPER(p.message_text) REGEXP UPPER("[[:<:]](' . $keywords . ')[[:>:]]")';
	}
	else if($search_within == PM_SUBJECT)
	{
		$sql_search = ($search_case) ? ' AND p.message_subject REGEXP "[[:<:]](' . $keywords . ')[[:>:]]"' : ' AND UPPER(p.message_subject) REGEXP UPPER("[[:<:]](' . $keywords . ')[[:>:]]")';
	}
	else
	{
		$sql_search = ($search_case) ? ' AND (p.message_text REGEXP "[[:<:]](' . $keywords . ')[[:>:]]" OR p.message_subject REGEXP "[[:<:]](' . $keywords . ')[[:>:]]")' : ' AND (UPPER(p.message_text) REGEXP UPPER("[[:<:]](' . $keywords . ')[[:>:]]") OR UPPER(p.message_subject) REGEXP UPPER("[[:<:]](' . $keywords . ')[[:>:]]"))';
	}
}
else if($search_author && $search_exact)
{
	// The exact author search.
	$sql_user = '(t.user_id = ' . $user->data['user_id'] . ' OR t.author_id = ' . $user->data['user_id'] . ')';
	// We do this in two queries. First find the users
	$keyword_arr = explode(',', $keywords);
	$sql_where = '';
	foreach($keyword_arr as $word)
	{
		$word = $db->sql_escape(trim($word));
		if($word != '')
		{
			$sql_where .= (($sql_where == '') ?  ' WHERE ' : ' OR ' ) . (($search_case) ? ' username = "' . $word . '"' : ' UPPER(username) = UPPER("' . $word . '")');
		}
	}
	// Get usename and user_colour so we can cache them and don't need to bother the database with it again.
	$sql = 'SELECT user_id, username, user_colour FROM ' . USERS_TABLE . $sql_where;
	$result = $db->sql_query($sql);

	$user_count = 0;
	$sql = '';
	// Now let's build the real search query
	while($row = $db->sql_fetchrow($result))
	{
		// put it in the cache-array
		$receiver_cache['u_' . $row['user_id']] = $row;

		$user_id = $row['user_id'];
		$user_count++;
		// It's easier for humans to read this way.
		if($search_author == PM_SENDER)
		{
			$sql .= ' AND (t.author_id = ' . $user_id . ' AND t.user_id = ' . $user->data['user_id'] . ')';
		}
		else if($search_author == PM_RECIPIENT) // SELECT * FROM table_1 WHERE to_address REGEXP "u_15([^0-9]|$)" ;
		{
			$sql .= ' AND (t.author_id = ' . $user->data['user_id'] . ' AND p.to_address REGEXP "u_' . $user_id . '([^0-9]|$)" AND t.pm_deleted = 0)';
		}
		else
		{
			$sql .= ' AND ((t.author_id = ' . $user_id . ' AND t.user_id = ' . $user->data['user_id'] . ') OR (t.author_id = ' . $user->data['user_id'] . ' AND p.to_address REGEXP "u_' . $user_id . '([^0-9]|$)" AND t.pm_deleted = 0))';
		}
	}
	$db->sql_freeresult($result);
	$sql_search = $sql;
}
else if($search_author && !$search_exact)
{
	// The author search.
	$sql_user = '(t.user_id = ' . $user->data['user_id'] . ' OR t.author_id = ' . $user->data['user_id'] . ')';
	// We'll do this in two queries. We start by getting the user_id's
	$keyword_arr = explode(',', $keywords);
	$sql_where = '';
	foreach($keyword_arr as $word)
	{
		$word = $db->sql_escape(trim($word));
		if($word != '')
		{
			$sql_where .= (($sql_where == '') ?  ' WHERE ' : ' OR ' ) . (($search_case) ? ' username LIKE "%' . $word . '%"' : ' UPPER(username) LIKE UPPER("%' . $word . '%")');
		}
	}
	// Get usename and user_colour so we can cache them.
	$sql = 'SELECT user_id, username, user_colour FROM ' . USERS_TABLE . $sql_where;
	$result = $db->sql_query($sql);

	$user_count = 0;
	$sql = '';
	// Now let's build the real search query
	while($row = $db->sql_fetchrow($result))
	{
		// put it in the cache-array
		$receiver_cache['u_' . $row['user_id']] = $row;

		$user_id = $row['user_id'];
		$user_count++;
		if($search_author == PM_SENDER)
		{
			$sql .= (($sql == '') ? ' AND (' : ' OR ') . '(t.author_id = ' . $user_id . ' AND t.user_id = ' . $user->data['user_id'] . ' AND t.pm_deleted = 0)';
		}
		else if($search_author == PM_RECIPIENT)
		{
			$sql .= (($sql == '') ? ' AND (' : ' OR ') . '(t.author_id = ' . $user->data['user_id'] . ' AND p.to_address REGEXP "u_' . $user_id . '([^0-9]|$)" AND t.pm_deleted = 0)';
		}
		else
		{
			$sql .= (($sql == '') ? ' AND (' : ' OR ') . '((t.author_id = ' . $user_id . ' AND t.user_id = ' . $user->data['user_id'] . ') OR (t.author_id = ' . $user->data['user_id'] . ' AND p.to_address REGEXP "u_' . $user_id . '([^0-9]|$)" AND t.pm_deleted = 0))';
		}

	}
	$db->sql_freeresult($result);
	$sql_search = ($sql == '') ? '' : $sql . ')';
}
else
{
	// Set some search-keys
	if($search_word)
	{
		$equals = 'REGEXP';
		$sign_start = '[[:<:]](';
		$sign_end = ')[[:>:]]';
	}
	else
	{
		$equals = 'LIKE';
		$sign_start = $sign_end = '%';
	}
	if($search_case)
	{
		$upper_start =  $upper_end = '';
	}
	else
	{
		$upper_start = 'UPPER(';
		$upper_end = ')';
	}

	$sql_user = 't.user_id = ' . $user->data['user_id'];
	$sql_and = $sql_or = '';
	// Adding the keywords.
	$keyword_arr = explode(' ', $keywords);
	foreach($keyword_arr as $word)
	{
		$char = substr($word, 0, 1);
		if($char == '+' || $search_all_words)
		{
			$word = ltrim($word, '+-');
			if($search_within == PM_MESSAGE)
			{
				$sql_and .= " AND {$upper_start}p.message_text{$upper_end} $equals $upper_start'{$sign_start}{$word}{$sign_end}'$upper_end";
			}
			else if($search_within == PM_SUBJECT)
			{
				$sql_and .= " AND {$upper_start}p.message_subject{$upper_end} $equals $upper_start'{$sign_start}{$word}{$sign_end}'$upper_end";
			}
			else
			{
				$sql_and .= " AND ({$upper_start}p.message_text{$upper_end} $equals $upper_start'{$sign_start}{$word}{$sign_end}'$upper_end OR {$upper_start}p.message_subject{$upper_end} $equals $upper_start'{$sign_start}{$word}{$sign_end}'$upper_end)";
			}
		}
		else if($char == '-')
		{
			$word = ltrim($word, '+-');
			if($search_within == PM_MESSAGE)
			{
				$sql_and .= " AND {$upper_start}p.message_text{$upper_end} NOT $equals $upper_start'{$sign_start}{$word}{$sign_end}'$upper_end";
			}
			else if($search_within == PM_SUBJECT)
			{
				$sql_and .= " AND {$upper_start}p.message_subject{$upper_end} NOT $equals $upper_start'{$sign_start}{$word}{$sign_end}'$upper_end";
			}
			else
			{
				$sql_and .= " AND ({$upper_start}p.message_text{$upper_end} NOT $equals $upper_start'{$sign_start}{$word}{$sign_end}'$upper_end OR {$upper_start}p.message_subject{$upper_end} NOT $equals $upper_start'{$sign_start}{$word}{$sign_end}'$upper_end)";
			}
		}
		else
		{
			if($search_within == PM_MESSAGE)
			{
				$sql_or .= (($sql_or == '') ? ' AND (' : ' OR ') . "{$upper_start}p.message_text{$upper_end} $equals $upper_start'{$sign_start}{$word}{$sign_end}'$upper_end";
			}
			else if($search_within == PM_SUBJECT)
			{
				$sql_or .= (($sql_or == '') ? ' AND (' : ' OR ') . "{$upper_start}p.message_subject{$upper_end} $equals $upper_start'{$sign_start}{$word}{$sign_end}'$upper_end";
			}
			else
			{
				$sql_or .= (($sql_or == '') ? ' AND (' : ' OR ') . "({$upper_start}p.message_text{$upper_end} $equals $upper_start'{$sign_start}{$word}{$sign_end}'$upper_end OR {$upper_start}p.message_subject{$upper_end} $equals $upper_start'{$sign_start}{$word}{$sign_end}'$upper_end)";
			}
		}
		$hilit .= (($hilit == '') ? '' : ' ') . $word;
	}

	$sql_search = $sql_and . ' ' . (($sql_or == '') ? '' : $sql_or . ')');
}

$sql_array = array(
	'SELECT' => 't.*, p.*, u.*',
	'FROM' => array(PRIVMSGS_TO_TABLE => 't', USERS_TABLE => 'u'),
	'WHERE' => 't.user_id = ' . $user->data['user_id'] . '
						' . $search_folder . '
						AND u.user_id = t.author_id
						' . $sql_limit_time . '
						' . $sql_search,
);

$sql_array['LEFT_JOIN'][] = array(
	'FROM'	=> array(PRIVMSGS_TABLE => 'p'),
	'ON'	=> 't.msg_id = p.msg_id'
);

$sql = $db->sql_build_query('SELECT', $sql_array);
// We sort by message_time even for sort by Author
$sql .= ' ORDER BY ' . (($sort_key == 's') ? 'LOWER(p.message_subject) ' : 'p.message_time ') . (($sort_dir == 'd') ? 'DESC' : 'ASC');

//echo $sql . '<br />';

$result = $db->sql_query($sql);

if($sort_by_author)
{
	// these are only used if we sort by author
	$sort_author_names = $sort_author_arr = array();
}

if($hilit != '')
{
	$hilit = implode('|', explode(' ', preg_replace('#\s+#u', ' ', str_replace(array('+', '-', '|', '(', ')', '&quot;'), ' ', $hilit))));
	// Do not allow *only* wildcard being used for hilight
	$hilit = (strspn($hilit, '*') === strlen($hilit)) ? '' : $hilit;

	$hilit = urlencode(htmlspecialchars_decode(str_replace('|', ' ', $hilit)));
	$match = ($search_exact) ? PM_SEARCH_MATCH_EXACT : (($search_word) ? PM_SEARCH_MATCH_WORD : PM_SEARCH_MATCH_ALL);
	$hilit = ($hilit != '') ? '&amp;hilit=' . $hilit . '&amp;match=' . $match: '';
}

$page_end = $start + $config['topics_per_page'];
$pm_count = 0;
while($row = $db->sql_fetchrow($result))
{
	// If we searched for users and none where found, we'll leave here.
	if(isset($user_count) && $user_count == 0)
	{
		continue;
	}

	// Output the pm's if there are any matches...
	$pm_count++;

	// Sort by Author needs all rows.
	if(($pm_count > $start && $pm_count <= $page_end) || $sort_by_author)
	{
		$folder_img = ($row['pm_unread']) ? 'pm_unread' : 'pm_read';
		$folder_alt = ($row['pm_unread']) ? 'NEW_MESSAGES' : 'NO_NEW_MESSAGES';

		$row_indicator = '';
		foreach ($color_rows as $var)
		{
			if (($var != 'friend' && $var != 'foe' && $row['pm_' . $var])
				||
				(($var == 'friend' || $var == 'foe') && isset(${$var}[$row['author_id']]) && ${$var}[$row['author_id']]))
			{
				$row_indicator = $var;
				break;
			}
		}

		$row_message_id = $row['msg_id'];
		$row_folder_id = (int) $row['folder_id'];

		$recipients = '';
		if($row['author_id'] == $user->data['user_id'])
		{
			// We need a list of recipients for sent PM's
			if($row['to_address'] == '')
			{
				// The to_address field should never be empty, But better safe than sorry.
				continue;
			}
			if($row['bcc_address'] != '')
			{
				$address = $row['to_address'] . ':' . $row['bcc_address'];
			}
			else
			{
				$address = $row['to_address'];
			}
			$address_list = explode(':', $address);
			foreach($address_list as $receiver)
			{
				if(substr($receiver, 0, 1) == 'u')
				{
					// The receiver is a user
					if(array_key_exists($receiver, $receiver_cache))
					{
						// No need to do the query if the user is cached
						$receiver_row = $receiver_cache[$receiver];
					}
					else
					{
						$receiver = ltrim($receiver, 'u_');
						$sql = 'SELECT user_id, username, user_colour
							FROM ' . USERS_TABLE . '
							WHERE user_id = ' . (int) $receiver;
						$receiver_result = $db->sql_query($sql);
						$receiver_row = $db->sql_fetchrow($receiver_result);
						// Cache this user
						$receiver_cache['u_' . $receiver_row['user_id']] = $receiver_row;
						$db->sql_freeresult($receiver_result);
					}
					$recipients .= (($recipients == '') ? '' : ', ') . get_username_string('full', $receiver_row['user_id'], $receiver_row['username'], $receiver_row['user_colour']);
				}
				else
				{
					// The receiver is a group
					if(array_key_exists($receiver, $receiver_cache))
					{
						// No need to do the query if the group is cached
						$receiver_row = $receiver_cache[$receiver];
					}
					else
					{
						$receiver = ltrim($receiver, 'g_');
						$sql = 'SELECT group_id, group_name, group_colour, group_type
							FROM ' . GROUPS_TABLE . '
							WHERE group_id = ' . (int) $receiver;
						$receiver_result = $db->sql_query($sql);
						$receiver_row = $db->sql_fetchrow($receiver_result);
						// Cache this group
						$receiver_cache['g_' . $receiver_row['group_id']] = $receiver_row;
						$db->sql_freeresult($receiver_result);
					}
					$receiver_row['group_name'] = ($receiver_row['group_type'] == GROUP_SPECIAL) ? $user->lang['G_' . $receiver_row['group_name']] : $receiver_row['group_name'];
					$user_colour = ($receiver_row['group_colour']) ? ' style="font-weight: bold; color:#' . $receiver_row['group_colour'] . '"' : '';
					$link = '<a href="' . append_sid("{$phpbb_root_path}memberlist.$phpEx", 'mode=group&amp;g=' . $receiver_row['group_id']) . '"' . $user_colour . '>';
					$recipients .= (($recipients == '') ? '' : ', ') . $link . $receiver_row['group_name'] . '</a>';
				}
			}
		}

		// Get the folder name except for search in the current folder
		$folder_name = '';
		if(!$search_current)
		{
			switch($row_folder_id)
			{
				case PRIVMSGS_OUTBOX:
					$folder_name = $user->lang['PM_OUTBOX'];
				break;

				case PRIVMSGS_SENTBOX:
					$folder_name = $user->lang['PM_SENTBOX'];
				break;

				case PRIVMSGS_INBOX:
					$folder_name = $user->lang['PM_INBOX'];
				break;

				default:
					// If nothing else fits we try to get it from the cache or the db.
					if($row_folder_id > 0)
					{
						if(isset($folder_name_cache[$row_folder_id]))
						{
							$folder_name = $folder_name_cache[$row_folder_id];
						}
						else
						{
							$sql = 'SELECT folder_name FROM ' . PRIVMSGS_FOLDER_TABLE . '
											WHERE folder_id = ' . $row_folder_id;
							$folder_result = $db->sql_query($sql);
							$folder_name = $db->sql_fetchfield('folder_name');
							$db->sql_freeresult($folder_result);
							$folder_name_cache[$row_folder_id] = $folder_name;
						}
					}
				break;
			}
		}

		// Generate the URLs...
		$view_message_url = append_sid($phpbb_root_path . 'ucp.' . $phpEx, 'i=' . $id . ' &amp;mode=view&amp;p=' . $row_message_id . $hilit);
		$remove_message_url = append_sid($phpbb_root_path . 'ucp.' . $phpEx, 'i=' . $id . '&amp;mode=compose&amp;action=delete&amp;p=' . $row_message_id);

		$messagerow = array(
			'PM_CLASS'			=> ($row_indicator) ? 'pm_' . $row_indicator . '_colour' : '',

			'MESSAGE_AUTHOR_FULL'		=> get_username_string('full', $row['author_id'], $row['username'], $row['user_colour'], $row['username']),
			'MESSAGE_AUTHOR_COLOUR'		=> get_username_string('colour', $row['author_id'], $row['username'], $row['user_colour'], $row['username']),
			'MESSAGE_AUTHOR'			=> get_username_string('username', $row['author_id'], $row['username'], $row['user_colour'], $row['username']),
			'U_MESSAGE_AUTHOR'			=> get_username_string('profile', $row['author_id'], $row['username'], $row['user_colour'], $row['username']),

			'FOLDER_ID'			=> $row_folder_id,
			'MESSAGE_ID'		=> $row_message_id,
			'SENT_TIME'			=> $user->format_date($row['message_time']),
			'SUBJECT'			=> censor_text($row['message_subject']),
			'FOLDER'			=> $folder_name,
			'U_FOLDER'			=> append_sid("{$phpbb_root_path}ucp.$phpEx", 'i=pm&amp;folder=' . $row_folder_id),
			'PM_ICON_IMG'		=> (!empty($icons[$row['icon_id']])) ? '<img src="' . $config['icons_path'] . '/' . $icons[$row['icon_id']]['img'] . '" width="' . $icons[$row['icon_id']]['width'] . '" height="' . $icons[$row['icon_id']]['height'] . '" alt="" title="" />' : '',
			'PM_ICON_URL'		=> (!empty($icons[$row['icon_id']])) ? $config['icons_path'] . '/' . $icons[$row['icon_id']]['img'] : '',
			'FOLDER_IMG'		=> $user->img($folder_img, $folder_alt),
			'FOLDER_IMG_SRC'	=> $user->img($folder_img, $folder_alt, false, '', 'src'),
			'PM_IMG'			=> ($row_indicator) ? $user->img('pm_' . $row_indicator, '') : '',
			'ATTACH_ICON_IMG'	=> ($auth->acl_get('u_pm_download') && $row['message_attachment'] && $config['allow_pm_attach']) ? $user->img('icon_topic_attach', $user->lang['TOTAL_ATTACHMENTS']) : '',

			'S_PM_DELETED'		=> ($row['pm_deleted']) ? true : false,
			'S_AUTHOR_DELETED'	=> ($row['author_id'] == ANONYMOUS) ? true : false,

			'U_VIEW_PM'			=> ($row['pm_deleted']) ? '' : $view_message_url,
			'U_REMOVE_PM'		=> ($row['pm_deleted']) ? $remove_message_url : '',
			'RECIPIENTS'		=> $recipients,
		);

		if($sort_by_author)
		{
			// Add to array for sorting on author / recipient
			$sort_author_names[$row_message_id] = ($recipients == '') ? strtolower($row['username']) : strtolower($recipients);
			$sort_author_arr[$row_message_id] = $messagerow;
		}
		else
		{
			// If we don't sort by Author we can send the template strings here
			$template->assign_block_vars('messagerow', $messagerow);
		}
	}
}
$db->sql_freeresult($result);

if($sort_by_author)
{
	// Now that we have all recipients we can do the author sorting if needed.
	if($sort_dir == 'd')
	{
		arsort($sort_author_names, SORT_STRING);
	}
	else
	{
		asort($sort_author_names, SORT_STRING);
	}

	$row_count = 0;
	foreach($sort_author_names as $key => $value)
	{
		$row_count++;
		if($row_count > $start && $row_count <= $page_end)
		{
			$template->assign_block_vars('messagerow', $sort_author_arr[$key]);
		}
	}
}

$folder_status_msg = ($pm_count == 1) ? 'FOUND_SEARCH_MATCH' : 'FOUND_SEARCH_MATCHES';

// Replace spaces with %20 for the pagination
$get_keys = str_replace(' ', '%20', $keywords);

$tpl_file = 'ucp_pm_search_results';

// Send vars to template
$template->assign_vars(array(
	'CUR_FOLDER_ID' => $folder_id,
	'PAGINATION' => generate_pagination(append_sid("{$phpbb_root_path}ucp.$phpEx", "i=pm&amp;mode=search&amp;action=view_folder&amp;f=$folder_id&amp;$u_sort_param&amp;exact=$search_exact&amp;case=$search_case&amp;current=$search_current&amp;author=$search_author&amp;keywords=$keywords"), $pm_count, $config['topics_per_page'], $start),
	'PAGE_NUMBER' => on_page($pm_count, $config['topics_per_page'], $start),
	'TOTAL_MESSAGES' => (($pm_count == 1) ? $user->lang['VIEW_PM_MESSAGE'] : sprintf($user->lang['VIEW_PM_MESSAGES'], $pm_count)),

	'POST_IMG' => (!$auth->acl_get('u_sendpm')) ? $user->img('button_topic_locked', 'PM_LOCKED') : $user->img('button_pm_new', 'POST_PM'),

	'L_NO_MESSAGES' => (!$auth->acl_get('u_sendpm')) ? $user->lang['POST_PM_LOCKED'] : $user->lang['NO_MESSAGES'],

	'S_SELECT_SORT_DIR' => $s_sort_dir,
	'S_SELECT_SORT_KEY' => $s_sort_key,
	'S_SELECT_SORT_DAYS' => $s_limit_days,
	'S_TOPIC_ICONS' => ($config['enable_pm_icons']) ? true : false,

	'S_DISPLAY_SEARCHBOX' => true,
	'S_PM_SEARCHBOX_ACTION' => append_sid("{$phpbb_root_path}ucp.$phpEx", 'mode=search'),
	'SEARCH_KEYWORDS' => $keywords,
	'S_PM_SEARCH_ADVANCED' => append_sid("{$phpbb_root_path}ucp.$phpEx", "i=pm&amp;mode=view&amp;action=view_folder&amp;f=$folder_id" . (($start !== 0) ? "&amp;start=$start" : '') . (($advanced_search == '') ? '&amp;pmsearch=advanced' : '')),
	'DISPLAY_ADVANCED' => ($advanced_search == '') ? 'none' : 'block',

	'U_POST_NEW_TOPIC' => ($auth->acl_get('u_sendpm')) ? append_sid("{$phpbb_root_path}ucp.$phpEx", 'i=pm&amp;mode=compose') : '',
	'S_PM_ACTION' => append_sid("{$phpbb_root_path}ucp.$phpEx", "i=pm&amp;mode=view&amp;action=view_folder&amp;f=$folder_id" . (($start !== 0) ? "&amp;start=$start" : '')),
	'S_PM_SEARCH_RESULT_ACTION' => append_sid("{$phpbb_root_path}ucp.$phpEx", "i=pm&amp;mode=search&amp;action=view_folder&amp;f=$folder_id&amp;exact=$search_exact&amp;case=$search_case&amp;current=$search_current&amp;author=$search_author&amp;keywords=$keywords" . (($start !== 0) ? "&amp;start=$start" : '')),
	'S_PM_SEARCH_RETURN' => "i=pm&amp;mode=search&amp;action=view_folder&amp;f=$folder_id&amp;exact=$search_exact&amp;case=$search_case&amp;current=$search_current&amp;author=$search_author&amp;keywords=$keywords" . (($start !== 0) ? "&amp;start=$start" : ''),
	'FOLDER_CUR_MESSAGES' => $pm_count,

	'S_SEARCH_EXACT' => $search_exact,
	'S_SEARCH_CASE' => $search_case,
	'S_SEARCH_CURRENT' => $search_current,
	'S_SEARCH_ALL_WORDS' => $search_all_words,
	'S_WORD_ONLY' => $search_word,

	'S_PM_SENDER' => ($search_author == PM_SENDER) ? TRUE : FALSE,
	'S_PM_RECIPIENT' => ($search_author == PM_RECIPIENT) ? TRUE : FALSE,
	'S_PM_BOTH' => ($search_author == PM_BOTH) ? TRUE : FALSE,

	'S_WITHIN_MESSAGE' => ($search_within == PM_MESSAGE) ? TRUE : FALSE,
	'S_WITHIN_SUBJECT' => ($search_within == PM_SUBJECT) ? TRUE : FALSE,
	'S_WITHIN_BOTH' => ($search_within == PM_BOTH) ? TRUE : FALSE,

	'C_PM_SENDER' => PM_SENDER,
	'C_PM_RECIPIENT' => PM_RECIPIENT,
	'C_PM_BOTH' => PM_BOTH,
	'C_PM_SUBJECT' => PM_SUBJECT,
	'C_PM_MESSAGE' => PM_MESSAGE,
));

?>
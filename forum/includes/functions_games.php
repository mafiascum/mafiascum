<?php
/**
*
* @package phpBB3
* @version $Id$
* @copyright (c) 2005 phpBB Group
* @license http://opensource.org/licenses/gpl-license.php GNU Public License
*
* Added by Kison, January 16 2012
*/

/**
* @ignore
*/
$phpEx = substr(strrchr(__FILE__, '.'), 1);
if (!defined('IN_PHPBB'))
{
	exit;
}


function assembleMods($mods)
{
	$data = array();
	$mods = explode($mods, ';');
	
	foreach($mods as $mod)
	{
		$moddata = explode($mod, '|');
		$data[]['name'] = $moddata[0];
		$data[]['type'] = $moddata[1];
	}
	return $data;
}

/**
 * Prepares and submits a game into a queue.
 * @param string $gameName 			Name of the game.
 * @param int	 $gameType 			Queue to submit the game into. 
 * @param int	 $main_mod 			Main moderator user id.
 * @param int	 $requested_slots 	Number of slots requested.
 * @param string $game_description	Description of the game.
 * @param mixed[] $altMods			Other mods to add. //TODO
 */
function createGame($gameName, $gameType, $main_mod, $requested_slots, $game_description = '', $altMods = '') {

	global $db, $auth, $user, $config, $phpEx;
	$message = $game_description;
	$allow_bbcode = $allow_smilies = $allow_urls = true;
	generate_text_for_storage($message, $uid, $bitfield, $options, $allow_bbcode, $allow_urls, $allow_smilies);
	$insertArray = array
	(
		"name"					=> $gameName,
		"description"			=> $message,
		"game_type"				=> $gameType,
		"status"				=> 1, //Pending should ALWAYS default to 1.//TODO
		"maximum_players" 	=> $requested_slots,
		"created_time"			=> time(),
		"main_mod_id"		=> $main_mod,
		"created_by_user_id"	=> $user->data['user_id'],
		"bbcode_uid"			=> $uid,
		"bbcode_bitfield"		=> $bitfield
	);
	
	$sql = 'INSERT INTO ' . MAFIA_GAMES_TABLE . ' ' . $db->sql_build_array('INSERT', $insertArray);
	$db->sql_query($sql);
	$new_game_id = $db->sql_nextid();
	
	$sql_ary = array(
		'user_id'	=> $main_mod,
		'game_id'	=> $new_game_id,
		'type'		=> 0,
	);
	$sql = 'INSERT INTO ' . MAFIA_MODERATORS_TABLE . ' ' . $db->sql_build_array('INSERT', $sql_ary);
	$db->sql_query($sql);
	
	return $new_game_id;
}
/**
 * Creates a series of <option> HTML elements for game types.
 * @return string A string of HTML elements. */
function createGameTypeSelect($gtype = 0)
{
	global $db;
	$html = '';
	$sql = 'SELECT type_id, type_name from ' . MAFIA_GAME_TYPES_TABLE . '
			ORDER BY type_id ASC';
	$result = $db->sql_query($sql);
	while($type = $db->sql_fetchrow($result))
	{
		$html .= '<option value="'.$type['type_id'].'"'.(($type['type_id'] == $gtype) ? ' selected="selected"' : '').'>'.$type['type_name'].'</option>';
	}
	$db->sql_freeresult($result);
	return $html;
}

/** 
 *Creates a string of <option> HTML elements for statuses.
 * @param int $status The currently selected status.
 * @return string A string of HTML elements.
 */
function createStatusOptions($status = 0, $available = false)
{
	$select = '';
	$select .= '<option value="' . GAME_PROGRESS_PENDING . '"' . (($status == GAME_PROGRESS_PENDING) ? ' selected="selected"' : '') . '>Pending</option>';
	$select .= '<option value="' . GAME_PROGRESS_QUEUED . '"' . (($status == GAME_PROGRESS_QUEUED) ? ' selected="selected"' : '') . '>Queued</option>';
	$select .= '<option value="' . GAME_PROGRESS_SIGNUPS . '"' . (($status == GAME_PROGRESS_SIGNUPS) ? ' selected="selected"' : '') . '>Signups</option>';
	$select .= '<option value="' . GAME_PROGRESS_SETUP . '"' . (($status == GAME_PROGRESS_SETUP) ? ' selected="selected"' : '') . '>Setup</option>';
	$select .= '<option value="' . GAME_PROGRESS_ONGOING . '"' . (($status == GAME_PROGRESS_ONGOING) ? ' selected="selected"' : '') . '>Ongoing</option>';
	$select .= '<option value="' . GAME_PROGRESS_COMPLETED . '"' . (($status == GAME_PROGRESS_COMPLETED) ? ' selected="selected"' : '') . '>Completed</option>';
	return $select;
}

function createStatusDetailOptions($status = 0)
{
	global $db;
	$html = '';

		$sql = 'SELECT status_id, status_name FROM ' . MAFIA_GAME_STATUS_TABLE . '
				ORDER BY status_id ASC';

	$result = $db->sql_query($sql);
	while($type = $db->sql_fetchrow($result))
	{
		$html .= '<option value="'.$type['status_id'].'"'.(($type['status_id'] == $status) ? ' selected="selected"' : '').'>'.$type['status_name'].'</option>';
	}
	$db->sql_freeresult($result);
	return $html;
}

//Player status select, maybe hook up to db...
//For now read straight from established constants.
function createPlayerStatusOptions($status = 0)
{
	$select = '';
	$select .= '<option value="' . SLOT_ALIVE . '"' . (($status == SLOT_ALIVE) ? ' selected="selected"' : '') . '>Alive</option>';
	$select .= '<option value="' . SLOT_DEAD . '"' . (($status == SLOT_DEAD) ? ' selected="selected"' : '') . '>Dead</option>';
	$select .= '<option value="' . SLOT_OTHER . '"' . (($status == SLOT_OTHER) ? ' selected="selected"' : '') . '>Other</option>';
	$select .= '<option value="' . SLOT_STATUS_PENDING . '"' . (($status == SLOT_STATUS_PENDING) ? ' selected="selected"' : '') . '>Pending</option>';
	return $select;
}
//Player status select, maybe hook up to db...
//For now read straight from established constants.
function createOutcomeOptions($outcome = 0)
{
	$select = '';
	$select .= '<option value="' . SLOT_LOSS . '"' . (($outcome == SLOT_LOSS) ? ' selected="selected"' : '') . '>Lost</option>';
	$select .= '<option value="' . SLOT_WIN . '"' . (($outcome == SLOT_WIN) ? ' selected="selected"' : '') . '>Won</option>';
	$select .= '<option value="' . SLOT_DRAW . '"' . (($outcome == SLOT_DRAW) ? ' selected="selected"' : '') . '>Draw</option>';
	$select .= '<option value="' . SLOT_OUTCOME_PENDING . '"' . (($outcome == SLOT_OUTCOME_PENDING) ? ' selected="selected"' : '') . '>Pending</option>';
	return $select;
}
function createRoleOptions($role = 0)
{
	global $db;
	$select = '';
	$sql = " SELECT *"
			 . " FROM " . MAFIA_ROLES_TABLE;
	$result = $db->sql_query($sql);

	while($row = $db->sql_fetchrow($result))
	{
		$select .= '<option value="'.$row['role_id'].'"'.(($role == $row['role_id']) ? ' selected="selected"' : '').'>'.$row['role_name'].'</option>';
	}
	return $select;
}

function createModTypeOptions($type = 0)
{
	$select = '';
	$select .= '<option value="' . MODERATOR_TYPE_MAIN. '"' . (($type == MODERATOR_TYPE_MAIN) ? ' selected="selected"' : '') . '>Primary</option>';
	$select .= '<option value="' . MODERATOR_TYPE_COMOD . '"' . (($type == MODERATOR_TYPE_COMOD) ? ' selected="selected"' : '') . '>CoMod</option>';
	$select .= '<option value="' . MODERATOR_TYPE_BACKUP . '"' . (($type == MODERATOR_TYPE_BACKUP) ? ' selected="selected"' : '') . '>Backup</option>';
	return $select;
}

//Optimize the database calls in this function so its all one call instead.
//See: SQL "in" operation.
function createModifierOptions($modifierList = array())
{
	global $db;
	
	$html = '';
	$sql = 'SELECT *
    FROM ' . MAFIA_MODIFIERS_TABLE;
	$result = $db->sql_query($sql);

	while($row = $db->sql_fetchrow($result)){
		$html .= '<option value="'.$row['modifier_id'].'" '.((in_array($row['modifier_id'], $modifierList)) ? 'selected="selected"' : '').'>'.$row['modifier_name'].'</option>';
		}
	return $html;
}


function getGameTypeName($id)
{
	global $db;
	$name = '';
	$sql = 'SELECT type_name from ' . MAFIA_GAME_TYPES_TABLE . '
			WHERE type_id = '.$db->sql_escape($id);
	$result = $db->sql_query($sql);
	if($type = $db->sql_fetchrow($result))
	{
		$name = $type['type_name'];
	}

	$db->sql_freeresult($result);
	return $name;
}


/**
 * Get the proper language string for moderator type.
 * @return string Language string for type.
 */
function getModTypeName($mafiaModeratorType) {

	switch($mafiaModeratorType)
	{
	case MODERATOR_TYPE_MAIN: return "Main Moderator";
	case MODERATOR_TYPE_COMOD: return "Co-Mod";
	case MODERATOR_TYPE_BACKUP: return "Backup";
	}
}
function getAlignmentName($alignment_id) {
	switch($alignment_id)
	{
	case MAFIA_ALIGNMENT_TOWN: return "Town";
	case MAFIA_ALIGNMENT_MAFIA: return "Mafia";
	case MAFIA_ALIGNMENT_THIRD_PARTY: return "Third Party";
	return "Other";
	}
}
/**
 * Generate the list of queues for display on the queue page.
 * @param string $template_block	The name of template block to pass the vars into.
 * @param int	 $activeID			An ID of the currently active queue.
 */
function generateQueueList($template_block = 'queues', $activeID = 0)
{
	global $template, $db, $phpEx;
	
	$sql = 'SELECT * FROM '.MAFIA_GAME_TYPES_TABLE;
	$result = $db->sql_query($sql);
	while($queues = $db->sql_fetchrow($result))
	{
		$template->assign_block_vars($template_block, array(
			'QUEUE_NAME' 	=> $queues['type_name'],
			'QUEUE_ID'		=> $queues['type_id'],
			'QUEUE_LINK' 	=> append_sid('viewqueue.' . $phpEx . '?q=' . $queues['type_id']),
			'IS_ACTIVE'		=> ($activeID == $queues['type_id']) ? true : false,
		));
	}
	$template->assign_vars(array(
		'REPLACEMENT_LINK' => append_sid('viewqueue.' . $phpEx . '?mode=replacement'),
	));
	$db->sql_freeresult($result);
}

/**
 * Load a game from the game database. Returns the game information.
 * @param int $queue	A queue to select the game from.
 * @param int $game_id	A specific game id to search for.
 * @param int $moderator A specific mod to match against.
 * @param int $approval An approval state to match against.
 * @param int $status	A game status to match against.
 * @param int $start	Starting offset.
 * @param int $limit	Offset limit.
 * @param string $template_block Block name to pass the vars into.
 * @param bool $moderate Whether to display moderation actions.
 * @return mixed[] The game information.
 */
function load_game($queue = 0, $game_id = 0, $moderator = 0, $approval = 0, $status = 0, $start = 0, $limit = 25, $template_block = '', $moderate = false, $game_ids = array(), $allqueues = false)
{
	global $db, $template, $phpEx;
	// no result rows greater than 100 per page
	$limit = ($limit > 100) ? 100 : $limit;

	//Build WHERE clause and parameters for pagination.
	$where = $params = '';
	if($game_id) {
		$where .= (empty($where)) ? 'g.game_id = '.$db->sql_escape($game_id) : ' AND g.game_id = '.$db->sql_escape($game_id);
		
	} else if($queue){
		$where .= 'game_type = '.$queue;
		$params .= 'q='.$queue;
	} else if (sizeOf($game_ids)) {
		$ids = join(',',$game_ids);
		$where .= (empty($where)) ? 'g.game_id IN (' . $ids . ')' : ' AND g.game_id IN (' . $ids . ')' ;
	} else if (!$allqueues) {
		$where .= (empty($where)) ? '1=0' : ' AND 1=0' ;
	}
	
	if($approval)
	{
		$where .= (empty($where)) ? 'approved_time IS NOT NULL' : ' AND approved_time IS NOT NULL';
	} else {
		$params .= (empty($params)) ? 'appr='.$approval : '&amp;appr='.$approval;
	}
	
	if($status)
	{
		if (is_array($status)){
			$statuslist = '(' . $db->sql_escape($status[0]);
			for ($i =1; $i < sizeOf($status); $i++){
				$statuslist .= ',' . $db->sql_escape($status[$i]);
			}
			$statuslist .= ')';
			 $where .= (empty($where)) ? 'status IN '. $statuslist : ' AND status IN '. $statuslist ;
			 $params .= (empty($params)) ? 'sta='.$status : '&amp;sta='.$status;
		} else {
			$where .= (empty($where)) ? 'status = '.$db->sql_escape($status) : ' AND status = '.$db->sql_escape($status);
			$params .= (empty($params)) ? 'sta='.$status : '&amp;sta='.$status;
		}
	} else {
		$params .= (empty($params)) ? 'sta='.$status : '&amp;sta='.$status;
	}
	if($moderator)
	{
		$where .= (empty($where)) ? 'main_mod_id = '.$db->sql_escape($moderator) : ' AND main_mod_id = '.$db->sql_escape($moderator);
	}
	if($limit)
	{
		$params .= (empty($params)) ? 'limit='.$limit : '&amp;limit='.$limit;
	}
	
	// Build a SQL Query...
	$sql_ary = array(
		'SELECT'    =>  'g.* , c.username as game_creator_name, u.user_id as mod_user_id, u.username as mod_username, a.user_id as app_user_id, a.username as app_username, s.status_id, s.status_name, t.* ',
		'FROM'      => array(MAFIA_GAMES_TABLE => 'g'),
		'LEFT_JOIN'	=> array(
			array(
				'FROM'	=> array(USERS_TABLE => 'u'), //Main mod user info.
				'ON'	=> 'u.user_id = g.main_mod_id'),
			array(
				'FROM'	=> array(USERS_TABLE => 'a'), //Approval mod user info.
				'ON'	=> 'a.user_id = g.approved_by_user_id'),
			array(
				'FROM'	=> array(MAFIA_GAME_STATUS_TABLE => 's'), //Status info.
				'ON'	=> 's.status_id = g.status'),
			array(
				'FROM'	=> array(MAFIA_GAME_TYPES_TABLE => 't'), //Type info.
				'ON'	=> 't.type_id = g.game_type'),
			array(
				'FROM'	=> array(USERS_TABLE => 'c'), //creator user info
				'ON'	=> 'c.user_id = g.created_by_user_id'),
				),
		'WHERE'		=> $where,	
	);
	
	
	$sql = $db->sql_build_query('SELECT', $sql_ary);
	$sql .= ' ORDER BY game_type ASC, approved_time ASC';
	$result = $db->sql_query_limit($sql, $limit, $start);
	//Assign all necessary template variables.
	$blockt = ($template_block) ? true : false;
	$data = array();
	while($row = $db->sql_fetchrow($result))
	{
		$data[] = $row;
	}
	foreach($data as $game)
	{
		$sql = 'SELECT COUNT(slot_id) AS slots_alive FROM ' . MAFIA_SLOTS_TABLE . ' WHERE game_id=' . $game['game_id'] . ' AND slot_status=' . SLOT_ALIVE;
		$result = $db->sql_query_limit($sql, 1);
		$temp_var = $db->sql_fetchrow($result);
		$game['slots_alive'] = $temp_var['slots_alive']; 
		assignGameVars($game, $blockt, $template_block);
	}
	$db->sql_freeresult($result);
	
		$sql = 'SELECT count(game_id) as num_games FROM ' . MAFIA_GAMES_TABLE . ' g ' . (empty($where) ? '' : ('WHERE ' . $where));
		$result = $db->sql_query($sql);
		$data2 = $db->sql_fetchrow($result);

		$count = $data2['num_games'];
		$pagination_url = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx, $params);
		// Assign the pagination variables to the template.
		$template->assign_vars(array(
			'PAGINATION'        => generate_pagination($pagination_url, $count, $limit, $start),
			'PAGE_NUMBER'       => on_page($count, $limit, $start),
			'TOTAL_GAMES'		=> $count,
		));
	
	
	//Return the game dataset for other uses.
	//If there is only one game, only return that entry, otherwise the full game array.
	if(sizeof($data) > 1)
	{
		return $data;
	}
	else
	{
		return $data[0];
	}
}

/**
 * Load moderators for a specific game.
 * @return SQL dataset of relevant moderators.
 */
function grabModInfo($game_id)
{
	global $db, $template, $phpEx;
	
	$sql_ary = array(
	'SELECT'	=> 'm.*, u.*',
	'FROM'		=> array(MAFIA_MODERATORS_TABLE => 'm'),
	'LEFT_JOIN' => array(		  
		array(
			'FROM'	=> array(USERS_TABLE => 'u'),
			'ON'	=> 'm.user_id = u.user_id')
		),
	'WHERE'		=> 'm.game_id = '.(int)$db->sql_escape($game_id),
	'ORDER_BY'	=> 'm.type',
	);
	
	$sql = $db->sql_build_query('SELECT', $sql_ary);

	$res = $db->sql_query($sql);
	
	while($mod = $db->sql_fetchrow($res))
	{
		$template->assign_block_vars('mods', array(
		'MODERATOR' 	=> get_username_string('full', $mod['user_id'], $mod['username'], $mod['user_colour']),
		'USER_ID'		=> $mod['user_id'],
		'MOD_TYPE'		=> getModTypeName($mod['type']),
		'MOD_TYPE_OPTIONS'	=> createModTypeOptions($mod['type']),
		'U_REMOVE_MOD' => append_sid("{$phpbb_root_path}viewgame.$phpEx?g=$game_id&deletemod=true&mod_id=" . $mod['user_id']),
		));
	}
	$db->sql_freeresult($res);
}

/**
 * Provide moderator template variables.
 * @param int $game_id Game to display mods for.
 * @param SQL $prev_data Previously loaded mod data.
 */
function display_moderators($game_id, $prev_data = false)
{
	global $db, $template;
	$db_result = ($prev_data) ? $prev_data : load_moderators($game_id);
	while($moderator_row = $db->sql_fetchrow($db_result))
		{
			$moderatorId = $moderator_row['moderator_id'];
			$template->assign_block_vars('moderators', array(
			
				
			
			));
		}
	
}

/**
 * Assign all the template variables available from the SQL resultset.
 * @param mixed[] $row A resultset from a sql query.
 * @param bool $block Whether this is a block template or not.
 * @param string $block_name The name of the block to assign the vars to.
 */
function assignGameVars($game, $block = false, $block_name = 'games')
{
	global $template, $user, $db, $phpEx, $auth;
	if(!$block){
	//build game type select element
		$gameType = $game['game_type'];
		$typeSelect = '<select id="gameInfoGameTypeInputField" name="gameInfoGameType" class="gameInfoLaben"';
		$typeSelect .='>';

		$sql = " SELECT *"
			 . " FROM " . MAFIA_GAME_TYPES_TABLE;

		$result = $db->sql_query($sql);

		while($row = $db->sql_fetchrow($result)){
			$typeSelect .= '<option value="';
			$typeSelect .= $row['type_id'];
			$typeSelect .= '"';
			if ($row['type_id'] == $gameType){
				$typeSelect .= ' selected ';
				$gameType = $row['type_name'];
				$forum_id = $row['forum_id'];
			}
			$typeSelect .= '>';
			$typeSelect .= $row['type_name'];
			$typeSelect .= '</option>';
		}
		$db->sql_freeresult($result);

		$typeSelect .= '</select>';

		//build game status select element
		$gameStatusVal = $game_data['status'];
		
			$statusSelect = '<select id="gameInfoGameStatusInputField" name="gameInfoGameStatus" class="gameInfoLabel"';
			$statusSelect .='>';
			$sql = " SELECT *"
				 . " FROM " . MAFIA_GAME_STATUS_TABLE;

			$result = $db->sql_query($sql);
			$statusOptions = $auth->acl_get('u_queue_'.$game['game_type']) ? 0 : 3;
			while($row = $db->sql_fetchrow($result)){
				if ($row['status_id'] >$statusOptions){
					$statusSelect .= '<option value="';
					$statusSelect .= $row['status_id'];
					$statusSelect .= '"';
				}
				if ($row['status_id'] == $game['status'] ){
						$statusSelect .= ' selected ';
				}
				if ($row['status_id'] >$statusOptions){
					$statusSelect .= '>';
					$statusSelect .= $row['status_name'];
					$statusSelect .= '</option>';
				}
			}
			$db->sql_freeresult($result);
			$statusSelect .= '</select>';
	}
	if (!class_exists('bbcode'))
		{
			global $phpbb_root_path, $phpEx;
			include($phpbb_root_path . 'includes/bbcode.' . $phpEx);
		}
	$bbcode_bitfield = $bbcode_bitfield | base64_decode($game['bbcode_bitfield']);
	
	// Instantiate BBCode if need be
	if ($bbcode_bitfield !== '')
	{
		$bbcode = new bbcode(base64_encode($bbcode_bitfield));
	}
	$message = censor_text($game['description']);

	// Second parse bbcode here
	if ($game['bbcode_bitfield'])
	{
		$bbcode->bbcode_second_pass($message, $game['bbcode_uid'], $game['bbcode_bitfield']);
	}

	$message = bbcode_nl2br($message);
	$message = smiley_text($message);
	
	$sql = 'SELECT s.slot_id FROM ' . MAFIA_SLOTS_TABLE . ' s LEFT JOIN ' . MAFIA_PLAYERS_TABLE . ' p ON p.slot_id = s.slot_id AND p.game_id = s.game_id AND p.type<>5 WHERE p.player_id IS NULL AND s.game_id=' . $game['game_id'] .' LIMIT 1';
	$result = $db->sql_query($sql);
	$slot = $db->sql_fetchrow($result);
	if ($slot['slot_id']){
		$replace = true;
	}
	$gamevars = array(
		'GAME_NAME'		=> $game['name'],
		'GAME_TYPE'	=> $game['type_name'],
		'GAME_ID'	=> $game['game_id'],
		'CREATOR_NAME' 	=> get_username_string('full', $game['created_by_user_id'], $game['game_creator_name']),
		'GAME_LINK' => append_sid('viewgame.' . $phpEx . '?g=' . $game['game_id']),
		'PREIN_TOTAL'	=> $game['requested_players'],
		'ENTERED_PLAYER_TOTAL'	=> $game['entered_players'],
		'MAXIMUM_PLAYER_TOTAL'	=> $game['maximum_players'],
		'MAXIMUM_LIMIT'			=> $game['max_players'], //Maximum players available for this game type.
		'GAME_PROGRESS'			=> $game['status_id'] != 1 ? (($game['status_id'] == 2 || $game['status_id'] == 3) ? 'Signup progress: ' .$game['entered_players'] . '/' . $game['maximum_players'] : 'Game progress: ' . $game['slots_alive'] . '/' . $game['maximum_players']) : '',
		'AVAILABLE_REPLACEMENT_TOTAL'	=> $game['replacements'],
		'STATUS'	=> $game['status_name'],
		'STATUS_ID'	=> $game['status_id'],
		'STATUS_ALTERNATE'	=> $game['status_alternate'],
		'GAME_DESCRIPTION'	=> $message,
		'APPROVAL_USER'		=> get_username_string('full', $game['app_user_id'], $game['app_username']),
		'MAIN_MODERATOR'	=> get_username_string('full', $game['mod_user_id'], $game['mod_username']),
		'SUBMISSION_USER'	=> 'ADD_THIS',
		'CREATION_TIME'		=> !empty($game['created_time'])? strftime("%Y-%m-%d",$game['created_time']) : "", //TODO - Nicely format datetime.
		'APPROVAL_TIME'		=> !empty($game['approved_time'])? strftime("%Y-%m-%d",$game['approved_time']) : "", //TODO - Nicely format datetime.
		'STARTED_TIME'		=> !empty($game['started_time'])? strftime("%Y-%m-%d",$game['started_time']) : "", //TODO - Nicely format datetime.
		'COMPLETED_TIME'	=> !empty($game['completed_time'])? strftime("%Y-%m-%d",$game['completed_time']) : "", //TODO - Nicely format datetime.
		'IS_APPROVED'		=> ($game['approved_time']) ? true : false,
		'IS_STARTED'		=> ($game['started_time']) ? true : false,
		'APPROVAL_LINK'		=> ($game['approved_time']) ? '' : '<a href="'. append_sid("viewqueue." .$phpEx. '?mode=approve&amp;g=' .$game['game_id']) . '">',
		'STATUS_IMG'		=> $user->img('forum_unread_locked', '', false, '', 'src'),
		'IS_ONGOING'		=> ($game['status'] == GAME_PROGRESS_ONGOING) ? true : false,
		'STATUS_VAL'		=> $gameStatusVal,
		'IN_GAME' 				=> alreadyEntered($game['game_id'], $user->data['user_id'], 0),
		'U_EDITFORM'			=> append_sid("{$phpbb_root_path}viewgame.$phpEx?g=" . $game['game_id']),
		'TYPE_SELECT'			=> $typeSelect,
		'STATUS_SELECT'			=> $statusSelect,
		'REPLACE'				=> $replace,
		'GAME_TOPIC'			=> empty($game['topic_id']) ? "" : $phpbb_root_path . "viewtopic.$phpEx?t=" . $game['topic_id'],
	);
	if($block)
	{
		$template->assign_block_vars($block_name, $gamevars);
	}
	else
	{
		$template->assign_vars($gamevars);
	}
}
/**
 * Check whether the mod has reached their limits for a specific queue.
 * @param int $modID The moderator to lookup.
 * @param int $queue The queue to check limits in.
 * @return bool Whether the mod is over the limit or not.
 */
function checkModLimits($modID, $queue = 0)
{
	//TODO - Make limits up. For now return true.
	return true;
}
/**
 * Player limits hook.
 * @todo Add limits.
 */
function overPlayerLimits($queue)
{
	return false;
}

/**
 * Check whether the submitted game information is valid.
 * @param mixed[] $data The data to validate.
 * @return mixed[] An array of triggered errors.
 */
function errorsInGameData($data)
{
	global $db;
	$errors = array(); 
	
	//Check moderator and their limits.
	$mod = checkModerator($data['main_mod']);
	if(!$mod)
	{
		$errors['BAD_MOD_SELECTED'];
	}
	else
	{
		if(!checkModLimits($data['main_mod'], $data['game_type']))
		{
			$errors[] = 'MOD_QUEUE_LIMITS'; 
		}
	}
		
	//Validate game name.
	if(!$data['game_name']) 
	{
		$errors[] = 'MISSING_GAME_NAME'; 
	}
	if(strlen($data['game_name'] ) > 80)
	{
		$errors[] = 'GAME_NAME_TOO_LONG';
	}
	
	//Validate game type.
	if(!$data['game_type'])
	{
		$errors[] = 'MISSING_GAME_TYPE';
	} else {
		if(!$data['requested_slots'])
		{
			$errors[] = 'MISSING_REQUESTED_SLOTS';
		}
		else
		{
			$sql = 'SELECT * FROM phpbb_mafia_game_types WHERE type_id=' . $data['game_type'];
			$res = $db->sql_query($sql);
			$game_type_data = $db->sql_fetchrow($res);
			if($data['requested_slots'] < $game_type_data['min_players'])
			{
				$errors[] = 'NOT_ENOUGH_REQUESTED_SLOTS';
			} else if ($data['requested_slots'] > $game_type_data['max_players']){
				$errors[] = 'Too Many Slots.';
			}
		}
	}
	
	return $errors;
	
}

/**
 * Check whether the moderator specified is a valid user.
 * @param string $modName The user name to validate.
 * @param bool $getID Whether to retrieve the user's id as well.
 * @return bool|int Return the user's id or whether the mod exists.
 */
function checkModerator($modName, $getID = false)
{
	$checkusernameArray = array($modName);
	$user_id_ary = array();
	user_get_id_name($user_id_ary, $checkusernameArray);

	if(sizeof($user_id_ary) && $getID) {
		return $user_id_ary[0]; }
	elseif(sizeof($user_id_ary)) {
		return true; }
	else { return false; }
}


/**
 * Insert a player into a game.
 * @param int $game_id The game to add the player.
 * @param int $user_id The user to add as the player.
 * @param int $type The type of player this is.
 * @param int $replacement_start The time the player is available to replace in.
 */
function insertPlayer($game_id, $user_id, $type = 0, $replacement_start = null)
{
	global $db;
	if ($type == 1 || $type == 2 || $type == 0){
		//Insert the player into the table.
		$player_ary = array(
			'game_id'	=> (int) $game_id,
			'user_id'	=> (int) $user_id,
			'type'		=> (int) $type,
			'replacement_start'	=> $replacement_start,
		);
		
		$sql = 'INSERT INTO ' . MAFIA_PLAYERS_TABLE . ' ' . $db->sql_build_array('INSERT', $player_ary);
		$db->sql_query($sql);
		$db->sql_freeresult($result);
	//Update game player counts if we have successfully added a player.
	switch($type)
	{
		case STANDARD_IN:
			$ptype = 'requested_players';
			break;
		case PREIN:
			$ptype = 'requested_players';
			break;
		case REPLACEMENT:
			$ptype = 'replacements';
			break;
	}
	
	$sql = 'UPDATE ' . MAFIA_GAMES_TABLE . '
			SET ' .$db->sql_escape($ptype). ' = '.$db->sql_escape($ptype).' + 1
			WHERE game_id = ' . (int)$db->sql_escape($game_id);
	$db->sql_query($sql);
	} else if ($type == 3) {
		//update player table
		$sql = 'UPDATE phpbb_mafia_players p
			SET p.type=' . APPROVED_IN . '
			WHERE p.user_id = '.$db->sql_escape($user_id).'
			AND p.game_id = '.$db->sql_escape($game_id);
		$db->sql_query($sql);
		//update game table
		$sql = 'UPDATE phpbb_mafia_games g
			SET g.entered_players = g.entered_players + 1
			WHERE g.game_id = '.$db->sql_escape($game_id);
		$db->sql_query($sql);
	}
}

/**
 * Insert a slot associated with a player.
 * @param int $player_id The id of the player we are associating.
 * @param int $slot_id The slot number of the game.
 * @param int $game_id The game this slot is for.
 */
function insertSlot($player_id, $slot_id, $game_id, $manual = false, $replace = false)
{
	global $db;
	//Insert the slot.
	$sql = 'SELECT * FROM ' . MAFIA_SLOTS_TABLE . ' WHERE slot_id=' . $slot_id . ' AND game_id=' . $game_id;
	$result = $db->sql_query($sql);
	$slot = $db->sql_fetchrow($result);
	if (sizeOf($slot)){
	
	} else {
		$slot_ary = array(
			'game_id'	=> (int)$db->sql_escape($game_id),
			'slot_id' 	=> (int)$db->sql_escape($slot_id)
		);
			
		$sql = 'INSERT INTO ' . MAFIA_SLOTS_TABLE . ' ' . $db->sql_build_array('INSERT', $slot_ary);
		$db->sql_query($sql);
		$db->sql_freeresult($result);
	}
	$player_ary = array(
			'slot_id'	=> $slot_id,
		);
	//Update game player counts.
	$sql = 'UPDATE ' . MAFIA_GAMES_TABLE . '
			SET '.(($manual) ? '' : 'requested_players = requested_players - 1,') .'
			entered_players = entered_players + 1 
			WHERE game_id = ' . (int)$db->sql_escape($game_id);
	$db->sql_query($sql);
	//Update the player table with the proper slot id.
	
	
	$sql = 'UPDATE ' . MAFIA_PLAYERS_TABLE . ' SET ' . $db->sql_build_array('UPDATE', $player_ary)
		. ' WHERE player_id = '. $db->sql_escape($player_id);
	$db->sql_query($sql);
	$db->sql_freeresult($result);
	if (!$replace){
		//Check to see if signups are complete, move into setup status.
		//Then bump up any approved games to make sure that the max amount are in signups.
		$com_ary = array(
			'SELECT'	=> 'g.maximum_players, g.entered_players, g.game_type',
			'FROM'		=> array(MAFIA_GAMES_TABLE => 'g'),
			'WHERE'		=> 'g.game_id = '.(int)$db->sql_escape($game_id),
			);
		
		$sql = $db->sql_build_query('SELECT', $com_ary);
		$res = $db->sql_query($sql);
		if($ga = $db->sql_fetchrow($res))
		{
			if($ga['entered_players'] >= $ga['maximum_players'])
			{
				
				//Delete any orphaned unapproved player signups.
				$del = 'DELETE FROM '.MAFIA_PLAYERS_TABLE.' WHERE slot_id = 0 AND game_id = '.$db->sql_escape($game_id);
				$db->sql_query($del);	
					
				bumpIntoSignups($ga['game_type']);
				$sql = 'UPDATE ' . MAFIA_GAMES_TABLE . '
						SET status = '.GAME_PROGRESS_SETUP.'
						WHERE game_id = ' . (int)$db->sql_escape($game_id);
				$db->sql_query($sql);
			}
		}
		$db->sql_freeresult($res);
		startGame($game_id);
	}
}

/**
 * Moves approved games into signups when signup slots open up.
 * @param int $queue_id The queue to manipulate games in.
 */
function bumpintoSignups($queue_id)
{
	global $db;
	
	//Grab the game type information for signup limitations.
	$ga_ary = array(
		'SELECT'	=> 't.signup_limit',
		'FROM'		=> array(MAFIA_GAME_TYPES_TABLE => 't'),
		'WHERE'		=> 't.type_id = '.(int)$db->sql_escape($queue_id),
	);
	$sql = $db->sql_build_query('SELECT', $ga_ary);
	$res = $db->sql_query($sql);
	$info = $db->sql_fetchrow($res);
	$db->sql_freeresult($res);
	
	//Find out how many games are currently in signups.
	$sql_ary = array(
		'SELECT'	=> 'COUNT(g.game_id) as count',
		'FROM'		=> array(MAFIA_GAMES_TABLE => 'g'),
		'WHERE'		=> 'g.game_type = '.(int)$db->sql_escape($queue_id).' AND g.status = '.GAME_PROGRESS_SIGNUPS,
	);
	$sql = $db->sql_build_query('SELECT', $sql_ary);
	$res = $db->sql_query($sql);
	
	
	if($co = $db->sql_fetchrow($res))
	{
		$total = ($info['signup_limit'] - $co['count']);
		if($total > 0)
		{
			//Find the game that we will be moving in next.
			$move_ary = array(
				'SELECT'	=> 'g.*',
				'FROM'		=> array(MAFIA_GAMES_TABLE => 'g'),
				'WHERE'		=> 'g.approved_time is NOT NULL AND g.game_type=' . (int)$db->sql_escape($queue_id) . ' AND status = '.GAME_PROGRESS_QUEUED,
				'ORDER_BY'	=> 'g.approved_time ASC',
			);
			$sql = $db->sql_build_query('SELECT', $move_ary);

			$result = $db->sql_query_limit($sql, 1);
			//Make sure we actually have a game to bump.
			if($game = $db->sql_fetchrow($result))
			{
				//Move preins to entered players.
				$p_ary = array('type' => STANDARD_IN);
				
				$sql = 'UPDATE ' . MAFIA_PLAYERS_TABLE . '
				SET ' . $db->sql_build_array('UPDATE', $p_ary) . '
				WHERE game_id=' . $game['game_id'] .'
				AND type = 1';		
			
				$db->sql_query($sql);
				
				//Finally update the games.
				$sql = 'UPDATE ' . MAFIA_GAMES_TABLE . ' SET
					entered_players = requested_players,
					requested_players = 0,
					status = '. GAME_PROGRESS_SIGNUPS.'
					WHERE game_id=' . $game['game_id'];

					$db->sql_query($sql);
			}
			$db->sql_freeresult($result);
		}
	}
}
/**
 * Check whether a player is already in the specified game.
 * @param int $game_id The game to match against.
 * @param int $user_id The user to match against.
 * @param int $type Whether to check for a mod (1), player (2), or both (0).
 * @return bool Whether the user is already associated.
 */
function alreadyEntered($game_id, $user_id, $type = 0)
{
	global $db;
	$asPlayer = false;
	$asMod = false;
	$row = array();
	if($type == 0 || $type == 1)
	{
		$sql = 'SELECT m.game_id, m.user_id
			FROM phpbb_mafia_moderators m
			WHERE m.user_id = '.$db->sql_escape($user_id).'
			AND m.game_id = '.$db->sql_escape($game_id);
		$result = $db->sql_query($sql);
		$asMod = ($row = $db->sql_fetchrow($result)) ? true : false;
		$db->sql_freeresult($result);
	}
	if($type == 0 || $type == 2)
	{
		$sql = 'SELECT p.game_id, p.user_id, p.type
			FROM phpbb_mafia_players p
			WHERE p.user_id = '.$db->sql_escape($user_id).'
			AND p.game_id = '.$db->sql_escape($game_id);
		$result = $db->sql_query($sql);
		$row = $db->sql_fetchrow($result);
		$asPlayer = ($row) ? true : false;
		$db->sql_freeresult($result);
	}
	switch($type)
	{
		case 0:
			return ($asPlayer || $asMod) ? true : false;
			break;
		case 1: 
			return $asMod;
			break;
		case 2:
			return $asPlayer;
			break;
	}
}
	

/**
 * Create the template for the replacement selection.
 */
function setReplaceDateTemplate()
{
	global $template;
	
	//Select the proper display date based on V/LA status. Select today's date if none specified.
	$selectionDay = date('j');
	$selectionMonth = date('n');
	$selectionYear = date('Y');
	
	//Start Dates.
	for ($i = 1; $i < 32; $i++)
	{
		$selected = ($i == $selectionDay) ? ' selected="selected"' : '';
		$s_replace_day_options .= "<option value=\"$i\"$selected>$i</option>";
	}
	
	for ($i = 1; $i < 13; $i++)
	{
		$selected = ($i == $selectionMonth) ? ' selected="selected"' : '';
		$s_replace_month_options .= "<option value=\"$i\"$selected>" . date('F', mktime(0, 0, 0, $i, 1, date('Y'))) ."</option>";
	}
	$s_replace_year_options = '';
	
	$now = getdate();
	for ($i = $now['year']; $i <= ($now['year'] + 1); $i++)
	{
		$selected = ($i == $selectionyear) ? ' selected="selected"' : '';
		$s_replace_year_options .= "<option value=\"$i\"$selected>$i</option>";
	}
	unset($now);

	$template->assign_vars(array(
		'S_REPLACE_DAY_OPTIONS'	=> $s_replace_day_options,
		'S_REPLACE_MONTH_OPTIONS'	=> $s_replace_month_options,
		'S_REPLACE_YEAR_OPTIONS'	=> $s_replace_year_options,
	));	
}

/**
 * Validate and develop the replacement date from input.
 */
function developReplacementDate()
{
	$data['replace_day'] = request_var('replace_day', 0);
	$data['replace_month'] = request_var('replace_month', 0);
	$data['replace_year'] = request_var('replace_year', 0);

	
	//Validate the submitted dates.
	$validate_array = array(
		'replace_day'		=> array('num', true, 1, 31),
		'replace_month'	=> array('num', true, 1, 12),
		'replace_year'		=> array('num', true, 2011, gmdate('Y', time()) + 50),
	);
	$error = validate_data($data, $validate_array);
	if($error)
	{
		trigger_error('Shit messed up.');
	}
	
	//Static timestamp for date validations.
	$mkStatic = mktime(0,0,0,0,0,0);
	//Start Date timestamp for date comparison.
	$mkStart =  mktime(0,0,0,$data['replace_month'],$data['replace_day'],$data['replace_year']);
	
	
	//Make sure that every variable is set properly.
	if(($data['replace_day'] === 0) || ($data['replace_month'] === 0) || ($data['replace_year'] === 0))
	{
		trigger_error('TOO_SMALL');
	}
	//Make sure date exists.
	else if($mkStart == $mkStatic)
	{
		trigger_error('NO_REPLACE_DATA');
	}
	//Make sure that the end date is after today.
	else if($mkStart < time())
	{
		trigger_error('REPLACE_DATE_PRIOR');
	}
	//Make sure that date is not farther away than 2 months.
	else if(($mkStart - time()) > 5259487)
	{
		trigger_error('REPLACE_TOO_LARGE');
	}

	return $mkStart;
}

/**
 * Remove a player from signups or replacement.
 * @param int $game_id The game to remove the player from.
 * @param int $user_id The user to remove.
 */
function removeSignup($game_id, $user_id)
{
	global $db, $template;
	$game = load_game(0, $game_id);

	
	$sql_ary = array(
		'SELECT'	=> 'p.slot_id, p.type',
		'FROM'		=> array(
						MAFIA_PLAYERS_TABLE	=> 'p'),
		'WHERE'		=> 'p.game_id = '.$game_id.'
						AND p.user_id = '.$user_id,
	);
	
	$sql = $db->sql_build_query('SELECT', $sql_ary);
	$result = $db->sql_query($sql);
	if($row = $db->sql_fetchrow($result))
	{
	
		//First, make sure game isn't already started, otherwise this shouldn't happen.
		if($game['started_time']){
			$del = 'UPDATE '.MAFIA_PLAYERS_TABLE.' SET type=' . REPLACED_OUT . ' WHERE user_id = '.$db->sql_escape($user_id).' AND game_id = '.$db->sql_escape($game_id);
			$db->sql_query($del);
		} else {
			$del = 'DELETE FROM '.MAFIA_PLAYERS_TABLE.' WHERE user_id = '.$db->sql_escape($user_id).' AND game_id = '.$db->sql_escape($game_id);
			$db->sql_query($del);
			if($row['slot_id']){
				$del = 'DELETE FROM '.MAFIA_SLOTS_TABLE.' WHERE slot_id = '. $row['slot_id'] .' AND game_id = '.$db->sql_escape($game_id);
				$db->sql_query($del);
			}
		}
	
		//Check what type of signup it is so we can change proper totals.
		switch($row['type'])
		{
			case 0:
			case 1:
				//Delete player entry.
				//Update player totals for the game.
				if($row['slot_id'] == 0)
				{
					$upd = 'UPDATE '.MAFIA_GAMES_TABLE.' SET requested_players = ('.((int)($game['requested_players']) - 1).')
							WHERE game_id = '.$db->sql_escape($game['game_id']);
					$db->sql_query($upd);
				}
				else
				{
					$upd = 'UPDATE '.MAFIA_GAMES_TABLE.' SET entered_players = ('.((int)($game['entered_players']) - 1).')
							WHERE game_id = '.$db->sql_escape($game['game_id']);
					$db->sql_query($upd);
				}
				break;
			case 2:
				//Delete player entry.
				//Update player totals for the game.
				$upd = 'UPDATE '.MAFIA_GAMES_TABLE.' SET replacements = ('.((int)($game['replacements']) - 1).')
						WHERE game_id = '.$db->sql_escape($game['game_id']);
				$db->sql_query($upd);
				break;
			default:
				trigger_error('NO_SIGNUP_TYPE');
				break;
		}
	}
	else
	{
		trigger_error('NOT_SIGNED_UP');
	}
}

/**
 * Assemble the necessary info to add a game topic to the forum.
 * @param int $queue_forum the forum to add the topic to.
 */
function assembleGameTopic($queue_forum)
{

$uid = $bitfield = $options = ''; // will be modified by generate_text_for_storage
	$allow_bbcode = $allow_smilies = $allow_urls = true;
	$message = 'bonus2!';
	generate_text_for_storage($message, $uid, $bitfield, $options, $allow_bbcode, $allow_urls, $allow_smilies);
					
// New Topic Example
	$data = array( 
    // General Posting Settings
    'forum_id'            => $queue_forum,    // The forum ID in which the post will be placed. (int)
    'topic_id'            => 0,    // Post a new topic or in an existing one? Set to 0 to create a new one, if not, specify your topic ID here instead.
    'icon_id'            => false,    // The Icon ID in which the post will be displayed with on the viewforum, set to false for icon_id. (int)

    // Defining Post Options
    'enable_bbcode'    => true,    // Enable BBcode in this post. (bool)
    'enable_smilies'    => true,    // Enabe smilies in this post. (bool)
    'enable_urls'        => true,    // Enable self-parsing URL links in this post. (bool)
    'enable_sig'        => true,    // Enable the signature of the poster to be displayed in the post. (bool)

    // Message Body
    'message'            => $message,        // Your text you wish to have submitted. It should pass through generate_text_for_storage() before this. (string)
    'message_md5'    => md5($message),// The md5 hash of your message

    // Values from generate_text_for_storage()
    'bbcode_bitfield'    => $bitfield,    // Value created from the generate_text_for_storage() function.
    'bbcode_uid'        => $uid,        // Value created from the generate_text_for_storage() function.

    // Other Options
	//Automatically lock the topic so that the game mod can work on it.
    'post_edit_locked'    => 1,        // Disallow post editing? 1 = Yes, 0 = No
    'topic_title'        => $subject,    // Subject/Title of the topic. (string)

    // Email Notification Settings
    'notify_set'        => false,        // (bool)
    'notify'            => false,        // (bool)
    'post_time'         => 0,        // Set a specific time, use 0 to let submit_post() take care of getting the proper time (int)
    'forum_name'        => '',        // For identifying the name of the forum in a notification email. (string)

    // Indexing
    'enable_indexing'    => true,        // Allow indexing the post? (bool)

    // 3.0.6
    'force_approved_state'    => true, // Allow the post to be submitted without going into unapproved queue
	);

	return $data;
}
/**
 * Change the thread poster of a game topic.
 * @param int $post_id The topic id to manipulate.
 * @param int $new_mod The new topic poster to install.
 */
function correctThreadPoster($post_id, $new_mod)
{
	global $db;
	//Select the original poster's id from the tables.
	$sql = "SELECT p.poster_id, u.username FROM " . POSTS_TABLE . " p, " . USERS_TABLE . " u
	WHERE p.poster_id = u.user_id
	AND post_id = $post_id";
	
	//Establish the old posters variables.
	$result2 = $db->sql_query($sql);
	$old_poster = $db->sql_fetchrow($result2);
	$old_poster_id = $old_poster['poster_id'];
	$old_postername = $old_poster['username'];
	
	//Get the new posters information.
	$sql = "SELECT user_id, user_colour, username FROM " . USERS_TABLE . "
	WHERE user_id = '" . $db->sql_escape($new_mod) . "'";
	
	//Establish the new posters variables.
	$result = $db->sql_query($sql);
	$sql_username = $db->sql_fetchrow($result);
	$new_poster_id = $sql_username['user_id'];
	$new_user_color = $sql_username['user_colour'];
	$new_username	= $sql_username['username'];
	
	//Update the posts table with the new poster.
	$sql = 'UPDATE ' . POSTS_TABLE . '
	SET poster_id = '.$db->sql_escape($new_poster_id).'
	WHERE post_id = '.$db->sql_escape($post_id);
	$result = $db->sql_query($sql);
	
	//Variable to determine how many posts were affected.
	$moved_posts = $db->sql_affectedrows($result);
	
	
	//Update the posters post count to keep in sync.
	$sql = 'UPDATE ' . USERS_TABLE . '
	SET user_posts = user_posts + '.$db->sql_escape($moved_posts).'
	WHERE user_id = '.$db->sql_escape($new_poster_id);
	$db->sql_query($sql);


	$sql = 'UPDATE ' . USERS_TABLE . '
	SET user_posts = user_posts - '.$db->sql_escape($moved_posts).'
	WHERE user_id = '.$db->sql_escape($old_poster_id);
	$db->sql_query($sql);
	
	//Update the topic poster.
	//First make sure that the topic exists with the first post.
	$sql = 'SELECT topic_id FROM ' . TOPICS_TABLE . '
	WHERE topic_first_post_id = '.$db->sql_escape($post_id);
	$result = $db->sql_query($sql);

	$topic_array = $db->sql_fetchrow($result);
	
	if($topic_array)
	{
		$topic_id = $topic_array['topic_id'];
	
		//Update all necessary columns in the topic.
		$sql = 'UPDATE ' . TOPICS_TABLE . '
		SET topic_poster = '.$db->sql_escape($new_poster_id).',
		topic_first_poster_name = "'.$db->sql_escape($new_username).'",
		topic_first_poster_colour = "'.$db->sql_escape($new_user_color).'",	
		topic_last_poster_name = "'.$db->sql_escape($new_username).'",
		topic_last_poster_colour = "'.$db->sql_escape($new_user_color).'",
		topic_last_poster_id = '.$db->sql_escape($new_poster_id).'
		WHERE topic_id = '.$db->sql_escape($topic_id);
		$db->sql_query($sql);
	}
	
	//Update the forum's most recent post to reflect poster changes.
	
	$sql = 'SELECT forum_id FROM ' . FORUMS_TABLE . '
	WHERE forum_last_post_id = '.$db->sql_escape($post_id);
	$result = $db->sql_query($sql);

	$forum_array = $db->sql_fetchrow($result);
	if($forum_array)
	{
		$forum_id = $forum_array['forum_id'];
	
		//Update all necessary columns in forum.
		$sql = 'UPDATE ' . FORUMS_TABLE . '
		SET forum_last_poster_name = "'.$db->sql_escape($new_username).'",
		forum_last_poster_colour = "'.$db->sql_escape($new_user_color).'",
		forum_last_poster_id = '.$db->sql_escape($new_poster_id).'
		WHERE forum_id = '.$db->sql_escape($forum_id);
		$db->sql_query($sql);
	}
}

/**
 * Build the navigation links for the queue section.
 * @param int $game_id The current game displayed.
 * @param int $queue_id The current queue displayed.
 */
function buildQueueBreadcrumbs($game_id, $queue_id)
{
	global $template, $db, $user, $phpEx;
	// Set up the initial breadcrumb
	$template->assign_block_vars('navlinks', array(
				'FORUM_NAME'         => $user->lang['QUEUES'],
				'U_VIEW_FORUM'      => append_sid("{$phpbb_root_path}viewqueue.$phpEx"))
	);
	
	if($game_id)
	{
		$sql_ary = array(
			'SELECT'	=> 'g.name, g.game_id, t.type_name, t.type_id',
			'FROM'		=> array( MAFIA_GAMES_TABLE => 'g'),
			'LEFT_JOIN'	=> array(
				array(
					'FROM'	=> array(MAFIA_GAME_TYPES_TABLE => 't'),
					'ON'	=> 'g.game_type = t.type_id')),
			'WHERE'		=> 'g.game_id = '.$db->sql_escape($game_id)
			);
			$sql = $db->sql_build_query('SELECT', $sql_ary);
			$res = $db->sql_query($sql);
			$game = $db->sql_fetchrow($res);
			$db->sql_freeresult($res);
		
		//Assign queue breadcrumb.
		$template->assign_block_vars('navlinks', array(
					'FORUM_NAME'         => sprintf($user->lang['SINGLE_QUEUE'], $game['type_name']),
					'U_VIEW_FORUM'      => append_sid($phpbb_root_path.'viewqueue.'.$phpEx.'?q='.$game['type_id']))
		);
		//Assign game breadcrumb.
		$template->assign_block_vars('navlinks', array(
					'FORUM_NAME'         => $game['name'],
					'U_VIEW_FORUM'      => append_sid($phpbb_root_path.'viewgame.'.$phpEx.'?g='.$game['game_id']))
		);
	}
	else if($queue_id)
	{
		$sql_ary = array(
			'SELECT'	=> ' t.type_name, t.type_id',
			'FROM'		=> array( MAFIA_GAME_TYPES_TABLE => 't'),
			'WHERE'		=> 't.type_id = '.$db->sql_escape($queue_id)
			);
			$sql = $db->sql_build_query('SELECT', $sql_ary);
			$res = $db->sql_query($sql);
			$queue = $db->sql_fetchrow($res);
			$db->sql_freeresult($res);
			
		//Assign queue breadcrumb.
		$template->assign_block_vars('navlinks', array(
					'FORUM_NAME'         => sprintf($user->lang['SINGLE_QUEUE'], $queue['type_name']),
					'U_VIEW_FORUM'      => append_sid($phpbb_root_path.'viewqueue.'.$phpEx.'?q='.$queue['type_id']))
		);
	}
}

/** 
 * Grab all information about a game's players from the database.
 * @param int $game_id The game to grab information for.
 */
function grabPlayerInfo ($game_id, $approved_only = false, $exclude_rejected = true){
	global $template, $db, $user, $phpEx;
	$sql_ary = array(
	'SELECT'	=> 'p.*, s.*, r.*, f.*, m.*, u.user_id, u.username, g.status',
	'FROM'		=> array(MAFIA_PLAYERS_TABLE => 'p'),
	'LEFT_JOIN'	=> array(
		array(
			'FROM'	=> array(MAFIA_SLOTS_TABLE => 's'),
			'ON'	=> 'p.slot_id = s.slot_id AND s.game_id =' .$game_id),
		array(
			'FROM'	=> array(MAFIA_GAMES_TABLE => 'g'),
			'ON'	=> 'p.game_id = g.game_id'),
		array(
			'FROM'	=> array(MAFIA_ROLES_TABLE => 'r'),
			'ON'	=> 's.slot_role_id = r.role_id' ),
		array(
			'FROM'	=> array(USERS_TABLE => 'u'),
			'ON'	=> 'p.user_id = u.user_id'),
		array(
			'FROM'	=> array(MAFIA_FACTIONS_TABLE => 'f'),
			'ON'	=> 's.faction_id = f.id'),
		array(
			'FROM'	=> array(MAFIA_MODIFIERS_TABLE => 'm'),
			'ON'	=> 'm.modifier_id = r.role_modifier')),
	'WHERE'		=> 'p.game_id = '.$db->sql_escape($game_id) . ' AND ' . ($approved_only ? 'NOT s.slot_id = 0 ': '1=1') . ' AND ' . ($exclude_rejected ? 'NOT p.type =' . REJECTED_IN : '1=1'),
	'ORDER_BY'	=> 'p.slot_id DESC',
	);
	
	//Specifically select only players that have associated slots.
	if($approved_only)
	{
		$sql_ary['WHERE'] = 'p.game_id = '.$db->sql_escape($game_id).' AND p.slot_id != 0';
	}
	$sql = $db->sql_build_query('SELECT', $sql_ary);
	$res = $db->sql_query($sql);
		//setup for faction select element
		$factionSelectStart = '<select id="editPlayerFaction" name="editPlayerFaction"  class="addPlayerInput">';
		$factionSelectStart .= '<option value="0"></option>';
		$factionOptionStart = array();
		$factionOptionEnd = array();
		$factions = array();
		$sql = " SELECT *"
			 . " FROM " . MAFIA_FACTIONS_TABLE
			 . " WHERE game_id=" . $db->sql_escape($game_id);

		$result = $db->sql_query($sql);
		$count = 0;
		while($row = $db->sql_fetchrow($result)){
			$factions[$count] = $row['id'];
			$factionOptionStart[$row['id']] = '<option value="';
			$factionOptionStart[$row['id']] .= $row['id'];
			$factionOptionStart[$row['id']] .= '"';
			$factionOptionEnd[$row['id']] = '>';
			$factionOptionEnd[$row['id']] .= $row['name'] . "(" . getAlignmentName($row['alignment_id']) . ")";
			$factionOptionEnd[$row['id']] .= '</option>';
			$count++;
		}
		$db->sql_freeresult($result);
		
		$factionSelectEnd  = '</select>';

		//setup for modifier select element
		$modifierSelectStart = '<select id="editRoleModifier" name="editRoleModifier" class="addPlayerInput"';
		$modifierSelectStart .='>';
		$modifierSelectStart .= '<option value="0"></option>';
		$modifierOptionStart = array();
		$modifierOptionEnd = array();
		$modifiers = array();

		$sql = " SELECT *"
			 . " FROM " . MAFIA_MODIFIERS_TABLE;

		$result = $db->sql_query($sql);
		$count = 0;

		while($row = $db->sql_fetchrow($result)){
			$modifiers[$count]=$row['modifier_id'];
			$modifierOptionStart[$row['modifier_id']] = '<option value="';
			$modifierOptionStart[$row['modifier_id']] .= $row['modifier_id'];
			$modifierOptionStart[$row['modifier_id']] .= '"';
			$modifierOptionEnd[$row['modifier_id']] = '>';
			$modifierOptionEnd[$row['modifier_id']] .= $row['modifier_name'];
			$modifierOptionEnd[$row['modifier_id']] .= '</option>';
			$count++;
		}

		$db->sql_freeresult($result);
		$modifierSelectEnd  .= '</select>';

		//setup for basicRole select element
		$basicRoleSelectStart = '<select id="editBasicRole" name="editBasicRole" class="addPlayerInput"';
		$basicRoleSelectStart .='>';
		$basicRoleSelectStart .= '<option value="0"></option>';
		$basicRoleOptionStart = array();
		$basicRoleOptionEnd = array();
		$basicRoles = array();

		$sql = " SELECT *"
			 . " FROM " . MAFIA_ROLES_TABLE;

		$result = $db->sql_query($sql);
		$count = 0;

		while($row = $db->sql_fetchrow($result)){
			$basicRoles[$count]=$row['role_id'];
			$basicRoleOptionStart[$row['role_id']] = '<option value="';
			$basicRoleOptionStart[$row['role_id']]  .= $row['role_id'];
			$basicRoleOptionStart[$row['role_id']]  .= '"';
			$basicRoleOptionEnd[$row['role_id']]  = '>';
			$basicRoleOptionEnd[$row['role_id']]  .= $row['role_name'];
			$basicRoleOptionEnd[$row['role_id']]  .= '</option>';
			$count++;
		}

		$db->sql_freeresult($result);
		$basicRoleSelectEnd  .= '</select>';
	while($player = $db->sql_fetchrow($res))
	{
		//build factionSelect
			$factionSelect = $factionSelectStart;
			for ($i = 0; $i< sizeof($factions); $i++){
				$factionSelect .= $factionOptionStart[$factions[$i]];
				if ($factions[$i] == $player['id']){
					$factionSelect .= 'selected="selected"';
				}
				$factionSelect .= $factionOptionEnd[$factions[$i]];
			}
			$factionSelect .= $factionSelectEnd;

			//build modifierSelect
			$modifierSelect = $modifierSelectStart;
			for ($i = 0; $i< sizeof($modifiers); $i++){
				$modifierSelect .= $modifierOptionStart[$modifiers[$i]];
				if ($modifiers[$i] == $player['modifier_id']){
					$modifierSelect .= 'selected="selected"';
				}
				$modifierSelect .= $modifierOptionEnd[$modifiers[$i]];
			}
			$modifierSelect .= $modifierSelectEnd;

			//build basicRoleSelect
			$basicRoleSelect = $basicRoleSelectStart;
			for ($i = 0; $i< sizeof($basicRoles); $i++){
				$basicRoleSelect .= $basicRoleOptionStart[$basicRoles[$i]];
				if ($basicRoles[$i] == $player['role_id']){
					$basicRoleSelect .= 'selected="selected"';
				}
				$basicRoleSelect .= $basicRoleOptionEnd[$basicRoles[$i]];
			}
			$basicRoleSelect .= $basicRoleSelectEnd;
			
			//build statusSelect
			$playerStatusSelect = "<select name='editStatus' class='addPlayerInput'> <option value='" . SLOT_ALIVE . "'";
			if ($player['stauts'] == SLOT_ALIVE){
				$playerStatusSelect .='select="selected"';
			}
			$playerStatusSelect	.= ">Alive</option> <option value='" . SLOT_DEAD . "'";
			if ($player['stauts'] == SLOT_DEAD){
				$playerStatusSelect .='select="selected"';
			}
			$playerStatusSelect	.= ">Dead</option> </select>";
			
		$template->assign_block_vars('players', array(
			'USER'		=> get_username_string('full', $player['user_id'], $player['username']),
			'USER_ID'	=> $player['user_id'],
			'ROLE_NAME'	=> $player['role_name'], //Actual Role name - //TODO -- Add Role name lookup.
			'PRESENTED_ROLE' => ($player['g.slot_status'] == SLOT_DEAD) ? $player['role_flavour_name'] . '('.$player['role_name'] . ')' : $user->lang['PENDING'], //Role name to show regular players.
			'FULL_ROLE'	=> generateFullRoleName($player['role_name'], $player['role_flavour_name'], $player['modifiers']),
			'FLAVOUR_NAME'	=> $player['role_flavour_name'],
			'STATUS'	=>  $player['type']== REPLACED_OUT ? 'Replaced' : getSlotStatusName($player['slot_status']),
			'STATUS_OPTIONS'	=> createPlayerStatusOptions($player['slot_status']),
			'ROLE_OPTIONS'	=> createRoleOptions($player['role_id']),
			'MODIFIER_OPTIONS'		=> createModifierOptions(explode(',', $player['modifier_name'])),
			'SLOT_ID'	=> $player['slot_id'] ? $player['slot_id'] : 'Pending Approval' ,
			'FACTION_NAME' => generateFullFactionName($player['name'], $player['alignment_id']), //Actual Factional name. //TODO -- Add faction name.
			/*'PRESENTED_FACTION' => ($player['g.slot_status'] == SLOT_DEAD) ? 'ROLE_NAME' : $user->lang['PENDING'],*/ //For now only add faction on slot reveal -- Faction to show regular players.
			'OUTCOME'	=> getSlotOutcomeName($player['slot_outcome']),
			'OUTCOME_OPTIONS'	=> createOutcomeOptions($player['slot_outcome']),
			'IS_ACCEPTED'	=> ($player['slot_id'] == 0) ? false : true,
			'IS_REJECTED' 	=> ($player['type'] == 4) ? true : false,
			'APPROVAL_LINK'	=> append_sid($phpbb_root_path.'viewgame.'.$phpEx.'?mode=approve_player&amp;g='.$game_id.'&amp;pid='.$player['player_id']),
			'REMOVE_LINK'	=> append_sid($phpbb_root_path.'viewgame.'.$phpEx.'?mode=remove_player&amp;g='.$game_id.'&amp;u='.$player['user_id']),
			'REJECT_LINK'	=> append_sid($phpbb_root_path.'viewgame.'.$phpEx.'?mode=disapprove_player&amp;g='.$game_id.'&amp;pid='.$player['player_id']),
			'FACTION_SELECT'		=> $factionSelect,
			'MODIFIER_SELECT'		=> $modifierSelect,
			'BASICROLES_SELECT'		=> $basicRoleSelect,
			'PLAYER_STATUS_SELECT'	=> $playerStatusSelect,
		));
	}
	$db->sql_freeresult($res);
}

function generateFullFactionName($factionName, $alignment_id){
	if($alignment_id) { 
		return $factionName . "(" .  getAlignmentName($alignment_id) . ")";
	}
	return $factionName;
}

function generateFullRoleName($name, $flavour, $modifiers)
{
	
	global $db;
	$html = '';
	if ($name && $flavour && $modifiers){
		/*$sql = 'SELECT * FROM '. MAFIA_MODIFIERS_TABLE .'
		WHERE modifier_id IN ('.$db->sql_escape($modifiers).')';
		$result = $db->sql_query($sql);

		while($row = $db->sql_fetchrow($result)){
			$html .= $row['modifier_name'].'&nbsp;';
			}*/
		$html .= $flavour . ' ('. $modifiers. ' ' . $name .')';
	}
	return $html;
	
}
/**
 * Get the proper language string for a slot's status.
 * @param int $status The status to compare against.
 * @return The proper language string.
 */
function getSlotStatusName($status)
{
	global $user;
	//Define the proper status label.
	switch($status)
	{
		case SLOT_STATUS_PENDING:
			$stat = $user->lang['PENDING'];
			break;
		case SLOT_ALIVE:
			$stat = $user->lang['SLOT_ALIVE'];
			break;
		case SLOT_DEAD:
			$stat = $user->lang['SLOT_DEAD'];
			break;
		case SLOT_OTHER:
			$stat = $user->lang['SLOT_OTHER'];
			break;
		default:
			$stat = $user->lang['PENDING'];
			break;
	}
	return $stat;
}

/**
 * Get the proper language string for a slot's outcome.
 * @param int $status The outcome to compare against.
 * @return The proper language string.
 */
function getSlotOutcomeName($out)
{
	global $user;
	//Define the proper outcome label.
	switch($out)
	{
		case SLOT_OUTCOME_PENDING:
			$outcome = $user->lang['PENDING'];
			break;
		case SLOT_WIN:
			$outcome = $user->lang['SLOT_WIN'];
			break;
		case SLOT_LOSS:
			$outcome = $user->lang['SLOT_LOSS'];
			break;
		case SLOT_DRAW:
			$outcome = $user->lang['SLOT_DRAW'];
			break;
		default:
			$outcome = $user->lang['PENDING'];
			break;
	}
	return $outcome;
}

function getPlayerTypeName($type){
	switch($type){
		case STANDARD_IN : return 'In';
		case PREIN : return 'PreIn';
		case REPLACEMENT : return 'Replacement';
	}
}
/**
 * Check to see if this status is a specific status.
 * If both parameters are defined it defaults to id. 
 * @param string $status_name Status name to compare against.
 * @param int $status_id Status id to compare against.
 * @return bool Defining whether it is that status type.
 */
function isStatus($status_name = '', $status_id = 0, $return = 0)
{
	global $db;
	if($status_id)
	{
		$where = 's.status_id = '.$db->sql_escape($status_id);
	}
	elseif($status_name)
	{
		$where = 's.status_name = '.$db->sql_escape($status_name);
	}
	
	$sql = array(
		'SELECT'	=> 's.*',
		'FROM'		=> array(MAFIA_GAME_STATUS_TABLE => 's'),
		'WHERE'		=> $where,
	);
	
	$sql = $db->sql_build_query('SELECT', $sql_ary);
	$res = $db->sql_query($sql);
	if($return)
	{
		$row = $db->sql_fetchrpw($result);
		return $row;
	}
	else
	{
		return ($row = $db->sql_fetchrow($result)) ? true : false;
	}
}
/**
 * Check to see if this status is a specific status.
 * If both parameters are defined it defaults to id. 
 * @param int $game_id Status name to compare against.
 * @return bool false if the game doesn't exist.
 */
function startGame($game_id)
{
	global $db, $game;
	// make sure the game exists
	if($game){
		//Create the game topic for the moderator.
		//TODO - Review permission scheme.
		$que = 'SELECT * FROM '.MAFIA_GAME_TYPES_TABLE.' WHERE
		type_id = '. $game['game_type'];
		$res = $db->sql_query($que);
		$forum = $db->sql_fetchrow($res);
		$db->sql_freeresult($res);
		
		//Create subject line.
		$subject = $game['type_name'] . ' ' . ($nu['numbering'] + 1) . ' - ' . $game['name'];
		$data = assembleGameTopic($forum['forum_id']);
		submit_post('post',  $subject,  'mith',  POST_NORMAL,  $poll,  $data);
		//Correct all the discrepancies in thread poster.
		correctThreadPoster($data['post_id'],$game['main_mod_id']);
		//Add them to the proper group so they have permissions.
		group_user_add($forum['group_id'], $game['main_mod_id']); 
		
		//update the status, started_time and game topic
		$sql = 'UPDATE ' . MAFIA_GAMES_TABLE . '
			SET started_time = '. time() .',
				status=4 ,
				topic_id = ' . $data['topic_id'] . '
			WHERE game_id = ' . (int)$db->sql_escape($game_id);
		$db->sql_query($sql);
		return true;
	} else { 
		return false;
	}
	
}
?>
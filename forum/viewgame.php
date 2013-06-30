<?php
/**
*
* @package phpBB3
* @version $Id$
* @copyright (c) 2005 phpBB Group
* @license http://opensource.org/licenses/gpl-license.php GNU Public License
*
*/

/**
* @ignore
*/
define('IN_PHPBB', true);
$phpbb_root_path = (defined('PHPBB_ROOT_PATH')) ? PHPBB_ROOT_PATH : './';
$phpEx = substr(strrchr(__FILE__, '.'), 1);
include($phpbb_root_path . 'common.' . $phpEx);
include($phpbb_root_path . 'includes/functions_display.' . $phpEx);
include($phpbb_root_path . 'includes/functions_mafia_games.' . $phpEx);

// Start session.
$user->session_begin();
$auth->acl($user->data);

// Start initial var setup.
$gameId = request_var('g', 0);
$action = request_var('action', '');

if($action == "create")
{
	$gameName = request_var("gameName", "");
	$gameId = createMafiaGame($gameName);
	redirect(getViewMafiaGameUrl($gameId));
}

// Check if the user has actually sent a forum ID with his/her request
// If not give them a nice error page.
if (!$gameId)
{
	trigger_error('NO_FORUM'); //TODO: NO_GAME
}

$sql = " SELECT games.*"
     . " FROM " . MAFIA_GAMES_TABLE . " games"
     . " WHERE game_id=" . $gameId;

$result = $db->sql_query($sql);
$game_data = $db->sql_fetchrow($result);
$db->sql_freeresult($result);

// Configure style, language, etc.
$user->setup('viewgame', $forum_data['forum_style']);

$template->set_filenames(array(
	'body' => 'viewgame.html')
);

if (!$game_data)
{
	trigger_error('NO_FORUM'); //TODO: NO_GAME
}

page_header("Test Page Header");

//These are guaranteed not to be null.
$gameName = $game_data['name'];
$gameId = $game_data['game_id'];
$gameType = getMafiaGameTypeName($game_data['game_type']);
$gameStatus = getMafiaGameStatusName($game_data['status']);
$createdTime = strftime("%Y-%m-%d", $game_data['created_time']);

//These, however, might be null.
$startedTime = ($game_data['started_time'] === NULL ? "" : strftime("%Y-%m-%d", $game_data['started_time']));
$completedTime = ($game_data['completed_time'] === NULL ? "" : strftime("%Y-%m-%d", $game_data['completed_time']));

//Push the data into the template.
$template->assign_vars(array(
	'GAME_ID'		=> $gameId,
	'GAME_NAME'		=> $gameName,
	'CREATED_TIME'		=> $createdTime,
	'STARTED_TIME'		=> $startedTime,
	'COMPLETED_TIME'	=> $completedTime,
	'GAME_TYPE'		=> $gameType,
	'GAME_STATUS'		=> $gameStatus
));

//Load the moderators.
$sql = " SELECT"
     . "  mafia_moderators.*,"
     . "  users.*"
     . " FROM"
     . "  " . MAFIA_MODERATORS_TABLE . " mafia_moderators,"
     . "  " . USERS_TABLE . " users"
     . " WHERE mafia_moderators.user_id = users.user_id"
     . " AND mafia_moderators.game_id = " . $gameId
     . " ORDER BY type";

$result = $db->sql_query($sql);
while( $moderator_row = $db->sql_fetchrow($result) )
{
	$moderatorType = getMafiaModeratorTypeName($moderator_row['type']);
	$moderatorId = $moderator_row['moderator_id'];
	$template->assign_block_vars('moderator', array(
	
		'USER_URL' 	=> get_username_string('full', $moderator_row['user_id'], $moderator_row['username'], $moderator_row['user_colour']),
		'TYPE'		=> $moderatorType,
		'ID'		=> $moderatorId
	
	));
}
$db->sql_freeresult($result);


//Load the players.
$sql = " SELECT"
     . "  mafia_players.*,"
     . "  mafia_slots.*,"
     . "  mafia_factions.*,"
     . "  users.*,"
     . "  mafia_players.status AS player_status,"
     . "  mafia_slots.status AS slot_status,"
     . "  first_active_post.post_time AS first_active_time"
     . " FROM"
     . "  " . MAFIA_PLAYERS_TABLE . " mafia_players,"
     . "  " . MAFIA_SLOTS_TABLE . " mafia_slots,"
     . "  " . MAFIA_FACTIONS_TABLE . " mafia_factions,"
     . "  " . USERS_TABLE . " users,"
     . "  " . POSTS_TABLE . " first_active_post"
     . " WHERE mafia_players.slot_id = mafia_slots.id"
     . " AND mafia_slots.faction_id = mafia_factions.id"
     . " AND mafia_players.user_id = users.user_id"
     . " AND first_active_post.post_id = mafia_players.first_active_post_id"
     . " AND mafia_players.game_id = " . $gameId;

$result = $db->sql_query($sql);
while( $player_row = $db->sql_fetchrow($result) )
{
	//$playerStatus = getMafiaPlayerStatusName($player_row['status']);
	$slotNumber = $player_row['slot'];
	$factionName = $player_row['name'];
	$alignmentName = getMafiaAlignmentName($player_row['alignment_id']);
	$outcome = getMafiaSlotOutcomeName($player_row['outcome']);
	$status = getMafiaSlotStatusName($player_row['slot_status']);
	$role = "";
	$firstActiveTime = strftime("%Y-%m-%d", $player_row['first_active_time']);
	$playerId = $player_row['player_id'];

	$template->assign_block_vars('player', array(
	
		'USER_URL' 		=> get_username_string('full', $player_row['user_id'], $player_row['username'], $player_row['user_colour']),
		'STATUS'		=> $status,
		'OUTCOME'		=> $outcome,
		'FACTION'		=> $factionName,
		'ALIGNMENT'		=> $alignmentName,
		'ROLE'			=> $role,
		'ID'			=> $playerId,
		'FIRST_ACTIVE_TIME'	=> $firstActiveTime
	
	));
}
$db->sql_freeresult($result);


make_jumpbox(append_sid("{$phpbb_root_path}viewforum.$phpEx"));

page_footer();
?>

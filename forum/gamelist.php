<?php
/**
*
* @package phpBB3
* @version $Id$
* @copyright (c) 2005 phpBB Group
* @license http://opensource.org/licenses/gpl-license.php GNU Public License
*
*/
//Test comment.
/**
* @ignore
*/
define('IN_PHPBB', true);
$phpbb_root_path = (defined('PHPBB_ROOT_PATH')) ? PHPBB_ROOT_PATH : './';
$phpEx = substr(strrchr(__FILE__, '.'), 1);
include($phpbb_root_path . 'common.' . $phpEx);
include($phpbb_root_path . 'includes/functions_display.' . $phpEx);
include($phpbb_root_path . 'includes/functions_mafia_games.' . $phpEx);

// Start session
$user->session_begin();
$auth->acl($user->data);

$orderBy = request_var("order_by", "");
$orderDirection = request_var("order_dir", "");

// Configure style, language, etc.
$user->setup('gamelist', $forum_data['forum_style']);

$template->set_filenames(array(
	'body' => 'gamelist.html')
);

page_header("Mafia Game List");






// Pull the list of games from the database.
$sqlOrderBy = "game_id";
if($orderBy == "game_id")
	$sqlOrderBy = "game_id";
else if($orderBy == "game_name")
	$sqlOrderBy = "name";
else if($orderBy == "game_type")
	$sqlOrderBy = "game_type";
else if($orderBy == "game_status")
	$sqlOrderBy = "status";
else if($orderBy == "start_time")
	$sqlOrderBy = "started_time";
else if($orderBy == "end_time")
	$sqlOrderBy = "completed_time";

$sqlOrderDirection = "ASC";
if($orderDirection == "asc")
	$sqlOrderDirection = "ASC";
else if($orderDirection == "desc")
	$sqlOrderDirection = "DESC";

$sql = " SELECT games.*"
     . " FROM " . MAFIA_GAMES_TABLE . " games"
     . " ORDER BY $sqlOrderBy $sqlOrderDirection";

$result = $db->sql_query($sql);

//Type Status Start Date End Date
while( ($row = $db->sql_fetchrow($result)) )
{
	$gameId = $row['game_id'];
	$gameName = $row['name'];
	$gameType = getMafiaGameTypeName($row['game_type']);
	$gameStatus = getMafiaGameStatusName($row['status']);
	$startDate = ($row['started_time'] === NULL ? "&nbsp;" : strftime("%Y-%m-%d", $row['started_time']));
	$endDate = ($row['completed_time'] === NULL ? "&nbsp;" : strftime("%Y-%m-%d", $row['completed_time']));
	$gameUrl = getViewMafiaGameAnchorTag($gameId, $gameName);
	
	$template->assign_block_vars("gamerow", array
	(
		"GAME_URL"		=> $gameUrl,
		"GAME_ID"		=> $gameId,
		"GAME_NAME"		=> $gameName,
		"GAME_TYPE"		=> $gameType,
		"GAME_STATUS"		=> $gameStatus,
		"STARTED_DATE"		=> $startDate,
		"COMPLETED_DATE"	=> $endDate
	));
//	exit;
}

$db->sql_freeresult($result);

$createGameAction = append_sid("{$phpbb_root_path}viewgame.$phpEx");

$template->assign_vars(array(
	'CREATE_GAME_ACTION'		=> $createGameAction
));

page_footer();
?>

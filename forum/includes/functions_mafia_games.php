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

function getMafiaGameTypeName($mafiaGameType)
{

	switch($mafiaGameType)
	{
	case MAFIA_GAME_TYPE_LARGE_NORMAL: return 'Large Normal';
	case MAFIA_GAME_TYPE_MINI_NORMAL: return 'Mini Normal';
	case MAFIA_GAME_TYPE_NEWBIE: return 'Newbie';
	case MAFIA_GAME_TYPE_LARGE_THEME: return 'Large Theme';
	case MAFIA_GAME_TYPE_MINI_NORMAL: return 'Mini Normal';
	case MAFIA_GAME_TYPE_MINI_THEME: return 'Mini Theme';
	case MAFIA_GAME_TYPE_OPEN: return 'Open Setup';
	case MAFIA_GAME_TYPE_MARATHON: return 'Marathon';
	}
	
	//You should never get here.
}

function getMafiaGameStatusName($mafiaGameStatus) {

	switch($mafiaGameStatus)
	{
	case MAFIA_GAME_PROGRESS_PENDING: return 'Pending';
	case MAFIA_GAME_PROGRESS_SIGNUPS: return 'Signups';
	case MAFIA_GAME_PROGRESS_ONGOING: return 'Ongoing';
	case MAFIA_GAME_PROGRESS_COMPLETE: return 'Complete';
	}
}

function getMafiaModeratorTypeName($mafiaModeratorType) {

	switch($mafiaModeratorType)
	{
	case MAFIA_MODATOR_TYPE_PRIMARY: return "Primary";
	case MAFIA_MODATOR_TYPE_COMOD: return "Co-Mod";
	case MAFIA_MODATOR_TYPE_BACKUP: return "Backup";
	}
}

function getMafiaPlayerStatusName($mafiaPlayerStatus) {
	
	switch($mafiaPlayerStatus)
	{
	case MAFIA_PLAYER_STATUS_ALIVE: return "Alive";
	case MAFIA_PLAYER_STATUS_DEAD: return "Dead";
	}
}

function getMafiaAlignmentName($mafiaAlignment) {

	switch($mafiaAlignment)
	{
	case MAFIA_ALIGNMENT_TOWN: return "Town";
	case MAFIA_ALIGNMENT_MAFIA: return "Mafia";
	case MAFIA_ALIGNMENT_CULT: return "Cult";
	case MAFIA_ALIGNMENT_THIRD_PARTY: return "Third Party";
	case MAFIA_ALIGNMENT_NEUTRAL: return "Neutral";
	}
}

function getMafiaSlotStatusName($mafiaSlotStatus) {

	switch($mafiaSlotStatus)
	{
	case MAFIA_SLOT_STATUS_ALIVE: return "Alive";
	case MAFIA_SLOT_STATUS_DEAD: return "Dead";
	}
}

function getMafiaSlotOutcomeName($mafiaSlotOutcome) {

	switch($mafiaSlotOutcome)
	{
	case MAFIA_SLOT_OUTCOME_WON: return "Won";
	case MAFIA_SLOT_OUTCOME_LOST: return "Lost";
	case MAFIA_SLOT_OUTCOME_DRAW: return "Draw";
	}
}

function getViewMafiaGameAnchorTag($mafiaGameId, $mafiaGameName) {

	return "<a href='" . getViewMafiaGameUrl($mafiaGameId). "'>$mafiaGameName</a>";
}

function getViewMafiaGameUrl($mafiaGameId) {

	global $phpEx;
	return append_sid("{$phpbb_root_path}viewgame.$phpEx", "g=$mafiaGameId");
}

//Returns the newly created mafia game id
function createMafiaGame($gameName) {

	global $db, $auth, $user, $config, $phpEx;
	$insertArray = array
	(
		"name"			=> $gameName,
		"description"		=> "",
		"game_type"		=> MAFIA_GAME_TYPE_LARGE_NORMAL,
		"status"		=> MAFIA_GAME_PROGRESS_PENDING,
		"created_time"		=> time(),
		"created_by_user_id"	=> $user->data['user_id']
	);
	
	$db->sql_multi_insert(MAFIA_GAMES_TABLE, $insertArray);
	
	return $db->sql_nextid();
}

?>
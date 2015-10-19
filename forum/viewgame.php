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
include($phpbb_root_path . 'includes/functions_posting.' . $phpEx);
include($phpbb_root_path . 'includes/functions_user.' . $phpEx);
include($phpbb_root_path . 'includes/functions_games.' . $phpEx);


// Start session
$user->session_begin();
$auth->acl($user->data);
$user->setup('games');

//Display variables.
$game_id = (int)request_var('g', 0);
$queue = (int)request_var('q', 0);
$mode = request_var('mode', '');
$type = request_var('type', '');
$d_approval = (int)request_var('appr', 1);
$d_status = (int)request_var('sta', 0);

//Pagination variables.
$start   = request_var('start', 0);
$limit   = request_var('limit', (int) 25);

//Ensure that user is logged in before proceeding with anything.
if ($user->data['user_id'] == ANONYMOUS)
{
	$loc = append_sid($phpbb_root_path . 'ucp.' . $phpEx . '?mode=login');
	$message = $user->lang['LOGIN_ERROR_QUEUE'] . '<br /><br />' . sprintf($user->lang['LOGIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
	meta_refresh(3, $loc);
	trigger_error($message);
}
	if ($game_id){
		$sql = 'SELECT game_type FROM ' . MAFIA_GAMES_TABLE .
			   ' WHERE game_id =' . $game_id;
		$res = $db->sql_query($sql);
		$qu = $db->sql_fetchrow($res);
		$queue = $qu['game_type'];
		$db->sql_freeresult($res);
	}
	switch($mode)
	{
	//*********************************************
	//Handle player signups.
		case 'enter':
			if(!$game_id)
			{
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
				$message = $user->lang['NO_GAME_SPECIFIED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
				
			}
			$game = load_game(0, $game_id);
			
			//check if the player has not reached their limit yet.
			if(overPlayerLimits($game['game_type']))
			{
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx.'?mode=view&q='.$game['game_id']);
				$message = $user->lang['QUEUE_PLAYER_LIMIT_REACHED'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
			}
			
			//Make sure we aren't a mod or already entered.
			if(alreadyEntered($game['game_id'], $user->data['user_id']))
			{
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx.'?mode=view&g='.$game['game_id']);
				$message = $user->lang['ALREADY_PART_GAME'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
			}
			
			
			switch($type)
			{
				case 'in':
					//Check to make sure the game is approved for regular signups.
					if(!$game['approved_time'])
					{
						$loc = append_sid($phpbb_root_path . 'viewgame.' . $phpEx.'?g='.$game['game_id']);
						$message = $user->lang['GAME_NOT_APPROVED'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
						meta_refresh(3, $loc);
						trigger_error($message);
					}
					if($game['started_time'])
					{
						$loc = append_sid($phpbb_root_path . 'viewgame.' . $phpEx.'?g=' . $game['game_id']);
						$message = 'Game has already started' . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
						meta_refresh(3, $loc);
						trigger_error($message);
					}
					
					//If we get here we are all set to accept the /in.
					insertPlayer($game['game_id'], $user->data['user_id']);
					$loc = append_sid($phpbb_root_path . 'viewgame.' . $phpEx.'?g=' . $game['game_id']);
					$message = $user->lang['IN_SUCCESSFUL'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');;
					meta_refresh(3, $loc);
					trigger_error($message);
					break;
					
				case 'prein':
					//Check to make sure the game isn't already approved for regular signups.
					if($game['status_id'] != GAME_PROGRESS_QUEUED)
					{
						$loc = append_sid($phpbb_root_path . 'viewgame.' . $phpEx.'?mode=view&g='.$game['game_id']);
						$message = 'Game not available for preins' . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
						meta_refresh(3, $loc);
						trigger_error($message);
					}
					//Check to see if prein slots are full.
					if($game['requested_players'] >= (floor($game['maximum_players'] * ($game['prein_percentage'] / 100))))
					{
						$loc = append_sid($phpbb_root_path . 'viewgame.' . $phpEx.'?mode=view&g='.$game['game_id']);
						$message = 'All prein slots have already been taken.'. '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
						meta_refresh(3, $loc);
						trigger_error($message);
					}
					
					//If we get here we are all set to accept the /prein.
					insertPlayer($game['game_id'], $user->data['user_id'], 1);
					$loc = append_sid($phpbb_root_path . 'viewgame.' . $phpEx.'?mode=view&g='.$game['game_id']);
						$message = $user->lang['GAME_PREIN_SUCCESSFUL'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
						meta_refresh(3, $loc);
						trigger_error($message);
					break;
				case 'replace':
					if(!$game['approved_time'])
					{
						$loc = append_sid($phpbb_root_path . 'viewgame.' . $phpEx.'?g='.$game['game_id']);
						$message = $user->lang['GAME_NOT_APPROVED'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
						meta_refresh(3, $loc);
						trigger_error($message);
					}
					$sql = 'SELECT s.slot_id FROM ' . MAFIA_SLOTS_TABLE . ' s LEFT JOIN ' . MAFIA_PLAYERS_TABLE . ' p ON p.slot_id = s.slot_id AND p.game_id = s.game_id AND p.type<>5 WHERE p.player_id IS NULL AND s.game_id=' . $game_id .' LIMIT 1';
					$result = $db->sql_query($sql);
					$slot = $db->sql_fetchrow($result);
					if (!$slot['slot_id']){
						$loc = append_sid($phpbb_root_path . 'viewgame.' . $phpEx.'?g='.$game['game_id']);
						$message = 'No players need replacement.' . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
						meta_refresh(3, $loc);
						trigger_error($message);
					}
					insertPlayer($game['game_id'], $user->data['user_id']);
					$loc = append_sid($phpbb_root_path . 'viewgame.' . $phpEx.'?g=' . $game['game_id']);
					$message = $user->lang['IN_SUCCESSFUL'] . '<br /><br /><a href="' . $loc . '">'.$user->lang['RETURN_GAME_VIEW'].'</a>';
					meta_refresh(3, $loc);
					trigger_error($message);
					break;
				default:
					$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
					$message = $user->lang['NO_INTYPE_SPECIFIED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
					break;
			}
			break;
	//*********************************************
	//Handle player removals.
		case 'approve_player':
			if(alreadyEntered($game_id, $user->data['user_id'], 1) || $auth->acl_get('u_queue_'.$queue)){
				$template->assign_vars(array(
					'EDIT'	=> true,
				));
			} else {
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
				$message = "You don't have permission to approve players" . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
			}
			$player_id = (int)request_var('pid', 0);
			
			$sql = 'SELECT slot_id FROM ' . MAFIA_PLAYERS_TABLE . ' WHERE player_id=' . $player_id . ' AND game_id=' . $game_id;
			$result = $db->sql_query($sql);
			$slot_id = $db->sql_fetchrow($result);
			if (!empty($slot_id['slot_id'])){
				$loc = append_sid($phpbb_root_path . 'viewgame.'.$phpEx.'?g='. $game_id);
				$message = 'Player already approved.' . '<br /><br />' . '<a href="' . $loc . '">' . $user->lang['RETURN_GAME_VIEW'] . '</a>';
				meta_refresh(3, $loc);
				trigger_error($message);
			}
			$sql = 'SELECT s.slot_id FROM ' . MAFIA_SLOTS_TABLE . ' s LEFT JOIN ' . MAFIA_PLAYERS_TABLE . ' p ON p.slot_id = s.slot_id AND p.game_id = s.game_id AND p.type<>5 WHERE p.player_id IS NULL AND s.game_id=' . $game_id .' LIMIT 1';
			$result = $db->sql_query($sql);
			$slot = $db->sql_fetchrow($result);
			if ($slot['slot_id']){
				$slot_id = $slot['slot_id'];
			} else {
				$sql = 'SELECT count(*) AS num_slots FROM ' . MAFIA_SLOTS_TABLE . ' WHERE game_id=' . $game_id;
				$result = $db->sql_query($sql);
				$slot_num = $db->sql_fetchrow($result);
				$slot_id = $slot_num['num_slots'] + 1;
			}
			$game = load_game(0, $game_id);
			insertSlot($player_id,$slot_id, $game_id, false, $game['started_time']);
			$loc = append_sid($phpbb_root_path . 'viewgame.'.$phpEx.'?g='. $game_id);
			$message = 'Player approved.' . '<br /><br />' . '<a href="' . $loc . '">' . $user->lang['RETURN_GAME_VIEW'] . '</a>';
			meta_refresh(3, $loc);
			trigger_error($message);
		break;
		case 'disapprove_player':
			if(alreadyEntered($game_id, $user->data['user_id'], 1) || $auth->acl_get('u_queue_'.$queue)){
				$template->assign_vars(array(
					'EDIT'	=> true,
				));
			} else {
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
				$message = "You don't have permission to disapprove players" . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
			}
			$player_id = (int)request_var('pid', 0);
			$sql='UPDATE '. MAFIA_PLAYERS_TABLE . ' SET type= ' . REJECTED_IN . ' WHERE player_id= '. $player_id . ' AND game_id= '. $game_id; 
			$db->sql_query($sql);
			$loc = append_sid($phpbb_root_path . 'viewgame.' . $phpEx . '?g=' . $game_id);
			$message = 'Player disapproved' . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
			meta_refresh(3, $loc);
			trigger_error($message);
		break;
		case 'out':
			if(!$game_id)
			{
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
				$message = $user->lang['NO_GAME_SPECIFIED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
				
			}
			removeSignup($game_id, $user->data['user_id']);
			$loc = append_sid($phpbb_root_path . 'viewgame.' . $phpEx . '?g=' . $game_id);
			$message = $user->lang['OUT_SUCCESSFUL'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
			meta_refresh(3, $loc);
			trigger_error($message);
			break;
		case 'remove_player':
			if(alreadyEntered($game_id, $user->data['user_id'], 1) || $auth->acl_get('u_queue_'.$queue)){
				$template->assign_vars(array(
					'EDIT'	=> true,
				));
			} else {
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
				$message = "You don't have permission to remove players" . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
			}
			$user_id = (int)request_var('u', 0);
			removeSignup($game_id, $user_id);
			$loc = append_sid($phpbb_root_path . 'viewgame.' . $phpEx . '?g=' . $game_id);
			$message = 'Player removed successfully.' . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
			meta_refresh(3, $loc);
			trigger_error($message);
			break;
	//*********************************************
	//Handle game approval.
		case 'approve':
			//Make sure we have a game id.
			if(!$game_id)
			{
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
				$message = $user->lang['NO_GAME_SPECIFIED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
			}
			
			//Check if game exists and load data if it does.
			$game = load_game(0, $game_id);
			
			if(sizeof($game))
			{
				
				//Make sure we have listmod permissions before approving...
				if(!$auth->acl_get('u_queue_'.$game['game_type']))
				{
					$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
					$message = $user->lang['NOT_AUTHORISED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				}
				//if the game is already approved, don't bother.
				if($game['approved_time'])
				{
					$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
					$message = $user->lang['ALREADY_APPROVED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				}
				
				if(confirm_box(true))
				{	
					//Create the proper numbering for the game.
					$num = 'SELECT numbering FROM '.MAFIA_GAMES_TABLE.' WHERE
					game_type = '.$game['game_type'].' ORDER BY numbering DESC LIMIT 1';
					$res = $db->sql_query($num);
					$nu = $db->sql_fetchrow($res);
					$db->sql_freeresult($res);	
					
					//Finish by updating the game table...
					$update_ary = array(
						'approved_by_user_id'	=> $user->data['user_id'],
						'approved_time'			=> time(),
						'numbering'				=> ($nu['numbering'] + 1),
						'status'				=> GAME_PROGRESS_QUEUED,
					);
					
					$sql = 'UPDATE ' . MAFIA_GAMES_TABLE . '
					SET ' . $db->sql_build_array('UPDATE', $update_ary) . '
					WHERE game_id = ' . (int) $game['game_id'];
					$db->sql_query($sql);
					
					//Update the queue, moving the proper number of games into signups.
					bumpIntoSignups($game['game_type']);
					$loc = append_sid('viewqueue.'.$phpEx.'?q='. $game['game_type']);
					$message = $user->lang['APPROVAL_SUCCESS'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
					
				}
				else
				{
					$hiddenFields = build_hidden_fields(array(
					'game_id'	=> $game['game_id'],	
					));
					confirm_box(false, 'APPROVE_GAME', $hiddenFields, 'game_moderate_approve.html');
				}
				
			}
			else
			{
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
				$message = $user->lang['GAME_NOT_EXIST'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
			}
			break;
	//*********************************************
	//Handle game unapproval. */
		case 'disapprove':
			if(!$game_id){
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
				$message = $user->lang['NO_GAME_SPECIFIED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
			}
			$game = load_game(0, $game_id);
			if(sizeof($game)){
			
				//Make sure we have listmod permissions before approving...
				if(!$auth->acl_get('u_queue_'.$game['game_type']))
				{
					$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
					$message = $user->lang['NOT_AUTHORISED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				}
				//if the game is already approved, don't bother.
				if($game['approved_time'])
				{
					$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
					$message = $user->lang['ALREADY_APPROVED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				}
			
				if(confirm_box(true)){
					$data = array();
					$data['game_id'] = (int)request_var('game_id', 0);
					
					//remove entry from game table
					$sql = 'DELETE FROM ' . MAFIA_GAMES_TABLE . ' WHERE game_id=' . $data['game_id'];
					$db->sql_query($sql);
					
					//remove entry from mod table
					$sql = 'DELETE FROM ' . MAFIA_MODERATORS_TABLE . ' WHERE game_id=' . $data['game_id'];
					$db->sql_query($sql);
					
					
					$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
					$message = 'Game removed succesfully' . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, append_sid("viewqueue.$phpEx"));
					trigger_error($message);
				} else {
					$hiddenFields = build_hidden_fields(array ('game_id' => $game_id));
					confirm_box(false, 'Confirm game rejection.', $hiddenFields, 'game_moderate_approve.html');
				
				}
			} else {
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
				$message = $user->lang['GAME_NOT_EXIST'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
			}
			break;
	//Shoddy interface with my old View and Edit Game detail's code
		case 'editdetails':
			global $template;
			if(!$game_id)
			{
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
				$message = $user->lang['NO_GAME_SPECIFIED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
			}
			$submit	= isset($_POST['post']) ? true : false;
			if(alreadyEntered($game_id, $user->data['user_id'], 1) || $auth->acl_get('u_queue_'.$queue)){
				$template->assign_vars(array(
					'EDIT'	=> true,
				));
			} else {
				$loc = append_sid($phpbb_root_path . 'viewgame.' . $phpEx . '?g=' . $game_id);
				$message = "You don't have permission to edit this game" . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
			}
			if($auth->acl_get('u_queue_'.$queue)){
				$template->assign_vars(array(
					'LISTMOD'	=> true,
				));
			}
			if ($submit){
					$game = load_game($queue, $game_id);
					$gameAccepted = ($game['approved_time']) ? true : false ;
					$gameStarted = ($game['started_time']) ? true : false ;
					
					if (!$gameAccepted){
						$gameTypeEdited = (int)request_var('gameInfoGameType', 0);
						$gameSizeEdited = (int)request_var('gameInfoGameSize', 0);
						$gameNameEdited = $db->sql_escape(request_var('gameInfoGameName', ""));
					} elseif ( $auth->acl_get('u_queue_'.$queue)){
						$gameSizeEdited = (int)request_var('gameInfoGameSize', 0);
						$gameNameEdited = $db->sql_escape(request_var('gameInfoGameName', ""));
					}
					if (!$gameStarted){
						$statusEdited = (int)request_var('gameInfoGameStatus', 1);
					} elseif ( $auth->acl_get('u_queue_'.$queue)){
						$statusEdited = (int)request_var('gameInfoGameStatus', 1);
					}
					if ($statusEdited == 46 || $statusEdited == 5){
						if ($game['status'] != 46 && $game['status'] != 5){
							$completedEdited = time();
							if ($game['status'] == 3) {
								bumpIntoSignups($game['game_type']);
							}
						}
					}
					$gameDescriptionEdited = $db->sql_escape(request_var('message', ""));
					
					$error = "";

					if ($gameStarted && $completedEdited === False && !($preCompletedEdited === "")){
						$error = 'Error with entered Completed date format';
					}else if (!$gameAccepted && $gameSizeEdited <5){
						$error = 'Entered game size is too small or not a number';
					}else if (!$gameAccepted && (strlen($gameNameEdited) > 25 || strlen($gameNameEdited) < 5)){
						$error = 'Entered game name is too long or too short';				
					} else {
						$message = $gameDescriptionEdited;
						$allow_bbcode = $allow_smilies = $allow_urls = true;
						generate_text_for_storage($message, $uid, $bitfield, $options, $allow_bbcode, $allow_urls, $allow_smilies);
						
						$setString = '';
						if (!$gameAccepted){
							$setString .= $gameTypeEdited <= 7 && $gameTypeEdited > 0 ? ("game_type=" . $gameTypeEdited) : "";
							$setString .= $gameSizeEdited > 3 && !$gameAccepted ? ((empty($setString)? '' : ', ') ."maximum_players=" . $gameSizeEdited ): "";
							$setString .= empty($gameNameEdited) || $gameAccepted ? "" : ((empty($setString)? '' : ', ') ."name='" . $gameNameEdited . "'");
						}
						if (!$gameStarted){
							$setString .= $statusEdited >= 1 ? ((empty($setString)? '' : ', ') . "status=" . $statusEdited ): "";
							$setString .= empty($completedEdited) ? "" : ((empty($setString)? '' : ', ') ."completed_time=" . $completedEdited);
						}
						
						$setString .= empty($message) ? "" : ((empty($setString)? '' : ', ') ."description='" . $message . "'");
						$setString .= empty($message) ? "" : ", bbcode_bitfield='" . $bitfield . "'";
						$setString .= empty($message) ? "" : ", bbcode_uid='" . $uid . "'";
						if(!empty($setString)){
							$sql = " UPDATE " . MAFIA_GAMES_TABLE 
								. " Set " . $setString
								. " WHERE game_id=" . $game_id;
							$db->sql_query($sql);
						}
					}
				$template->assign_var('DETAIL_EDIT_ERROR',$error);
				if (empty($error)){
						$loc = append_sid($phpbb_root_path . 'viewgame.' . $phpEx . '?g=' . $game_id);
						$message = "Game edited succesfully" . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
						meta_refresh(3, $loc);
						trigger_error($message);
				} else {
					include($phpbb_root_path . 'includes/message_parser.' . $phpEx);
				$game = load_game($queue, $game_id);
				$message_parser = new parse_message();
				$message_parser->message = $game['description'];
				$message_parser->message = decode_bbcodes_non_preview($message_parser->message);
				$message_parser->bbcode_bitfield = $game['bbcode_bitfield'];
				$message_parser->decode_message($game['bbcode_uid']);
				
				// HTML, BBCode, Smilies, Images and Flash status
				$bbcode_status	= ($config['allow_bbcode']) ? true : false;
				$smilies_status	= ($config['allow_smilies']) ? true : false;
				$img_status		= ($bbcode_status) ? true : false;
				$url_status		= ($config['allow_post_links']) ? true : false;
				$flash_status	= ($bbcode_status && $config['allow_post_flash']) ? true : false;
				$quote_status	= true;
				display_custom_bbcodes();
				
				$templateFile = 'editgame.html';
				$template->assign_vars(array(
					'L_POST_A' => 'Edit Game',
					'MESSAGE' => $message_parser->message,
					'S_POST_ACTION' => $phpbb_root_path . 'viewgame.' . $phpEx . '?mode=editdetails&amp;g=' . $game_id,
					'S_GAME'		=> true,
					'S_BBCODE_IMG'			=> $img_status,
					'S_BBCODE_URL'			=> $url_status,
					'S_BBCODE_FLASH'		=> $flash_status,
					'S_BBCODE_QUOTE'		=> $quote_status,
					'S_BBCODE_ALLOWED'			=> ($bbcode_status) ? 1 : 0,
					'S_LINKS_ALLOWED'			=> $url_status,
					'L_FONT_COLOR'				=> 'Font colour',
					'L_FONT_COLOR_HIDE'			=> 'Hide font colour',
					'L_FONT_HUGE'					=> 'Huge',
					'L_FONT_LARGE'				=> 'Large',
					'L_FONT_NORMAL'				=> 'Normal',
					'L_FONT_SIZE'					=> 'Font size',
					'L_FONT_SMALL'				=> 'Small',
					'L_FONT_TINY'					=> 'Tiny',
					'L_BBCODE_A_HELP'				=> 'Inline uploaded attachment: [attachment=]filename.ext[/attachment]',
					'L_BBCODE_B_HELP'				=> 'Bold text: [b]text[/b]',
					'L_BBCODE_C_HELP'				=> 'Code display: [code]code[/code]',
					'L_BBCODE_E_HELP'				=> 'List: Add list element',
					'L_BBCODE_F_HELP'				=> 'Font size: [size=75]small text[/size]',
					'L_BBCODE_IS_OFF'				=> '%sBBCode%s is <em>OFF</em>',
					'L_BBCODE_IS_ON'				=> '%sBBCode%s is <em>ON</em>',
					'L_BBCODE_I_HELP'				=> 'Italic text: [i]text[/i]',
					'L_BBCODE_L_HELP'				=> 'List: [list]text[/list]',
					'L_BBCODE_LISTITEM_HELP'		=> 'List item: [*]text[/*]',
					'L_BBCODE_O_HELP'				=> 'Ordered list: [list=]text[/list]',
					'L_BBCODE_P_HELP'				=> 'Insert image: [img]http://image_url[/img]',
					'L_BBCODE_Q_HELP'				=> 'Quote text: [quote]text[/quote]',
					'L_BBCODE_S_HELP'				=> 'Font colour: [color=red]text[/color]  Tip: you can also use color=#FF0000',
					'L_BBCODE_U_HELP'				=> 'Underline text: [u]text[/u]',
					'L_BBCODE_W_HELP'				=> 'Insert URL: [url]http://url[/url] or [url=http://url]URL text[/url]',
					'L_BBCODE_D_HELP'				=> 'Flash: [flash=width,height]http://url[/flash]',
					'L_BBCODE_COUNTDOWN_HELP'		=> 'Countdown: [countdown]YYYY-mm-dd HH:MM:SS[/countdown]',
					'L_BBCODE_DICE_HELP'			=> 'Dice tag: [dice]1d6[/dice]',
					'L_BBCODE_POST_HELP'			=> 'Two varieties: [post=<ISO_POST_NUMBER>]Text[/post] OR [post=#<POST_ID>]Text[/post]',
								));
				page_header($game['name']);
				$template->set_filenames(array(
					'body' => $templateFile)
				);
				buildQueueBreadcrumbs($game_id, $queue);
				page_footer();
				}
			} else {
				include($phpbb_root_path . 'includes/message_parser.' . $phpEx);
				$game = load_game($queue, $game_id);
				$message_parser = new parse_message();
				$message_parser->message = $game['description'];
				$message_parser->message = decode_bbcodes_non_preview($message_parser->message);
				$message_parser->bbcode_bitfield = $game['bbcode_bitfield'];
				$message_parser->decode_message($game['bbcode_uid']);
				
				// HTML, BBCode, Smilies, Images and Flash status
				$bbcode_status	= ($config['allow_bbcode']) ? true : false;
				$smilies_status	= ($config['allow_smilies']) ? true : false;
				$img_status		= ($bbcode_status) ? true : false;
				$url_status		= ($config['allow_post_links']) ? true : false;
				$flash_status	= ($bbcode_status && $config['allow_post_flash']) ? true : false;
				$quote_status	= true;
				display_custom_bbcodes();
				
				$templateFile = 'editgame.html';
				$template->assign_vars(array(
					'L_POST_A' => 'Edit Game',
					'MESSAGE' => $message_parser->message,
					'S_POST_ACTION' => $phpbb_root_path . 'viewgame.' . $phpEx . '?mode=editdetails&amp;g=' . $game_id,
					'S_GAME'		=> true,
					'S_BBCODE_IMG'			=> $img_status,
					'S_BBCODE_URL'			=> $url_status,
					'S_BBCODE_FLASH'		=> $flash_status,
					'S_BBCODE_QUOTE'		=> $quote_status,
					'S_BBCODE_ALLOWED'			=> ($bbcode_status) ? 1 : 0,
					'S_LINKS_ALLOWED'			=> $url_status,
					'L_FONT_COLOR'				=> 'Font colour',
					'L_FONT_COLOR_HIDE'			=> 'Hide font colour',
					'L_FONT_HUGE'					=> 'Huge',
					'L_FONT_LARGE'				=> 'Large',
					'L_FONT_NORMAL'				=> 'Normal',
					'L_FONT_SIZE'					=> 'Font size',
					'L_FONT_SMALL'				=> 'Small',
					'L_FONT_TINY'					=> 'Tiny',
					'L_BBCODE_A_HELP'				=> 'Inline uploaded attachment: [attachment=]filename.ext[/attachment]',
					'L_BBCODE_B_HELP'				=> 'Bold text: [b]text[/b]',
					'L_BBCODE_C_HELP'				=> 'Code display: [code]code[/code]',
					'L_BBCODE_E_HELP'				=> 'List: Add list element',
					'L_BBCODE_F_HELP'				=> 'Font size: [size=75]small text[/size]',
					'L_BBCODE_IS_OFF'				=> '%sBBCode%s is <em>OFF</em>',
					'L_BBCODE_IS_ON'				=> '%sBBCode%s is <em>ON</em>',
					'L_BBCODE_I_HELP'				=> 'Italic text: [i]text[/i]',
					'L_BBCODE_L_HELP'				=> 'List: [list]text[/list]',
					'L_BBCODE_LISTITEM_HELP'		=> 'List item: [*]text[/*]',
					'L_BBCODE_O_HELP'				=> 'Ordered list: [list=]text[/list]',
					'L_BBCODE_P_HELP'				=> 'Insert image: [img]http://image_url[/img]',
					'L_BBCODE_Q_HELP'				=> 'Quote text: [quote]text[/quote]',
					'L_BBCODE_S_HELP'				=> 'Font colour: [color=red]text[/color]  Tip: you can also use color=#FF0000',
					'L_BBCODE_U_HELP'				=> 'Underline text: [u]text[/u]',
					'L_BBCODE_W_HELP'				=> 'Insert URL: [url]http://url[/url] or [url=http://url]URL text[/url]',
					'L_BBCODE_D_HELP'				=> 'Flash: [flash=width,height]http://url[/flash]',
					'L_BBCODE_COUNTDOWN_HELP'		=> 'Countdown: [countdown]YYYY-mm-dd HH:MM:SS[/countdown]',
					'L_BBCODE_DICE_HELP'			=> 'Dice tag: [dice]1d6[/dice]',
					'L_BBCODE_POST_HELP'			=> 'Two varieties: [post=<ISO_POST_NUMBER>]Text[/post] OR [post=#<POST_ID>]Text[/post]',
								));
				page_header($game['name']);
				$template->set_filenames(array(
					'body' => $templateFile)
				);
				buildQueueBreadcrumbs($game_id, $queue);
				page_footer();
				
			}
			break;
		default:
			if(!$game_id)
			{
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
				$message = $user->lang['NO_GAME_SPECIFIED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
			}
			$game = load_game($queue, $game_id);
			if (!sizeOf($game)){
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
				$message = "The specified game doesn't exist." . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
			}
			//Check if we are already a player.
			if(alreadyEntered($game_id, $user->data['user_id'], 2))
			{
				$template->assign_vars(array(
					'IS_ENTERED'	=> true,
				));
			}
			//Check if we are a mod.
			if(alreadyEntered($game_id, $user->data['user_id'], 1))
			{
				$template->assign_vars(array(
					'IS_ENTERED'	=> true,
					'IS_MODERATOR'  => true,
				));
			}
			//Check if we have edit permissions
			if(alreadyEntered($game_id, $user->data['user_id'], 1) || $auth->acl_get('u_queue_'.$queue)){
				$template->assign_vars(array(
					'EDIT'	=> true,
				));
				$can_edit = true;
			}
			if($auth->acl_get('u_queue_'.$queue)){
				$template->assign_vars(array(
					'LISTMOD'	=> true,
				));
			}
			
			//Check if we can approve games
			if($auth->acl_get('u_queue_'.$queue)){
				$template->assign_vars(array(
					'EDIT'	=> true,
					'CAN_APPROVE' => true,
				));
				$can_edit = true;
			}
			
			if ($can_edit){
				$submitMod = request_var('addModeratorSaveButton', false);
				$submitPlayer = request_var('addPlayerSaveButton', false);
				$submitDetails = request_var('saveGameInfoButton', false);
				$submitEditMod = request_var('editModeratorSaveButton', false);
				$submitEditPlayer = request_var('editPlayerSaveButton', false);
				$deleteMod = request_var('deletemod',false);
				$submitDeleteMod = request_var('confirmDeleteMod', false);
				$submitFaction = request_var('addFactionSubmit', false);
				if ($submitEditPlayer){

					$factionEdited = request_var('editPlayerFaction', 0);
					$roleEdited = request_var('editBasicRole', 0);
					$roleModifierEdited = request_var('editRoleModifier', 0);
					$roleFlavourNameEdited = request_var('editPlayerRoleFlavourName', 0);
					$playerID = request_var('playerID' , 0);

					$error = "";
					$sql = "SELECT slot_id FROM " . MAFIA_PLAYERS_TABLE . " WHERE user_id=" . $playerID . " AND game_id=" . $game_id;
					$result = $db->sql_query($sql);
					$data = $db->sql_fetchrow($result);
					$db->sql_freeresult($result);
					$slot_id = $data['slot_id'];
				
					if ($playerID === 0){
						$error = 'Changes are not associated with a player.';
					}else{
						user_get_id_name($playerID, $playerName, array(USER_NORMAL, USER_FOUNDER));
						if (empty($playerName)){
							$error = 'Entered ID is not of a valid user';
						}else{
								$changeFaction = $factionEdited > 0 ? (" faction_id=" . $factionEdited) : "" ;
								$changeRole = $roleEdited > 0 ? (", role_id=" . $roleEdited) : "" ;
								$changeRoleModifier = $roleModifierEdited > 0 ? ", modifier_id=" . $roleModifierEdited : "";
								$changeRoleFlavourName = empty($roleFlavourNameEdited) ? "" : (", role_flavour_name='" . $roleFlavourNameEdited . "'");
									if(!empty($changeFaction) || !empty($changeRole) || !empty($changeRoleModifier) || !empty ($changeRoleFlavoutName)){
										$sql = "UPDATE " . MAFIA_SLOTS_TABLE . "  SET " .
											$changeFaction .
											$changeRole .
											$changeRoleModifier .
											$changeRoleFlavourName .
											" WHERE game_id=" . $game_id .
											" AND slot_id=" . $slot_id;
										$db->sql_query($sql);
									}
						}
					}
					$template->assign_var('PLAYER_EDIT_ERROR',$error);
				} else if ($submitFaction){
					$factionName = $db->sql_escape(request_var('factionNameInput', ""));
					$factionAlignment = request_var('factionAlignmentInput', 0);
					if (strlen($factionName) > 32){
						$loc = append_sid($phpbb_root_path . 'viewgame.' . $phpEx . '?g=' . $game_id);
						$message = "Faction name is too long" . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
						meta_refresh(3, $loc);
						trigger_error($message);
					}
					$sql = "INSERT INTO " . MAFIA_FACTIONS_TABLE . " (game_id, name, alignment_id) VALUES ($game_id, '$factionName', $factionAlignment)";
					$db->sql_query($sql);
				} else if ($submitMod){
					$modName = request_var('addModeratorName', "");
					$type = (int)request_var('addModeratorType', 0);
					$error = "";
					if (empty($modName)){
						$error = 'No mod name entered.';
					}else {
						user_get_id_name($user_id, $modName, array(USER_NORMAL, USER_FOUNDER));
						if (empty($user_id)){
							$error = 'Entered name is not a user.';
						}else{
							if (alreadyEntered($game_id, $user_id[0])){
								$error = 'User is already a mod or player in this game.';
								$db->sql_freeresult($result);
							} else {
								$sql = "SELECT * FROM " . MAFIA_MODERATORS_TABLE . " WHERE game_id=$game_id";
								$result = $db->sql_query($sql);
								$mods = array();
								$modIDs = array();
								$count=0;
								while ($row = $db->sql_fetchrow($result)){
									$mods[$row['user_id']] = $row['type'];
									$modIDs[$count] = $row['user_id'];
									$count+=1;
								}
								foreach ($modIDs as $modID){
									if ($mods[$modID]==MODERATOR_TYPE_MAIN){
										$oldPrimary = $modID;
									}
								}
								if(empty($oldPrimary) && $type != MODERATOR_TYPE_MAIN){
									$error = "You must first add a primary mod";
								}
								else {
									if (!empty($oldPrimary) && $type == MODERATOR_TYPE_MAIN){
										$sql = " UPDATE " . MAFIA_MODERATORS_TABLE 
											. " Set type=" . MODERATOR_TYPE_COMOD
											. " WHERE user_id=" . $oldPrimary . ' AND game_id=' . $game_id;
										$db->sql_query($sql);
									}
									if ($type == MODERATOR_TYPE_MAIN){
										$sql = " UPDATE " . MAFIA_GAMES_TABLE 
												. " Set main_mod_id=" . $user_id[0]
												. " WHERE game_id=" . $game_id;
											$db->sql_query($sql);
									}
									$db->sql_freeresult($result);
									$sql = "INSERT INTO " . MAFIA_MODERATORS_TABLE . "(user_id, game_id, type) VALUES ($user_id[0] , $game_id, $type)" ;
									$db->sql_query($sql);
								}
							}
						}
					}
					$template->assign_var('MOD_ADD_ERROR',$error);
				} else if ($submitEditMod){
					$type = (int)request_var('editModeratorType' ,0);
					$moderatorID = (int)request_var('moderatorID' , 0);
					$error = "";
					if ($moderatorID == 0){
						$error = 'Not a valid Mod';
					} else {
						$sql = "SELECT * FROM " . MAFIA_MODERATORS_TABLE . " WHERE user_id=$moderatorID AND game_id=$game_id LIMIT 1";
						$result = $db->sql_query($sql);
						$data = $db->sql_fetchrow($result);
						if (empty($data)){
							$error = 'Not a valid Mod';
						} else {
							$sql = "SELECT * FROM " . MAFIA_MODERATORS_TABLE . " WHERE game_id=$game_id";
							$result = $db->sql_query($sql);
							$mods = array();
							$modIDs = array();
							$count=0;
							while ($row = $db->sql_fetchrow($result)){
								$mods[$row['user_id']] = $row['type'];
								$modIDs[$count] = $row['user_id'];
								$count+=1;
							}
							if($mods[$moderatorID]==MODERATOR_TYPE_MAIN){
								$error = 'You must have at least 1 primary mod.';
							}else{
								foreach ($modIDs as $modID){
									if ($mods[$modID]==MODERATOR_TYPE_MAIN){
										$oldPrimary = $modID;
									}
								}
								if(empty($oldPrimary) && $type != MODERATOR_TYPE_MAIN){
									$error = 'Add a primary mod';
								} else {
									//$error = 'test' . $oldPrimary;
									//print(!empty($oldPrimary));
									if (!empty($oldPrimary) && $type == MODERATOR_TYPE_MAIN){
										$sql = " UPDATE " . MAFIA_MODERATORS_TABLE 
											. " Set type=" . MODERATOR_TYPE_COMOD
											. " WHERE user_id=" . $oldPrimary . ' AND game_id=' . $game_id;
										$db->sql_query($sql);
									}
									if ($type == MODERATOR_TYPE_MAIN){
										$sql = " UPDATE " . MAFIA_GAMES_TABLE 
												. " Set main_mod_id=" . $moderatorID
												. " WHERE game_id=" . $game_id;
											$db->sql_query($sql);
									}
									$sql = " UPDATE " . MAFIA_MODERATORS_TABLE 
											. " Set type=" . $type
											. " WHERE user_id=" . $moderatorID . ' AND game_id=' . $game_id;
									$db->sql_query($sql);
								}
							}
						}
					}
					$template->assign_var('MOD_ADD_ERROR',$error);
				} else if ($deleteMod){
					$moderatorID = request_var('mod_id',0);
					$error = "";
					if ($moderatorID ==0){
						$error = 'Not a valid Mod';
					}else {
						$sql = 'SELECT *
							FROM ' . MAFIA_MODERATORS_TABLE . '
							WHERE user_id=' . $moderatorID . " AND game_id=" . $game_id;
						$result = $db->sql_query($sql);
						$modData = $db->sql_fetchrow($result); 
						$db->sql_freeresult($result);
						if (empty($modData)){
							$error = "That user is not a mod";
						}else if($modData['type'] == MODERATOR_TYPE_MAIN){
							$error = "You may not delete the primary mod.";
						}else{
							$sql = 'SELECT username, user_colour
								FROM ' . USERS_TABLE . '
								WHERE user_id=' . $moderatorID;
							$result = $db->sql_query($sql);
							$username = $db->sql_fetchrow($result); 
							$db->sql_freeresult($result);
							$template->assign_var('DELETE_MOD_USER_NAME', get_username_string('full', $moderatorID, $username['username'], $username['user_colour']));
						}
					}
					$template->assign_vars(array(
						'DELETE_MOD'			=> empty($error) ? $moderatorID  : False,
						'MOD_ADD_ERROR'			=> $error
					));
					
				} else if ($submitDeleteMod){
					$moderatorID = request_var('mod_id',0);
					$error = "";
					if ($moderatorID ==0){
						$error = 'Not a valid Mod';
					}else {
						$sql = 'SELECT *
							FROM ' . MAFIA_MODERATORS_TABLE . '
							WHERE user_id=' . $moderatorID . " AND game_id=" . $game_id;
						$result = $db->sql_query($sql);
						$modData = $db->sql_fetchrow($result); 
						$db->sql_freeresult($result);
						if (empty($modData)){
							$error = "That user is not a mod";
						} else if($modData['type'] == MODERATOR_TYPE_MAIN){
							$error = "You may not delete the primary mod.";
						}else{
							$sql = " DELETE FROM " . MAFIA_MODERATORS_TABLE . " WHERE user_id=$moderatorID AND game_id=$game_id";
							$db->sql_query($sql);
						}
					}
				}
			}
			//Grab all the game info.
			$game = load_game($queue, $game_id);
			//Grab all the player info.
			grabPlayerInfo($game_id,false,!$can_edit);

			//Grab all the mod info.
			grabModInfo($game_id);
			page_header($game['name']);
			$template->assign_vars(array(
				'S_IN_ACTION'	=>  append_sid($phpbb_root_path . 'viewgame.'.$phpEx.'?mode=enter&amp;type=in&amp;g='.$game_id),
				'EDIT_DETAILS_ACTION'	=> append_sid($phpbb_root_path . 'viewgame.'.$phpEx.'?mode=editdetails&amp;g='.$game_id),
				'S_PREIN_ACTION'	=>  append_sid($phpbb_root_path .'viewgame.'.$phpEx.'?mode=enter&amp;type=prein&amp;g='.$game_id),
				'S_OUT_ACTION'	=>  append_sid($phpbb_root_path .'viewgame.'.$phpEx.'?mode=out&amp;g='.$game_id),
				'S_REPLACE_ACTION'	=>  append_sid($phpbb_root_path .'viewgame.'.$phpEx.'?mode=enter&amp;type=replace&amp;g='.$game_id),
				'S_APPROVE_ACTION'		=> append_sid($phpbb_root_path .'viewgame.' . $phpEx. '?mode=approve&amp;g=' . $game_id),
				'S_DISAPPROVE_ACTION'		=> append_sid($phpbb_root_path .'viewgame.' . $phpEx. '?mode=disapprove&amp;g=' . $game_id),
				'U_FIND_USERNAME'		=> append_sid("{$phpbb_root_path}memberlist.$phpEx", 'mode=searchuser&amp;form=ModeratorEditor&amp;field=addModeratorName&amp;select_single=true'),
			));
				$templateFile = 'viewgame.html';
				// Output page
				$template->set_filenames(array(
					'body' => $templateFile)
				);
				buildQueueBreadcrumbs($game_id, $queue);
				page_footer();
		break;
	}
?>
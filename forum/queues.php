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
//include($phpbb_root_path . 'includes/functions_posting.' . $phpEx);
//include($phpbb_root_path . 'includes/functions_user.' . $phpEx);
include($phpbb_root_path . 'includes/functions_games.' . $phpEx);


// Start session
$user->session_begin();
$auth->acl($user->data);
$user->setup('games');

//Display variables.
$queue = request_var('q', 0);
$gameID = request_var('g', 0);
$mode = request_var('mode', '');
$type = request_var('type', '');
$d_approval = request_var('appr', 1);
$d_status = request_var('sta', 0);

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
switch($mode)
{
//*********************************************
// Display views.
	case 'view':
		//Single game view.
		if($queue)
		{
			$moderate = $auth->acl_get('u_queue_'.$queue);
			//Check if the listmod has specifically chosen to only view approved games.
			if($moderate)
			{
				$d_approval = (isset($_REQUEST['appr'])) ? $d_approval : 0;
			}
			$game = load_game($queue, 0, 0, $d_approval, $d_status, $start, $limit, 'games', $moderate);

			generateQueueList('queues', $queue);
			$template->assign_vars(array(
				'QUEUE_NAME'	=> $game[0]['type_name'],
				'QUEUE_ID'		=> $queue,
				'MODERATION' => $moderate, //Show mod actions or not.
			));
			page_header(sprintf($user->lang['SINGLE_QUEUE'], $game[0]['type_name'], ''));
			$template->assign_vars(array(
				'S_STATUS_OPTIONS' 	=> createStatusOptions($d_status),
				'S_SORT_ACTION'		=> 'queues.'.$phpEx.'?mode=view&amp;q='.$queue,
				'APPROVAL_STATUS'	=> $d_approval,
			));
			$templateFile = 'game_queue_list.html';
		}
		///////
		//TODO - Add a view where you can check only the games currently available to moderate.
		///////
		else
		{
			load_game($queue, 0, 0, $d_approval, $d_status, $start, $limit, 'recent_games', false);
			generateQueueList('queues');
			page_header($user->lang['QUEUES']);
			$template->assign_vars(array(
				'S_STATUS_OPTIONS' => createStatusOptions($d_status),
				'S_SORT_ACTION'		=> 'queues.'.$phpEx,
				'APPROVAL_STATUS'	=> $d_approval,
			));
			$templateFile = 'game_queue.html';
		}
		break;


//*********************************************
//Handle player signups.
	case 'enter':
		if(!$gameID)
		{
			$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx);
			$message = $user->lang['NO_GAME_SPECIFIED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
			meta_refresh(3, $loc);
			trigger_error($message);
			
		}
		$game = load_game(0, $gameID);
		
		//check if the player has not reached their limit yet.
		if(overPlayerLimits($game['game_type']))
		{
			$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx.'?mode=view&q='.$game['game_id']);
			$message = $user->lang['QUEUE_PLAYER_LIMIT_REACHED'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
			meta_refresh(3, $loc);
			trigger_error($message);
		}
		
		//Make sure we aren't a mod or already entered.
		if(alreadyEntered($game['game_id'], $user->data['user_id']))
		{
			$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx.'?mode=view&g='.$game['game_id']);
			$message = $user->lang['ALREADY_PART_GAME'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
			meta_refresh(3, $loc);
			trigger_error($message);
		}
		
		
		switch($type)
		{
			case 'in':
				//Check to make sure the game is approved for regular signups AND isn't full already.
				if(!$game['approved_time'])
				{
					$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx.'?mode=view&g='.$game['game_id']);
					$message = $user->lang['GAME_NOT_APPROVED'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				}
				if($game['entered_players'] >= $game['maximum_players'])
				{
					$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx.'?mode=view&g='.$game['game_id']);
					$message = $user->lang['GAME_FULL'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				}
				
				//If we get here we are all set to accept the /in.
				insertPlayer($game['game_id'], $user->data['user_id']);
				$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx.'?mode=view&g='.$game['game_id']);
				$message = $user->lang['IN_SUCCESSFUL'] . '<br /><br /><a href="' . $loc . '">'.$user->lang['RETURN_GAME_VIEW'].'</a>';
				meta_refresh(3, $loc);
				trigger_error($message);
				break;
				
			case 'prein':
				//Check to make sure the game isn't already approved for regular signups.
				if($game['approved_time'])
				{
					$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx.'?mode=view&g='.$game['game_id']);
					$message = $user->lang['GAME_ALREADY_APPROVED'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				}
				//Check to see if prein slots are full.
				if($game['requested_players'] >= (floor($game['maximum_players'] * ($game['percent'] / 100))))
				{
					$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx.'?mode=view&g='.$game['game_id']);
					$message = $user->lang['GAME_PREIN_FULL'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				}
				
				//If we get here we are all set to accept the /prein.
				insertPlayer($game['game_id'], $user->data['user_id'], 1);
				$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx.'?mode=view&g='.$game['game_id']);
					$message = $user->lang['GAME_PREIN_SUCCESSFUL'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				break;
			case 'replace':
				if(!$game['approved_time'])
				{
					$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx.'?mode=view&g='.$game['game_id']);
					$message = $user->lang['GAME_NOT_APPROVED'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				}
				if(confirm_box(true))
				{
					$replaceStart = developReplacementDate();
					//If we get here we are all set to accept the replacement
					insertPlayer($game['game_id'], $user->data['user_id'], 2, $replaceStart);
					$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx.'?mode=view&g='.$game['game_id']);
					$message = $user->lang['REPLACEMENT_SUCCESSFUL'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				}
	
				setReplaceDateTemplate();
				confirm_box(false, 'REPLACEMENT_REQUEST', '', 'game_replacement_request.html');
				break;
			default:
				$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx);
				$message = $user->lang['NO_INTYPE_SPECIFIED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
				break;
		}
		break;
//*********************************************
//Handle player removals.
	case 'out':
		if(!$gameID)
		{
			$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx);
			$message = $user->lang['NO_GAME_SPECIFIED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
			meta_refresh(3, $loc);
			trigger_error($message);
			
		}
		removeSignup($gameID, $user->data['user_id']);
		$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx);
		$message = $user->lang['OUT_SUCCESSFUL'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
		meta_refresh(3, $loc);
		trigger_error($message);
		break;
//*********************************************
//Handle game submission.
	case 'submit':
		$submit = request_var('submit_queue', false);
		if(confirm_box(true))
		{
			//Check and add the game to queue.
			$data = array();
			$data['main_mod'] = request_var('main_moderator', '');
			$data['game_name'] = request_var('game_name', '');
			$data['game_type'] = request_var('game_type', 0);
			$data['requested_slots'] = request_var('requested_slots', 0);
			$data['game_description'] = request_var('game_description', '');
			
			//Double check in case they editted the variables manually. 
			$errors = errorsInGameData($data);
			if(sizeof($errors))
			{
				trigger_error('CANT_EDIT_CONFIRMATION');
			}
			$newID = createGame($data['game_name'], $data['game_type'], checkModerator($data['main_mod'], true), $data['requested_slots'], $data['game_description']);
			
		
			meta_refresh(3, "queues.$phpEx?mode=view&amp;g=$newID");
			trigger_error($user->lang['GAME_SUBMISSION_SUCCESS']);
		}
		elseif($submit)
		{
			$data = array();
			$data['main_mod'] = request_var('main_moderator', '');
			$data['game_name'] = request_var('game_name', '');
			$data['game_type'] = request_var('game_type', 0);
			$data['requested_slots'] = request_var('requested_slots', 0);
			$data['game_description'] = request_var('game_description', '');
			
			$errors = errorsInGameData($data);
			if(sizeof($errors))
			{
				$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx);
				$message = $user->lang['ERROR_GAME_SUBMISSION'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
			}
			
			$hiddenFields = build_hidden_fields(array(
				'main_moderator'	=> $data['main_mod'],
				'game_name'			=> $data['game_name'],
				'game_type'			=> $data['game_type'],
				'requested_slots'	=> $data['requested_slots'],
				'game_description'	=> $data['game_description'],
				
				
			));
			
			$template->assign_vars(array(	
					'MAIN_MOD'	=> $data['main_mod'],
					'GAME_NAME'			=> $data['game_name'],
					'GAME_TYPE'			=> $data['type_name'],
					'REQUESTED_SLOTS'	=> $data['requested_slots'],
					'GAME_DESCRIPTION'	=> $data['game_description'],
			));
		
			confirm_box(false, 'APPROVE_SUBMISSION', $hiddenFields, 'game_submission_approve.html');
		}
		
		$template->assign_vars(array(
		'U_FIND_USERNAME'	=> append_sid("{$phpbb_root_path}memberlist.$phpEx", 'mode=searchuser&amp;form=ucp&amp;field=main_moderator&amp;select_single=true'),
		'U_USERNAME'		=> $user->data['username'],
		'GAME_TYPE_SELECT'	=> createGameTypeSelect($queue),
		'CAN_MODERATE'		=> checkModLimits($user->data['user_id'], 0),
		));
		
		page_header($user->lang['SUBMIT_GAME']);
		$templateFile = 'game_request.html';
		break;
//*********************************************
//Handle game approval.
	case 'approve':
		//Make sure we have a game id.
		if(!$gameID)
		{
			$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx);
			$message = $user->lang['NO_GAME_SPECIFIED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
			meta_refresh(3, $loc);
			trigger_error($message);
		}
		
		//Check if game exists and load data if it does.
		$game = load_game(0, $gameID);
		
		if(sizeof($game))
		{
			
			//Make sure we have listmod permissions before approving...
			if(!$auth->acl_get('u_queue_'.$game['game_type']))
			{
				$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx);
				$message = $user->lang['NOT_AUTHORISED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
			}
			//if the game is already approved, don't bother.
			if($game['approved_time'])
			{
				$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx);
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
				
				
				
				//Create the game topic for the moderator.
				//TODO - Review permission scheme.
				$que = 'SELECT * FROM '.MAFIA_GAME_TYPES_TABLE.' WHERE
				type_id = '.$game['game_type'];
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
				
				
				//Finish by updating the game table...
				$update_ary = array(
					'approved_by_user_id'	=> $user->data['user_id'],
					'approved_time'			=> time(),
					'numbering'				=> ($nu['numbering'] + 1),
					'topic_id'				=> $data['topic_id'],
					'status'				=> GAME_STATUS_QUEUED,
				);
				
				$sql = 'UPDATE ' . MAFIA_GAMES_TABLE . '
				SET ' . $db->sql_build_array('UPDATE', $update_ary) . '
				WHERE game_id = ' . (int) $game['game_id'];
				$db->sql_query($sql);
				
				//Update the queue, moving the proper number of games into signups.
				bumpIntoSignups($game['game_type']);
				
				//meta_refresh(3, 'queues.'.$phpEx.'mode=view); //TODO - Figure out where to redirect to.
				$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx);
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
			$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx);
			$message = $user->lang['GAME_NOT_EXIST'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
			meta_refresh(3, $loc);
			trigger_error($message);
		}
		break;
//*********************************************
//Handle game editting.
	case 'edit':
		$submitMod = request_var('addModeratorSaveButton', false);
		$submitPlayer = request_var('addPlayerSaveButton', false);
		$submitDetails = request_var('saveGameInfoButton', false);
		$submitEditMod = request_var('editModeratorSaveButton', false);
		$submitEditPlayer = request_var('editPlayerSaveButton', false);
		$deleteMod = request_var('deletemod',false);
		$submitDeleteMod = request_var('confirmDeleteMod', false);
		$submitFaction = request_var('addFactionSubmit', false);
		//Make sure we have a game id.
		if(!$gameID)
		{
			$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx);
			$message = $user->lang['NO_GAME_SPECIFIED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
			meta_refresh(3, $loc);
			trigger_error($message);
		}
		//Check if we are a mod. Otherwise cancel everything.
		if((alreadyEntered($gameID, $user->data['user_id'], 1)) || $auth->acl_get('u_queue_'.$game['game_type']))
		{
			$template->assign_vars(array(
				'IS_ENTERED'	=> true,
				'IS_MODERATOR'  => true,
			));
			
		}
		else
		{
			$loc = append_sid($phpbb_root_path . 'queues.' . $phpEx);
			$message = $user->lang['NOT_AUTHORISED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
			meta_refresh(3, $loc);
			trigger_error($message);
		}
		$game = load_game(0, $gameID);
		page_header($game['name']);
		if ($submitDetails){
			$preStartedEdited = $db->sql_escape(request_var('gameInfoGameStarted', ""));
			$preCompletedEdited = $db->sql_escape(request_var('gameInfoGameCompleted', ""));
			$gameTypeEdited = (int)request_var('gameInfoGameType', 0);
			$statusEdited = (int)request_var('gameInfoGameStatus', 0);
			$gameSizeEdited = (int)request_var('gameInfoGameSize', 0);
			$gameNameEdited = $db->sql_escape(request_var('gameInfoGameName', ""));
			$gameDescriptionEdited = $db->sql_escape(request_var('gameInfoGameDescription', ""));
			$error = "";
			$startedEdited = strtotime($preStartedEdited);
			$completedEdited = strtotime($preCompletedEdited);

			if ($startedEdited === False && !($preStartedEdited === "")){
				$error = 'Error with entered Started date format';
			}else if ($completedEdited === False && !($preCompletedEdited === "")){
				$error = 'Error with entered Completed date format';
			}else if ($gameSizeEdited <5){
			    $error = 'Entered game size is too small or not a number';
			}else if (strlen($gameNameEdited) > 25){
				$error = 'Entered game name is too long';				
			} else {
				$setString = '';
				$setString .= $gameTypeEdited <= 7 && $gameTypeEdited > 0 ? ("game_type=" . $gameTypeEdited) : "";
				$setString .= $statusEdited >= 0 ? ((empty($setString)? '' : ', ') . "status=" . $statusEdited ): "";
				$setString .= empty($startedEdited) ? "" : ((empty($setString)? '' : ', ') ."started_time=" . $startedEdited);
				$setString .= empty($completedEdited) ? "" : ((empty($setString)? '' : ', ') ."completed_time=" . $completedEdited);
				$setString .= $gameSizeEdited > 4 && !$gameAccepted ? ((empty($setString)? '' : ', ') ."maximum_players=" . $gameSizeEdited ): "";
				$setString .= empty($gameNameEdited) || $gameAccepted ? "" : ((empty($setString)? '' : ', ') ."name='" . $db->sql_escape($gameNameEdited) . "'");
				$setString .= empty($gameDescriptionEdited) || $gameAccepted ? "" : ((empty($setString)? '' : ', ') ."description='" . $db->sql_escape($gameDescriptionEdited) . "'");
				if(!empty($setString)){
					//todo: verify that these are of the right form.
					$sql = " UPDATE " . MAFIA_GAMES_TABLE 
						. " Set " . $setString
						. " WHERE game_id=" . $gameID;
					$db->sql_query($sql);
				}
			}
			$template->assign_var('DETAIL_EDIT_ERROR',$error);
		}else if ($submitEditPlayer){

			$factionEdited = request_var('editPlayerFaction', 0);
			$roleEdited = request_var('editBasicRole', 0);
			$roleModifierEdited = request_var('editRoleModifier', 0);
			$roleFlavourNameEdited = request_var('editPlayerRoleFlavourName', 0);
			$playerID = request_var('playerID' , 0);

			$error = "";
			$sql = "SELECT slot_id FROM " . MAFIA_PLAYERS_TABLE . " WHERE user_id=" . $playerID . " AND game_id=" . $gameID;
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
									" WHERE game_id=" . $gameID .
									" AND slot_id=" . $slot_id;
								$db->sql_query($sql);
							}
				}
			}
			$template->assign_var('PLAYER_EDIT_ERROR',$error);
		} else if ($submitFaction){
			$factionName = $db->sql_escape(request_var('factionNameInput', ""));
			$factionAlignment = request_var('factionAlignmentInput', 0);
			
			$sql = "INSERT INTO " . MAFIA_FACTIONS_TABLE . " (game_id, faction_name, alignment_id) VALUES ($gameID, '$factionName', $factionAlignment)";
			$db->sql_query($sql);
		} else if ($submitMod){
			$modName = request_var('addModeratorName', "");
			$type = request_var('addModeratorType', 0);
			$error = "";
			if (empty($modName)){
				$error = 'No mod name entered.';
			}else {
				user_get_id_name($user_id, $modName, array(USER_NORMAL, USER_FOUNDER));
				if (empty($user_id)){
					$error = 'Entered name is not a user.';
				}else{
					if (alreadyEntered($gameID, $user_id[0])){
						$error = 'User is already a mod or player in this game.';
						$db->sql_freeresult($result);
					} else {
						$sql = "SELECT * FROM " . MAFIA_MODERATORS_TABLE . " WHERE game_id=$gameID";
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
							if ($mods[$modID]==MAFIA_MODATOR_TYPE_PRIMARY){
								$oldPrimary = $modID;
							}
						}
						if(empty($oldPrimary) && $type != MAFIA_MODATOR_TYPE_PRIMARY){
							$error = "You must first add a primary mod";
						}
						else {
							if (!empty($oldPrimary) && $type == MAFIA_MODATOR_TYPE_PRIMARY){
								$sql = " UPDATE " . MAFIA_MODERATORS_TABLE 
									. " Set type=" . MAFIA_MODATOR_TYPE_COMOD
									. " WHERE user_id=" . $oldPrimary . ' AND game_id=' . $gameID;
								$db->sql_query($sql);
							}
							if ($type == MAFIA_MODATOR_TYPE_PRIMARY){
								$sql = " UPDATE " . MAFIA_GAMES_TABLE 
										. " Set main_mod_id=" . $user_id[0]
										. " WHERE game_id=" . $gameID;
									$db->sql_query($sql);
							}
							$db->sql_freeresult($result);
							$sql = "INSERT INTO " . MAFIA_MODERATORS_TABLE . "(user_id, game_id, type) VALUES ($user_id[0] , $gameID, $type)" ;
							$db->sql_query($sql);
						}
					}
				}
			}
			$template->assign_var('MOD_ADD_ERROR',$error);
		} else if ($submitEditMod){
			$type = request_var('editModeratorType' ,0);
			$moderatorID = request_var('moderatorID' , 0);
			$error = "";
			if ($moderatorID == 0){
				$error = 'Not a valid Mod';
			} else {
				$sql = "SELECT * FROM " . MAFIA_MODERATORS_TABLE . " WHERE user_id=$moderatorID AND game_id=$gameID LIMIT 1";
				$result = $db->sql_query($sql);
				$data = $db->sql_fetchrow($result);
				if (empty($data)){
					$error = 'Not a valid Mod';
				} else {
					$sql = "SELECT * FROM " . MAFIA_MODERATORS_TABLE . " WHERE game_id=$gameID";
					$result = $db->sql_query($sql);
					$mods = array();
					$modIDs = array();
					$count=0;
					while ($row = $db->sql_fetchrow($result)){
						$mods[$row['user_id']] = $row['type'];
						$modIDs[$count] = $row['user_id'];
						$count+=1;
					}
					if($mods[$moderatorID]==MAFIA_MODATOR_TYPE_PRIMARY){
						$error = 'You must have at least 1 primary mod.';
					}else{
						foreach ($modIDs as $modID){
							if ($mods[$modID]==MAFIA_MODATOR_TYPE_PRIMARY){
								$oldPrimary = $modID;
							}
						}
						if(empty($oldPrimary) && $type != MAFIA_MODATOR_TYPE_PRIMARY){
							$error = 'Add a primary mod';
						} else {
							if (!empty($oldPrimary) && $type == MAFIA_MODATOR_TYPE_PRIMARY){
								$sql = " UPDATE " . MAFIA_MODERATORS_TABLE 
									. " Set type=" . MAFIA_MODATOR_TYPE_COMOD
									. " WHERE user_id=" . $oldPrimary . ' AND game_id=' . $gameID;
								$db->sql_query($sql);
							}
							if ($type == MAFIA_MODATOR_TYPE_PRIMARY){
								$sql = " UPDATE " . MAFIA_GAMES_TABLE 
										. " Set main_mod_id=" . $moderatorID
										. " WHERE game_id=" . $gameID;
									$db->sql_query($sql);
							}
							$sql = " UPDATE " . MAFIA_MODERATORS_TABLE 
									. " Set type=" . $type
									. " WHERE user_id=" . $moderatorID . ' AND game_id=' . $gameID;
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
					WHERE user_id=' . $moderatorID . " AND game_id=" . $gameID;
				$result = $db->sql_query($sql);
				$modData = $db->sql_fetchrow($result); 
				$db->sql_freeresult($result);
				if (empty($modData)){
					$error = "That user is not a mod";
				}else if($modData['type'] == MAFIA_MODATOR_TYPE_PRIMARY){
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
					WHERE user_id=' . $moderatorID . " AND game_id=" . $gameID;
				$result = $db->sql_query($sql);
				$modData = $db->sql_fetchrow($result); 
				$db->sql_freeresult($result);
				if (empty($modData)){
					$error = "That user is not a mod";
				} else if($modData['type'] == MAFIA_MODATOR_TYPE_PRIMARY){
					$error = "You may not delete the primary mod.";
				}else{
					$sql = " DELETE FROM " . MAFIA_MODERATORS_TABLE . " WHERE user_id=$moderatorID AND game_id=$gameID";
					$db->sql_query($sql);
				}
			}
		}

		$sql = " SELECT games.*"
			 . " FROM " . MAFIA_GAMES_TABLE . " games"
			 . " WHERE game_id=" . $gameID;

		$result = $db->sql_query($sql);
		$game_data = $db->sql_fetchrow($result);
		$db->sql_freeresult($result);
		$gameAccepted = !($game_data['approved_time']===null);

		if (!$game_data)
		{
			trigger_error('NO_FORUM'); //TODO: NO_GAME
		}
		$edit = True;
		//These are guaranteed not to be null.
		$gameName = $game_data['name'];
		$gameID = $game_data['game_id'];
		$gamePlayers = $game_data['entered_players'];
		$gameSize = $game_data['maximum_players'];
		$gameTopic = $game_data['topic_id'];
		$createdTime = strftime("%Y-%m-%d", $game_data['created_time']);

		//These, however, might be null.
		$startedTime = ($game_data['started_time'] === NULL ? "" : strftime("%Y-%m-%d", $game_data['started_time']));
		$completedTime = ($game_data['completed_time'] === NULL ? "" : strftime("%Y-%m-%d", $game_data['completed_time']));
		

		$description = $game_data['description'];

		//build game type select element
		$gameType = $game_data['game_type'];
		$typeSelect = '<select id="gameInfoGameTypeInputField" name="gameInfoGameType" class="gameInfoLabel hidden" ';
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
		if ($gameStatusVal < 4){
			$statusSelect ='';
		}
		if ($gameStatusVal > 3 || empty($gameStatusVal)){
			$statusSelect = '<select id="gameInfoGameStatusInputField" name="gameInfoGameStatus" class="gameInfoLabel hidden" ';
			$statusSelect .='>';
		}
			$sql = " SELECT *"
				 . " FROM " . MAFIA_GAME_STATUS_TABLE;

			$result = $db->sql_query($sql);

			while($row = $db->sql_fetchrow($result)){
				if ($row['status_id'] >3 &&($gameStatusVal > 3 || empty($gameStatusVal))){
					$statusSelect .= '<option value="';
					$statusSelect .= $row['status_id'];
					$statusSelect .= '"';
				}
				if ($row['status_id'] >3 && ($row['status_id'] == $gameStatusVal)|| empty($gameStatusVal)){
					if ($gameStatusVal > 3 || empty($gameStatusVal)){
						$statusSelect .= ' selected ';
					}
					$gameStatus= $row['status_name'];
				}
				if ($row['status_id'] >3 && ($gameStatusVal > 3 || empty($gameStatusVal))){
					$statusSelect .= '>';
					$statusSelect .= $row['status_name'];
					$statusSelect .= '</option>';
				}
			}
			$db->sql_freeresult($result);
		if ($gameStatusVal > 3 || empty($gameStatusVal)){
			$statusSelect .= '</select>';
		}
		$template->assign_vars(array(
			'GAME_ID'				=> $gameID,
			'GAME_NAME'				=> $gameName,
			'GAME_NAME_ESCAPED'		=> addcslashes($gameName,"\'\"&\n\r<>"),
			'GAME_NAME_EDITED'		=> $gameNameEdited,
			'CREATION_TIME'			=> $createdTime,
			'STARTED_TIME'			=> $startedTime,
			'STARTED_TIME_EDITED'	=> !empty($startedEdited)? strftime("%Y-%m-%d",$startedEdited) : "",
			'COMPLETED_TIME'		=> $completedTime,
			'COMPLETED_TIME_EDITED'	=> !empty($completedEdited)? strftime("%Y-%m-%d",$completedEdited) : "",
			'GAME_TYPE'				=> $gameType,
			'STATUS'			=> $gameStatus,
			'STATUS_EDITED'	=> $statusEdited,
			'STATUS_VAL'		=> $gameStatusVal,
			'GAME_DESCRIPTION'			=> empty($description)? "" : $description,
			'GAME_DESCRIPTION_ESCAPED'	=> addcslashes(empty($description)? "" : $description,"\\\'\"&\n\r<>"),
			'GAME_DESCRIPTION_EDITED'	=> empty($description)? "" : $gameDescriptionEdited,
			'U_EDITFORM'			=> "{$phpbb_root_path}queues.$phpEx?g=$gameID&mode=edit",
			'ACCEPTED'				=> $gameAccepted,
			'TYPE_SELECT'			=> $typeSelect,
			'STATUS_SELECT'			=> $statusSelect,
			'MAXIMUM_PLAYER_TOTAL'	=> $gameSize,
			'GAME_SIZE_EDITED'		=> $gameSizeEdited,
			'GAME_TOPIC'			=> empty($gameTopic) ? "" : $phpbb_root_path . "viewtopic.$phpEx?f=" . $forum_id . "&t=" . $gameTopic,
			'ENTERED_PLAYER_TOTAL'	=> $gamePlayers,
			'EDIT'					=> $edit

		));
		
		//Grab all the player info.
		grabPlayerInfo($gameID);
		//Grab all the mod info.
		grabModInfo($gameID);
	
		$templateFile = 'viewgame.html';
		break;
//*********************************************
//Handle game unapproval. */
	case 'disapprove':
	default:
		load_game($queue, 0, 0, $d_approval, $d_status, $start, $limit, 'recent_games', false);
		generateQueueList('queues');
		page_header($user->lang['QUEUES']);
		$template->assign_vars(array(
				'S_STATUS_OPTIONS' 	=> createStatusOptions($d_status),
				'APPROVAL_STATUS'	=> $d_approval,
				'S_SORT_ACTION'		=> 'queues.'.$phpEx,
			));
		$templateFile = 'game_queue.html';
		break;
}


// Output page
$template->set_filenames(array(
	'body' => $templateFile)
);
buildQueueBreadcrumbs($gameID, $queue);
page_footer();
?>
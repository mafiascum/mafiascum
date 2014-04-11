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
$gameID = (int)request_var('g', 0);
$queue = request_var('q', 0);
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
			if(!$gameID)
			{
				$loc = append_sid($phpbb_root_path . 'viewgame.' . $phpEx);
				$message = $user->lang['NO_GAME_SPECIFIED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
				meta_refresh(3, $loc);
				trigger_error($message);
			}
			// if we have a gameID, rewrite the queue ID based on it
			$sql = "SELECT game_type FROM " .  MAFIA_GAMES_TABLE . " WHERE game_id = $gameID";
			$result = $db->sql_query_limit($sql, 1);
			$queue = $db->sql_fetchrow($result);
			$queue = $queue['game_type'];
			
			//Check if we are already a player.
			if(alreadyEntered($gameID, $user->data['user_id'], 2))
			{
				$template->assign_vars(array(
					'IS_ENTERED'	=> true,
				));
			}
			//Check if we are a mod.
			if(alreadyEntered($gameID, $user->data['user_id'], 1))
			{
				$template->assign_vars(array(
					'IS_ENTERED'	=> true,
					'IS_MODERATOR'  => true,
				));
			}
			//Check if we have edit permissions
			if(alreadyEntered($gameID, $user->data['user_id'], 1) || $auth->acl_get('u_queue_'.$queue)){
				$template->assign_vars(array(
					'EDIT'	=> true,
				));
				$edit = true;
			}			
			if ($edit){
				$submitMod = request_var('addModeratorSaveButton', false);
				$submitPlayer = request_var('addPlayerSaveButton', false);
				$submitDetails = request_var('saveGameInfoButton', false);
				$submitEditMod = request_var('editModeratorSaveButton', false);
				$submitEditPlayer = request_var('editPlayerSaveButton', false);
				$deleteMod = request_var('deletemod',false);
				$submitDeleteMod = request_var('confirmDeleteMod', false);
				$submitFaction = request_var('addFactionSubmit', false);
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
			}
			//Grab all the game info.
			$game = load_game($queue, $gameID);
			//Grab all the player info.
			grabPlayerInfo($gameID);
			//Grab all the mod info.
			grabModInfo($gameID);
			page_header($game['name']);
		$template->assign_vars(array(
			'S_IN_ACTION'	=>  append_sid('queues.'.$phpEx.'?mode=enter&amp;type=in&amp;g='.$gameID),
			'S_PREIN_ACTION'	=>  append_sid('queues.'.$phpEx.'?mode=enter&amp;type=prein&amp;g='.$gameID),
			'S_OUT_ACTION'	=>  append_sid('queues.'.$phpEx.'?mode=out&amp;g='.$gameID),
			'S_REPLACEIN_ACTION'	=>  append_sid('queues.'.$phpEx.'?mode=enter&amp;type=replace&amp;g='.$gameID),
			'U_FIND_USERNAME'		=> append_sid("{$phpbb_root_path}memberlist.$phpEx", 'mode=searchuser&amp;form=modAdd&amp;field=mod_name&amp;select_single=true'),
		));
			$templateFile = 'viewgame.html';
			// Output page
			$template->set_filenames(array(
				'body' => $templateFile)
			);
			buildQueueBreadcrumbs($gameID, $queue);
			page_footer();
?>
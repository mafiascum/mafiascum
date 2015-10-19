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
$queue = request_var('q', 0);
$gameID = request_var('g', 0);
$mode = request_var('mode', '');
$type = request_var('type', '');
$d_approval = request_var('appr', 1);
$d_status = request_var('sta', -1);

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
//Handle game submission.
	case 'submit':
		$submit = request_var('submit_queue', false);
		if(confirm_box(true))
		{
			//Check and add the game to queue.
			$data = array();
			$data['main_mod'] = request_var('main_moderator', '');
			$data['game_name'] = request_var('game_name', '');
			$data['game_type'] = (int)request_var('game_type', 0);
			$data['requested_slots'] = (int)request_var('requested_slots', 0);
			$data['game_description'] = request_var('game_description', '');
			
			//Double check in case they editted the variables manually. 
			$errors = errorsInGameData($data);
			if(sizeof($errors))
			{
				trigger_error('CANT_EDIT_CONFIRMATION');
			}
			$newID = createGame($data['game_name'], $data['game_type'], checkModerator($data['main_mod'], true), $data['requested_slots'], $data['game_description']);
			$loc = append_sid($phpbb_root_path . 'viewgame.' . $phpEx . '?g=' . $newID);
			$message = 'Game submitted successfully.' . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
			meta_refresh(3, $loc);
			trigger_error($message);
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
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
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
					'MAIN_MODERATOR'	=> $data['main_mod'],
					'GAME_NAME'			=> $data['game_name'],
					'GAME_TYPE'			=> getGameTypeName($data['game_type']),
					'REQUESTED_SLOTS'	=> $data['requested_slots'],
					'GAME_DESCRIPTION'	=> $data['game_description'],
			));
		
			confirm_box(false, 'APPROVE_SUBMISSION', $hiddenFields, 'game_moderate_approve.html');
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
// Display views.
		case 'replacement':
			$moderate = $auth->acl_get('u_queue_'.$queue);
			$sql = 'Select s.game_id FROM ' . MAFIA_SLOTS_TABLE . ' s LEFT JOIN ' . MAFIA_PLAYERS_TABLE . ' p ON p.slot_id=s.slot_id AND p.game_id = s.game_id AND p.type<>5 WHERE p.player_id IS NULL';
			$res = $db->sql_query($sql);
			$temp_game_ids = $db->sql_fetchrowset($res);
			$game_ids = array();
			foreach ($temp_game_ids as $game_id) {
				$game_ids[] = $game_id['game_id'];
			}
			$game = load_game(0, 0, 0, 0, 0, $start, $limit, 'recent_games', $moderate, $game_ids);
			if (is_array($game[0])){
				$queue_info = $game[0];
			} else{
				$queue_info = $game;
			}
			generateQueueList('queues', $queue);
			$template->assign_vars(array(
				'QUEUE_NAME'	=> $queue_info['type_name'],
				'QUEUE_ID'		=> $queue,
				'MODERATION' => $moderate, //Show mod actions or not.
				'REPLACEMENT'	=> true,
			));
			page_header(sprintf($user->lang['SINGLE_QUEUE'], $queue_info['type_name'], ''));
			$template->assign_vars(array(
				'S_STATUS_OPTIONS' 	=> createStatusOptions($d_status),
				'S_SORT_ACTION'		=> append_sid('viewqueue.'.$phpEx.'?mode=view&amp;q='.$queue),
				'APPROVAL_STATUS'	=> $d_approval,
			));
			$templateFile = 'game_queue.html';
			break;
		case 'users_games':
			$user_id = (int)request_var('u', 0);
			$sql = 'Select game_id FROM  ' . MAFIA_PLAYERS_TABLE . ' WHERE user_id =' . $user_id;
			$res = $db->sql_query($sql);
			$temp_game_ids = $db->sql_fetchrowset($res);
			$game_ids = array();
			foreach ($temp_game_ids as $game_id) {
				$game_ids[] = $game_id['game_id'];
			}
			load_game(0, 0, 0, 0, 0, $start, $limit, 'player_games',false, $game_ids);
			$sql = 'Select game_id FROM  ' . MAFIA_MODERATORS_TABLE . ' WHERE user_id =' . $user_id;
			$res = $db->sql_query($sql);
			$temp_game_ids = $db->sql_fetchrowset($res);
			$game_ids = array();
			foreach ($temp_game_ids as $game_id) {
				$game_ids[] = $game_id['game_id'];
			}
			load_game(0, 0, 0, 0, 0, $start, $limit, 'mod_games',false, $game_ids);
			page_header('Your Games');
			$templateFile = 'users_games.html';
			break;
		case 'view':
		default:
		//Single game view.
		if($queue)
		{
			if ($d_status === -1){
				$d_status = Array(GAME_PROGRESS_SIGNUPS, GAME_PROGRESS_QUEUED);
			}
			$moderate = $auth->acl_get('u_queue_'.$queue);
			//Check if the listmod has specifically chosen to only view approved games.
			if($moderate)
			{
				$d_approval = (isset($_REQUEST['appr'])) ? $d_approval : 0;
			}
			$game = load_game($queue, 0, 0, $d_approval, $d_status, $start, $limit, 'recent_games', $moderate, array());
			if (is_array($game[0])){
				$queue_info = $game[0];
			} else{
				$queue_info = $game;
			}
			generateQueueList('queues', $queue);
			$template->assign_vars(array(
				'QUEUE_NAME'	=> $queue_info['type_name'],
				'QUEUE_ID'		=> $queue,
				'MODERATION' => $moderate, //Show mod actions or not.
			));
			page_header(sprintf($user->lang['SINGLE_QUEUE'], $queue_info['type_name'], ''));
			$template->assign_vars(array(
				'S_STATUS_OPTIONS' 	=> createStatusOptions($d_status),
				'S_SORT_ACTION'		=> append_sid('viewqueue.'.$phpEx.'?mode=view&amp;q='.$queue),
				'APPROVAL_STATUS'	=> $d_approval,
			));
			$templateFile = 'game_queue.html';
		}
		///////
		//TODO - Add a view where you can check only the games currently available to moderate.
		///////
		else
		{
			if ($d_status === -1){
				$d_status = GAME_PROGRESS_SIGNUPS;
			}
			load_game(0, 0, 0, $d_approval, $d_status, $start, $limit, 'recent_games', false, array(), true);
			generateQueueList('queues');
			page_header($user->lang['QUEUES']);
			$template->assign_vars(array(
				'S_STATUS_OPTIONS' => createStatusOptions($d_status),
				'S_SORT_ACTION'		=> append_sid('viewqueue.'.$phpEx),
				'APPROVAL_STATUS'	=> $d_approval,
			));
			$templateFile = 'game_queue.html';
		}
		break;
}


// Output page
$template->set_filenames(array(
	'body' => $templateFile)
);
buildQueueBreadcrumbs($gameID, $queue);
page_footer();
?>
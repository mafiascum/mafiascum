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
				'S_SORT_ACTION'		=> 'viewqueue.'.$phpEx.'?mode=view&amp;q='.$queue,
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
				'S_SORT_ACTION'		=> 'viewqueue.'.$phpEx,
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
			$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
			$message = $user->lang['NO_GAME_SPECIFIED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
			meta_refresh(3, $loc);
			trigger_error($message);
			
		}
		$game = load_game(0, $gameID);
		
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
				//Check to make sure the game is approved for regular signups AND isn't full already.
				if(!$game['approved_time'])
				{
					$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx.'?mode=view&g='.$game['game_id']);
					$message = $user->lang['GAME_NOT_APPROVED'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				}
				if($game['entered_players'] >= $game['maximum_players'])
				{
					$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx.'?mode=view&g='.$game['game_id']);
					$message = $user->lang['GAME_FULL'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				}
				
				//If we get here we are all set to accept the /in.
				insertPlayer($game['game_id'], $user->data['user_id']);
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx.'?mode=view&g='.$game['game_id']);
				$message = $user->lang['IN_SUCCESSFUL'] . '<br /><br /><a href="' . $loc . '">'.$user->lang['RETURN_GAME_VIEW'].'</a>';
				meta_refresh(3, $loc);
				trigger_error($message);
				break;
				
			case 'prein':
				//Check to make sure the game isn't already approved for regular signups.
				if($game['approved_time'])
				{
					$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx.'?mode=view&g='.$game['game_id']);
					$message = $user->lang['GAME_ALREADY_APPROVED'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				}
				//Check to see if prein slots are full.
				if($game['requested_players'] >= (floor($game['maximum_players'] * ($game['percent'] / 100))))
				{
					$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx.'?mode=view&g='.$game['game_id']);
					$message = $user->lang['GAME_PREIN_FULL'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				}
				
				//If we get here we are all set to accept the /prein.
				insertPlayer($game['game_id'], $user->data['user_id'], 1);
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx.'?mode=view&g='.$game['game_id']);
					$message = $user->lang['GAME_PREIN_SUCCESSFUL'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				break;
			case 'replace':
				if(!$game['approved_time'])
				{
					$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx.'?mode=view&g='.$game['game_id']);
					$message = $user->lang['GAME_NOT_APPROVED'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				}
				if(confirm_box(true))
				{
					$replaceStart = developReplacementDate();
					//If we get here we are all set to accept the replacement
					insertPlayer($game['game_id'], $user->data['user_id'], 2, $replaceStart);
					$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx.'?mode=view&g='.$game['game_id']);
					$message = $user->lang['REPLACEMENT_SUCCESSFUL'] . '<br /><br />' . sprintf($user->lang['RETURN_GAME_VIEW'], '<a href="' . $loc . '">', '</a>');
					meta_refresh(3, $loc);
					trigger_error($message);
				}
	
				setReplaceDateTemplate();
				confirm_box(false, 'REPLACEMENT_REQUEST', '', 'game_replacement_request.html');
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
	case 'out':
		if(!$gameID)
		{
			$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
			$message = $user->lang['NO_GAME_SPECIFIED'] . '<br /><br />' . sprintf($user->lang['RETURN_MAIN_QUEUE'], '<a href="' . $loc . '">', '</a>');
			meta_refresh(3, $loc);
			trigger_error($message);
			
		}
		removeSignup($gameID, $user->data['user_id']);
		$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
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
			
		
			meta_refresh(3, "viewqueue.$phpEx?mode=view&amp;g=$newID");
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
			$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
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
				
				//meta_refresh(3, 'viewqueue.'.$phpEx.'mode=view); //TODO - Figure out where to redirect to.
				$loc = append_sid($phpbb_root_path . 'viewqueue.' . $phpEx);
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
	default:
		load_game($queue, 0, 0, $d_approval, $d_status, $start, $limit, 'recent_games', false);
		generateQueueList('queues');
		page_header($user->lang['QUEUES']);
		$template->assign_vars(array(
				'S_STATUS_OPTIONS' 	=> createStatusOptions($d_status),
				'APPROVAL_STATUS'	=> $d_approval,
				'S_SORT_ACTION'		=> 'viewqueue.'.$phpEx,
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
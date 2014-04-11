<?php
/**
*
* Mafia Games [English]
*
* @package language
* @version $Id$
* @copyright (c) 2012 Mafiascum.net
* @license http://opensource.org/licenses/gpl-license.php GNU Public License
*
*/

/**
* DO NOT CHANGE
*/
if (!defined('IN_PHPBB'))
{
	exit;
}

if (empty($lang) || !is_array($lang))
{
	$lang = array();
}

// DEVELOPERS PLEASE NOTE
//
// All language files should use UTF-8 as their encoding and the files must not contain a BOM.
//
// Placeholders can now contain order information, e.g. instead of
// 'Page %s of %s' you can (and should) write 'Page %1$s of %2$s', this allows
// translators to re-order the output of data while ensuring it remains correct
//
// You do not need this where single placeholders are used, e.g. 'Message %d' is fine
// equally where a string contains only two placeholders which are used to wrap text
// in a url you again do not need to specify an order e.g., 'Click %sHERE%s' is fine

$lang = array_merge($lang, array(
	//Authorization Errors
	'LOGIN_ERROR_QUEUE'			=> 'You must be logged in to view the queues.',
	'LOGIN_QUEUE'				=> '%sReturn to the login screen',
	
	//Not Specified
	'NO_GAME_SPECIFIED'			=> 'There was no game specified.',
	'NO_QUEUE_SPECIFIED'		=> 'There was no queue specified.',
	'NO_INTYPE_SPECIFIED'		=> 'There was no type of entry specified.',
	'NO_MODE_SPECIFIED'			=> 'There was no mode specified.',
	
	'RETURN_MAIN_QUEUE'			=> '%sReturn to the Queue list',
	'RETURN_SINGLE_QUEUE'		=> 'Return to %s Queue',
	'RETURN_GAME_VIEW'			=> 'Return to game page',
	
	'QUEUE_MOD_LIMIT_REACHED'	=> 'You have reached your modding limits for this queue.',
	'QUEUE_PLAYER_LIMIT_REACHED'=> 'You have reached your playing limits for this queue.',
	
	'ALREADY_PART_GAME'			=> 'You are already a part of this game.',
	'GAME_NOT_APPROVED'			=> 'The game has not been approved yet.',
	'GAME_ALREADY_APPROVED'		=> 'The game has already been approved.',
	'GAME_FULL'					=> 'The game is already full.',
	'IN_SUCCESSFUL'				=> 'You have successfully joined this game.',
	'PREIN_SUCCESSFUL'			=> 'You have successfully pre-inned for this game.',
	'REPLACEMENT_SUCCESSFUL'	=> 'You have successfully offered your replacement status.',
	'OUT_SUCCESSFUL'			=> 'You have successfully removed yourself from the game.',
	'SUBMIT_GAME'				=> 'Submit Game',
	'APPROVAL_SUCCESS'			=> 'This game has been successfully approved.',
	
	//Slot Approvals
	'SLOT_CREATED'				=> 'The slot for this game has been successfully created.',
	'SLOT_ALREADY_CREATED'		=> 'This player is already assigned a slot.',
	'PLAYER_DOESNT_EXIST'		=> 'This player doesn\'t exist.',
	'NO_SLOTS_SELECTED'			=> 'You did not choose any players to edit.',
	'PLAYER_EDITED'				=> 'You have successfully edited the players.',
	'PLAYER_REJECTED'			=> 'You have successfully rejected this player\s signup.',
	//Confirmation Boxes
	'REPLACEMENT_REQUEST'		=> 'Replacement Request',
	'REPLACEMENT_REQUEST_CONFIRM' => 'Set what day to begin availability for replacement:',
	'ALREADY_ASSOCIATED'		=> 'This user is already associated with this game.',
	'INVALID_TYPE'				=> 'That is not a valid mod type.',
	'MOD_ADDED'					=> 'You have successfully added this moderator.',
	'PLAYER_ADDED'				=> 'You have successfully added this player to the game.',
	'MOD_EDITED'				=> 'You have successfully edited this moderator.',
	'GAME_DETAILS_EDITED'		=> 'You have successfully edited the game details.',
	
	'QUEUES'					=> 'Queues',
	'SINGLE_QUEUE'				=> '%s Queue',
	
	'RECENTLY_APPROVED'			=> 'Recently Approved Games',
	'RUN_BY'					=> 'Moderated by',
	'GAME_APPROVAL'				=> 'Approval Status',
	'GAMES_IN_QUEUE'			=> 'Games In Queue',
	
	'PENDING'					=> 'Pending',
	'SIGNUPS'					=> 'Signups',
	'SETUP'						=> 'Setup',
	'ONGOING'					=> 'Ongoing',
	'COMPLETED'					=> 'Completed',
	'APPROVED'					=> 'Approved',
	'NOT_APPROVED'				=> 'Not Approved',
	'ALL_STATUSES'				=> 'All Statuses',
	
	'IN_GAME'					=> '/in Game',
	'PREIN_GAME'				=> '/prein Game',
	'REPLACE_IN_GAME'			=> 'Offer Replacement',
	'OUT_GAME'					=> '/out Game',
	
	'PENDING'					=> 'Pending',
	'SLOT_ALIVE'				=> 'Alive',
	'SLOT_DEAD'					=> 'Dead',
	'SLOT_OTHER'				=> 'Other',
	'SLOT_WIN'					=> 'Won',
	'SLOT_LOSS'					=> 'Lost',
	'SLOT_DRAW'					=> 'Draw',
	
	
	'SUBMIT_GAME'	=> 'Submit a Game',
		'ADD_GAME_EXPLAIN'			=> 'Fill out the form to enter a game into signups.',
	'GAME_NAME'					=> 'Game Name',
	'GAME_NAME_EXPLAIN'			=> 'The title of the game to approve.',
	'MODERATOR'					=> 'Moderator',
	'MODERATOR_EXPLAIN'			=> 'The moderator of the game.',
	'GAME_DESCRIPTION'			=> 'Description',
	'GAME_DESCRIPTION_EXPLAIN'	=> 'A short description of the game.',
	'NUM_PLAYERS'				=> 'Player slots',
	'NUM_PLAYERS_EXPLAIN'		=> 'How many players to allow.',
	'GAME_TYPE'					=> 'Game Type',
	'GAME_PLAYERS'				=> 'Players',
	'MAIN_MOD'					=> 'Main Moderator',
	'GAME_STATUS'				=> 'Game Status',
	'GAME_FORUM_EXPLAIN'		=> 'The type of game the setup is.',
	'GAME_DELETED'				=> 'This game has been deleted.',
	'DELETE_GAME_Q'				=> 'Delete?',
	'GAME_ADDITION_FAILED'		=> 'There was an incorrect or missing value from your submission.',
	'GAME_ADDITION_SUCCESSFUL'	=> 'The game was added successfully.',
	'BAD_MODERATOR_NAME'		=> 'The moderator you selected is nonexistent or incorrect.',
	'LATER_DETAILS_EXPLAIN'		=> 'Certain details about your game may be editted after approval.',
	'MOD_THIS_ACCOUNT'			=> 'Mod with this account',
	'REQUESTED_SLOTS'			=> 'Requested Slots',
	'REQUESTED_SLOTS_EXPLAIN'	=> 'How many slots you would like alloted for your game.',
	'ERROR_GAME_SUBMISSION'		=> 'There were errors in your game submission.',
	'APPROVE_SUBMISSION_CONFIRM' => 'Is the following submission correct?',
	'GAME_SUBMISSION_SUCCESS'	=> 'Your game has successfully been submitted. You will receive a PM with the details of your submission.<br />
									After your game has been approved you will receive a notification that your game is ready to go.',
	'CANT_ADD_UNAPPROVED'		=> 'You may not add players until the game has been approved.',
	'CANT_EDIT_CONFIRMATION'	=> 'Sneaky little scum, you can\'t edit the game details like that...',
	'GAME_NOT_EXIST'			=> 'The specified game doesn\'t exist.',
	'ALREADY_APPROVED'			=> 'This game has already been approved.',
));
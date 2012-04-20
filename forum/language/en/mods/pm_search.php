<?php
/**
*
* Search PM folders [English]
*
* @package language
* @version $Id: pm_search.php 6 2009-11-15 08:32:26Z jari $
* @copyright (c) 2009 Jari Kanerva tumba25@gmail.com
* @license http://opensource.org/licenses/gpl-license.php GNU Public License
*
*/

/**
* DO NOT CHANGE
*/
if(!defined('IN_PHPBB'))
{
	exit;
}

if(empty($lang) || !is_array($lang))
{
	$lang = array();
}

$lang = array_merge($lang, array(
	'DATE' => 'Date',

	'RECEIVED_AT' => 'Received',

	'SENDER' => 'Sender',
	'BOTH' => 'Both',

	'PM_SEARCH_EXPLAIN' => 'Place + in front of a word which must be found and - in front of a word which must not be found.',

	'SEARCH_ALL_WORDS' => 'Search for all terms',
	'SEARCH_ANY_WORD' => 'Search for any word',

	'PM_SEARCH_ALL_WORDS' => 'Search for all words',
	'PM_SEARCH_ALL_WORDS_EXPLAIN' => 'Set to yes to match on all words only, that equals a + sign in front of every word. Set to no to search for any word and use + in front of words that must be in the PM’s. This key is not used in the Sender / Recipient search.',
	'PM_SEARCH_WORD_ONLY' => 'Word search only',
	'PM_SEARCH_WORD_ONLY_EXPLAIN' => 'Only match on whole words. Set to no for search on partial words. This key is not used in the Sender / Recipient search.',
	'PM_SEARCH_EXACT_QUERY' => 'Search for the exact query as entered',
	'PM_SEARCH_EXACT_QUERY_EXPLAIN' => 'Select yes if you want to search for the exact phrase. + and - will not be parsed.',
	'PM_SEARCH_CASE' => 'Search case sensitive',
	'PM_SEARCH_CASE_EXPLAIN' => 'Set to yes for a case sensitive search.',
	'PM_SEARCH_CURRENT' => 'Search current folder',
	'PM_SEARCH_CURRENT_EXPLAIN' => 'Set to yes for search in current folder only. No will search all folders.',
	'PM_SEARCH_AUTHOR' => 'Search for sender or recipient',
	'PM_SEARCH_AUTHOR_EXPLAIN' => 'The search for sender(s) or recipient(s) will search for sender in PM’s to you and recipient in PM’s from you.<br />To search for several sender(s) or recipient(s), separate them with a comma (,).',

	'SEARCH_PM_FOLDERS' => 'PM Search…',

	'UCP_PM_SEARCH' => 'PM Search',

	'NO_AUTH_PM_SEARCH' => 'You don’t have permissions to use PM search.',
	'ADDED_PM_SEARCH_PERMISSIONS' => 'Successfully added permissions for PM Search.',
));

?>
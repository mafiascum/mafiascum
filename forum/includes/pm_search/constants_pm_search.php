<?php
/**
*
* @package pm_search
* @version $Id: constants_pm_search.php 7 2009-11-15 08:34:33Z jari $
* @copyright (c) 2009 Jari Kanerva tumba25@gmail.com
* @license http://opensource.org/licenses/gpl-2.0.php GNU Public License version 2
*
* constants.php
*
*/

/**
* DO NOT CHANGE
*/
if (!defined('IN_PHPBB'))
{
	exit;
}

define('IN_PM_SEARCH', true);

define('PM_SENDER', 1);
define('PM_RECIPIENT', 2);
define('PM_BOTH', 3);

define('PM_SUBJECT', 1);
define('PM_MESSAGE', 2);

define('PM_SEARCH_MATCH_ALL', 0);
define('PM_SEARCH_MATCH_WORD', 1);
define('PM_SEARCH_MATCH_EXACT', 2);

?>
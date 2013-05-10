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
if (!defined('IN_PHPBB'))
{
	exit;
}

/**
* valid external constants:
* PHPBB_MSG_HANDLER
* PHPBB_DB_NEW_LINK
* PHPBB_ROOT_PATH
* PHPBB_ADMIN_PATH
*/

// phpBB Version
define('PHPBB_VERSION', '3.0.9');

// QA-related
// define('PHPBB_QA', 1);

// User related
define('ANONYMOUS', 1);

define('USER_ACTIVATION_NONE', 0);
define('USER_ACTIVATION_SELF', 1);
define('USER_ACTIVATION_ADMIN', 2);
define('USER_ACTIVATION_DISABLE', 3);

define('BACKUP_STATUS_PENDING', 0);
define('BACKUP_STATUS_DUMPING_FORUM', 1);
define('BACKUP_STATUS_DUMPING_WIKI', 2);
define('BACKUP_STATUS_COPYING_FORUM_FILES', 3);
define('BACKUP_STATUS_COPYING_WIKI_FILES', 4);
define('BACKUP_STATUS_RENAMING_TEMP_BACKUP_DIRECTORY', 5);
define('BACKUP_STATUS_COMPRESSING_BACKUP_DIRECTORY', 6);
define('BACKUP_STATUS_COMPRESSING_BACKUP_DIRECTORY_FAILED', 7);
define('BACKUP_STATUS_SENDING_BY_FTP', 8);
define('BACKUP_STATUS_COMPLETE', 9);

define('BACKUP_REMOTE_FILE_STATUS_PENDING', 0);
define('BACKUP_REMOTE_FILE_STATUS_FAILED', 1);
define('BACKUP_REMOTE_FILE_STATUS_COMPLETE', 2);

define('AVATAR_UPLOAD', 1);
define('AVATAR_REMOTE', 2);
define('AVATAR_GALLERY', 3);

define('USER_NORMAL', 0);
define('USER_INACTIVE', 1);
define('USER_IGNORE', 2);
define('USER_FOUNDER', 3);

define('INACTIVE_REGISTER', 1);
define('INACTIVE_PROFILE', 2);
define('INACTIVE_MANUAL', 3);
define('INACTIVE_REMIND', 4);
define('INACTIVE_SPAMFLAG', 5);
define('INACTIVE_EMAILFLAG', 6);
define('INACTIVE_IPFLAG', 7);
define('INACTIVE_PASSWORDFLAG', 8);

//Mafia Game Types
define('MAFIA_GAME_TYPE_LARGE_NORMAL', 0);
define('MAFIA_GAME_TYPE_MINI_NORMAL', 1);
define('MAFIA_GAME_TYPE_NEWBIE', 2);
define('MAFIA_GAME_TYPE_LARGE_THEME', 3);
define('MAFIA_GAME_TYPE_MINI_NORMAL', 4);
define('MAFIA_GAME_TYPE_MINI_THEME', 5);
define('MAFIA_GAME_TYPE_OPEN', 6);
define('MAFIA_GAME_TYPE_MARATHON', 7);

//Mafia Game Statuses
define('MAFIA_GAME_STATUS_PENDING', 0);
define('MAFIA_GAME_STATUS_SIGNUPS', 1);
define('MAFIA_GAME_STATUS_ONGOING', 2);
define('MAFIA_GAME_STATUS_COMPLETE', 3);

define('MAFIA_MODATOR_TYPE_PRIMARY', 0);
define('MAFIA_MODATOR_TYPE_COMOD', 1);
define('MAFIA_MODATOR_TYPE_BACKUP', 2);

define('MAFIA_PLAYER_STATUS_ALIVE', 0);
define('MAFIA_PLAYER_STATUS_DEAD', 1);

//Mafia Alignments
define('MAFIA_ALIGNMENT_TOWN', 0);
define('MAFIA_ALIGNMENT_MAFIA', 1);
define('MAFIA_ALIGNMENT_CULT', 2);
define('MAFIA_ALIGNMENT_THIRD_PARTY', 3);
define('MAFIA_ALIGNMENT_NEUTRAL', 4);

//Mafia Slot Statuses
define('MAFIA_SLOT_STATUS_ALIVE', 0);
define('MAFIA_SLOT_STATUS_DEAD', 1);

//Mafia Slot Outcomes
define('MAFIA_SLOT_OUTCOME_WON', 0);
define('MAFIA_SLOT_OUTCOME_LOST', 1);
define('MAFIA_SLOT_OUTCOME_DRAW', 2);

// ACL
define('ACL_NEVER', 0);
define('ACL_YES', 1);
define('ACL_NO', -1);

// Login error codes
define('LOGIN_CONTINUE', 1);
define('LOGIN_BREAK', 2);
define('LOGIN_SUCCESS', 3);
define('LOGIN_SUCCESS_CREATE_PROFILE', 20);
define('LOGIN_ERROR_USERNAME', 10);
define('LOGIN_ERROR_PASSWORD', 11);
define('LOGIN_ERROR_ACTIVE', 12);
define('LOGIN_ERROR_ATTEMPTS', 13);
define('LOGIN_ERROR_EXTERNAL_AUTH', 14);
define('LOGIN_ERROR_PASSWORD_CONVERT', 15);

// Maximum login attempts
// The value is arbitrary, but it has to fit into the user_login_attempts field.
define('LOGIN_ATTEMPTS_MAX', 100);

// Group settings
define('GROUP_OPEN', 0);
define('GROUP_CLOSED', 1);
define('GROUP_HIDDEN', 2);
define('GROUP_SPECIAL', 3);
define('GROUP_FREE', 4);

// Forum/Topic states
define('FORUM_CAT', 0);
define('FORUM_POST', 1);
define('FORUM_LINK', 2);
define('ITEM_UNLOCKED', 0);
define('ITEM_LOCKED', 1);
define('ITEM_MOVED', 2);

// Forum Flags
define('FORUM_FLAG_LINK_TRACK', 1);
define('FORUM_FLAG_PRUNE_POLL', 2);
define('FORUM_FLAG_PRUNE_ANNOUNCE', 4);
define('FORUM_FLAG_PRUNE_STICKY', 8);
define('FORUM_FLAG_ACTIVE_TOPICS', 16);
define('FORUM_FLAG_POST_REVIEW', 32);
define('FORUM_FLAG_QUICK_REPLY', 64);

// Forum Options... sequential order. Modifications should begin at number 10 (number 29 is maximum)
define('FORUM_OPTION_FEED_NEWS', 1);
define('FORUM_OPTION_FEED_EXCLUDE', 2);

// Optional text flags
define('OPTION_FLAG_BBCODE', 1);
define('OPTION_FLAG_SMILIES', 2);
define('OPTION_FLAG_LINKS', 4);

// Topic types
define('POST_NORMAL', 0);
define('POST_STICKY', 1);
define('POST_ANNOUNCE', 2);
define('POST_GLOBAL', 3);

// Lastread types
define('TRACK_NORMAL', 0);
define('TRACK_POSTED', 1);

// Notify methods
define('NOTIFY_EMAIL', 0);
define('NOTIFY_IM', 1);
define('NOTIFY_BOTH', 2);

// Notify status
define('NOTIFY_YES', 0);
define('NOTIFY_NO', 1);

// Email Priority Settings
define('MAIL_LOW_PRIORITY', 4);
define('MAIL_NORMAL_PRIORITY', 3);
define('MAIL_HIGH_PRIORITY', 2);

// Log types
define('LOG_ADMIN', 0);
define('LOG_MOD', 1);
define('LOG_CRITICAL', 2);
define('LOG_USERS', 3);

// Private messaging - Do NOT change these values
define('PRIVMSGS_HOLD_BOX', -4);
define('PRIVMSGS_NO_BOX', -3);
define('PRIVMSGS_OUTBOX', -2);
define('PRIVMSGS_SENTBOX', -1);
define('PRIVMSGS_INBOX', 0);

// Full Folder Actions
define('FULL_FOLDER_NONE', -3);
define('FULL_FOLDER_DELETE', -2);
define('FULL_FOLDER_HOLD', -1);

// Download Modes - Attachments
define('INLINE_LINK', 1);
// This mode is only used internally to allow modders extending the attachment functionality
define('PHYSICAL_LINK', 2);

// Confirm types
define('CONFIRM_REG', 1);
define('CONFIRM_LOGIN', 2);
define('CONFIRM_POST', 3);

// Categories - Attachments
define('ATTACHMENT_CATEGORY_NONE', 0);
define('ATTACHMENT_CATEGORY_IMAGE', 1); // Inline Images
define('ATTACHMENT_CATEGORY_WM', 2); // Windows Media Files - Streaming
define('ATTACHMENT_CATEGORY_RM', 3); // Real Media Files - Streaming
define('ATTACHMENT_CATEGORY_THUMB', 4); // Not used within the database, only while displaying posts
define('ATTACHMENT_CATEGORY_FLASH', 5); // Flash/SWF files
define('ATTACHMENT_CATEGORY_QUICKTIME', 6); // Quicktime/Mov files

// BBCode UID length
define('BBCODE_UID_LEN', 8);

// Number of core BBCodes
define('NUM_CORE_BBCODES', 12);

// BBCode hard limit
define('BBCODE_LIMIT', 1511);

// Smiley hard limit
define('SMILEY_LIMIT', 1000);

// Magic url types
define('MAGIC_URL_EMAIL', 1);
define('MAGIC_URL_FULL', 2);
define('MAGIC_URL_LOCAL', 3);
define('MAGIC_URL_WWW', 4);

// Profile Field Types
define('FIELD_INT', 1);
define('FIELD_STRING', 2);
define('FIELD_TEXT', 3);
define('FIELD_BOOL', 4);
define('FIELD_DROPDOWN', 5);
define('FIELD_DATE', 6);

// referer validation
define('REFERER_VALIDATE_NONE', 0);
define('REFERER_VALIDATE_HOST', 1);
define('REFERER_VALIDATE_PATH', 2);

// phpbb_chmod() permissions
@define('CHMOD_ALL', 7);
@define('CHMOD_READ', 4);
@define('CHMOD_WRITE', 2);
@define('CHMOD_EXECUTE', 1);

// Captcha code length
define('CAPTCHA_MIN_CHARS', 4);
define('CAPTCHA_MAX_CHARS', 7);

// Additional constants
define('VOTE_CONVERTED', 127);

// Table names
define('ACL_GROUPS_TABLE',			$table_prefix . 'acl_groups');
define('ACL_OPTIONS_TABLE',			$table_prefix . 'acl_options');
define('ACL_ROLES_DATA_TABLE',		$table_prefix . 'acl_roles_data');
define('ACL_ROLES_TABLE',			$table_prefix . 'acl_roles');
define('ACL_USERS_TABLE',			$table_prefix . 'acl_users');
define('ATTACHMENTS_TABLE',			$table_prefix . 'attachments');
define('BANLIST_TABLE',				$table_prefix . 'banlist');
define('BBCODES_TABLE',				$table_prefix . 'bbcodes');
define('BOOKMARKS_TABLE',			$table_prefix . 'bookmarks');
define('BOTS_TABLE',				$table_prefix . 'bots');
define('CONFIG_TABLE',				$table_prefix . 'config');
define('CONFIRM_TABLE',				$table_prefix . 'confirm');
define('DISALLOW_TABLE',			$table_prefix . 'disallow');
define('DRAFTS_TABLE',				$table_prefix . 'drafts');
define('EXTENSIONS_TABLE',			$table_prefix . 'extensions');
define('EXTENSION_GROUPS_TABLE',	$table_prefix . 'extension_groups');
define('FORUMS_TABLE',				$table_prefix . 'forums');
define('FORUMS_ACCESS_TABLE',		$table_prefix . 'forums_access');
define('FORUMS_TRACK_TABLE',		$table_prefix . 'forums_track');
define('FORUMS_WATCH_TABLE',		$table_prefix . 'forums_watch');
define('GROUPS_TABLE',				$table_prefix . 'groups');
define('ICONS_TABLE',				$table_prefix . 'icons');
define('LANG_TABLE',				$table_prefix . 'lang');
define('LOG_TABLE',					$table_prefix . 'log');
define('LOGIN_ATTEMPT_TABLE',		$table_prefix . 'login_attempts');
define('MODERATOR_CACHE_TABLE',		$table_prefix . 'moderator_cache');
define('MODULES_TABLE',				$table_prefix . 'modules');
define('POLL_OPTIONS_TABLE',		$table_prefix . 'poll_options');
define('POLL_VOTES_TABLE',			$table_prefix . 'poll_votes');
define('POSTS_TABLE',				$table_prefix . 'posts');
define('PRIVMSGS_TABLE',			$table_prefix . 'privmsgs');
define('PRIVMSGS_FOLDER_TABLE',		$table_prefix . 'privmsgs_folder');
define('PRIVMSGS_RULES_TABLE',		$table_prefix . 'privmsgs_rules');
define('PRIVMSGS_TO_TABLE',			$table_prefix . 'privmsgs_to');
define('PROFILE_FIELDS_TABLE',		$table_prefix . 'profile_fields');
define('PROFILE_FIELDS_DATA_TABLE',	$table_prefix . 'profile_fields_data');
define('PROFILE_FIELDS_LANG_TABLE',	$table_prefix . 'profile_fields_lang');
define('PROFILE_LANG_TABLE',		$table_prefix . 'profile_lang');
define('RANKS_TABLE',				$table_prefix . 'ranks');
define('REPORTS_TABLE',				$table_prefix . 'reports');
define('REPORTS_REASONS_TABLE',		$table_prefix . 'reports_reasons');
define('SEARCH_RESULTS_TABLE',		$table_prefix . 'search_results');
define('SEARCH_WORDLIST_TABLE',		$table_prefix . 'search_wordlist');
define('SEARCH_WORDMATCH_TABLE',	$table_prefix . 'search_wordmatch');
define('SESSIONS_TABLE',			$table_prefix . 'sessions');
define('SESSIONS_KEYS_TABLE',		$table_prefix . 'sessions_keys');
define('SITELIST_TABLE',			$table_prefix . 'sitelist');
define('SMILIES_TABLE',				$table_prefix . 'smilies');
define('STYLES_TABLE',				$table_prefix . 'styles');
define('STYLES_TEMPLATE_TABLE',		$table_prefix . 'styles_template');
define('STYLES_TEMPLATE_DATA_TABLE',$table_prefix . 'styles_template_data');
define('STYLES_THEME_TABLE',		$table_prefix . 'styles_theme');
define('STYLES_IMAGESET_TABLE',		$table_prefix . 'styles_imageset');
define('STYLES_IMAGESET_DATA_TABLE',$table_prefix . 'styles_imageset_data');
define('TOPICS_TABLE',				$table_prefix . 'topics');
define('TOPICS_POSTED_TABLE',		$table_prefix . 'topics_posted');
define('TOPICS_TRACK_TABLE',		$table_prefix . 'topics_track');
define('TOPICS_WATCH_TABLE',		$table_prefix . 'topics_watch');
define('USER_GROUP_TABLE',			$table_prefix . 'user_group');
define('USERS_TABLE',				$table_prefix . 'users');
define('WARNINGS_TABLE',			$table_prefix . 'warnings');
define('WORDS_TABLE',				$table_prefix . 'words');
define('ZEBRA_TABLE',				$table_prefix . 'zebra');

// Additional tables
define('TOPIC_AUTHOR_MODERATION_TABLE', $table_prefix . 'topic_mod');
define('ALT_TABLE', $table_prefix . 'alts');
define('MAFIA_GAMES_TABLE', $table_prefix . 'mafia_games');
define('MAFIA_MODERATORS_TABLE', $table_prefix . 'mafia_moderators');
define('MAFIA_PLAYERS_TABLE', $table_prefix . 'mafia_players');
define('MAFIA_SLOTS_TABLE', $table_prefix . 'mafia_slots');
define('MAFIA_FACTIONS_TABLE', $table_prefix . 'mafia_factions');
define('BACKUP_TABLE', $table_prefix . 'backup');
define('BACKUP_REMOTE_FILE_TABLE', $table_prefix . 'backup_remote_file');
define('POSTS_ARCHIVE_TABLE', $table_prefix . 'posts_archive');
define('USER_POST_MASK_TABLE', $table_prefix . 'user_post_mask');

define('MOBILE_STYLE', '17');
?>
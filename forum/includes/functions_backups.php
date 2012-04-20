<?php
/**
*
* @package phpBB3
* @version $Id$
* @copyright (c) 2005 phpBB Group
* @license http://opensource.org/licenses/gpl-license.php GNU Public License
*
* Added by Kison, March 24 2012
*/

/**
* @ignore
*/
$phpEx = substr(strrchr(__FILE__, '.'), 1);
if (!defined('IN_PHPBB'))
{
	exit;
}


function getBackupStatusName($backupStatus)
{
	switch($backupStatus)
	{
	case BACKUP_STATUS_PENDING: return 'Pending';
	case BACKUP_STATUS_DUMPING_FORUM: return 'Dumping Forum';
	case BACKUP_STATUS_DUMPING_WIKI: return 'Dumping Wiki';
	case BACKUP_STATUS_COPYING_FORUM_FILES: return 'Copying Forum Files';
	case BACKUP_STATUS_COPYING_WIKI_FILES: return 'Copying Wiki Files';
	case BACKUP_STATUS_RENAMING_TEMP_BACKUP_DIRECTORY: return 'Renaming Temp Backup Directory';
	case BACKUP_STATUS_COMPRESSING_BACKUP_DIRECTORY: return 'Compressing Backup Directory';
	case BACKUP_STATUS_COMPRESSING_BACKUP_DIRECTORY_FAILED: return 'Compressing Backup Directory Failed';
	case BACKUP_STATUS_SENDING_BY_FTP: return 'Sending By FTP';
	case BACKUP_STATUS_COMPLETE: return 'Complete';
	}
}

function getBackupRemoteFileStatusName($backupRemoteFileStatus)
{
	switch($backupRemoteFileStatus)
	{
	case BACKUP_REMOTE_FILE_STATUS_PENDING: return "Pending";
	case BACKUP_REMOTE_FILE_STATUS_FAILED: return "Failed";
	case BACKUP_REMOTE_FILE_STATUS_COMPLETE: return "Complete";
	}
}

?>
<?php
/*****************************************
A simple function to handle calls to the
StopForumSpam API.

@param info: The information (email, ip, username) to check against the database.

@param [OPTIONAL] return: Whether to simply check for existence or return the result array.
    Defaults to 0 (check), 1 is for returning result set.

@param {OPTIONAL] mode: The type of info to check.
    Defaults to IP. Known alternatives (username, email).

@returns:	In Return Mode 0:
			Returns Boolean based on match.
		In Return Mode 1:
			Returns Array or False.
*****************************************/
function sfs_check($info, $return=0, $mode='ip')
{

	//Quick check for localhost address that causes issues.
	//EDIT -- ...and for blank submissions >_>
	if($info == '127.0.0.1' || $info == '')
	{
		return False;
	}
	//URL for the stopforumspam api.
	$stopSpamUrl = 'http://www.stopforumspam.com/api?';
	$xmlUrl = $stopSpamUrl .$mode . '=' .$info . '&f=serial';

	$xml = file_get_contents($xmlUrl);

	if ($xml)
	{
		$data = unserialize($xml);
		if ($data['success'])
		{
			if($return==0)
			{
				if ($data[$mode]['appears'])
				{
//					add_log('admin', 'LOG_USER_SFS_SPAM', $info);
					return True;
				}
				else
				{
					return False;
				}
			}
			else if($return==1)
			{
				return $data;
			}
		}
		else
		{
			return False;
		}
	}
	else
	{
		return False;
	}
}
?>
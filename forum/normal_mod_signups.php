<?php
//get phpbb shit
	define('IN_PHPBB', true);
	$phpbb_root_path = (defined('PHPBB_ROOT_PATH')) ? PHPBB_ROOT_PATH : './';
	$phpEx = substr(strrchr(__FILE__, '.'), 1);
	include($phpbb_root_path . 'common.' . $phpEx);
	include($phpbb_root_path . 'includes/functions_display.' . $phpEx);
	include($phpbb_root_path . 'includes/functions_posting.' . $phpEx);
	include($phpbb_root_path . 'includes/functions_user.' . $phpEx);
		
	// Start session
	$user->session_begin();
	$auth->acl($user->data);
	$user->setup('viewforum');
	$normal_mod = 19552;
	
	$submit = request_var('submit', false);
	if ($submit){
		$message = '';
		$num_players = $db->sql_escape(request_var('num_players', ''));
		$backup_mod = $db->sql_escape(request_var('backup_mod', ''));
		$op = $db->sql_escape(request_var('op', ''));
		$pms = $db->sql_escape(request_var('pms', ''));
		$review = $db->sql_escape(request_var('review', ''));
		$message .= 'Mod: ' . $user->data['username'] .'\n';
		$message .= 'Backup Mod: ' . $backup_mod .'\n';
		$message .= 'Number of players: ' . $num_players .'\n';
		if ($review == 'review'){
				$message .= 'Review for: Only Normalcy\n';
		} else {
				$message .= 'Review for: Balance and Normalcy\n';
		}
		$message .= 'Opening Post: ' .'\n' . $op .'\n';
		$message .= 'Pms: '.'\n' . $pms .'\n';
		
		poke_chamber(str_replace(array('\r\n', '\n', '\r'),"<br/>",$message));
		meta_refresh(3, 'forum.mafiascum.net/index.php');
	} 
	function poke_chamber($message)
	{
		global $user, $db, $phpbb_root_path, $phpEx, $normal_mod;

		if (!function_exists('pm_notification'))
		{
			include($phpbb_root_path . 'includes/functions_privmsgs.' . $phpEx);
		}

		$user->add_lang('ucp');

		$subject	= 'Normal Mod Form';
		$text		= utf8_normalize_nfc($message);

		$message	= $text;

		$uid			= $bitfield			= $options		= '';
		$allow_bbcode	= $allow_smilies	= $allow_urls   = $img_status   = $flash_status = true;

		generate_text_for_storage($message, $uid, $bitfield, $options, $allow_bbcode, $allow_urls, $allow_smilies);

		$pm_data = array(
			'address_list'			=> array('u' => array($normal_mod => 'to')),
			'from_user_id'			=> $normal_mod,
			'from_user_ip'			=> '127.0.0.1',
			'from_username'			=> 'Norml Mod Form',
			'enable_sig'			=> false,
			'enable_bbcode'			=> true,
			'enable_smilies'		=> true,
			'enable_urls'			=> true,
			'reply_from_root_level'	=> 0,
			'reply_from_msg_id'		=> 0,
			'icon_id'				=> 0,
			'bbcode_bitfield'		=> $bitfield,
			'bbcode_uid'			=> $uid,
			'message'				=> $message,
		);
		$msg_id			= submit_pm('post', $subject, $pm_data);
		$sender_id		= $normal_mod;
		$receiver_id	= $normal_mod;
		$recipients[$receiver_id] = 'to';
		pm_notification('post', 'Norml Mod Form', $recipients, $subject, $pm_data['message'], $msg_id);
	}
?>
<html>
<head>
<style type='text/css'>
body{
	background-color: #1a1a1a;
	color: white;
	font-size: 90%;
	font-family: Verdana,Helvetica,Arial,sans-serif;
}
textarea{
	color: #919191;
	border: #666666 1px solid;
	width: 84%;
	padding: 4px;
	margin-left: 8%;
	margin-right: 8%;
}
.input input{
	color: #919191;
	border: #666666 1px solid;
	width: 84%;
	padding: 4px;
	margin-left: 8%;
	margin-right: 8%;
}
.submit{
	width: 60px;
	margin: 20px 8% 10px;
	padding-top: 3px;
	padding-bottom: 3px;
	border: 1px solid #666666;
	vertical-align: middle;
	text-align: center;
	cursor: pointer;
	font-weight: bold;
	background-image: url("http://forum.mafiascum.net/styles/mafBlack/theme/images/bg_button.png");
}
.panel{
	background-color: #3f3f3f;
	border: 1px solid #800;
	margin: 30px; 3% 5px;
	padding: 20px;
}
.radios{
	margin: 20px 8% 10px;
	padding-top: 3px;
	padding-bottom: 3px;
	vertical-align: middle;
	text-align: center;
}
</style>
</head>
<body>
<form method='post'>
<div class='panel'>
<p style='font-size: 30px; text-align: center; padding-top: 8px;'>Normal Mod Signup Form</p>
<div class='input'>
<input name='num_players' placeholder='Number of players'/>
</div>
<div class='radios'>
<label for='review'>Only Review For Normlacy:</label>
<input  id='review' type='radio' name='review' value='review'/>
<label for='review'>Review for Balance and Normalcy:</label>
<input id='balance' type='radio' name='review' value='balance'/>
</div>
<div class='input'>
<input name='backup_mod' placeholder='Backup Mod'/>
</div>
<div class='input'>
<textarea name='op' placeholder='Rules/Opening Post' rows='7'></textarea>
</div>
<div class='input'>
<textarea name='pms' placeholder='Role PMs and Action Results' rows='7'></textarea>
</div>
<div>
<input class='submit' name='submit' value='submit' type='submit'/>
</div>
</div>
</form>
</body>
</html>
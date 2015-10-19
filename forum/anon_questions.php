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
	//$service_controller_id = 12696;
	//check if has SE access
	if (group_memberships(13671,$user->data['user_id'],true)){
		//check if forum submitted
		$submit = request_var('submit', false);
		$post = request_var('post_message', false);
		$delete = request_var('delete_message', false);
		$reveal = request_var('show_poster', false);
		if ($submit){
			$message = request_var('message', 'nothing');
			$message = $db->sql_escape(htmlspecialchars($message));
			$sql = 'INSERT INTO phpbb_anon_messages (user_id, message, is_posted) VALUES (' . $user->data['user_id'] .', "' . $message . '", 0)';
			$db->sql_query($sql);
			poke_chamber(str_replace(array('\r\n', '\n', '\r'),"<br/>",$message));
			//mail('earljamesmason@gmail.com', 'New Anon Message', $message);
			meta_refresh(3, 'forum.mafiascum.net/index.php');
		} else if ($post){
			$message_id = (int)request_var('message_id', 0);
			$sql = 'UPDATE phpbb_anon_messages SET is_posted=1 WHERE message_id=' . $message_id;
			$db->sql_query($sql);
		} else if ($delete){
			$message_id = (int)request_var('message_id', 0);
			$sql = 'DELETE FROM phpbb_anon_messages WHERE message_id=' . $message_id;
			$db->sql_query($sql);
		} else if ($reveal){
			$message_id = (int)request_var('message_id', 0);
			$sql = 'SELECT * FROM phpbb_anon_messages WHERE message_id=' . $message_id;
			$res = $db->sql_query($sql);
			$row = $db->sql_fetchrow($res);
			$message_user = $row['user_id'];
		}
	} else {
		$error[] = "You don't have speakeasy access";
	}
	function poke_chamber($message)
	{
		global $user, $db, $phpbb_root_path, $phpEx;

		if (!function_exists('pm_notification'))
		{
			include($phpbb_root_path . 'includes/functions_privmsgs.' . $phpEx);
		}

		$user->add_lang('ucp');

		$subject	= 'new anon message';
		$text		= utf8_normalize_nfc($message);

		$message	= $text;

		$uid			= $bitfield			= $options		= '';
		$allow_bbcode	= $allow_smilies	= $allow_urls   = $img_status   = $flash_status = true;

		generate_text_for_storage($message, $uid, $bitfield, $options, $allow_bbcode, $allow_urls, $allow_smilies);

		$pm_data = array(
			'address_list'			=> array('u' => array(12696 => 'to')),
			'from_user_id'			=> 12696,
			'from_user_ip'			=> '127.0.0.1',
			'from_username'			=> 'pie',
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
		$sender_id		= 'pie';
		$receiver_id	= 12696;

		$recipients[$receiver_id] = 'to';
		pm_notification('post', 'pie', $recipients, $subject, $pm_data['message'], $msg_id);
	}
?>
<html>
	<head>
	</head>
	<body style='background-color: #1a1a1a; color: white;'>
<?php
if ($submit){
	echo 'Question submtted successfully';
} else if (empty($error)){
?>
		<p style='text-align: center;'> This is a form for submitting anonymous questions to the speakeasy.</p>
		<p style='text-align: center;'><b>Your account will be logged in case of abuse</b></p>
		<form method='post' action='anon_questions.php'>
			<textarea name='message' style='width: 84%; margin-left: 8%; margin-right: 8%;' rows='7'>Put your message here</textarea>
			<br/>
			<input style='margin-left: 8%;' type='submit' name='submit' value='submit'/>
		</form>
<?php
} else {
	echo '<p>"You don\'t have speakeasy access"</p>';
}
if ($user->data['user_id'] == 12696){
	$sql = "SELECT * FROM phpbb_anon_messages WHERE is_posted=0";
	$res = $db->sql_query($sql);
	while ($row = $db->sql_fetchrow($res)){
?>
	<div style='width:84%; margin-left: 8%; background-color: #313131; padding: 5px; margin-bottom: 10px'>
		<p><?php if($reveal && $message_id == $row['message_id']){ echo '<b>user_id: </b>' . $message_user . '<br/><br/>';} echo  str_replace(array('\r\n', '\n', '\r'),"<br/>",$row['message']); ?></p>
		<form method='post'>
			<input type='submit' name='post_message' value='post'/>
			<input type='submit' name='delete_message' value='delete' />
			<input type='submit' name='show_poster' value='show poster' />
			<input type='hidden' name='message_id' value='<?php echo $row['message_id']; ?>'/>
		</form>
	</div> 
<?php	
}}
?>
	</body>
</html>
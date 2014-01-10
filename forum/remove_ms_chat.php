<?php
define('IN_PHPBB', true);
$phpbb_root_path = (defined('PHPBB_ROOT_PATH')) ? PHPBB_ROOT_PATH : './';
$phpEx = substr(strrchr(__FILE__, '.'), 1);
require($phpbb_root_path . 'common.' . $phpEx);
require($phpbb_root_path . 'includes/functions_user.' . $phpEx);
require($phpbb_root_path . 'includes/functions_module.' . $phpEx);
$user->session_begin();
$auth->acl($user->data);
			$data = array(
					'images'		=> request_var('images', (bool) $user->optionget('viewimg')),
					'flash'			=> request_var('flash', (bool) $user->optionget('viewflash')),
					'smilies'		=> request_var('smilies', (bool) $user->optionget('viewsmilies')),
					'sigs'			=> request_var('sigs', (bool) $user->optionget('viewsigs')),
					'avatars'		=> request_var('avatars', (bool) $user->optionget('viewavatars')),
					'wordcensor'	=> request_var('wordcensor', (bool) $user->optionget('viewcensors')),
					'youtube'		=> request_var('youtube', (bool) $user->optionget('viewyoutube')),
					'lobby'			=> request_var('lobby', (bool) $user->optionget('enterlobby')),
					'mobile'		=> request_var('mobile', (bool) $user->optionget('autodetectmobile')),
					'chat'			=> request_var('chat', false),
					'bbsigs'		=> request_var('bbsigs', (bool) $user->optionget('sigbb_disabled')),
				);
				
				$user->optionset('viewimg', $data['images']);
						$user->optionset('viewflash', $data['flash']);
						$user->optionset('viewsmilies', $data['smilies']);
						$user->optionset('viewsigs', $data['sigs']);
						$user->optionset('viewavatars', $data['avatars']);
						$user->optionset('viewyoutube', $data['youtube']);
						$user->optionset('autodetectmobile', $data['mobile']);
						$user->optionset('chat_enabled', $data['chat']);
						$user->optionset('enterlobby', $data['lobby']);
						$user->optionset('sigbb_disabled', $data['bbsigs']);

						if ($auth->acl_get('u_chgcensors'))
						{
							$user->optionset('viewcensors', $data['wordcensor']);
						}

						$sql_ary = array(
							'user_options'				=> $user->data['user_options'],
						);
						$sql_ary = array(
							'user_options'	=> $user->data['user_options'],
						);
						$sql = 'UPDATE ' . USERS_TABLE . '
							SET ' . $db->sql_build_array('UPDATE', $sql_ary) . '
							WHERE user_id = ' . $user->data['user_id'];
						$db->sql_query($sql);

echo 'confirm';
?>

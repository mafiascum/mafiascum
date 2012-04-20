<?php
/**
*
* @package phpBB3 FAQ Manager
* @copyright (c) 2007 EXreaction, Lithium Studios
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
 * @package acp
 */
class acp_faq_manager
{
	var $u_action;

	function main($id, $mode)
	{
		global $user, $template, $phpbb_root_path, $phpEx;

		$submit = (isset($_POST['submit'])) ? true : false;
		$action = request_var('action', '');
		$file = request_var('file', '');
		$cat_id = request_var('cat', 0);
		$field_id = request_var('var', 0);
		$new_name = utf8_normalize_nfc(request_var('var_name', '', true));
		$new_value = utf8_normalize_nfc(request_var('var_value', '', true));

		$faq = array(); // will hold the FAQ file data
		$self_url = ($cat_id) ? $this->u_action . "&amp;file={$file}&amp;cat={$cat_id}" . (($field_id) ? "&amp;var={$field_id}" : '') . (($action) ? "&amp;action={$action}" : '') : $this->u_action . "&amp;file={$file}" . (($action) ? "&amp;action={$action}" : '');

		$user->add_lang('mods/faq_manager');

		$this->tpl_name = 'acp_faq_manager';
		$this->page_title = 'ACP_FAQ_MANAGER';

		$template->assign_vars(array(
			'U_ACTION'			=> $self_url,
			'L_TITLE'			=> $user->lang['ACP_FAQ_MANAGER'],

			'S_UNDO'			=> ($file && file_exists($phpbb_root_path . 'store/faq_backup/' . $file . '.' . $phpEx . '.bak')) ? true : false,
			'U_UNDO'			=> $this->u_action . "&amp;file={$file}&amp;action=undo",
		));

		if ($file)
		{
			$faq = $this->build_faq_array($this->load_faq($file));
		}
		else
		{
			// list the available files
			$template->assign_vars(array(
				'L_TITLE_EXPLAIN'		=> $user->lang['FAQ_FILE_SELECT'],
				'S_DISPLAY_FILE_LIST'	=> true,
			));

			foreach ($this->get_faq_file_list() as $loc)
			{
				$template->assign_block_vars('file_list', array(
					'NAME'			=> substr($loc, (strpos($loc, '/help_') + 6)),
					'LANGUAGE'		=> substr($loc, 0, strpos($loc, '/')),
					'LOCATION'		=> 'language/' . $loc . '.' . $phpEx,
					'ACTION'		=> '<a href="' . $this->u_action . '&amp;file=' . $loc . '">' . $user->lang['EDIT'] . '</a>',
				));
			}
		}

		$category = $field = '';
		if ($field_id)
		{
			if (!isset($faq[$cat_id]))
			{
				trigger_error('CATEGORY_NOT_EXIST');
			}

			if (!isset($faq[$cat_id][$field_id]))
			{
				trigger_error('VAR_NOT_EXIST');
			}
			$field = $faq[$cat_id][$field_id][0];
			$category = $faq[$cat_id]['--'];
		}
		else if ($cat_id)
		{
			if (!isset($faq[$cat_id]))
			{
				trigger_error('CATEGORY_NOT_EXIST');
			}

			$category = $faq[$cat_id]['--'];
		}

		switch ($action)
		{
			case 'undo' :
				@copy($phpbb_root_path . 'store/faq_backup/' . $file . '.' . $phpEx . '.bak', $phpbb_root_path . 'language/' . $file . '.' . $phpEx);

				trigger_error($user->lang['FAQ_EDIT_SUCCESS'] . adm_back_link($this->u_action . "&amp;file={$file}"));
			break;
			case 'add' :
				if (isset($_POST['add']) && isset($_POST['var_value']) && !$category)
				{
					$submit = true;
				}

				if ($submit)
				{
					if ($cat_id)
					{
						$faq[$cat_id][] = array(0 => $new_name, 1 => $new_value);
					}
					else
					{
						$faq[] = array('--' => $new_value);
					}

					$this->output_faq($faq, $file);
					trigger_error($user->lang['FAQ_EDIT_SUCCESS'] . adm_back_link($this->u_action . "&amp;file={$file}" . (($cat_id) ? "&amp;cat={$cat_id}" : '&amp;cat=' . (sizeof($faq)))));
				}
				else
				{
					$template->assign_vars(array(
						'L_TITLE_EXPLAIN'		=> $user->lang['FAQ_CAT_LIST'],
						'NAVIGATION'			=> "<a href=\"{$this->u_action}&amp;file={$file}\">{$file}</a>" . (($cat_id) ? ' -&gt; ' . "<a href=\"{$this->u_action}&amp;file={$file}&amp;cat={$cat_id}\">{$category}</a>" : ''),
						'VARIABLE_NAME'			=> $new_name,
						'VARIABLE_VALUE'		=> $new_value,
						'S_ADD'					=> true,
						'S_CAT'					=> (!$cat_id),
					));
				}
			break;
			case 'edit' :
				if ($submit)
				{
					if ($field_id)
					{
						$faq[$cat_id][$field_id] = array(0 => $new_name, 1 => $new_value);
					}
					else if ($cat_id)
					{
						$faq[$cat_id]['--'] = $new_value;
					}

					$this->output_faq($faq, $file);
					trigger_error($user->lang['FAQ_EDIT_SUCCESS'] . adm_back_link($this->u_action . "&amp;file={$file}" . (($field_id) ? "&amp;cat={$cat_id}" : '')));
				}
				else
				{
					$template->assign_vars(array(
						'L_TITLE_EXPLAIN'		=> $user->lang['FAQ_CAT_LIST'],
						'NAVIGATION'			=> "<a href=\"{$this->u_action}&amp;file={$file}\">{$file}</a>" . (($cat_id) ? ' -> ' . "<a href=\"{$this->u_action}&amp;file={$file}&amp;cat={$cat_id}\">{$category}</a>" : '') . (($field_id) ? ' -> ' . $field : ''),
						'VARIABLE_NAME'			=> str_replace('"', '&quot;', ($field_id) ? $faq[$cat_id][$field_id][0] : $faq[$cat_id]['--']),
						'VARIABLE_VALUE'		=> str_replace('"', '&quot;', ($field_id) ? $faq[$cat_id][$field_id][1] : $faq[$cat_id]['--']),
						'S_EDIT'				=> true,
						'S_CAT'					=> (!$field),
					));
				}
			break;
			case 'delete' :
				if (confirm_box(true))
				{
					if ($field_id)
					{
						unset($faq[$cat_id][$field_id]);
					}
					else if ($category)
					{
						unset($faq[$cat_id]);
					}

					$this->output_faq($faq, $file);
					trigger_error($user->lang['FAQ_EDIT_SUCCESS'] . adm_back_link($this->u_action . "&amp;file={$file}" . (($field_id) ? "&amp;cat={$cat_id}" : '')));
				}
				else
				{
					confirm_box(false, ((!$field_id) ? 'DELETE_CAT' : 'DELETE_VAR'));
				}
				redirect($this->u_action);
			break;
			case 'move_down' : // we do a trick with the output url, so there is no move_up mode
				$next = false;
				if ($field_id)
				{
					$temp = $faq[$cat_id][$field_id];
					$faq[$cat_id][$field_id] = $faq[$cat_id][$field_id + 1];
					$faq[$cat_id][$field_id + 1] = $temp;
				}
				else if ($cat_id)
				{
					$temp = $faq[$cat_id];
					$faq[$cat_id] = $faq[$cat_id + 1];
					$faq[$cat_id + 1] = $temp;
					$cat_id = 0;
				}
				unset($temp);
				$this->output_faq($faq, $file);
			// no break
			default :
				$template->assign_vars(array(
					'L_TITLE_EXPLAIN'		=> $user->lang['FAQ_CAT_LIST'],
					'L_CREATE'				=> ($cat_id) ? $user->lang['CREATE_FIELD'] : $user->lang['CREATE_CATEGORY'],
					'NAVIGATION'			=> "<a href=\"{$this->u_action}&amp;file={$file}\">{$file}</a>" . (($cat_id) ? ' -> ' . "<a href=\"{$this->u_action}&amp;file={$file}&amp;cat={$cat_id}\">{$category}</a>" : ''),
					'S_DISPLAY_LIST'		=> ($file) ? true : false,
					'S_CAT'					=> (!$cat_id),
				));

				$move_up = '';
				$i = 1;
				foreach ((($cat_id) ? $faq[$cat_id] : $faq) as $data)
				{
					if (!$cat_id)
					{
						$name = $data['--'];
					}
					else
					{
						if (!is_array($data))
						{
							continue;
						}

						$name = $data[0];
					}

					$url = ($cat_id) ? $this->u_action . "&amp;file={$file}&amp;cat={$cat_id}&amp;var={$i}" : $this->u_action . "&amp;file={$file}&amp;cat={$i}";
					$template->assign_block_vars('file_list', array(
						'NAME'			=> $name,
						'U_VIEW'		=> (!$cat_id) ? $url : '',
						'U_MOVE_DOWN'	=> $url . '&amp;action=move_down',
						'U_MOVE_UP'		=> $move_up,
						'U_EDIT'		=> $url . '&amp;action=edit',
						'U_DELETE'		=> $url . '&amp;action=delete',
					));
					$move_up = $url . '&amp;action=move_down';
					$i++;
				}
			break;
		}
	}

	/**
	* Output the file
	*
	* @param array $faq - The FAQ list (in the parsed style from build_faq_array())
	* @param string $file - The name of the file that the data will be outputted to
	*/
	function output_faq($faq, $file)
	{
		global $phpbb_root_path, $phpEx;

		if (strpos($file, '.') !== false)
		{
			trigger_error('NOT_ALLOWED_OUT_OF_DIR');
		}

		$output = '<?php
/**
*
* ' . $file . '
*
* Built with the FAQ Manager Mod by EXreaction
* http://www.lithiumstudios.org/forum/viewtopic.php?f=31&t=464
*
*/

if (!defined(\'IN_PHPBB\'))
{
	exit;
}

$help = array(
';

		foreach ($faq as $category)
		{
			foreach ($category as $name => $value)
			{
				if (is_array($value))
				{
					$name = $value[0];
					$value = $value[1];
				}

				$output .= "	array(
		0 => '" . str_replace("'", "\'", htmlspecialchars_decode($name)) . "',
		1 => '" . str_replace("'", "\'", htmlspecialchars_decode($value)) . "',
	),
";
			}
		}
		$output .= ');

?>';

		// First, make a backup of the current FAQ.
		if (!@file_exists($phpbb_root_path . 'store/faq_backup/' . substr($file, 0, strrpos($file, '/'))))
		{
			@mkdir($phpbb_root_path . 'store/faq_backup/' . substr($file, 0, strrpos($file, '/')));
		}

		if (!@is_writable($phpbb_root_path . 'store/faq_backup/' . substr($file, 0, strrpos($file, '/'))))
		{
			@chmod($phpbb_root_path . 'store/faq_backup/' . substr($file, 0, strrpos($file, '/')), 0777);
		}

		if (!@is_writable($phpbb_root_path . 'store/faq_backup/' . substr($file, 0, strrpos($file, '/'))))
		{
			trigger_error($user->lang['BACKUP_LOCATION_NO_WRITE'] . ' - store/faq_backup/' . substr($file, 0, strrpos($file, '/')));
		}

		@copy($phpbb_root_path . 'language/' . $file . '.' . $phpEx, $phpbb_root_path . 'store/faq_backup/' . $file . '.' . $phpEx . '.bak');

		// Now edit the existing file
		if (!@is_writable($phpbb_root_path . 'language/' . $file . '.' . $phpEx))
		{
			@chmod($phpbb_root_path . 'language/' . $file . '.' . $phpEx, 0777);
		}

		if (!@is_writable($phpbb_root_path . 'language/' . $file . '.' . $phpEx))
		{
			trigger_error($user->lang['FAQ_FILE_NO_WRITE'] . ' - language/' . $file . '.' . $phpEx);
		}

		if ($fp = @fopen($phpbb_root_path . 'language/' . $file . '.' . $phpEx, 'wb'))
		{
			@flock($fp, LOCK_EX);
			@fwrite ($fp, $output);
			@flock($fp, LOCK_UN);
			@fclose($fp);

			@chmod($filename, 0666);
		}
	}

	/**
	* Gets the FAQ File list
	*/
	function get_faq_file_list()
	{
		global $phpbb_root_path, $phpEx;

		$list = array();
		$dh = @opendir($phpbb_root_path . 'language/');

		if ($dh)
		{
			while (($file = readdir($dh)) !== false)
			{
				if (strpos($file, '.') === false && is_dir($phpbb_root_path . 'language/' . $file))
				{
					$dh1 = @opendir($phpbb_root_path . 'language/' . $file);
					if ($dh1)
					{
						while (($file1 = readdir($dh1)) !== false)
						{
							if (strpos($file1, 'help_') === 0 && substr($file1, -(strlen($phpEx) + 1)) === '.' . $phpEx)
							{
								$list[] = $file . '/' . substr($file1, 0, -(strlen($phpEx) + 1));
							}
							else
							{
								if (strpos($file1, '.') === false && is_dir($phpbb_root_path . 'language/' . $file . '/' . $file1))
								{

									$dh2 = @opendir($phpbb_root_path . 'language/' . $file . '/' . $file1);
									if ($dh2)
									{
										while (($file2 = readdir($dh2)) !== false)
										{
											if (strpos($file2, 'help_') === 0 && substr($file2, -(strlen($phpEx) + 1)) === '.' . $phpEx)
											{
												$list[] = $file . '/' . $file1 . '/' . substr($file2, 0, -(strlen($phpEx) + 1));
											}
										}
										closedir($dh2);
									}
								}
							}
						}
						closedir($dh1);
					}
				}
			}
			closedir($dh);
		}

		return $list;
	}

	/**
	* Loads the FAQ file
	*
	* @param string $file - The file we wish to load.  Include the lang folder name and the file name, like en/help_faq.
	*/
	function load_faq($file)
	{
		global $phpbb_root_path, $phpEx;

		if (strpos($file, '.') !== false)
		{
			trigger_error('NOT_ALLOWED_OUT_OF_DIR');
		}

		if (file_exists($phpbb_root_path . 'language/' . $file . '.' . $phpEx))
		{
			include($phpbb_root_path . 'language/' . $file . '.' . $phpEx);

			if (!isset($help) || !is_array($help))
			{
				trigger_error('BAD_FAQ_FILE');
			}

			return $help;
		}
		else
		{
			trigger_error('FAQ_FILE_NOT_EXIST');
		}
	}

	/**
	* Builds the FAQ array and returns it
	*
	* @param array $help - the raw FAQ data
	*/
	function build_faq_array($help)
	{
		$faq = array();

		$cat_id = $field_id = 0;
		foreach ($help as $ary)
		{
			if ($ary[0] == '--')
			{
				$cat_id++;
				$faq[$cat_id]['--'] = $ary[1];
				$field_id = 0;
			}
			else
			{
				$field_id++;
				$faq[$cat_id][$field_id] = $ary;
			}
		}

		return $faq;
	}
}

?>
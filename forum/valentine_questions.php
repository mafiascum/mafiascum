<?php
define('IN_PHPBB', true);
$phpbb_root_path = (defined('PHPBB_ROOT_PATH')) ? PHPBB_ROOT_PATH : './';
$phpEx = substr(strrchr(__FILE__, '.'), 1);
require($phpbb_root_path . 'common.' . $phpEx);
require($phpbb_root_path . 'includes/functions_user.' . $phpEx);
$user->session_begin();
if ($user->data['username'] == 'Anonymous'){
	echo 'You must be logged in.';
	exit;
}
$user_id = $user->data['user_id'];
if ($user_id != 1637){
	$query = "SELECT * FROM phpbb_alts WHERE alt_user_id=$user_id";
	$result = $db->sql_query($query);
	$alt_data = mysqli_fetch_array($result);
	if (sizeOf($alt_data) > 0){
		echo '<p>You are an alt!</p>';
		exit;
	}
}
if (!empty($_POST)){
	$question_id = (int)$_POST['question_id'];
	if ($question_id<0 || $question_id>53){
		echo '<html><body><p>Invalid Question ID, stop hacking, yo!</p></body></html>';
		exit;
	}
	if ($_POST['submit']){
		$youranswer = (int)$_POST['youranswer'];
		if ($youranswer>5){
			$youranswer=5;
		} else if ($youranswer<1){
			$youranswer=1;
		}
		$prefanswer = (int)$_POST['prefanswer'];
		if ($prefanswer>5){
			$prefanswer=5;
		} else if ($prefanswer<1){
			$prefanswer=1;
		}
		$weight = (int)$_POST['weight'];
		if ($weight>4){
			$weight=4;
		} else if ($weight<0){
			$weight=0;
		}
		$query = "INSERT INTO valentines_answers VALUES ($question_id,$user_id,$youranswer,$prefanswer,$weight) ON DUPLICATE KEY UPDATE answer=values(answer), prefanswer=(prefanswer), weight=(weight)";
		$result = $db->sql_query($query);
		$query = "INSERT INTO valentines_users (user_id, question_id) values($user_id, $question_id) ON DUPLICATE KEY UPDATE question_id=values(question_id)";
		$result = $db->sql_query($query);
		$question_id++;
	} else if ($_POST['pass']){
		$query = "INSERT INTO valentines_users (user_id, question_id) values($user_id, $question_id) ON DUPLICATE KEY UPDATE question_id=values(question_id)";
		$result = $db->sql_query($query);
		$question_id++;
	}
}else {
	$query = "SELECT * FROM valentines_users WHERE user_id=$user_id";
	$result = $db->sql_query($query);
	if ($result){
		$question_data = mysqli_fetch_array($result);
	}
	if ($question_data){
		$question_id = $question_data['question_id']+1;
	}else{
		$question_id = 1;
	}
}

$query = "SELECT * FROM valentines_questions WHERE question_id=$question_id";
$result = $db->sql_query($query);
$question_data = mysqli_fetch_array($result);
if (!$question_data && $question_id < 54){
	$question_id++;
	$query = "SELECT * FROM valentines_questions WHERE question_id=$question_id";
	$result = $db->sql_query($query);
	$question_data = mysqli_fetch_array($result);
}
?>

<html>
<head>
<style type='text/css'>
#loggedinas{
	float: right;
	display: inline-block;
	color: white;
	text-shadow: 1px 1px #000000;
}
h1 {
	text-align: center;
	color: #FFA3C2;
	text-shadow: 2px 2px #000000;
}
body{
	background-image:url('valentines_images/valentines_bg.png');
	background-position:center center;
	background-repeat:no-repeat;
	background-color:#820000;
}
#mainbody {
	margin: 5% 25%;
	background-color:rgba(255,255,255,0.7);
	border-radius: 5px;
	border-style: solid; 
	border-width: 101px 101px 98px 100px;
	-moz-border-image: url(valentines_images/heart_border.png) 101 101 98 100 round;
	-webkit-border-image: url(valentines_images/heart_border.png) 101 101 98 100 round;
	-o-border-image: url(valentines_images/heart_border.png) 101 101 98 100 round;
	border-image: url(valentines_images/heart_border.png) 101 101 98 100 round;
	border-image-outset: 40px;
}
#subbody{
}
#question{
	font-weight:bold;
	font-size: 20px;
	text-align: center;
	padding-bottom: 10px;
}
h2{
	font-size: 18px;
}
.inner{
	padding-left: 15px;
}
.radiolabel{
	position: relative; 
	display:inline-block;
	cursor: pointer;
	position: relative;
	padding: 8px 0px;
	padding-left: 25px;
	border-style: solid;
	border-width: 0px 0px 1px 0px;
	min-width: 100%;
	border-color: #777777;
}
.lastlabel{
	border-width: 0px;
}
input[type=radio]{
	display: none;
}
input[type=submit]{
	display: none;
}
input[type=radio] + label::before {
    content: url('valentines_images/radio.png');
	width: 16px;  
    height: 16px;
	margin-right: 10px;  
    position: absolute;  
    left: 0;
	top: 11px;
}

input[type=radio]:checked + label::before {
    content: url('valentines_images/radio_checked.png');
	width: 16px;  
    height: 16px;
	margin-right: 10px;  
    position: absolute;  
    left: 0;
}
.heart { cursor: pointer; position: relative; width: 100px; height: 90px; z-index:2; margin-right: 10px;} .heart:before, .heart:after { position: absolute; content: ""; left: 50px; top: 0; width: 50px; height: 80px; z-index:-1; background: red; -moz-border-radius: 50px 50px 0 0; border-radius: 50px 50px 0 0; -webkit-transform: rotate(-45deg); -moz-transform: rotate(-45deg); -ms-transform: rotate(-45deg); -o-transform: rotate(-45deg); transform: rotate(-45deg); -webkit-transform-origin: 0 100%; -moz-transform-origin: 0 100%; -ms-transform-origin: 0 100%; -o-transform-origin: 0 100%; transform-origin: 0 100%; } .heart:after {z-index:-1; left: 0; -webkit-transform: rotate(45deg); -moz-transform: rotate(45deg); -ms-transform: rotate(45deg); -o-transform: rotate(45deg); transform: rotate(45deg); -webkit-transform-origin: 100% 100%; -moz-transform-origin: 100% 100%; -ms-transform-origin: 100% 100%; -o-transform-origin: 100% 100%; transform-origin :100% 100%; }
.button{ display: inline-block; position:relative; top:20px; width:100px; cursor: pointer; text-align: center; color: white; font-weight: bold;}
#submit_buttons{
	margin-top: 20px;
}
</style>
<title>Mafiascum Valentines Day Match Making Test</title>
</head>
<body>
<form action="valentine_questions.php" method="post">
<h1>Mafiascum Valentines Day Match Making Test</h1>
	<div id='loggedinas'><p>You are logged in as <?php echo $user->data['username']; ?></p></div>
	<div id='mainbody'>
	<div id='subbody'>
	<?php if (!$question_data){
		echo '<p>Thank you for taking the time to fill out the quiz. Results will be released on the 14th.</p>';
	} else {?>
	<input type='hidden' value="<?php echo $question_data['question_id']; ?>" name='question_id'/>
	<div id='question'>
	<?php
		echo '#' . $question_data['question_id'] . '/53: ' .$question_data['question'];
	?>
	</div>
	<div id='youranswer'>
		<h2>Your Answer:</h2>
		<div class='inner'>
		<?php
			$answers = array();
			for ($i = 1;$i <6; $i++){
				if (!empty($question_data["Answer$i"])){
					$answers[] = $question_data["Answer$i"];
				}
			}
			for ($i = 1; $i < sizeOf($answers)+1; $i++){
				echo '<input type="radio" id="youranswer' . $i . '" name="youranswer" value="' . $i . '">';
				echo '<label for="youranswer' . $i . '" class="radiolabel' .  ($i == 1 ? ' firstlabel' : (($i == sizeOf($answers) )? ' lastlabel' : '')) .'">';
				echo $answers[$i-1];
				echo '</label>';
			}
		?>
		</div>
	</div>
	<div id='preferedanswer'>
	<h2>How you'd want a match to answer:</h2>
		<div class='inner'>
			<?php
				for ($i = 1; $i < sizeOf($answers)+1; $i++){
					echo '<input type="radio" id="prefanswer' . $i . '" name="prefanswer" value="' . $i . '">';
					echo '<label for="prefanswer' . $i . '" class="radiolabel' .  ($i == 1 ? ' firstlabel' : (($i == sizeOf($answers) )? ' lastlabel' : '')) .'">';
					echo $answers[$i-1];
					echo '</label>';
				}
			?>
		</div>
	</div>
	<div id='weight'>
		<h2>How highly do you value your matches response to this question?</h2>
		<div class='inner'>
			<input id='weight1' type="radio" name="weight" value="4"><label for="weight1" class="radiolabel" > This question is very important to me </label>
			<input id='weight2' type="radio" name="weight" value="3"><label for="weight2" class="radiolabel">This question is important to me </label>
			<input id='weight3' type="radio" name="weight" value="2"><label for="weight3" class="radiolabel">This question is somewhat important to me </label>
			<input id='weight4' type="radio" name="weight" value="1"><label for="weight4" class="radiolabel">This question isn't that important to me</label>
			<input id='weight5' type="radio" name="weight" value="0"><label for="weight5" class="radiolabel lastlabel">This question is of no importance to me</label>
		</div>
	</div>
	<div id='submit_buttons'>
		<input id='submit' name='submit' value='submit' type='submit'/>
		<label for='submit' class='heart'><span class='button'>Answer</span></label>
		
		<input id='pass' name='pass' value='pass' type='submit'/>
		<label for='pass' class='heart'><span class='button'>Skip</span></label>
	</div>
	</div>
	</div>
	<?php }?>
</form>
</body>
</html>
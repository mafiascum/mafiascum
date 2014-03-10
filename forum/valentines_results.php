<?php
define('IN_PHPBB', true);
$phpbb_root_path = (defined('PHPBB_ROOT_PATH')) ? PHPBB_ROOT_PATH : './';
$phpEx = substr(strrchr(__FILE__, '.'), 1);
require($phpbb_root_path . 'common.' . $phpEx);
require($phpbb_root_path . 'includes/functions_user.' . $phpEx);
$user->session_begin();
$weight = array(0,10,25,50,200);


if ($user->data['username'] == 'Anonymous'){
	echo 'You must be logged in.';
	exit;
}
$user_id = $user->data['user_id'];
if ($user_id == '1637' && $_GET['user_id']){
	$user_id = $_GET['user_id'];
}
if (!$user_id){
	echo 'You must be logged in.';
	exit;
}
$user_array = array();

$query = 'SELECT user_id FROM valentines_users';
$result = $db->sql_query($query);
while ($row = $db->sql_fetchrow($result)){
	$temp_id = $row['user_id'];
	if ($temp_id != $user_id){
		$user_array[] = $temp_id;
	}
}
user_get_id_name($user_array, $user_array_names);
$query = 'SELECT * FROM valentines_answers WHERE user_id=' . $user_id;
$result = $db->sql_query($query);
$my_question_id_set = array();
$my_pref_answer = array();
$my_answer = array();
$my_weight = array();

$my_total_weight = array();
$my_score = array();

$their_gender_score = array();
$my_gender_score = array();

$their_score = array();
$their_total_weight = array();

$final_results = array();
while ($row = $db->sql_fetchrow($result)){
	$question_id = $row['question_id'];
	$my_question_id_set[] = $question_id;
	$my_pref_answer[$question_id] = $row['prefanswer'];
	$my_answer[$question_id] = $row['answer'];
	$my_weight[$question_id] = $row['weight'];
}
for ($i=0;$i<sizeOf($user_array);$i++){
	$their_id = $user_array[$i];
	$query = 'SELECT * FROM valentines_answers WHERE user_id=' . $their_id;
	$result = $db->sql_query($query);
	$their_question_id_set = array();
	$their_pref_answer = array();
	$their_answer = array();
	$their_weight = array();
	$my_total_weight[$their_id] = 0;
	$my_score[$their_id] = 0;
	$their_total_weight[$their_id] = 0;
	$their_score[$their_id] = 0;
	$question_match = 0;
	while ($row = $db->sql_fetchrow($result)){
		$question_id=$row['question_id'];
		$their_pref_answer[$question_id] = $row['prefanswer'];
		$their_answer[$question_id] = $row['answer'];
		$their_weight[$question_id] = $row['weight'];
	}
	for ($k=0;$k<sizeOf($my_question_id_set);$k++){
		$question_id = $my_question_id_set[$k];
		if ($question_id != 10){
			if ($their_pref_answer[$question_id]){
				$question_match++;
				$temp_weight = $their_weight[$question_id];
				$their_total_weight[$their_id] += $weight[$temp_weight];
				if ($their_pref_answer[$question_id] == $my_answer[$question_id]){
					$their_score[$their_id] += $weight[$temp_weight];
				}
				$temp_weight = $my_weight[$question_id];
				$my_total_weight[$their_id] += $weight[$temp_weight];
				if ($their_answer[$question_id] == $my_pref_answer[$question_id]){
					$my_score[$their_id] += $weight[$temp_weight];
					
				}
			}
		} else {
			if ($their_pref_answer[10]){
				$their_gender_score[$their_id] = 0;
				$my_gender_score[$their_id] = 0;
				if ($their_pref_answer[10] == $my_answer[10] || $their_weight[$question_id] == 0){
					$their_gender_score[$their_id] = 1;
				}
				if ($their_answer[10] == $my_pref_answer[10] || $my_weight[$question_id] == 0){
					$my_gender_score[$their_id] = 1;
				}
			}
			
			
		}
	}
	if ($their_total_weight[$their_id] > 0 && $my_total_weight[$their_id] > 0 && $question_match > 24){
		$my_percent = $my_score[$their_id] / ($my_total_weight[$their_id]);
		$their_percent = $their_score[$their_id]/($their_total_weight[$their_id]);
		$finalscore = pow(($my_percent*$their_percent),1/2);
		$final_results[$their_id] = $finalscore;
	}
	
}
?>
<html>
<head>
<style type='text/css'>
	.username{
		display: inline-block;
		width: 200px;
	}
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
</head>
<body>
<h1>Mafiascum Valentines Day Match Making Results</h1>
<div id='mainbody'>
<div id='subbody'>
<?php
arsort($final_results);
$count = 0;
echo '<h2>Non Gendered </h2>';
foreach ($final_results as $user => $percent) {
	echo '<span class="username">' . $user_array_names[$user] . ':</span>' . ((int)($percent * 10000)/100.0) . '%<br/>';
	$count++;
	if ($count == 5){break;}
}

asort($final_results);
foreach ($final_results as $user => $percent) {
	$worst_id = $user;
	$worst_percent = $percent;
	break;
}

foreach ($final_results as $user => $percent) {
	//echo ($user . ' mine: ' . $my_gender_score[$user] . ' theirs: ' . $their_gender_score[$user] . ' ');
	$final_results[$user] = $final_results[$user] * $my_gender_score[$user] * $their_gender_score[$user];
}

arsort($final_results);
$count = 0;
echo '<h2>Gendered(Overall): </h2>';
foreach ($final_results as $user => $percent) {
	echo '<span class="username">' . $user_array_names[$user] . ':</span>' . ((int)($percent * 10000)/100.0) . '%<br/>';
	$count++;
	if ($count == 5){break;}
}
foreach ($final_results as $user => $percent) {
	$final_results[$user] = ($my_score[$user]/$my_total_weight[$user]);
}
arsort($final_results);
$count = 0;

echo '<h2>Non Gendered(You to Them): </h2>';
foreach ($final_results as $user => $percent) {
	echo '<span class="username">' . $user_array_names[$user] . ':</span>' . ((int)($percent * 10000)/100.0) . '% (' . ((int)(($their_score[$user]/$their_total_weight[$user]) * 10000)/100.0) . '%)<br/>';
	$count++;
	if ($count == 5){break;}
}

foreach ($final_results as $user => $percent) {
	$final_results[$user] = ($their_score[$user]/$their_total_weight[$user]);
}

arsort($final_results);
$count = 0;

echo '<h2>Non Gendered(Them to You): </h2>';
foreach ($final_results as $user => $percent) {
	echo '<span class="username">' . $user_array_names[$user] . ':</span>' . ((int)($percent * 10000)/100.0) . '% (' . ((int)(($my_score[$user]/$my_total_weight[$user]) * 10000)/100.0) . '%)<br/>';
	$count++;
	if ($count == 5){break;}
}
foreach ($final_results as $user => $percent) {
	$final_results[$user] = ($my_score[$user]/$my_total_weight[$user]) * $my_gender_score[$user];
}
arsort($final_results);
$count = 0;

echo '<h2>Gendered(You to Them): </h2>';
foreach ($final_results as $user => $percent) {
	echo '<span class="username">' . $user_array_names[$user] . ':</span>' . ((int)($percent * 10000)/100.0) . '% (' . ((int)(($their_score[$user]/$their_total_weight[$user]) * $their_gender_score[$user] * 10000)/100.0) . '%)<br/>';
	$count++;
	if ($count == 5){break;}
}

foreach ($final_results as $user => $percent) {
	$final_results[$user] = ($their_score[$user]/$their_total_weight[$user]) * $their_gender_score[$user];
}

arsort($final_results);
$count = 0;

echo '<h2>Gendered(Them to You): </h2>';
foreach ($final_results as $user => $percent) {
	echo '<span class="username">' . $user_array_names[$user] . ':</span>' . ((int)($percent * 10000)/100.0) . '% (' . ((int)(($my_score[$user]/$my_total_weight[$user]) * $my_gender_score[$user]  * 10000)/100.0) . '%)<br/>';
	$count++;
	if ($count == 5){break;}
}
echo '<h2>Worst: </h2>';
echo '<span class="username">' . $user_array_names[$worst_id] . ':</span>' . ((int)($worst_percent * 10000)/100.0) . '%<br/>';

?>
</div>
</div>
</body>
</html>


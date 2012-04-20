<?php

include("./config.php");
include($pathToConfig);

$connection = mysql_connect($dbhost, $dbuser, $dbpasswd);

if (!$connection) {
    die('Could not connect: ' . mysql_error());
}

mysql_select_db($dbname, $connection);

$result = mysql_query("SELECT * FROM tempDistinctPostersPerMonth ORDER BY interval_datetime ASC", $connection);

$rootArray = array();

if(!$result) {
	exit;
}
$count = 1;
while($row = mysql_fetch_assoc($result)) {

	$rootArray[] = array($row['interval_datetime'], (int)$row['distinct_posters']);
	$count++;
}

$jsonArray = json_encode(array($rootArray));

mysql_free_result($result);

mysql_close($connection);


?>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
 <HEAD>
  <TITLE> New Document </TITLE>
  <META NAME="Generator" CONTENT="EditPlus">
  <META NAME="Author" CONTENT="">
  <META NAME="Keywords" CONTENT="">
  <META NAME="Description" CONTENT="">
  <script class="include" type="text/javascript" src="jquery.min.js"></script>
  <script class="include" type="text/javascript" src="jquery.jqplot.min.js"></script>
  <script type="text/javascript" src="./plugins/jqplot.canvasTextRenderer.min.js"></script>
  <script type="text/javascript" src="./plugins/jqplot.canvasAxisLabelRenderer.min.js"></script>
  <script type="text/javascript" src="./plugins/jqplot.canvasAxisTickRenderer.min.js"></script>
  <script type="text/javascript" src="./plugins/jqplot.categoryAxisRenderer.min.js"></script>
  <script type="text/javascript" src="./plugins/jqplot.cursor.min.js"></script>
  <script type="text/javascript" src="./plugins/jqplot.dateAxisRenderer.min.js"></script>
  <script type="text/javascript" src="./plugins/jqplot.highlighter.min.js"></script>
  <link rel="stylesheet" type="text/css" href="./style/jquery.jqplot.min.css" />
  <style type="text/css">
  #Chart_PRPM {max-width: 80%;}
  </style>
 
 </HEAD>

 <BODY>
     <script class="code" type="text/javascript" language="javascript">
$(document).ready(function(){
	var plot2 = $.jqplot ('Chart_PRPM', <?php echo($jsonArray);?>,
	{
		// Give the plot a title.
		title: 'Unique Posters Per Month',

		highlighter: {
			show: true,
			sizeAdjust: 7.5
		},
		cursor: {
			show: true,
			zoom:true
		},
		axesDefaults: {
			tickRenderer: $.jqplot.CanvasAxisTickRenderer
		},
		axes: {
			// options for each axis are specified in seperate option objects.
			xaxis: {
				label: "Month/Year",
				renderer: $.jqplot.DateAxisRenderer,
				labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
				tickOptions:{angle: 45, formatString:'%b %Y'},
				min:'jan 1, 2002',
				tickInterval:'4 month'
        	},
        	yaxis: {
				label: "Number of Unique Posters",
				labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
				min: 0,
				tickInterval: 10
        	}
      	}
    });
	$('.button-reset').click(function() { plot2.resetZoom() });
});
</script>
<div class="example-plot" id="Chart_PRPM"></div>
<button class="button-reset">Reset Zoom</button>
 </BODY>
</HTML>
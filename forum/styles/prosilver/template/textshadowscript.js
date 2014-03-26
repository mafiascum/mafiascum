	function getLightestShade(r,g,b){
		var percent = 0.0;
		var yiq = ((r*299)+(g*587)+(b*114));
		if (yiq < 75000){
			if (r > g && r > b){
				percent = 255.0/r;
			} else if (b > g) {
				console.log(b + ', ' + g);
				percent = 255.0/b;
			} else {
				percent = 255.0/g;
			}
			r = Math.round(r*percent);
			g = Math.round(g*percent);
			b = Math.round(b*percent);
			
			yiq = ((r*299)+(g*587)+(b*114));
			
			if (yiq < 75000){
			 var target = 75000 - yiq;
			 var r_g_ratio = (r/1.0)/g;
			 var newg = target/(r_g_ration*299 + 589);
			 var newr = r_g_ratio*newg;
			 r = r + Math.round(newr);
			 g = g + Math.round(newg);
			}
			return 'rgb(' + r + ', ' + g + ', ' + b + ')';
		}
	}
	function getContrastYIQ(r,g,b){
		var yiq = ((r*299)+(g*587)+(b*114))/1000;
	//	return (yiq >= 100) ? 'black' : ('rgb(' + Math.round((255-r)/1.4 - (0-r)) + ', ' + Math.round((255-g)/1.4 - (0-g)) + ', ' + Math.round((255-b)/1.4 - (0-b)) + ')');
		return (yiq >= 128) ? 'black' : 'white';
	}
$(document).ready(function(){
	if (templatepath.search("mafBlack") > 0){
		$("*").each(function( index ){
			var colour = $(this).css('color');
			components = colour.substring(colour.indexOf('(') + 1, colour.lastIndexOf(')')).split(/,\s*/);
			var r = parseInt(components[0]);
			var g = parseInt(components[1]);
			var b = parseInt(components[2]);
			if ((r != 238 && r != 221) || (g != 238 && g != 221) || (b != 238 && b != 221)){
				var contrast = getContrastYIQ(r, g, b);
				//console.log('contrast: ' + contrast + ' colour: ' + colour);
				$(this).css("color", getLightestShade(r,g,b));
			}
		});
	}
});
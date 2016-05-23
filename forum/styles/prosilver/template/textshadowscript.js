function rgbToHsl(r, g, b){
    r /= 255, g /= 255, b /= 255;
    var max = Math.max(r, g, b), min = Math.min(r, g, b);
    var h, s, l = (max + min) / 2;

    if(max == min){
        h = s = 0; // achromatic
    }else{
        var d = max - min;
        s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
        switch(max){
            case r: h = (g - b) / d + (g < b ? 6 : 0); break;
            case g: h = (b - r) / d + 2; break;
            case b: h = (r - g) / d + 4; break;
        }
        h /= 6;
    }

    return [h, s, l];
}
function calculateBrightness(r,g,b){
	var r1 = r/255;
	var b1 = b/255;
	var g1 = g/255;
	var r2, g2, b2;
	if (r1 <= 0.03928){
		r2 = r1/12.92
	} else {
		r2 = Math.pow(((r1+0.055)/1.055),2.4);
	}
	if (g1 <= 0.03928){
		g2 = g1/12.92
	} else {
		g2 = Math.pow(((g1+0.055)/1.055),2.4);
	}
	if (b1 <= 0.03928){
		b2 = b1/12.92
	} else {
		b2 = Math.pow(((b1+0.055)/1.055),2.4);
	}
	return .2126*r2 + .7152*g2 + .0722*b2; 
}
function test(){
}
function calcContrast (r1,g1,b1,r2,g2,b2){
	var brightness1 = calculateBrightness(r1,g1,b1);
	var brightness2 = 0.02518685962736163;
	
	return (brightness1 + 0.05)/(brightness2 + 0.05);
}
function hslToRgb(h, s, l){
    var r, g, b;

    if(s == 0){
        r = g = b = l; // achromatic
    }else{
        function hue2rgb(p, q, t){
            if(t < 0) t += 1;
            if(t > 1) t -= 1;
            if(t < 1/6) return p + (q - p) * 6 * t;
            if(t < 1/2) return q;
            if(t < 2/3) return p + (q - p) * (2/3 - t) * 6;
            return p;
        }

        var q = l < 0.5 ? l * (1 + s) : l + s - l * s;
        var p = 2 * l - q;
        r = hue2rgb(p, q, h + 1/3);
        g = hue2rgb(p, q, h);
        b = hue2rgb(p, q, h - 1/3);
    }

    return [Math.round(r * 255), Math.round(g * 255), Math.round(b * 255)];
}
	function lightenTenPercent(r,g,b){
		var hsl = rgbToHsl(r,g,b);
		hsl[2] = hsl[2] * 1.175;
		return hslToRgb(hsl[0],hsl[1],hsl[2]);
	}
	function getLightestShade(r,g,b){
	rgb = [r,g,b];
		var count = 0;
		if (calcContrast(rgb[0],rgb[1],rgb[2],44,44,44) > 1){
			while (calcContrast(rgb[0],rgb[1],rgb[2],44,44,44) < 4){
				rgb = lightenTenPercent(rgb[0],rgb[1],rgb[2]);
				count++;
				if (count>5){
					break;
				}
			}
		}
		return 'rgb(' + rgb[0] + ', ' + rgb[1] + ', ' + rgb[2] + ')';
	}

function adjustColor(index, element) {
	var $element = $(element);
    var colour = $element.css('color');
	components = colour.substring(colour.indexOf('(') + 1, colour.lastIndexOf(')')).split(/,\s*/);
	var r = parseInt(components[0]);
	var g = parseInt(components[1]);
	var b = parseInt(components[2]);
	if ((r != 238 && r != 221) || (g != 238 && g != 221) || (b != 238 && b != 221)){
		$element.css("color", getLightestShade(r,g,b));
	}
}
    
$(document).ready(function(){
	if (templatepath.search("mafBlack") > 0)
		$("*").each(adjustColor);
});
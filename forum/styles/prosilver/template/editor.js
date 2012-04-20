/**
* bbCode control by subBlue design [ www.subBlue.com ]
* Includes unixsafe colour palette selector by SHS`
*/

// Startup variables
var imageTag = false;
var theSelection = false;
var bbcodeEnabled = true;

// Check for Browser & Platform for PC & IE specific bits
// More details from: http://www.mozilla.org/docs/web-developer/sniffer/browser_type.html
var clientPC = navigator.userAgent.toLowerCase(); // Get client info
var clientVer = parseInt(navigator.appVersion); // Get browser version

var is_ie = ((clientPC.indexOf('msie') != -1) && (clientPC.indexOf('opera') == -1));
var is_win = ((clientPC.indexOf('win') != -1) || (clientPC.indexOf('16bit') != -1));

var baseHeight;

/**
* Shows the help messages in the helpline window
*/
function helpline(help)
{
	document.forms[form_name].helpbox.value = help_line[help];
}

/**
* Fix a bug involving the TextRange object. From
* http://www.frostjedi.com/terra/scripts/demo/caretBug.html
*/ 
function initInsertions() 
{
	var doc;

	if (document.forms[form_name])
	{
		doc = document;
	}
	else 
	{
		doc = opener.document;
	}

	var textarea = doc.forms[form_name].elements[text_name];
	if (is_ie && typeof(baseHeight) != 'number')
	{	
		textarea.focus();
		baseHeight = doc.selection.createRange().duplicate().boundingHeight;

		if (!document.forms[form_name])
		{
			document.body.focus();
		}
	}
}

/**
* bbstyle
*/
function bbstyle(bbnumber)
{	
	if (bbnumber != -1)
	{
		bbfontstyle(bbtags[bbnumber], bbtags[bbnumber+1]);
	} 
	else 
	{
		insert_text('[*]');
		document.forms[form_name].elements[text_name].focus();
	}
}

/**
* Apply bbcodes
*/
function bbfontstyle(bbopen, bbclose)
{
	theSelection = false;
		
	var textarea = document.forms[form_name].elements[text_name];

	textarea.focus();

	if ((clientVer >= 4) && is_ie && is_win)
	{
		// Get text selection
		theSelection = document.selection.createRange().text;

		if (theSelection)
		{
			// Add tags around selection
			document.selection.createRange().text = bbopen + theSelection + bbclose;
			document.forms[form_name].elements[text_name].focus();
			theSelection = '';
			return;
		}
	}
	else if (document.forms[form_name].elements[text_name].selectionEnd && (document.forms[form_name].elements[text_name].selectionEnd - document.forms[form_name].elements[text_name].selectionStart > 0))
	{
		mozWrap(document.forms[form_name].elements[text_name], bbopen, bbclose);
		document.forms[form_name].elements[text_name].focus();
		theSelection = '';
		return;
	}
	
	//The new position for the cursor after adding the bbcode
	var caret_pos = getCaretPosition(textarea).start;
	var new_pos = caret_pos + bbopen.length;

	// Open tag
	insert_text(bbopen + bbclose);

	// Center the cursor when we don't have a selection
	// Gecko and proper browsers
	if (!isNaN(textarea.selectionStart))
	{
		textarea.selectionStart = new_pos;
		textarea.selectionEnd = new_pos;
	}	
	// IE
	else if (document.selection)
	{
		var range = textarea.createTextRange(); 
		range.move("character", new_pos); 
		range.select();
		storeCaret(textarea);
	}

	textarea.focus();
	return;
}

/**
* Insert text at position
*/
function insert_text(text, spaces, popup)
{
	var textarea;
	
	if (!popup) 
	{
		textarea = document.forms[form_name].elements[text_name];
	} 
	else 
	{
		textarea = opener.document.forms[form_name].elements[text_name];
	}
	if (spaces) 
	{
		text = ' ' + text + ' ';
	}
	
	if (!isNaN(textarea.selectionStart))
	{
		var sel_start = textarea.selectionStart;
		var sel_end = textarea.selectionEnd;

		mozWrap(textarea, text, '');
		textarea.selectionStart = sel_start + text.length;
		textarea.selectionEnd = sel_end + text.length;
	}	
	
	else if (textarea.createTextRange && textarea.caretPos)
	{
		if (baseHeight != textarea.caretPos.boundingHeight) 
		{
			textarea.focus();
			storeCaret(textarea);
		}		
		var caret_pos = textarea.caretPos;
		caret_pos.text = caret_pos.text.charAt(caret_pos.text.length - 1) == ' ' ? caret_pos.text + text + ' ' : caret_pos.text + text;
		
	}
	else
	{
		textarea.value = textarea.value + text;
	}
	if (!popup) 
	{
		textarea.focus();
	} 	

}

/**
* Add inline attachment at position
*/
function attach_inline(index, filename)
{
	insert_text('[attachment=' + index + ']' + filename + '[/attachment]');
	document.forms[form_name].elements[text_name].focus();
}

/**
* Add quote text to message
*/
function addquote(post_id, username, l_wrote)
{
	var message_name = 'message_' + post_id;
	var theSelections = new Array();
	var divarea = false;
	var trueSelections='';
	var selectionError = false; // set to true if the selected segment(s) are from a post other than post_id.
	var selections; //used to hold the various continguous selections.
	if (l_wrote === undefined)
	{
		// Backwards compatibility
		l_wrote = 'wrote';
	}
	if (document.all)
	{
		divarea = document.all[message_name];
	}
	else
	{
		divarea = document.getElementById(message_name);
	}

	if (window.getSelection)
	{
		selections = window.getSelection();
		if (document.selection)
		{
			trueSelections = document.selection.createRange().text;
		}
		else{
			trueSelections= window.getSelection().toString(); // used for reformating individual lines.
		}
		//get all the non-contiguouse selections and save each to a spot in the array
		for (var count=0; count<selections.rangeCount; count++)
		{
			theSelections[count] = selections.getRangeAt(count).toString();
		}
		//check to make sure all the selections are from the post associated with this quote button
		for (var count=0; count<selections.rangeCount; count++)
		{
			if (postNotContains(post_id, theSelections[count])){
				selectionError = true; 
			}
		}
		//reformat that selections
		var i = 0;
		var char1;
		var char2;
		for (var j=0; j<theSelections.length; j++)
		{
			for (var k=0; k<theSelections[j].length; k++)
			{
				char1 = trueSelections.charAt(i);
				char2 = theSelections[j].charAt(k);
				if (char1 != char2)
				{
					theSelections[j]= theSelections[j].substring(0,k) + char1 + theSelections[j].substring(k,theSelections[j].length);
				}
				i++;
			}
		}


	}
	else if (document.getSelection)
	{
		theSelections[0] = document.getSelection();
	}
	else if (document.selection)
	{		
		theSelections[0] = document.selection.createRange().text;
	}

	//if nothing from this post was selected, or they selected things from multiple posts, give them this full post instead.
	if (theSelections[0] == '' || typeof theSelections[0] == 'undefined' || theSelections[0] == null || selectionError)
	{

		if (divarea.innerHTML)
		{
			theSelections[0] = divarea.innerHTML.replace(/<br>/ig, '\n');
			theSelections[0] = theSelections[0].replace(/<br\/>/ig, '\n');
			theSelections[0] = theSelections[0].replace(/&lt\;/ig, '<');
			theSelections[0] = theSelections[0].replace(/&gt\;/ig, '>');
			theSelections[0] = theSelections[0].replace(/&amp\;/ig, '&');
			theSelections[0] = theSelections[0].replace(/&nbsp\;/ig, ' ');
		}
		else if (document.all)
		{
			theSelections[0] = divarea.innerText;
		}
		else if (divarea.textContent)
		{
			theSelections[0] = divarea.textContent;
		}
		else if (divarea.firstChild.nodeValue)
		{
			theSelections[0] = divarea.firstChild.nodeValue;
		}
	}
	//output
	for (var count=0;count<theSelections.length ;count++)
	{
		if (!(count > 0 && selectionError))
		{
			if (bbcodeEnabled)
			{
				insert_text('[quote="' + username + '"]' + theSelections[count] + '[/quote]');
			}
			else
			{	
				insert_text(username + ' ' + l_wrote + ':' + '\n');
				var lines = split_lines(theSelections[0]);
				for (i = 0; i < lines.length; i++)
				{
					insert_text('> ' + lines[i] + '\n');
				}
			}
		}
	}
	return;
}
//Strips all the html tags from a string, replaces some with logical corrisponding strings.
function addQuoteFromSelection(post_id){
	var quotesArray = new Array();
	var quotesString = getCookie("ugEvYSDJEOAz2bHadHvOPOST_" + post_id);
	var quote = "";
	var username = quotesString.substr(0,quotesString.indexOf("NqLN5jsRe24krOHFSruT"));
	quotesString = quotesString.substr(quotesString.indexOf("NqLN5jsRe24krOHFSruT")+20);
	while(quotesString != ""){
		if (typeof quotesString == 'undefine' || quotesString == '' || quotesString == null)
		{
			setCookie("ugEvYSDJEOAz2bHadHvOPOST_" + post_id,'',-1);
			return; 
		}
		else
		{
			quote = quotesString.substr(0,quotesString.indexOf("NqLN5jsRe24krOHFSruT"));
			if (bbcodeEnabled)
				{
					insert_text('[quote="' + username + '"]' + quote + '[/quote]');
				}
				else
				{	
					insert_text(username + ' wrote:\n');
					var lines = split_lines(quote);
					for (i = 0; i < lines.length; i++)
					{
						insert_text('> ' + lines[i] + '\n');
					}
				}
				quotesString = quotesString.substr(quotesString.indexOf("NqLN5jsRe24krOHFSruT")+20);
		}
	}
	setCookie("ugEvYSDJEOAz2bHadHvOPOST_" + post_id,'',-1);
}


function getCookie(c_name)
{
var i,x,y,ARRcookies=document.cookie.split(";");
for (i=0;i<ARRcookies.length;i++)
  {
  x=ARRcookies[i].substr(0,ARRcookies[i].indexOf("="));
  y=ARRcookies[i].substr(ARRcookies[i].indexOf("=")+1);
  x=x.replace(/^\s+|\s+$/g,"");
  if (x==c_name)
    {
    return unescape(y);
    }
  }
}

function setCookie(c_name,value,exdays)
{
	var exdate=new Date();
	exdate.setDate(exdate.getDate() + exdays);
	var c_value=escape(value) + ((exdays==null) ? "" : "; expires="+exdate.toUTCString());
	document.cookie=c_name + "=" + c_value;
}

function stripHTML(text){
	var strippedText = text.replace(/<br>/ig, '\n');
	strippedText = strippedText.replace(/<br\/>/ig, '\n');
	strippedText = strippedText.replace(/&lt\;/ig, '<');
	strippedText = strippedText.replace(/&gt\;/ig, '>');
	strippedText = strippedText.replace(/&amp\;/ig, '&');
	strippedText = strippedText.replace(/&nbsp\;/ig, ' ');
	return strippedText.replace(/<\/?[^>]+(>|$)/g, "");	
}
//Strips whitespace from a string
function stripWhiteSpace(text){
  var blackText = text;
  blackText = blackText.replace(/\n/g, '');
  blackText = blackText.replace(/(\s)/ig, '');
  return blackText; 
}

//checks if a given post contains the given text, if it does it returns false.
function postNotContains(post_id, text){
	var message_name = 'message_' + post_id;
	var divarea = false;
	if (document.all)
	{
		divarea = document.all[message_name];
	}
	else
	{
		divarea = document.getElementById(message_name);
	}
	var array = divarea.parentNode.getElementsByTagName("div");
	return (array[1].innerHTML && stripWhiteSpace(stripHTML(array[1].innerHTML)).indexOf(stripWhiteSpace(text), 0)==-1);
}


function split_lines(text)
{
	var lines = text.split('\n');
	var splitLines = new Array();
	var j = 0;
	for(i = 0; i < lines.length; i++)
	{
		if (lines[i].length <= 80)
		{
			splitLines[j] = lines[i];
			j++;
		}
		else
		{
			var line = lines[i];
			do
			{
				var splitAt = line.indexOf(' ', 80);
				
				if (splitAt == -1)
				{
					splitLines[j] = line;
					j++;
				}
				else
				{
					splitLines[j] = line.substring(0, splitAt);
					line = line.substring(splitAt);
					j++;
				}
			}
			while(splitAt != -1);
		}
	}
	return splitLines;
}

/**
* From http://www.massless.org/mozedit/
*/
function mozWrap(txtarea, open, close)
{
	var selLength = (typeof(txtarea.textLength) == 'undefined') ? txtarea.value.length : txtarea.textLength;
	var selStart = txtarea.selectionStart;
	var selEnd = txtarea.selectionEnd;
	var scrollTop = txtarea.scrollTop;

	if (selEnd == 1 || selEnd == 2) 
	{
		selEnd = selLength;
	}

	var s1 = (txtarea.value).substring(0,selStart);
	var s2 = (txtarea.value).substring(selStart, selEnd);
	var s3 = (txtarea.value).substring(selEnd, selLength);

	txtarea.value = s1 + open + s2 + close + s3;
	txtarea.selectionStart = selStart + open.length;
	txtarea.selectionEnd = selEnd + open.length;
	txtarea.focus();
	txtarea.scrollTop = scrollTop;

	return;
}

/**
* Insert at Caret position. Code from
* http://www.faqts.com/knowledge_base/view.phtml/aid/1052/fid/130
*/
function storeCaret(textEl)
{
	if (textEl.createTextRange)
	{
		textEl.caretPos = document.selection.createRange().duplicate();
	}
}

/**
* Color pallette
*/
function colorPalette(dir, width, height)
{
	var r = 0, g = 0, b = 0;
	var numberList = new Array(6);
	var color = '';

	numberList[0] = '00';
	numberList[1] = '40';
	numberList[2] = '80';
	numberList[3] = 'BF';
	numberList[4] = 'FF';

	document.writeln('<table cellspacing="1" cellpadding="0" border="0">');

	for (r = 0; r < 5; r++)
	{
		if (dir == 'h')
		{
			document.writeln('<tr>');
		}

		for (g = 0; g < 5; g++)
		{
			if (dir == 'v')
			{
				document.writeln('<tr>');
			}
			
			for (b = 0; b < 5; b++)
			{
				color = String(numberList[r]) + String(numberList[g]) + String(numberList[b]);
				document.write('<td bgcolor="#' + color + '" style="width: ' + width + 'px; height: ' + height + 'px;">');
				document.write('<a href="#" onclick="bbfontstyle(\'[color=#' + color + ']\', \'[/color]\'); return false;"><img src="images/spacer.gif" width="' + width + '" height="' + height + '" alt="#' + color + '" title="#' + color + '" /></a>');
				document.writeln('</td>');
			}

			if (dir == 'v')
			{
				document.writeln('</tr>');
			}
		}

		if (dir == 'h')
		{
			document.writeln('</tr>');
		}
	}
	document.writeln('</table>');
}


/**
* Caret Position object
*/
function caretPosition()
{
	var start = null;
	var end = null;
}


/**
* Get the caret position in an textarea
*/
function getCaretPosition(txtarea)
{
	var caretPos = new caretPosition();
	
	// simple Gecko/Opera way
	if(txtarea.selectionStart || txtarea.selectionStart == 0)
	{
		caretPos.start = txtarea.selectionStart;
		caretPos.end = txtarea.selectionEnd;
	}
	// dirty and slow IE way
	else if(document.selection)
	{
		// get current selection
		var range = document.selection.createRange();

		// a new selection of the whole textarea
		var range_all = document.body.createTextRange();
		range_all.moveToElementText(txtarea);
		
		// calculate selection start point by moving beginning of range_all to beginning of range
		var sel_start;
		for (sel_start = 0; range_all.compareEndPoints('StartToStart', range) < 0; sel_start++)
		{		
			range_all.moveStart('character', 1);
		}
	
		txtarea.sel_start = sel_start;
	
		// we ignore the end value for IE, this is already dirty enough and we don't need it
		caretPos.start = txtarea.sel_start;
		caretPos.end = txtarea.sel_start;
	}

	return caretPos;
}
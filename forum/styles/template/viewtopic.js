function quote (post_id, username, forum){
	var append= "./posting.php?mode=";
	if (storeQuote (post_id, username, "")){
		append += "select&f=" + forum + "&p=" + post_id;
	}	
	else{
		append += "quote&f=" + forum + "&p=" + post_id;
	}
	window.location = append;
}



//checks for quote by selection, if used saves the selected text to a cookie and returns true, if not used or used incorrectly returns false.
function storeQuote (post_id, username, l_wrote){
	var message_name = 'p' + post_id;
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
		return false;
	}
	var cookiecontent = username + 'NqLN5jsRe24krOHFSruT';
	//output
	for (var count=0;count<theSelections.length ;count++)
	{
		cookiecontent += theSelections[count] + "NqLN5jsRe24krOHFSruT";
	}
	setCookie("ugEvYSDJEOAz2bHadHvOPOST_"+post_id,cookiecontent, 1);
	return true;
}

function postNotContains(post_id, text){
	var message_name = 'p' + post_id;
	var divarea = false;
	var content = '';
	if (document.all)
	{
		divarea = document.all[message_name];
	}
	else
	{
		divarea = document.getElementById(message_name);
	}
	var array = divarea.getElementsByTagName("div");
	content = array[5].innerHTML;
	return (stripWhiteSpace(stripHTML(content)).indexOf(stripWhiteSpace(text), 0)==-1);
}


//sets the cookie 'c_name' to 'value'
function setCookie(c_name,value,exdays)
{
	var exdate=new Date();
	exdate.setDate(exdate.getDate() + exdays);
	var c_value=escape(value) + ((exdays==null) ? "" : "; expires="+exdate.toUTCString());
	document.cookie=c_name + "=" + c_value;
}
//Strips HTML from a string
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

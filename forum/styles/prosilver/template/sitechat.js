var client = null;
var defaultAvatar = './styles/prosilver/imageset/defaultAvatar.png';
function supportsHtml5Storage()
{
	try
	{
		return 'localStorage' in window && window['localStorage'] !== null;
	}
	catch (e)
	{
		return false;
  	}
}

function zeroFill( number, width )
{
	width -= number.toString().length;
	return (width > 0) ? (new Array( width + 1 ).join( '0' ) + number) : number.toString();
}
function startsWith(sourceStr, subStr)
{
	if(subStr.length > sourceStr.length)
		return false;
	
	for(var index in subStr)
	{
		if(subStr[index] != sourceStr[index])
			return false;
	}
	return true;
}

function ChatWindow()
{
	this.id = undefined;
	this.title = undefined;
	this.expanded = false;
	this.userIdSet = [];
	this.blinking = false;
	this.userIsInChat = function(userId)
	{
		return $.inArray(userId, this.userIdSet) != -1;
	}
}

function Client()
{
	this.chatWindows = new Object();//Associative array
	this.userMap = new Object();//Associative array
	this.socket = null;
	this.userId = null;
	this.sessionId = null;
	this.pendingMessages = [];
	this.namespace = "ms_sc_";//Prepended to all local storage variables.
	this.attemptReconnectIntervalId = null;
	this.MAX_MESSAGES_PER_WINDOW = 500;
	this.attemptReconnect = function()
	{
		console.log("Attempting reconnect...");
		client.setupWebSocket();
	}
	
	this.clearLocalStorage = function()
	{
		for(var key in localStorage)
		{
			if(startsWith(key, client.namespace))
				localStorage.removeItem(key);
		}
	}
	
	this.loadFromLocalStorage = function()
	{
		var userIdSet, converstionIdSet;
		//Load users.
		if(localStorage[client.namespace + "userIdSet"])
		{
			userIdSet = JSON.parse(localStorage[client.namespace + "userIdSet"]);
			for(var index = 0;index < userIdSet.length;++index)
			{
				var userId = userIdSet[ index ];

				if(localStorage[client.namespace + "user" + userId])
				{
					try {
						var siteChatUser = JSON.parse(localStorage[client.namespace + "user" + userId]);
						client.addUser(siteChatUser, false);
					}
					catch(err)
					{
						console.log("Could not load user from localStorage: " + err);
					}
				}
			}
		}

		//Load conversations.
		if(localStorage[client.namespace + "conversationIdSet"])
		{
			conversationIdSet = JSON.parse(localStorage[client.namespace + "conversationIdSet"]);
			for(var index = 0;index < conversationIdSet.length;++index)
			{
				var siteChatConversationId = conversationIdSet[ index ];

				console.log("Loading Conversation: " + localStorage[client.namespace + "conversation" + siteChatConversationId]);
				var siteChatConversation = JSON.parse(localStorage[client.namespace + "conversation" + siteChatConversationId]);
				client.createChatWindow(siteChatConversationId, siteChatConversation.title, siteChatConversation.userIdSet, siteChatConversation.expanded, siteChatConversation.messages, false);
			}
		}
	}

	this.handleWindowTitleClick = function(event)
	{
		event.preventDefault();
		event.stopPropagation();
		var $window = $(this).closest(".chatWindow");
		var siteChatConversation = client.chatWindows[ parseInt($window.attr("id").replace("chat", "")) ];
		var $title = $(window).find(".title");
		if($window.hasClass("expanded"))
		{
			$window.removeClass("expanded");
			$window.addClass("collapsed");
			$title.stop(true);
			$title.css('backgroundColor', '');
			if(siteChatConversation)
				siteChatConversation.expanded = false;
		}
		else
		{
			$window.removeClass("collapsed");
			$window.addClass("expanded");
			$title.stop(true);
			$title.css('backgroundColor', '');
			$window.show();
			if(siteChatConversation){
				siteChatConversation.expanded = true;
				var outputbuffer = $("#chat" + siteChatConversation.siteChatConversationId + " .outputBuffer");
				outputbuffer.scrollTop(outputbuffer[0].scrollHeight);
			}
		}
		if (siteChatConversation != null && siteChatConversation.blinking == true){
			siteChatConversation.blinking = false;
		}
		if(siteChatConversation)
			client.saveChatWindow(siteChatConversation);
	}

	this.handleWindowCloseButtonClick = function(event)
	{
		event.preventDefault();
		event.stopPropagation();

		var $window = $(this).closest(".chatWindow");
		var conversationId = parseInt($window.attr("id").replace("chat", ""));

		var siteChatPacket = new Object();
		siteChatPacket.command = "LeaveConversation";
		siteChatPacket.siteChatConversationId = conversationId;
		
		client.sendSiteChatPacket(siteChatPacket);

		//Remove window from DOM
		$window.remove();

		if(client.chatWindows[ conversationId ])
			delete client.chatWindows[ conversationId ];

		if(localStorage[client.namespace + "conversation" + conversationId])
			localStorage.removeItem(client.namespace + "conversation" + conversationId);

		localStorage[client.namespace + "conversationIdSet"] = JSON.stringify(client.getConversationIdSet());
	}

	this.handleWindowInputSubmission = function(event)
	{
		if(event.which == 10 || event.which == 13)
		{
			event.preventDefault();

			var $window = $(this).closest(".chatWindow");
			var content = $(this).val();
			var chatWindowId = $window.attr("id");

			if(content.length > 0)
			{
				client.submitChatMessage(content, chatWindowId);
			}

			$(this).val("");
		}
	}

	this.getConversationIdSet = function()
	{
		var conversationIdSet = [];
		for(var conversationId in client.chatWindows)
		{
			conversationIdSet.push(parseInt(conversationId));
		}

		return conversationIdSet;
	}

	this.submitChatMessage = function(messageContent, chatWindowId)
	{
		$window = $("#" + chatWindowId);

		var siteChatPacket = new Object();
		siteChatPacket.command = "SendMessage";
		siteChatPacket.userId = client.userId;
		siteChatPacket.message = messageContent;
		siteChatPacket.siteChatConversationId = parseInt(chatWindowId.replace("chat", ""));

		this.sendSiteChatPacket(siteChatPacket);
	}
	
	this.handleSocketOpen = function()
	{
		console.log("Socket Open.");
		if(client.attemptReconnectIntervalId != null)
		{
			window.clearInterval(client.attemptReconnectIntervalId);
			client.attemptReconnectIntervalId = null;
			
			console.log("Reconnect Interval Cleared.");
		}
		$("#utilitywindow .exclamation").addClass("hidden");
		var siteChatPacket = new Object();
		siteChatPacket.command = "LogIn";
		siteChatPacket.userId = client.userId;
		siteChatPacket.sessionId = client.sessionId;
		siteChatPacket.conversationIdSet = client.getConversationIdSet();
		siteChatPacket.conversationIdToMostRecentMessageIdMap = new Object();
		
		for(var siteChatConversationId in client.chatWindows)
		{
			console.log("Scanning Conversation #" + siteChatConversationId);
			var chatWindow = client.chatWindows[ siteChatConversationId ];
			if(chatWindow.messages && chatWindow.messages.length > 0)
			{
				console.log(" - Adding Message #" + chatWindow.messages[ chatWindow.messages.length - 1 ].id);
				siteChatPacket.conversationIdToMostRecentMessageIdMap[ siteChatConversationId ] = chatWindow.messages[ chatWindow.messages.length - 1 ].id;
			}
		}
	
		client.sendSiteChatPacket(siteChatPacket);
	}
	
	this.handleSocketClose = function()
	{
		if(client.attemptReconnectIntervalId != null)
			window.clearInterval(client.attemptReconnectIntervalId);
		client.attemptReconnectIntervalId = setInterval(client.attemptReconnect, 15000);
		$("#utilitywindow .exclamation").removeClass("hidden");
	}

	this.createChatWindow = function(conversationId, title, userIdSet, expanded, messages, save)
	{
		$("#chatPanel").append
			(
				'<div class="chatWindow collapsed" id="chat' + conversationId + '">'
			+	'	<div class="chatWindowInner">'
			+	'		<div class="title">' + title + '<div class="close">X</div></div>'
			+	'		<div class="outputBuffer"></div>'
			+	'		<input class="inputBuffer" type="text" name="input"></input>'
			+	'	</div>'
			+	'</div>'
			);

		$("#chat" + conversationId + " .title").bind("click", client.handleWindowTitleClick);
		$("#chat" + conversationId + " .inputBuffer").bind("keypress", client.handleWindowInputSubmission);
		$("#chat" + conversationId + " .title .close").bind("click", client.handleWindowCloseButtonClick);
		
		var chatWindow = new ChatWindow();
		chatWindow.siteChatConversationId = conversationId;
		chatWindow.userIdSet = [];
		chatWindow.title = title;
		chatWindow.messages = [];
		
		if(expanded != undefined)
			chatWindow.expanded = expanded;
			
		if(chatWindow.expanded)
		{
			$("#chat" + conversationId).addClass("expanded");
			$("#chat" + conversationId).removeClass("collapsed");
		}
		else
		{
			$("#chat" + conversationId).addClass("collapsed");
			$("#chat" + conversationId).removeClass("expanded");
		}

		client.chatWindows[conversationId] = chatWindow;
		if(messages && messages.length > 0)
		{
			var messageArrayLength = messages.length;
			for(var messageIndex = 0;messageIndex < messageArrayLength;++messageIndex)
			{
				client.addSiteChatConversationMessage(messages[ messageIndex ], save, false);
			}
		}
		
		if(save){
			$("#chat" + conversationId + " .inputBuffer").focus();
		} else {
			$("#chat" + conversationId + " .outputBuffer").scrollTop($("#chat" + conversationId + " .outputBuffer").scrollHeight);
		}
		
		if(save)
		{
			client.saveChatWindow(chatWindow);
		}
		
	}

	this.saveChatWindow = function(chatWindow)
	{
		console.log("SAVING CHAT WINDOW: " + chatWindow);
		localStorage[client.namespace + "conversationIdSet"] = JSON.stringify(client.getConversationIdSet());
		localStorage[client.namespace + "conversation" + chatWindow.siteChatConversationId] = JSON.stringify(chatWindow);
	}
	
	this.addSiteChatConversationMessage = function(siteChatConversationMessage, save, isNew)
	{
		console.log("Adding Message #" + siteChatConversationMessage.id);
		var chatWindow = client.chatWindows[ siteChatConversationMessage.siteChatConversationId ];
		var siteChatUser = client.userMap[ siteChatConversationMessage.userId ];
		var messageDate = new Date(siteChatConversationMessage.createdDatetime);//TODO: Time zone differences
		
		var messageDateString = zeroFill(messageDate.getHours(), 2) + ":" + zeroFill(messageDate.getMinutes(), 2);
		
		chatWindow.messages.push(siteChatConversationMessage);
		var messagesLength = chatWindow.messages.length;
		if(messagesLength > client.MAX_MESSAGES_PER_WINDOW)
		{
			console.log("Message Length: " + messagesLength);
			chatWindow.messages.splice(0, messagesLength - client.MAX_MESSAGES_PER_WINDOW);
			console.log("Truncated to: " + chatWindow.messages.length);
		}
		if (siteChatUser.avatarUrl != ''){
			avatarUrl = 'http://forum.mafiascum.net/download/file.php?avatar=' + siteChatUser.avatarUrl;
		}
		else {
			avatarUrl = defaultAvatar;
		}
		$("#chat" + siteChatConversationMessage.siteChatConversationId + " .outputBuffer").append
		(
				'<div class="message">'
			+	'	<img src="' + avatarUrl + '" class="profile"></img>'
			+	'	<div class="messageUserName">' + siteChatUser.name + '</div> <span class="messageTimestamp">(' + messageDateString + ')</span>'
			+	'	<div class="messagecontent">' + siteChatConversationMessage.message + '</div>'
			+	'</div>'
		);
		if (chatWindow.expanded == false && isNew){
			chatWindow.blinking = true;
		}
		var outputBuffer = $("#chat" + siteChatConversationMessage.siteChatConversationId + " .outputBuffer").get(0);
		outputBuffer.scrollTop = outputBuffer.scrollHeight;
		if(save)
			client.saveChatWindow(chatWindow);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	this.printUsers = function()
	{
		for(var userId in client.userMap)
		{
			console.log(" - " + userId + ": " + client.userMap[ userId ].name + ", " + client.userMap[ userId ].avatarUrl);
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	this.addUser = function(siteChatUser, save)
	{
		console.log("ADDING USER #" + siteChatUser.id + ": " + siteChatUser.name + ", Save: " + save);
		if ( client.userMap[ siteChatUser.id ] == null){
			client.userMap[ siteChatUser.id ] = siteChatUser;
			client.addUserToOnlineList(siteChatUser);
			
			if(save)
			{
				client.saveUser(siteChatUser);
			}
		}
	}
	
	this.addUserToOnlineList = function(siteChatUser)
	{
		$("#onlinelist").append
		(
			'<li class="username" id="username' + siteChatUser.id + '">'
		+	'	<a href="#" onClick="return false;">' + siteChatUser.name + '</a>'
		+	'</li>'
		);
	}

	this.saveUser = function(siteChatUser)
	{
		var userIdSet = [];
		for(var userId in client.userMap)
		{
			userIdSet.push(parseInt(userId));
		}

		localStorage[client.namespace + "userIdSet"] = JSON.stringify(userIdSet);
		localStorage[client.namespace + "user" + siteChatUser.id] = JSON.stringify(siteChatUser);
	}

	this.processPendingMessages = function()
	{
		console.log("Processing Pending Messages... Total In Queue: " + client.pendingMessages.length);
		for(var index = 0;index < client.pendingMessages.length;++index)
		{
			var siteChatConversationMessage = client.pendingMessages[ index ];
			var siteChatUser = client.userMap[ siteChatConversationMessage.userId ];

			if(!siteChatUser)
			{
				console.log("Still could not process pending message. User ID: " + siteChatConversationMessage.userId);
				break;
			}

			client.addSiteChatConversationMessage(siteChatConversationMessage, true, true);
			client.pendingMessages.splice(index, 1);
			--index;
		}
	}	

	this.handleSocketMessage = function(message)
	{
		var data = message.data;
		var siteChatPacket = JSON.parse(data);

		console.log("RECEIVING: " + data);
		if(siteChatPacket.command == "LogIn")
		{
			console.log("Log In Result: " + siteChatPacket.wasSuccessful);
			
			if(siteChatPacket.missedSiteChatConversationMessages && siteChatPacket.missedSiteChatConversationMessages.length > 0)
			{
				var missedMessagesLength = siteChatPacket.missedSiteChatConversationMessages.length;
				for(var messageIndex = 0;messageIndex < missedMessagesLength;++messageIndex)
				{
					console.log("Adding Missed Message. ID: " + siteChatPacket.missedSiteChatConversationMessages[ messageIndex ].id);
					client.addSiteChatConversationMessage(siteChatPacket.missedSiteChatConversationMessages[ messageIndex ], true, true);
				}
			}
		}
		else if(siteChatPacket.command == "Connect")
		{
			if(client.chatWindows[siteChatPacket.siteChatConversationId] == undefined)
			{//Create the chat window.

				var siteChatUserIdSet = [];
				for(var siteChatUserIndex = 0;siteChatUserIndex < siteChatPacket.users.length;++siteChatUserIndex)
				{
					var siteChatUser = siteChatPacket.users[ siteChatUserIndex ];
					siteChatUserIdSet.push(siteChatUser.id);

					client.addUser(siteChatUser, true);
				}

				client.createChatWindow(siteChatPacket.siteChatConversationId, siteChatPacket.titleText, siteChatUserIdSet, true, [], true);
			}
		}
		else if(siteChatPacket.command == "NewMessage")
		{
			if(client.chatWindows[ siteChatPacket.siteChatConversationMessage.siteChatConversationId ] != undefined)
			{
				var siteChatUser = client.userMap[ siteChatPacket.siteChatConversationMessage.userId ];

				if(!siteChatUser || client.pendingMessages.length > 0)
				{//If for some reason we have no data on a user(or if there are other pending messages), queue the message to process later and kick off a user lookup request.
					console.log("Adding pending message. Site Chat User: " + siteChatUser + ", Previous Pending Messages: " + client.pendingMessages.length);
					client.pendingMessages.push(siteChatPacket.siteChatConversationMessage);

					if(!siteChatUser)
					{
						var lookupUserPacket = new Object();
						lookupUserPacket.command = "LookupUser";
						lookupUserPacket.userId = siteChatPacket.siteChatConversationMessage.userId;

						client.sendSiteChatPacket(lookupUserPacket);
					}
				}
				else
				{
					console.log("Preparing to add NewMessage siteChatConversationMessage.");
					client.addSiteChatConversationMessage(siteChatPacket.siteChatConversationMessage, true, true);
				}
			}
		}
		else if(siteChatPacket.command == "UserJoin")
		{
			if(client.userMap[ siteChatPacket.siteChatUser.id ] == undefined)
				client.addUser(siteChatPacket.siteChatUser, true);
			
			if(client.chatWindows[ siteChatPacket.siteChatConversationId ] != undefined)
				client.chatWindows[ siteChatPacket.siteChatConversationId ].userIdSet.push( siteChatPacket.siteChatUser.id );
		}
		else if(siteChatPacket.command == "LookupUser")
		{
			if(!siteChatPacket.siteChatUser)
			{
				console.log("Lookup for user #" + siteChatPacket.userId + " returned no result. Removing corresponding pending messages.");
				for(var index = 0;index < client.pendingMessages.length;++index)
				{
					if(client.pendingMessages[index].userId = siteChatPacket.userId)
					{
						client.pendingMessages.splice(index, 1);
						--index;
					}
				}
			}
			else
			{
				console.log("Adding User From Lookup. User ID: " + siteChatPacket.siteChatUser.id + ", Name: " + siteChatPacket.siteChatUser.name);
				client.addUser(siteChatPacket.siteChatUser, true);
				client.processPendingMessages();
			}
		}
		else if(siteChatPacket.command == "LeaveConversation")
		{
			console.log("Removing User #" + siteChatPacket.userId + " From Conversation #" + siteChatPacket.siteChatConversationId);
			var chatWindow = client.chatWindows[ siteChatPacket.siteChatConversationId ];
			if(chatWindow)
				chatWindow.userIdSet.splice($.inArray(siteChatPacket.userId, chatWindow.userIdSet), 1);
		}
		else if(siteChatPacket.command == "UserList")
		{
			console.log("Got list of users: " + siteChatPacket.siteChatUsers);
			var siteChatUserListLength = siteChatPacket.siteChatUsers.length;
			
			$("#onlinelist").html("");
			for(var siteChatUserIndex = 0;siteChatUserIndex < siteChatUserListLength;++siteChatUserIndex)
			{
				client.addUserToOnlineList(siteChatPacket.siteChatUsers[ siteChatUserIndex ]);
			}
		}
	}

	this.sendSiteChatPacket = function(packetObject)
	{
		var json = JSON.stringify(packetObject);
		console.log("SENDING: " + json);
		client.socket.send(json);
	}

	this.createChatPanel = function()
	{
		$("body").append("<div class='chatPanel' id='chatPanel'></div>");
	}
	this.createUtilityWindow = function()
	{
		$("#chatPanel").prepend
				(
					'<div class="chatWindow collapsed" id="utilitywindow">'
				+	'	<div class="chatWindowInner">'
				+	'		<div class="title">' + 'Join Chat' + '<div class="exclamation hidden">!</div></div>'
				+	'		<p id="onlinelisttitle">Online Users</p>'
				+	'		<ul id="onlinelist"></ul>'
				+	'		<div id="joindiv"><label>Chatroom:</label><input id="joinconversationinput" type="text" name="input"></input></div>'
				+	'	</div>'
				+	'</div>'
				);
		$("#utilitywindow .title").bind("click", client.handleWindowTitleClick);
		$("#utilitywindow .inputBuffer").bind("keypress", client.handleWindowInputSubmission);
		$('#joinconversationinput').keyup(function(e) {
				if(e.keyCode == 13 || e.keyCode == 10) {
					  var siteChatPacket = new Object(); 
					  siteChatPacket.command = "Connect";
					  siteChatPacket.siteChatConversationName = $("#joinconversationinput").val();
					  $("#joinconversationinput").val('');
					  client.sendSiteChatPacket(siteChatPacket);
				}
		});
	}
	this.heartbeat = function(){
		var siteChatPacket = new Object();
		siteChatPacket.command = "Heartbeat";
		siteChatPacket.isAlive = "true";
		client.sendSiteChatPacket(siteChatPacket);
	}
	this.blink = function(){
		for(var siteChatConversationId in client.chatWindows) {
			if(client.blinkstate == 0){
				if (client.chatWindows[siteChatConversationId].blinking == true){
					$('#chat' + siteChatConversationId + ' .title').animate({backgroundColor: '#F09B3C'}, 699);
					client.blinkstate = 1;
				}
			} else {
				if (client.chatWindows[siteChatConversationId].blinking == true){
					$('#chat' + siteChatConversationId + ' .title').animate({backgroundColor: '#E1DFE6'}, 699);
					client.blinkstate = 0;
				}
			}
		}
	}
	this.setup = function(sessionId, userId)
	{
		client.sessionId = sessionId;
		client.userId = userId;
		if(!supportsHtml5Storage() || typeof(WebSocket) != "function")
		{
			return;
		}
		
		//Before doing anything, ensure that the user that will attempt to connect is the same one for which we have localstorage data.
		if(localStorage[client.namespace + "userId"] == null || parseInt(localStorage[client.namespace + "userId"]) != client.userId)
		{
			console.log("Current User ID `" + client.userId + "` does not match stored user ID `" + localStorage[client.namespace + "userId"] + "`");
			console.log("Clearing local storage cache...");
			client.clearLocalStorage();
			localStorage[client.namespace + "userId"] = client.userId;
		}
		
		client.setupWebSocket();

		client.createChatPanel();
		client.createUtilityWindow();
		client.loadFromLocalStorage();
		setInterval('client.blink()', 700);
		setInterval('client.heartbeat()', 150000);
	}
	
	this.setupWebSocket = function()
	{
		client.socket = new WebSocket("ws://localhost:4241", "site-chat");
		client.socket.onopen = client.handleSocketOpen;
		client.socket.onclose = client.handleSocketClose;
		client.socket.onmessage = client.handleSocketMessage;
	}
}
var client = null;
var defaultAvatar = './styles/prosilver/imageset/defaultAvatar.png';
function supportsHtml5Storage()
{
	try{return 'localStorage' in window && window['localStorage'] !== null;}
	catch(e){return false;}
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
}

function Client()
{
	this.chatWindows = new Object();//Associative array
	this.userMap = new Object();//Associative array
	this.socket = null;
	this.userId = null;
	this.sessionId = null;
	this.pendingMessages = [];
	this.namespace = "ms_sc2_";//Prepended to all local storage variables.
	this.attemptReconnectIntervalId = null;
	this.onlineUserIdSet = [];
	this.MAX_MESSAGES_PER_WINDOW = 500;
	this.unloading = false;
	this.firstConnectionThisPageLoad = true;
	this.onlineUsers = 0;
	this.attemptReconnect = function()
	{
		client.setupWebSocket();
	}
	
	this.parseBBCode = function(message)
	{
		return message	.replace(/\[b\](.*?)\[\/b\]/g, "<b>$1</b>")
				.replace(/\[i\](.*?)\[\/i\]/g, "<i>$1</i>")
				.replace(/\[u\](.*?)\[\/u\]/g, "<u>$1</u>");
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
						//var addToOnlineUserList = _.contains(client.onlineUserIdSet, siteChatUser.id);
						client.addUser(siteChatUser, false, true);
					}
					catch(err)
					{
						console.log("Could not load user from localStorage: " + err);
					}
				}
			}
		}
		//Load online user ID set.
		if(localStorage[client.namespace + "onlineUserIdSet"])
		{
			onlineUserIdSet = JSON.parse(localStorage[client.namespace + "onlineUserIdSet"]);
			for(var index = 0;index < onlineUserIdSet.length;++index)
			{
				client.addUserToOnlineList(client.userMap[onlineUserIdSet[index]], true);
			}
		}

		//Load conversations.
		if(localStorage[client.namespace + "conversationIdSet"])
		{
			conversationIdSet = JSON.parse(localStorage[client.namespace + "conversationIdSet"]);
			for(var index = 0;index < conversationIdSet.length;++index)
			{
				var recipientUserId = null, conversationId = null, key = conversationIdSet[index];
				if(typeof key == "string")
				{
					if(key[0] == "C")
						conversationId = parseInt(key.substring(1));
					else if(key[0] == "P")
						recipientUserId = parseInt(key.substring(1));
				}
				else
					conversationId = parseInt(key);//Support old format.

				var siteChatConversation = JSON.parse(localStorage[client.namespace + "conversation" + key]);
				client.createChatWindow(conversationId, recipientUserId, siteChatConversation.title, siteChatConversation.userIdSet, siteChatConversation.expanded, siteChatConversation.messages, false);
			}
		}
	}

	this.handleUserListUsernameClick = function(event)
	{
		event.preventDefault();
		event.stopPropagation();
		
		var recipientUserId = parseInt($(this).data("user-id"));
		if(client.chatWindows["P" + recipientUserId] == undefined)
			client.createChatWindow(null, recipientUserId, $(this).data("username"), [], true, [], true);
	}
	
	this.handleWindowTitleClick = function(event)
	{
		event.preventDefault();
		event.stopPropagation();
		var $window = $(this).closest(".chatWindow");
		var siteChatConversationId = $window.data("conversation-id");
		var recipientUserId = $window.data("recipient-user-id");
		var windowKey = siteChatConversationId != null ? ("C" + siteChatConversationId) : ("P" + recipientUserId);
		var siteChatConversation = client.chatWindows[ windowKey ];
		var $title = $window.find(".title");
		
		if (siteChatConversation != null && siteChatConversation.blinking == true)
			siteChatConversation.blinking = false;
		
		$title.stop(true);
		$title.css('backgroundColor', '');

		if($window.hasClass("expanded"))
		{
			$window.removeClass("expanded").addClass("collapsed");
			if(siteChatConversation)
				siteChatConversation.expanded = false;
			else
				sessionStorage[client.namespace + "utilityExpanded"] = false;
		}
		else
		{
			$window.removeClass("collapsed").addClass("expanded").show();
			if(siteChatConversation){
				siteChatConversation.expanded = true;
				var outputbuffer = $("#chat" + windowKey + " .outputBuffer");
				outputbuffer.scrollTop(outputbuffer[0].scrollHeight);
			}
			else
				sessionStorage[client.namespace + "utilityExpanded"] = true;
		}
		if(siteChatConversation)
			client.saveChatWindow(siteChatConversation);
	}

	this.handleWindowCloseButtonClick = function(event)
	{
		event.preventDefault();
		event.stopPropagation();

		var $window = $(this).closest(".chatWindow");
		var conversationId = $window.data("conversation-id");
		var recipientUserId = $window.data("recipient-user-id");
		var uniqueIdentifier = conversationId != null ? parseInt(conversationId) : parseInt(recipientUserId);
		var windowKey = (conversationId != null ? "C" : "P") + uniqueIdentifier;

		if(conversationId != null)
		{
			var siteChatPacket =
			{
				command:"LeaveConversation",
				siteChatConversationId:conversationId
			};
			client.sendSiteChatPacket(siteChatPacket);
		}

		//Remove window from DOM
		$window.remove();

		var conversation = client.chatWindows[ windowKey ];
		if(conversation)
		{
			if(conversationId != null && conversation.title == "Lobby")
				sessionStorage[client.namespace + "lobbyForcefullyClosed"] = true;
			delete client.chatWindows[ windowKey ];
		}
		if(localStorage[client.namespace + "conversation" + windowKey])
			localStorage.removeItem(client.namespace + "conversation" + windowKey);

		localStorage[client.namespace + "conversationIdSet"] = JSON.stringify(client.getConversationKeySet());
	}

	this.handleWindowInputSubmission = function(event)
	{
		if(event.which == 10 || event.which == 13)
		{
			event.preventDefault();

			var $window = $(this).closest(".chatWindow");
			var content = $(this).val();
			var chatWindowId = $window.attr("id");
			var conversationId = $window.data("conversation-id");
			var recipientUserId = $window.data("recipient-user-id");

			if(content.length > 0)
			{
				client.submitChatMessage(content, chatWindowId, conversationId, recipientUserId);
			}

			$(this).val("");
		}
	}

	this.getConversationKeySet = function()
	{
		var conversationKeySet = [];
		for(var conversationKey in client.chatWindows)
		{
			conversationKeySet.push(conversationKey);
		}

		return conversationKeySet;
	}

	this.submitChatMessage = function(messageContent, chatWindowId, conversationId, recipientUserId)
	{
		var siteChatPacket =
		{
			command:"SendMessage",
			userId:client.userId,
			message:messageContent,
			siteChatConversationId:conversationId == null ? null : parseInt(conversationId),
			recipientUserId:recipientUserId == null ? null : parseInt(recipientUserId)
		};
		this.sendSiteChatPacket(siteChatPacket);
	}
	
	this.handleSocketOpen = function()
	{
		console.log("Socket Opened.");
		client.socket.connected = true;
		if(client.attemptReconnectIntervalId != null)
		{
			window.clearInterval(client.attemptReconnectIntervalId);
			client.attemptReconnectIntervalId = null;
		}
		$("#utilitywindow .exclamation").addClass("hidden");
		var siteChatPacket =
		{
			command:"LogIn",
			userId:client.userId,
			sessionId:client.sessionId,
			conversationKeySet:client.getConversationKeySet(),
			conversationKeyToMostRecentMessageIdMap:new Object()
		};
		
		for(var conversationKey in client.chatWindows)
		{
			var chatWindow = client.chatWindows[ conversationKey ];
			if(chatWindow.messages && chatWindow.messages.length > 0)
			{
				siteChatPacket.conversationKeyToMostRecentMessageIdMap[ conversationKey ] = chatWindow.messages[ chatWindow.messages.length - 1 ].id;
			}
		}
	
		client.sendSiteChatPacket(siteChatPacket);
	}
	
	this.handleSocketClose = function()
	{
		console.log("Web Socket Closed.");
		client.socket.connected = false;
		if(!client.unloading)
		{
			if(client.attemptReconnectIntervalId != null)
				window.clearInterval(client.attemptReconnectIntervalId);
			client.attemptReconnectIntervalId = setInterval(client.attemptReconnect, 15000);
			$("#utilitywindow .exclamation").removeClass("hidden");
		}
	}

	this.createChatWindow = function(conversationId, recipientUserId, title, userIdSet, expanded, messages, save)
	{
		var chatWindowIdPrefix = (conversationId != null ? "C" : "P");
		var chatWindowUniqueIdentifier = (conversationId != null ? conversationId : recipientUserId);
		$("#chatPanel").append
			(
				'<div class="chatWindow expanded" id="chat' + chatWindowIdPrefix + chatWindowUniqueIdentifier + '">'
			+	'	<div class="chatWindowInner">'
			+	'		<div class="title"><div class="name">' + title + '</div><div class="close">X</div></div>'
			+	'		<div class="outputBuffer"></div>'
			+	'		<textarea class="inputBuffer" name="input" style="height:20px;"></textarea>'
			+	'	</div>'
			+	'</div>'
			);

		var $chatWindow = $("#chat" + chatWindowIdPrefix + chatWindowUniqueIdentifier);
		var $inputBuffer = $chatWindow.find(".inputBuffer");
		var $outputBuffer = $chatWindow.find(".outputBuffer");
		
		if(conversationId != null)
			$chatWindow.data("conversation-id", conversationId);
		if(recipientUserId != null)
			$chatWindow.data("recipient-user-id", recipientUserId);
		
		//Window defaults to an expanded state so autogrow can see the proper CSS values.
		$inputBuffer.autoGrow();
		//We now collapse it.
		$inputBuffer.removeClass("expanded").addClass("collapsed");
		
		var chatWindow = new ChatWindow();
		chatWindow.siteChatConversationId = conversationId;
		chatWindow.recipientUserId = recipientUserId;
		chatWindow.userIdSet = [];
		chatWindow.title = title;
		chatWindow.messages = [];
		
		if(expanded != undefined)
			chatWindow.expanded = expanded;
			
		if(chatWindow.expanded)
		{
			$chatWindow.addClass("expanded").removeClass("collapsed");
		}
		else
		{
			$chatWindow.addClass("collapsed").removeClass("expanded");
		}

		client.chatWindows[chatWindowIdPrefix + chatWindowUniqueIdentifier] = chatWindow;
		if(messages && messages.length > 0)
		{
			var messageArrayLength = messages.length;
			for(var messageIndex = 0;messageIndex < messageArrayLength;++messageIndex)
			{
				var message = messages[messageIndex];
				if(!client.userMap[message.userId])
					console.log("User Not In Map(" + message.userId + ") when creating chat window. Conversation #" + chatWindowIdPrefix + chatWindowUniqueIdentifier);
				else
					client.addSiteChatConversationMessage(message, save, false);
			}
		}
		
		if(save){
			$inputBuffer.focus();
		} else {
			$outputBuffer.scrollTop($outputBuffer.scrollHeight);
		}
		
		if(save)
		{
			client.saveChatWindow(chatWindow);
		}
		
	}

	this.saveChatWindow = function(chatWindow)
	{
		localStorage[client.namespace + "conversationIdSet"] = JSON.stringify(client.getConversationKeySet());
		localStorage[client.namespace + "conversation" + client.getWindowMapKey(chatWindow)] = JSON.stringify(chatWindow);
	}
	
	this.getMessageMapKeyUserId = function(siteChatConversationMessage)
	{
		return (siteChatConversationMessage.recipientUserId == client.userId ? siteChatConversationMessage.userId : siteChatConversationMessage.recipientUserId);
	}
	this.getMessageMapKey = function(siteChatConversationMessage)
	{
		return siteChatConversationMessage.recipientUserId != null ? ("P" + client.getMessageMapKeyUserId(siteChatConversationMessage)) : ("C" + siteChatConversationMessage.siteChatConversationId);
	}
	
	this.getWindowMapKey = function(chatWindow)
	{
		return chatWindow.recipientUserId != null ? ("P" + chatWindow.recipientUserId) : ("C" + chatWindow.siteChatConversationId);
	}
	
	this.addSiteChatConversationMessage = function(siteChatConversationMessage, save, isNew)
	{
		var messageKey = client.getMessageMapKey(siteChatConversationMessage);
		var siteChatUser = client.userMap[ siteChatConversationMessage.userId ];
		var messageDate = new Date(siteChatConversationMessage.createdDatetime);
		
		if(!client.chatWindows[messageKey] && siteChatConversationMessage.recipientUserId != null)
		{//If this is a private conversation & the window has not yet been created, we should have enough information to make it ourselves.
			client.createChatWindow(null, siteChatUser.id, siteChatUser.name, [siteChatUser.id], true, [], true);
		}
		var chatWindow = client.chatWindows[ messageKey ];
		
		var messageDateString = zeroFill(messageDate.getHours(), 2) + ":" + zeroFill(messageDate.getMinutes(), 2);
		
		chatWindow.messages.push(siteChatConversationMessage);
		var messagesLength = chatWindow.messages.length;
		if(messagesLength > client.MAX_MESSAGES_PER_WINDOW)
		{
			chatWindow.messages.splice(0, messagesLength - client.MAX_MESSAGES_PER_WINDOW);
		}
		if (siteChatUser.avatarUrl != ''){
			avatarUrl = 'http://forum.mafiascum.net/download/file.php?avatar=' + siteChatUser.avatarUrl;
		}
		else {
			avatarUrl = defaultAvatar;
		}
		 
		var $outputBuffer = $("#chat" + messageKey + " .outputBuffer")
		var isScrolledToBottom = $outputBuffer.get(0).scrollTop == ($outputBuffer.get(0).scrollHeight - $outputBuffer.get(0).offsetHeight);
		
		$outputBuffer.append
		(
				'<div class="message">'
			+	'	<a href="http://forum.mafiascum.net/memberlist.php?mode=viewprofile&u=' + siteChatUser.id + '"><img src="' + avatarUrl + '" class="profile"></img></a>'
			+	'	<div class="messageUserName"><a href="http://forum.mafiascum.net/memberlist.php?mode=viewprofile&u=' + siteChatUser.id + '">' + siteChatUser.name + '</a></div> <span class="messageTimestamp">(' + messageDateString + ')</span>'
			+	'	<div class="messagecontent">' + client.parseBBCode(siteChatConversationMessage.message) + '</div>'
			+	'</div>'
		);
		if(chatWindow.expanded == false && isNew && siteChatConversationMessage.userId != client.userId)
			chatWindow.blinking = true;
		
		if(isScrolledToBottom)
			$outputBuffer.get(0).scrollTop = $outputBuffer.get(0).scrollHeight;
		
		if(save)
			client.saveChatWindow(chatWindow);
	}

	this.addUser = function(siteChatUser, save, doNotAddToOnlineList)
	{
		if ( client.userMap[ siteChatUser.id ] == null){
			client.userMap[ siteChatUser.id ] = siteChatUser;
			
			if(!doNotAddToOnlineList)
				client.addUserToOnlineList(siteChatUser, false);
			if(save)
				client.saveUser(siteChatUser);
		}
	}
	
	this.addUserToOnlineList = function(siteChatUser, onlyAddToHTML)
	{
		var indexToInsert = _.sortedIndex(client.onlineUserIdSet, siteChatUser.id, function(userId) { return client.userMap[ userId ].name.toLowerCase() });
		if(indexToInsert < client.onlineUserIdSet.length && client.onlineUserIdSet[ indexToInsert ] == siteChatUser.id)
		{
			return;
		}
		
		var html
		= '<li class="username" id="username' + siteChatUser.id + '"><span class="onlineindicator"></span>'
		+ siteChatUser.name
		+ '</li>';
		if(indexToInsert >= client.onlineUserIdSet.length)
		{
			$("#onlinelist").append(html);
			if(!onlyAddToHTML)
				client.onlineUserIdSet.push(siteChatUser.id);
		}
		else
		{
			$("#username" + client.onlineUserIdSet[indexToInsert]).before(html);
			if(!onlyAddToHTML)
				client.onlineUserIdSet.splice(indexToInsert, 0, siteChatUser.id);
		}
		
		$("#username" + siteChatUser.id).data("username", siteChatUser.name).data("user-id", siteChatUser.id);
		
		if(!onlyAddToHTML)
			localStorage[client.namespace + "onlineUserIdSet"] = JSON.stringify(client.onlineUserIdSet);
		client.onlineUsers += 1;
		$('#onlinelisttitle .usercount').html('(' + client.onlineUsers + ')');
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

		if(siteChatPacket.command == "LogIn")
		{
			if(siteChatPacket.missedSiteChatConversationMessages && siteChatPacket.missedSiteChatConversationMessages.length > 0)
			{
				var missedMessagesLength = siteChatPacket.missedSiteChatConversationMessages.length;
				for(var messageIndex = 0;messageIndex < missedMessagesLength;++messageIndex)
				{
					var message = siteChatPacket.missedSiteChatConversationMessages[ messageIndex ];
					if(!client.userMap[message.userId])
						console.log("Missed Message. User ID: " + message.userId + ", In Map: " + client.userMap[message.userId]);
					else
						client.addSiteChatConversationMessage(message, true, true);
				}
			}
			
			if(client.firstConnectionThisPageLoad && client.autoJoinLobby && !_.find(client.chatWindows, function(chatWindow) {return chatWindow.title == "Lobby";}))
			{
				client.sendConnectMessage("Lobby");
			}
			
			client.firstConnectionThisPageLoad = false;
		}
		else if(siteChatPacket.command == "Connect")
		{
			if(client.chatWindows["C" + siteChatPacket.siteChatConversationId] == undefined)
			{//Create chat window
				var siteChatUserIdSet = [];
				for(var siteChatUserIndex = 0;siteChatUserIndex < siteChatPacket.users.length;++siteChatUserIndex)
				{
					var siteChatUser = siteChatPacket.users[ siteChatUserIndex ];
					siteChatUserIdSet.push(siteChatUser.id);

					client.addUser(siteChatUser, true);
				}

				//Setting recipientUserId to null because I do not believe we will be "connecting" to private conversations.
				client.createChatWindow(siteChatPacket.siteChatConversationId, null, siteChatPacket.titleText, siteChatUserIdSet, true, [], true);
			}
		}
		else if(siteChatPacket.command == "NewMessage")
		{
			var message = siteChatPacket.siteChatConversationMessage;
			var uniqueIdentifier = message.recipientUserId != null ? (client.getMessageMapKeyUserId(message)) : message.siteChatConversationId;
			var prefix = message.recipientUserId != null ? "P" : "C";
			var key = prefix + uniqueIdentifier;
			
			if(prefix == "P" || client.chatWindows[ key ] != undefined)
			{
				var siteChatUser = client.userMap[ message.userId ];

				if(!siteChatUser || client.pendingMessages.length > 0)
				{//If for some reason we have no data on a user(or if there are other pending messages), queue the message to process later and kick off a user lookup request.
					console.log("Adding pending message. Site Chat User: " + siteChatUser + ", Previous Pending Messages: " + client.pendingMessages.length);
					client.pendingMessages.push(message);

					if(!siteChatUser)
					{
						var lookupUserPacket = new Object();
						lookupUserPacket.command = "LookupUser";
						lookupUserPacket.userId = message.userId;

						client.sendSiteChatPacket(lookupUserPacket);
					}
				}
				else
				{
					client.addSiteChatConversationMessage(message, true, true);
				}
			}
		}
		else if(siteChatPacket.command == "UserJoin")
		{
			if(client.userMap[ siteChatPacket.siteChatUser.id ] == undefined)
				client.addUser(siteChatPacket.siteChatUser, true);
			
			if(client.chatWindows[ "C" + siteChatPacket.siteChatConversationId ] != undefined)
				client.chatWindows[ "C" + siteChatPacket.siteChatConversationId ].userIdSet.push( siteChatPacket.siteChatUser.id );
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
			var chatWindow = client.chatWindows[ "C" + siteChatPacket.siteChatConversationId ];
			if(chatWindow)
				chatWindow.userIdSet.splice($.inArray(siteChatPacket.userId, chatWindow.userIdSet), 1);
		}
		else if(siteChatPacket.command == "UserList")
		{
			var siteChatUserListLength = siteChatPacket.siteChatUsers.length;
			client.onlineUsers = 0;
			var newOnlineUserIdSet = [];
			client.onlineUserIdSet = [];
			
			$("#onlinelist").html("");
			for(var siteChatUserIndex = 0;siteChatUserIndex < siteChatUserListLength;++siteChatUserIndex)
			{
				var siteChatUser = siteChatPacket.siteChatUsers[ siteChatUserIndex ];
				if(client.userMap[siteChatUser.id] == null)
					client.addUser(siteChatUser, true, true);
				client.addUserToOnlineList(siteChatUser, false);
			}
		}
	}

	this.sendSiteChatPacket = function(packetObject)
	{
		if(client.socket.connected)
			client.socket.send(JSON.stringify(packetObject));
	}

	this.createChatPanel = function()
	{
		$("body").append("<div class='chatPanel' id='chatPanel'></div>");
	}
	this.createUtilityWindow = function()
	{
		if (localStorage[client.namespace +'chatGroups']){
			client.chatGroups = JSON.parse(localStorage[client.namespace +'chatGroups']);
		} else {
			client.chatGroups = new Array();
			// load chat groups for the first time
		}
		if (localStorage[client.namespace +'onlineGroup']){
			client.onlineGroup = JSON.parse(localStorage[client.namespace +'onlineGroup']);
		} else {
			client.onlineGroup = new Object();
			client.onlineGroup.expanded = true;
			//load online list for the first time
		}
		var windowStateClass = sessionStorage[client.namespace + "utilityExpanded"] == "true" ? "expanded" : "collapsed";
		$("#chatPanel").prepend
				(
					'<div class="chatWindow ' + windowStateClass + '" id="utilitywindow">'
				+	'	<div class="chatWindowInner">'
				+	'		<div class="title">Site Chat<div class="exclamation hidden">!</div></div>'
				+	' 		<ul id="chattabs">'
				+	' 			<li class="tab active"><a href="#utilitywindow-1">Users</a></li>'
				+	' 			<li class="tab"><a href="#utilitywindow-2">Rooms</a></li>'
				+	' 			<li class="tab"><a href="#utilitywindow-3">Settings</a></li>'
				+ 	' 			<div class="clear"></div>'
				+	' 		</ul> '
				+	'		<div id="utilitywindow-1" class="tab_content">'
				+	'			<div id="onlinelistcontainer">'
				+	'				<p id="onlinelisttitle"><span class="expand-icon">-</span>Online Users <span class="usercount">(0)</span></p>'
				+	'				<ul id="onlinelist"></ul>'
				+	'			</div>'
				+	'			<div id="joindiv"><form id="joinConversationForm"><input autocomplete="off" placeholder="Enter Chat Room Name" type="text" name="input"></input></div>'
				+	'		</div>'
				+	'		<div id="utilitywindow-2" class="tab_content">'
				+	'		<div>rooms</div>'
				+	'		</div>'
				+	'		<div id="utilitywindow-3" class="tab_content">'
				+	'		<div>settings</div>'
				+	'		</div>'
				+	'	</div>'
				+	'</div>'
				);
		$("#onlinelisttitle").bind('click', this.onlinelistexpand);
		$("#utilitywindow").tabify();
	}
	this.onlinelistexpand = function(){
		if (client.onlineGroup.expanded == false){
			$('#onlinelist').css('display', 'block');
			$('#onlinelisttitle .expand-icon').html('-');
			client.onlineGroup.expanded = true;
			localStorage[client.namespace +'onlineGroup'] = JSON.stringify(client.onlineGroup);
		} else {
			$('#onlinelist').css('display', 'none');
			$('#onlinelisttitle .expand-icon').html('+');
			client.onlineGroup.expanded = false;
			localStorage[client.namespace +'onlineGroup'] = JSON.stringify(client.onlineGroup);
		}
	}
	this.heartbeat = function(){
		var siteChatPacket = new Object();
		siteChatPacket.command = "Heartbeat";
		siteChatPacket.isAlive = "true";
		client.sendSiteChatPacket(siteChatPacket);
	}
	this.blink = function(){
		for(var converstionKey in client.chatWindows) {
			if(client.blinkstate == 0){
				if (client.chatWindows[converstionKey].blinking == true){
					$('#chat' + converstionKey + ' .title').animate({backgroundColor: '#F09B3C'}, 699);
					client.blinkstate = 1;
				}
			} else {
				if (client.chatWindows[converstionKey].blinking == true){
					$('#chat' + converstionKey + ' .title').animate({backgroundColor: '#E1DFE6'}, 699);
					client.blinkstate = 0;
				}
			}
		}
	}
	this.setup = function(sessionId, userId, autoJoinLobby, siteChatUrl, siteChatProtocol)
	{
		client.sessionId = sessionId;
		client.userId = userId;
		client.autoJoinLobby = autoJoinLobby && !sessionStorage[client.namespace + "lobbyForcefullyClosed"];
		client.siteChatUrl = siteChatUrl;
		client.siteChatProtocol = siteChatProtocol;
		
		if(!supportsHtml5Storage() || (typeof(WebSocket) != "function" && typeof(WebSocket) != "object"))
		{
			return;
		}
		
		//Before doing anything, ensure that the user that will attempt to connect is the same one for which we have localstorage data.
		if(localStorage[client.namespace + "userId"] == null || parseInt(localStorage[client.namespace + "userId"]) != client.userId)
		{
			client.clearLocalStorage();
			localStorage[client.namespace + "userId"] = client.userId;
		}
		
		$(window).bind("beforeunload", function() {
		
			client.unloading = true;
			if(client.socket.connected)
				client.socket.close();
		});
		$(document).on("submit", "#joinConversationForm", function(event) {
			event.preventDefault();
			var $input = $(this).children("input");
			client.sendConnectMessage($input.val());
			$input.val("");
		});
		$(document).on("blur", "#joinConversationForm > input", function(event) {
			$(this).val("");
		});
		
		
		$(document).on("click", "#chatPanel .chatWindow .title", client.handleWindowTitleClick);
		$(document).on("click", "#chatPanel .chatWindow .title .close", client.handleWindowCloseButtonClick);
		$(document).on("keypress", "#chatPanel .chatWindow .inputBuffer", client.handleWindowInputSubmission);
		$(document).on("click", "#utilitywindow .username", client.handleUserListUsernameClick);
		
		client.setupWebSocket();

		client.createChatPanel();
		
		client.createUtilityWindow();
		
		client.loadFromLocalStorage();
		setInterval('client.blink()', 700);
		setInterval('client.heartbeat()', 150000);
	}
	
	this.sendConnectMessage = function(conversationName)
	{
		client.sendSiteChatPacket({command: "Connect", siteChatConversationName: conversationName});
	}
	
	this.setupWebSocket = function()
	{
		client.socket = new WebSocket(client.siteChatUrl, client.siteChatProtocol);
		client.socket.connected = false;
		client.socket.onopen = client.handleSocketOpen;
		client.socket.onclose = client.handleSocketClose;
		client.socket.onmessage = client.handleSocketMessage;
	}
}
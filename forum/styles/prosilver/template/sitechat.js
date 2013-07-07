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
	this.rooms = new Object();
	this.selectedTab = 0;
	this.tabs = [];
	this.namespace = "ms_sc2_";//Prepended to all local storage variables.
	this.attemptReconnectIntervalId = null;
	this.onlineUserIdSet = [];
	this.MAX_MESSAGES_PER_WINDOW = 500;
	this.MAX_MESSAGE_LENGTH = 255;
	this.unloading = false;
	this.firstConnectionThisPageLoad = true;
	this.onlineUsers = 0;
	this.attemptingLogin = false;
	this.dragWindow = null;
	this.attemptReconnect = function()
	{
		client.setupWebSocket();
	}
	
	this.parseBBCode = function(message)
	{
		return message	.replace(/\[b\](.*?)\[\/b\]/g, "<b>$1</b>")
				.replace(/\[i\](.*?)\[\/i\]/g, "<i>$1</i>")
				.replace(/\[u\](.*?)\[\/u\]/g, "<u>$1</u>")
				.replace(/\[s\](.*?)\[\/s\]/g, "<s>$1</s>")
				.replace(/\[v\](.*?)\[\/v\]/g, "<b>Vote: $1</b>")
				.replace(/\[room\](.*?)\[\/room\]/g, "<a href='#' class='chatroomlink' data-room='$1'>$1</a>")
				.replace(/\b(([\w-]+:\/\/?|www[.])[^\s()<>]+(?:\([\w\d]+\)|([^[:punct:]\s]|)))/g, function(str) {
					if(!startsWith(str, "http://") && !startsWith(str, "https://"))
						var url = "http://" + str;
					else
						url = str;
					return "<a href='" + url + "' target='_blank'>" + str + "</a>";
				});
	}
	this.unescapeHTMLEntities = function(str)
	{
		return str.replace("&quot;", '"').replace("&amp;", "&").replace("&apos;", "'").replace("&lt;", "<").replace("&gt;", ">");
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

				var conversation = JSON.parse(localStorage[client.namespace + "conversation" + key]);
				client.createChatWindow(conversationId, recipientUserId, conversation.createdByUserId, conversation.title, conversation.userIdSet, conversation.expanded, conversation.messages, false, conversation.blinking, conversation.width, conversation.height);

			}
		}
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
		if(localStorage[client.namespace +'selectedTab']){
			client.selectedTab = localStorage[client.namespace +'selectedTab'];
		} else {
			client.selectedTab = 0;
		}
		if(localStorage[client.namespace + 'rooms']){
			client.rooms = JSON.parse(localStorage[client.namespace + 'rooms']);
		}
	}

	this.handleUserListUsernameClick = function(event)
	{
		event.preventDefault();
		event.stopPropagation();
		
		var recipientUserId = parseInt($(this).data("user-id"));
		if(client.chatWindows["P" + recipientUserId] == undefined)
			client.createChatWindow(null, recipientUserId, null, $(this).data("username"), [], true, [], true, null, null);
	}

	
	this.handleWindowTitleClick = function(event)
	{
		event.preventDefault();
		event.stopPropagation();
		var $window = $(this).closest(".chatWindow");
		var $title = $window.find(".title");
		var siteChatConversation = client.chatWindows[ client.getWindowMapKeyFromDomObject($window) ];
		
		if (siteChatConversation != null && siteChatConversation.blinking == true)
			siteChatConversation.blinking = false;
		
		$title.stop(true).css('backgroundColor', '').removeClass("backgroundColorTransition");
		
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
				var $outputBuffer = $window.find(".outputBuffer");
				$outputBuffer.scrollTop($outputBuffer[0].scrollHeight);
			}
			else
				sessionStorage[client.namespace + "utilityExpanded"] = true;
		}
		
		setTimeout(function() {$title.addClass("backgroundColorTransition");}, 50);
		
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
			client.sendPacket(siteChatPacket);
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
			var $this = $(this);
			var content = $this.val();

			if(content.length > 0)
			{
				var $window = $this.closest(".chatWindow");
				client.submitChatMessage(content, $window.attr("id"), $window.data("conversation-id"), $window.data("recipient-user-id"));
			}

			$this.val("");
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
	
	this.getUserIdSet = function()
	{
	
		var userIdSet = [];
		for(var userId in client.userMap)
		{
			userIdSet.push(parseInt(userId));
		}
		return userIdSet;
	}

	this.submitChatMessage = function(messageContent, chatWindowId, conversationId, recipientUserId)
	{
		var offset = 0, messageLength = messageContent.length;
		while(offset < messageLength)
		{
			var siteChatPacket =
			{
				command:"SendMessage",
				message:messageContent.substr(offset, client.MAX_MESSAGE_LENGTH),
				siteChatConversationId:conversationId == null ? null : parseInt(conversationId),
				recipientUserId:recipientUserId == null ? null : parseInt(recipientUserId)
			};
			this.sendPacket(siteChatPacket);
			offset += client.MAX_MESSAGE_LENGTH;
		}
	}
	
	this.handleSocketOpen = function()
	{
		console.log("[" + new Date() + "] Socket Opened.");
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
		
		client.attemptingLogin = true;
		client.sendPacket(siteChatPacket);
	}
	
	this.handleSocketClose = function()
	{
		console.log("[" + new Date() + "] Web Socket Closed.");
		client.socket.connected = false;
		if(!client.unloading)
		{
			if(client.attemptReconnectIntervalId != null)
				window.clearInterval(client.attemptReconnectIntervalId);
			
			if( !client.attemptingLogin )
				client.attemptReconnectIntervalId = setInterval(client.attemptReconnect, 20000);
			$("#utilitywindow .exclamation").removeClass("hidden");
		}
		client.attemptingLogin = false;
	}

	this.createChatWindow = function(conversationId, recipientUserId, createdByUserId, title, userIdSet, expanded, messages, save, blinking, width, height)

	{
		var chatWindowIdPrefix = (conversationId != null ? "C" : "P");
		var chatWindowUniqueIdentifier = (conversationId != null ? conversationId : recipientUserId);
		$("#chatPanel").append
			(
				'<div class="chatWindow conversation expanded" id="chat' + chatWindowIdPrefix + chatWindowUniqueIdentifier + '">'
			+	'	<div class="chatWindowOuter">'
			+	'		<div class="chatWindowInner">'
			+	'			<div class="title"><div class="name">' + title + '</div><div class="options"></div><div class="close">X</div></div>'
			+	'			<div class="menu"><ul></ul></div>'
			+	'			<div class="outputBuffer"></div>'
			+	'			<textarea class="inputBuffer" name="input" style="height:20px;"></textarea>'
			+	'		</div>'
			+	'	</div>'
			+	'</div>'
			);

		var $chatWindow = $("#chat" + chatWindowIdPrefix + chatWindowUniqueIdentifier);
		var $inputBuffer = $chatWindow.find(".inputBuffer");
		var $outputBuffer = $chatWindow.find(".outputBuffer");
		var $title = $chatWindow.find(".title");
		
		if(conversationId != null)
			$chatWindow.data("conversation-id", conversationId);
		if(recipientUserId != null)
			$chatWindow.data("recipient-user-id", recipientUserId);
		$chatWindow.data("key", chatWindowIdPrefix + chatWindowUniqueIdentifier);
		
		//Window defaults to an expanded state so autogrow can see the proper CSS values.
		$inputBuffer.autoGrow();
		//We now collapse it.
		$inputBuffer.removeClass("expanded").addClass("collapsed");
		
		setTimeout(function(){$title.addClass("backgroundColorTransition");}, 50);
		
		var chatWindow = new ChatWindow();
		chatWindow.siteChatConversationId = conversationId;
		chatWindow.recipientUserId = recipientUserId;
		chatWindow.createdByUserId = createdByUserId;
		chatWindow.userIdSet = [];
		chatWindow.title = title;
		chatWindow.messages = [];
		chatWindow.width = (width ? width : $chatWindow.width());
		chatWindow.height = (height ? height : $outputBuffer.height());
		
		$chatWindow.width(chatWindow.width);
		$outputBuffer.height(chatWindow.height);
		
		if(expanded != undefined)
			chatWindow.expanded = expanded;
		if(blinking != undefined)
			chatWindow.blinking = blinking;
		if(chatWindow.expanded)
			$chatWindow.addClass("expanded").removeClass("collapsed");
		else
			$chatWindow.addClass("collapsed").removeClass("expanded");

		if(client.userId != createdByUserId)
			$title.find(".options").addClass("hidden");

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
		
		if(save)
			$inputBuffer.focus();
		else
			$outputBuffer.scrollTop($outputBuffer.scrollHeight);		

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

	this.getWindowMapKeyFromDomObject = function($window)
	{
		var conversationId = $window.data("conversation-id");
		var recipientUserId = $window.data("recipient-user-id");
		return conversationId != null ? ("C" + conversationId) : ("P" + recipientUserId);
	}
	
	this.addSiteChatConversationMessage = function(siteChatConversationMessage, save, isNew)
	{
		var messageKey = client.getMessageMapKey(siteChatConversationMessage);
		var siteChatUser = client.userMap[ siteChatConversationMessage.userId ];
		var messageDate = new Date(siteChatConversationMessage.createdDatetime);
		
		if(!client.chatWindows[messageKey] && siteChatConversationMessage.recipientUserId != null)
		{//If this is a private conversation & the window has not yet been created, we should have enough information to make it ourselves.
			client.createChatWindow(null, siteChatUser.id, null, siteChatUser.name, [siteChatUser.id], true, [], true, null, null);
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
				client.saveUser(siteChatUser, true);
		}
	}
	
	this.addUserToOnlineList = function(siteChatUser, onlyAddToHTML)
	{
		var indexToInsert = _.sortedIndex(client.onlineUserIdSet, siteChatUser.id, function(userId) { return client.userMap[ userId ].name.toLowerCase() });
		if(indexToInsert < client.onlineUserIdSet.length && client.onlineUserIdSet[ indexToInsert ] == siteChatUser.id)
		{
			return;
		}
		
		var active = siteChatUser.lastActivityDatetime ? ((new Date().getTime() - siteChatUser.lastActivityDatetime) / 1000) < (60) * (5) : false;
		var html
		= '<li class="username" id="username' + siteChatUser.id + '"><span class="onlineindicator ' + (active ? "active" : "idle") + '"></span>'
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

	this.saveUser = function(siteChatUser, saveUserIdSet)
	{
		if(saveUserIdSet)
			localStorage[client.namespace + "userIdSet"] = JSON.stringify(client.getUserIdSet());
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
	
	this.isInChatRoom = function(roomName)
	{
		return _.find(client.chatWindows, function(chatWindow) {return client.unescapeHTMLEntities(chatWindow.title).toLowerCase() == roomName.toLowerCase();});
	}

	this.handleSocketMessage = function(message)
	{
		var data = message.data;
		var siteChatPacket = JSON.parse(data);

		if(siteChatPacket.command == "LogIn")
		{
			client.attemptingLogin = false;
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
			
			if(client.firstConnectionThisPageLoad && client.autoJoinLobby && !client.isInChatRoom("Lobby"))
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
				client.createChatWindow(siteChatPacket.siteChatConversationId, null, siteChatPacket.createdByUserId, siteChatPacket.titleText, siteChatUserIdSet, true, [], true, false, null, null);
			}

			$.fancybox.close();
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

						client.sendPacket(lookupUserPacket);
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
				client.saveUser(siteChatUser, false);
			}
			$("#roomstab").html("");
			var oldrooms = client.rooms;
			client.rooms = new Object();

			var newRoomListLength = siteChatPacket.siteChatConversations.length;
			for(var i = 0;i < newRoomListLength;++i)
			{
				var conversationFromPacket = siteChatPacket.siteChatConversations[i];
				var room = (oldrooms[conversationFromPacket.id] != null ? oldrooms[conversationFromPacket.id] : new Object());
				if(room.expanded == null)
					room.expanded = false;
				
				room.userIdSet = conversationFromPacket.userIdSet;
				room.name = conversationFromPacket.name;
				room.id = conversationFromPacket.id;
				room.createdByUserId = conversationFromPacket.createdByUserId;
				client.rooms[room.id] = room;
			}
			client.generateRooms(true);
		}
		else if(siteChatPacket.command == "PasswordRequired")
		{
			var conversationName = siteChatPacket.conversationName;
			var $lightbox = $("#siteChatPasswordLightbox");
			var $ul = $lightbox.find("ul");

			$lightbox.find(".conversationName").text(conversationName);
			$lightbox.find("input[name='ConversationName']").val(conversationName);

			var $password = $lightbox.find("input[name='Password']");
			$password.val("");
			$ul.html("");

			$.fancybox.open("#siteChatPasswordLightbox");
			$password.focus();
		}
		else if(siteChatPacket.command == "IncorrectPassword")
		{
			var $ul = $("#siteChatPasswordLightbox ul");
			var $li = $("<li></li>");

			$li.text("The password you entered is incorrect.");

			$ul.html("");
			$ul.append($li);
		}
		else if(siteChatPacket.command == "SetPassword")
		{
			if(siteChatPacket.errorMessage)
			{
				var $li = $("<li></li>");
				var $ul = $("#siteChatSetPasswordLightbox").find("ul");
				
				$li.text(siteChatPacket.errorMessage);
				$ul.html("");
				$ul.append($li);
			}
			else
				$.fancybox.close();
		}
	}
	this.generateRooms = function (save){
		if (client.rooms){
					for (var room in client.rooms) {
						if (client.rooms[room] !== undefined && client.rooms[room] !== null){
							$("#roomstab").append('<div id="chatroom' + client.rooms[room].id + '"><div class="roomtitle"><span class="expand-icon">' + (client.rooms[room].expanded ? '-' : "+") + '</span>' + client.rooms[room].name + '<span class="usercount">(' + client.rooms[room].userIdSet.length + ')</span></div><div class="userlist"' + (client.rooms[room].expanded ? '' : "style='display:none;'") + '></div></div>');
							var identifier = '#chatroom' + client.rooms[room].id + ' .userlist';
							$(identifier).append('<ul>');
							for (var k = 0; k < client.rooms[room].userIdSet.length; k++){
								var siteChatUser = client.userMap[client.rooms[room].userIdSet[k]];
								var active = siteChatUser.lastActivityDatetime ? ((new Date().getTime() - siteChatUser.lastActivityDatetime) / 1000) < (60) * (5) : false;
								var html = '<li class="username" id="username' + client.rooms[room].name.replace(/[^A-Za-z0-9]/g, '') + siteChatUser.id + '"><span class="onlineindicator ' + (active ? "active" : "idle") + '"></span>'
											+ siteChatUser.name
											+ '</li>';
								$(identifier).append(html);
								$("#username"+ client.rooms[room].name.replace(/[^A-Za-z0-9]/g, '') + siteChatUser.id).data("username", siteChatUser.name).data("user-id", siteChatUser.id);
							}
							$(identifier).append('</ul>');
						}
					}
				}
		$(".roomtitle").bind('click', client.roomlistexpand);
		if (save){
			localStorage[client.namespace + "rooms"] = JSON.stringify(client.rooms);
			localStorage[client.namespace + "userIdSet"] = JSON.stringify(client.getUserIdSet());
		}
	}
	this.sendPacket = function(packetObject)
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
		var windowStateClass = sessionStorage[client.namespace + "utilityExpanded"] == "true" ? "expanded" : "collapsed";
		$("#chatPanel").prepend
				(
					'<div class="chatWindow ' + windowStateClass + '" id="utilitywindow">'
				+	'	<div class="chatWindowOuter">'
				+	'	<div class="chatWindowInner">'
				+	'		<div class="title">Site Chat<div class="exclamation hidden">!</div></div>'
				+	' 		<ul id="chattabs">'
				+	' 			<li id="tab0" class="tab"><a href="#utilitywindow-1">Users</a></li>'
				+	' 			<li id="tab1" class="tab"><a href="#utilitywindow-2">Rooms</a></li>'
				+	' 			<li id="tab2" class="tab"><a href="#utilitywindow-3">Settings</a></li>'
				+ 	' 			<div class="clear"></div>'
				+	' 		</ul> '
				+	'		<div id="utilitywindow-1" class="tab_content">'
				+	'			<div id="onlinelistcontainer">'
				+	'				<p id="onlinelisttitle"><span class="expand-icon">-</span>Online Users <span class="usercount">(0)</span></p>'
				+	'				<ul id="onlinelist"></ul>'
				+	'			</div>'
				+	'		</div>'
				+	'		<div id="utilitywindow-2" class="tab_content">'
				+	'		<div id="roomstab"></div>'
				+	'		</div>'
				+	'		<div id="utilitywindow-3" class="tab_content">'
				+	'		<div>settings</div>'
				+	'		</div>'
				+	'			<div id="joindiv"><form id="joinConversationForm"><input autocomplete="off" placeholder="Enter Chat Room Name" type="text" name="input"></input></div>'
				+	'	</div>'
				+	'	</div>'
				+	'</div>'
				);
		var index = client.tabs.push(new Object()) -1;
		client.tabs[index].id = 0;
		index =	client.tabs.push(new Object()) -1;
		client.tabs[index].id = 1;
		index = client.tabs.push(new Object()) -1;
		client.tabs[index].id = 2;
		
		$("#utilitywindow .title").addClass("backgroundColorTransition");
		
	}
	this.setActiveTab = function(id){
		$('#tab' + id).addClass('active');
		$($('#tab' + id).children('a').attr('href')).css('display','block');
		for (var i= 0; i < client.tabs.length; i++){
			if(id != client.tabs[i].id){
				$($('#tab' + client.tabs[i].id).children('a').attr('href')).css('display','none');
				$('#tab' + client.tabs[i].id).removeClass('active');
			}
		}
	}
	this.populateUtilityWindow = function (){
		client.generateRooms(false);
		if (client.onlineGroup.expanded == false){
			$('#onlinelist').css('display','none');
			$('#onlinelisttitle .expand-icon').html('+');
		}
		var tabfound = false;
		for (var i= 0; i < client.tabs.length; i++){
			if(client.selectedTab == client.tabs[i].id){
				client.setActiveTab(client.tabs[i].id);
				tabfound = true;
				break;
			}
		}
		if (!tabfound){
			client.setActiveTab(0);
		}
		$("#utilitywindow .inputBuffer").bind("keypress", client.handleWindowInputSubmission);
		$("#onlinelisttitle").bind('click', this.onlinelistexpand);
		//$("#utilitywindow").tabify();
		$('#tab0').bind('click', function(){
			client.setActiveTab(0);
			localStorage[client.namespace +'selectedTab'] = 0;
			return false;
		});
		$('#tab1').bind('click', function(){
			client.setActiveTab(1);
			localStorage[client.namespace +'selectedTab'] = 1;
			return false;
		});
		$('#tab2').bind('click', function(){
			client.setActiveTab(2);
			localStorage[client.namespace +'selectedTab'] = 2;
			return false;
		});
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
	this.roomlistexpand = function(){
		if ($(this).next().css('display') == 'none'){
			$(this).next().css('display', 'block');
			$('.expand-icon',this).html('-');
			var id = $(this).parent().attr('id').match(/\d+/)
			client.rooms[id].expanded = true;
			localStorage[client.namespace +'rooms'] = JSON.stringify(client.rooms);
		} else {
			$(this).next().css('display', 'none');
			$('.expand-icon',this).html('+');
			var id = $(this).parent().attr('id').match(/\d+/);
			client.rooms[id].expanded = false;
			localStorage[client.namespace +'rooms'] = JSON.stringify(client.rooms);
		}
	}
	this.heartbeat = function(){
		var siteChatPacket = new Object();
		siteChatPacket.command = "Heartbeat";
		siteChatPacket.isAlive = "true";
		
		if(client.socket.connected)
			client.sendPacket(siteChatPacket);
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
		
		$(document).on("click", "#chatPanel .chatWindow .title .options", function(e) {

			e.preventDefault();
			e.stopPropagation();
			var $menu = $(this).closest(".chatWindow").find("div.menu");
			var $ul = $menu.find("ul");

			$menu.toggleClass("expanded");
			
			$ul.html("").append("<li><a href='#' class='siteChatChangePasswordAnchor'>Change Password</a></li>");
		});
		$(document).on("click", "a.siteChatChangePasswordAnchor", function(e) {

			e.preventDefault();
			e.stopPropagation();
			var $window = $(this).closest(".chatWindow");
			var conversation = client.chatWindows[ client.getWindowMapKeyFromDomObject($window) ];

			var $lightbox = $("#siteChatSetPasswordLightbox");
			var $password = $lightbox.find("input[name='Password']");

			$lightbox.find(".conversationName").text(client.unescapeHTMLEntities(conversation.title));
			$password.val("");
			$lightbox.find("input[name='ConversationID']").val(conversation.siteChatConversationId);

			$window.find(".menu").removeClass("expanded");

			$.fancybox.open("#siteChatSetPasswordLightbox");

			$password.focus();
		});
		$(document).on("click", "#chatPanel .chatWindow .title", client.handleWindowTitleClick);
		$(document).on("click", "#chatPanel .chatWindow .title .close", client.handleWindowCloseButtonClick);
		$(document).on("keypress", "#chatPanel .chatWindow .inputBuffer", client.handleWindowInputSubmission);
		$(document).on("click", "#utilitywindow .username", client.handleUserListUsernameClick);
		
		$(document).on("mousewheel", "#onlinelistcontainer, #chatPanel .outputBuffer", function(e) {

			if( e.originalEvent ) e = e.originalEvent;
			var delta = e.wheelDelta || e.detail;
			this.scrollTop += ( delta < 0 ? 1 : -1 ) * 30;
			e.preventDefault();
		});
		
		$(document).on("mousemove", "#chatPanel > .chatWindow.conversation > .chatWindowOuter", function(e) {
		
			var $elem = $(this);
			if($elem.parent().hasClass("collapsed"))
				return;
			
			var left  = (e.pageX - $elem.offset().left ) / $elem.width() * 100;
			var top = (e.pageY - $elem.offset().top ) / $elem.height() * 100;

			if(left < 3 && top < 3)
				$elem.css("cursor", "nw-resize");
			else if(top < 3)
				$elem.css("cursor", "n-resize");
			else if(left < 3)
				$elem.css("cursor", "w-resize");
			else
				$elem.css("cursor", "");
		});
		$(document).on("mousemove", "#chatPanel > .chatWindow.conversation > .chatWindowOuter > .chatWindowInner", function(e) {
		
			if(client.dragWindow)
				return;
			e.stopPropagation();
		});
		
		$(document).on("mouseenter", "#chatPanel > .chatWindow.conversation > .chatWindowOuter > .chatWindowInner", function(e) {
		
			if(!client.dragWindow)
				$(this).parent().css("cursor", "");
		});
		
		$(document).on("mousedown", "#chatPanel > .chatWindow.conversation > .chatWindowOuter", function(e) {
		
			var $elem = $(this);
			if($elem.parent().hasClass("collapsed"))
				return;
			e.preventDefault();
			e.stopPropagation();
			var $window = $elem.closest(".chatWindow");
			var $outputBuffer = $elem.find(".outputBuffer");
			
			client.dragWindow = {
				windowId: $window.attr("id"),
				window: $window,
				outputBuffer: $outputBuffer,
				startPageX: e.pageX,
				startPageY: e.pageY,
				startWindowWidth: $window.width(),
				startWindowHeight: $outputBuffer.height()
			};
			
			
			var left  = (e.pageX - $elem.offset().left ) / $elem.width() * 100;
			var top = (e.pageY - $elem.offset().top ) / $elem.height() * 100;
			if(top < 3 && left < 3)
				client.dragWindow.edge = "topleft";
			else if(top < 3)
				client.dragWindow.edge = "top";
			else if(left < 3)
				client.dragWindow.edge = "left";
		});
		
		$(document).on("mousedown", "#chatPanel > .chatWindow.conversation > .chatWindowOuter > .chatWindowInner", function(e) {
			
			e.stopPropagation();
		});
		
		$(document).on("mouseup", "body", function(e) {
		
			if(client.dragWindow)
			{
				var $window = client.dragWindow.window;
				var $outputBuffer = client.dragWindow.outputBuffer;
				var chatWindow = client.chatWindows[$window.data("key")];
				
				chatWindow.height = $outputBuffer.height();
				chatWindow.width = $window.width();
				client.saveChatWindow(chatWindow);
				
				client.dragWindow = null;
			}
		});
		
		$(document).on("mousemove", "body", function(e) {
		
			if(client.dragWindow)
			{
				var $window = client.dragWindow.window;
				var $outputBuffer = client.dragWindow.outputBuffer;
				if(client.dragWindow.edge == "left" || client.dragWindow.edge == "topleft")
					$window.css("width", client.dragWindow.startWindowWidth + (client.dragWindow.startPageX - e.pageX) );
				if(client.dragWindow.edge == "top" || client.dragWindow.edge == "topleft")
					$outputBuffer.css("height", client.dragWindow.startWindowHeight + (client.dragWindow.startPageY - e.pageY) );
			}
		});
		
		$(document).on("click", "#chatPanel a.chatroomlink", function(e) {
		
			e.preventDefault();
			var roomName = $(this).data("room");
			if(!client.isInChatRoom(roomName))
				client.sendConnectMessage(roomName);
		});

		$(document).on("submit", "#siteChatPasswordForm", function(e) {

			e.preventDefault();
			var $form = $(this);

			var password = $form.find("input[name='Password']").val();
			var conversationName = $form.find("input[name='ConversationName']").val();

			client.sendConnectMessage(conversationName, password);
		});
		$(document).on("submit", "#siteChatSetPasswordForm", function(e) {
			e.preventDefault();
			var $form = $(this);
			var password = $form.find("input[name='Password']").val();
			var conversationId = parseInt($form.find("input[name='ConversationID']").val());

			var packet =
			{
				command:"SetPassword",
				conversationId:conversationId,
				password:password
			};

			client.sendPacket(packet);
		});
		
		client.setupWebSocket();

		client.createChatPanel();
		client.createUtilityWindow();
		client.loadFromLocalStorage();
		client.populateUtilityWindow();
		setInterval('client.blink()', 700);
		setInterval('client.heartbeat()', 150000);

		client.createPasswordLightbox();
	}

	this.createPasswordLightbox = function() {

		if($("#siteChatPasswordLightbox").length == 0) {

			$("body").append("<div style='display:none;'><div id='siteChatPasswordLightbox' class='siteChatLightbox'><ul></ul>The room `<span class='conversationName'></span>` requires a password.<br/><br/><form id='siteChatPasswordForm'>Password: <input type='password' name='Password' /> <input type='hidden' name='ConversationName' /> <button type='submit' class='button1'>Join Room</button></form></div</div>");
		}

		if($("#siteChatSetPasswordLightbox").length == 0) {

			$("body").append("<div style='display:none;'><div id='siteChatSetPasswordLightbox' class='siteChatLightbox'><ul></ul>Set password for the room `<span class='conversationName'></span>`.<br/><br/><form id='siteChatSetPasswordForm'>New Password: <input type='password' name='Password' /> <input type='hidden' name='ConversationID' /> <button type='submit' class='button1'>Set Password</button></form></div</div>");
		}
	}
	
	this.sendConnectMessage = function(conversationName, password)
	{
		client.sendPacket({command: "Connect", siteChatConversationName: conversationName, password: password});
	}
	
	this.setupWebSocket = function()
	{
		console.log("[" + new Date() + "] CONNECTING");
		client.socket = new WebSocket(client.siteChatUrl, client.siteChatProtocol);
		client.socket.connected = false;
		client.socket.onopen = client.handleSocketOpen;
		client.socket.onclose = client.handleSocketClose;
		client.socket.onmessage = client.handleSocketMessage;
		
		client.attemptingLogin = false;
	}
}

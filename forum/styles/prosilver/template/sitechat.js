var siteChat = (function() {

	var siteChat = siteChat || {};
	var defaultAvatar = './styles/prosilver/imageset/defaultAvatar.png';

	function supportsHtml5Storage() {
		try{return 'localStorage' in window && window['localStorage'] !== null;}
		catch(e){return false;}
	}

	function zeroFill( number, width ) {
		width -= number.toString().length;
		return (width > 0) ? (new Array( width + 1 ).join( '0' ) + number) : number.toString();
	}

	function startsWith(sourceStr, subStr) {
		if(subStr.length > sourceStr.length)
			return false;

		for(var index in subStr) {
			if(subStr[index] != sourceStr[index])
				return false;
		}
		return true;
	}
	
	function ChatWindow(siteChatConversationId, recipientUserId, createdByUserId, userIdSet, title, messages, width, height, authCode, expanded, blinking) {
		this.siteChatConversationId = siteChatConversationId;
		this.recipientUserId = recipientUserId;
		this.createdByUserId = createdByUserId;
		this.userIdSet = userIdSet;
		this.title = title;
		this.messages = messages;
		this.width = width;
		this.height = height;
		this.authCode = authCode;
		this.expanded = expanded;
		this.blinking = blinking;

		this.save = function() {
			siteChat.setLocalStorage("conversationIdSet", JSON.stringify(siteChat.getConversationKeySet()));
			siteChat.setLocalStorage("conversation" + this.getWindowMapKey(), JSON.stringify(this));
		};

		this.getWindowMapKey = function() {
			return this.recipientUserId != null ? ("P" + this.recipientUserId) : ("C" + this.siteChatConversationId);
		};

		this.getWindow = function() {
			return $("#chat" + this.getWindowMapKey());
		};

		this.startBlinking = function() {
			this.getWindow().find(".title").addClass("blink").addClass("blinkEnd");
			this.blinking = true;
		};

		this.stopBlinking = function() {
			this.getWindow().find(".title").removeClass("blink").removeClass("blinkEnd");
			this.blinking = false;
		};
	}

	siteChat.tryReconnect = true;
	siteChat.chatWindows = {};//Associative array
	siteChat.userMap = {};//Associative array
	siteChat.socket = null;
	siteChat.userId = null;
	siteChat.sessionId = null;
	siteChat.pendingMessages = [];
	siteChat.rooms = {};
	siteChat.selectedTab = 0;
	siteChat.tabs = [];
	siteChat.namespace = "ms_sc2_";//Prepended to all local storage variables.
	siteChat.attemptReconnectIntervalId = null;
	siteChat.onlineUserIdSet = [];
	siteChat.MAX_MESSAGES_PER_WINDOW = 30;
	siteChat.MAX_MESSAGE_LENGTH = 255;
	siteChat.unloading = false;
	siteChat.firstConnectionThisPageLoad = true;
	siteChat.onlineUsers = 0;
	siteChat.attemptingLogin = false;
	siteChat.dragWindow = null;
	siteChat.commandHandlers = {};

	siteChat.roomListRoomTemplate = Handlebars.compile(
		'<div id="chatroom{{roomId}}">'
		+		'<div class="roomtitle">'
		+		'<span class="expand-icon">{{expandIcon}}</span>'
		+		'<span class="roomName">{{roomName}}</span>'
		+		'<span class="usercount">({{numberOfUsers}})</span>'
		+		'<a href="#" class="joinroom">Join Room</a>'
		+	'</div>'
		+	'<div class="userlist">'
		+		'<ul>'
		+		'{{#each users}}'
		+			'<li class="username" id="username{{roomNameCleaned}}{{userId}}" data-username="{{userName}}" data-user-id="{{userId}}">'
		+				'<span class="onlineindicator {{activeClass}}"></span>'
		+				'{{userName}}'
		+			'</li>'
		+		'{{/each}}'
		+		'</ul>'
		+	'</div>'
	);

	siteChat.chatAnchorTemplate = Handlebars.compile(
		'<a href="{{{url}}}" target="_blank">{{{display}}}</a>'
	);

	siteChat.chatWindowTemplate = Handlebars.compile(
		'<div class="chatWindow conversation expanded" data-key="{{key}}" {{#if conversationId}}data-conversation-id="{{conversationId}}" {{/if}} {{#if recipientUserId}}data-recipient-user-id="{{recipientUserId}}" {{/if}} id="chat{{idPrefix}}{{uniqueIdentifier}}">'
		+		'<div class="chatWindowOuter">'
		+			'<div class="chatWindowInner">'
		+				'<div class="title"><div class="name">{{title}}</div><div class="options"></div><div class="close">X</div></div>'
		+				'<div class="menu"><ul></ul></div>'
		+				'<div class="outputBuffer">'
		+					'<a href="#" class="loadMore">Load More Messages</a>'
		+					'<div class="messages"></div>'
		+				'</div>'
		+				'<textarea class="inputBuffer" name="input" style="height:20px;"></textarea>'
		+			'</div>'
		+		'</div>'
		+	'</div>'
	);

	siteChat.utilityWindowTemplate = Handlebars.compile(
		'<div class="chatWindow {{windowStateClass}}" id="utilitywindow">'
		+	'	<div class="chatWindowOuter">'
		+	'	<div class="chatWindowInner">'
		+	'		<div class="title">Site Chat<div class="exclamation hidden">!</div></div>'
		+	' 		<ul id="chattabs">'
		+	' 			<li id="tab0" data-tab-id="0" class="tab"><a href="#utilitywindow-1">Users</a></li>'
		+	' 			<li id="tab1" data-tab-id="1" class="tab"><a href="#utilitywindow-2">Rooms</a></li>'
		+	' 			<li id="tab2" data-tab-id="2" class="tab"><a href="#utilitywindow-3">Settings</a></li>'
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
		+	'		<div id="settingstab"><div id="disable_button">Disable Chat</div></div>'
		+	'		</div>'
		+	'			<div id="joindiv"><form id="joinConversationForm"><input autocomplete="off" placeholder="Enter Chat Room Name" type="text" name="input"/></div>'
		+	'	</div>'
		+	'	</div>'
		+	'</div>'
	);

	siteChat.liTemplate = Handlebars.compile("<li>{{content}}</li>");

	siteChat.getLocalStorage = function(key) {
		return localStorage[siteChat.namespace + key];
	};

	siteChat.setLocalStorage = function(key, value) {
		if(arguments.length != 2)
			throw "Invalis number of arguments: " + arguments.length;
		localStorage[siteChat.namespace + key] = value;
	};

	siteChat.attemptReconnect = function() {
		siteChat.setupWebSocket();
	};

	siteChat.parseBBCode = function(message) {
		return message
			.replace(/\[b\](.*?)\[\/b\]/g, "<b>$1</b>")
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
				return siteChat.chatAnchorTemplate({url: url, display: str});
			});
	};

	siteChat.clearLocalStorage = function() {
		for(var key in localStorage) {
			if(startsWith(key, siteChat.namespace))
				localStorage.removeItem(key);
		}
	};

	siteChat.loadFromLocalStorage = function() {
		var userIdSet, converstionIdSet;

		//Load users.
		if(siteChat.getLocalStorage("userIdSet")) {
			userIdSet = JSON.parse(siteChat.getLocalStorage("userIdSet"));
			for(var index = 0;index < userIdSet.length;++index) {
				var userId = userIdSet[ index ];

				if(siteChat.getLocalStorage("user" + userId)) {
					try {
						var siteChatUser = JSON.parse(siteChat.getLocalStorage("user" + userId));
						siteChat.addUser(siteChatUser, false, true);
					}
					catch(err) {
						console.log("Could not load user from localStorage: " + err);
					}
				}
			}
		}

		//Load online user ID set.
		if(siteChat.getLocalStorage("onlineUserIdSet")) {
			JSON.parse(siteChat.getLocalStorage("onlineUserIdSet")).forEach(function(userId) {
				siteChat.addUserToOnlineList(siteChat.userMap[userId], true);
			});
		}

		//Load conversations.
		if(siteChat.getLocalStorage("conversationIdSet")) {
			JSON.parse(siteChat.getLocalStorage("conversationIdSet")).forEach(function (key) {
				var recipientUserId = null, conversationId = null;
				if (typeof key == "string") {
					if (key[0] == "C")
						conversationId = parseInt(key.substring(1));
					else if (key[0] == "P")
						recipientUserId = parseInt(key.substring(1));
				}
				else
					conversationId = parseInt(key);//Support old format.

				var conversation = JSON.parse(siteChat.getLocalStorage("conversation" + key));
				siteChat.createChatWindow(conversationId, recipientUserId, conversation.createdByUserId, conversation.title, conversation.userIdSet, conversation.expanded, conversation.messages, false, conversation.blinking, conversation.width, conversation.height, conversation.authCode);
			});
		}

		if (siteChat.getLocalStorage('chatGroups')) {
			siteChat.chatGroups = JSON.parse(siteChat.getLocalStorage('chatGroups'));
		}
		else {
			siteChat.chatGroups = [];
			// load chat groups for the first time
		}
		if (siteChat.getLocalStorage('onlineGroup')) {
			siteChat.onlineGroup = JSON.parse(siteChat.getLocalStorage('onlineGroup'));
		}
		else {
			siteChat.onlineGroup = {
				expanded: true
			};
			//load online list for the first time
		}
		if(siteChat.getLocalStorage('selectedTab'))
			siteChat.selectedTab = siteChat.getLocalStorage('selectedTab');
		else
			siteChat.selectedTab = 0;

		if(siteChat.getLocalStorage('rooms'))
			siteChat.rooms = JSON.parse(siteChat.getLocalStorage('rooms'));
	};

	siteChat.handleUserListUsernameClick = function(event) {
		event.preventDefault();
		event.stopPropagation();

		var recipientUserId = parseInt($(this).data("user-id"));
		if(siteChat.chatWindows["P" + recipientUserId] == undefined)
			siteChat.createChatWindow(null, recipientUserId, null, $(this).data("username"), [], true, [], true, null, null, null);
	};


	siteChat.handleWindowTitleClick = function(event) {
		event.preventDefault();
		event.stopPropagation();
		var $window = $(this).closest(".chatWindow");
		var $title = $window.find(".title");
		var chatWindow = siteChat.chatWindows[ siteChat.getWindowMapKeyFromDomObject($window) ];

		if (chatWindow)
			chatWindow.stopBlinking();
		$title.removeClass("backgroundColorTransition");

		if($window.hasClass("expanded")) {
			$window.removeClass("expanded").addClass("collapsed");
			if(chatWindow)
				chatWindow.expanded = false;
			else
				sessionStorage[siteChat.namespace + "utilityExpanded"] = false;
		}
		else {
			$window.removeClass("collapsed").addClass("expanded").show();
			if(chatWindow) {
				chatWindow.expanded = true;
				var $outputBuffer = $window.find(".outputBuffer");
				$outputBuffer.scrollTop($outputBuffer[0].scrollHeight);
			}
			else
				sessionStorage[siteChat.namespace + "utilityExpanded"] = true;
		}

		setTimeout(function() {$title.addClass("backgroundColorTransition");}, 50);

		if(chatWindow)
			chatWindow.save();
	};

	siteChat.handleWindowCloseButtonClick = function(event) {
		event.preventDefault();
		event.stopPropagation();

		var $window = $(this).closest(".chatWindow");
		var conversationId = $window.data("conversation-id");
		var recipientUserId = $window.data("recipient-user-id");
		var uniqueIdentifier = conversationId != null ? parseInt(conversationId) : parseInt(recipientUserId);
		var windowKey = (conversationId != null ? "C" : "P") + uniqueIdentifier;

		if(conversationId != null) {
			var siteChatPacket = {
				command:"LeaveConversation",
				siteChatConversationId:conversationId
			};
			siteChat.sendPacket(siteChatPacket);
		}

		//Remove window from DOM
		$window.remove();

		var conversation = siteChat.chatWindows[ windowKey ];
		if(conversation) {
			if(conversationId != null && conversation.title == "Lobby")
				sessionStorage[siteChat.namespace + "lobbyForcefullyClosed"] = true;
			delete siteChat.chatWindows[ windowKey ];
		}
		if(siteChat.getLocalStorage("conversation" + windowKey))
			localStorage.removeItem(siteChat.namespace + "conversation" + windowKey);

		siteChat.setLocalStorage("conversationIdSet", JSON.stringify(siteChat.getConversationKeySet()));
	};

	siteChat.handleWindowInputSubmission = function(event) {
		if(event.which == 10 || event.which == 13) {
			event.preventDefault();
			var $this = $(this);
			var content = $this.val();

			if(content.length > 0) {
				var $window = $this.closest(".chatWindow");
				siteChat.submitChatMessage(content, $window.attr("id"), $window.data("conversation-id"), $window.data("recipient-user-id"));
			}

			$this.val("");
		}
	};

	siteChat.getConversationKeySet = function() {
		var conversationKeySet = [];
		for(var conversationKey in siteChat.chatWindows) {
			conversationKeySet.push(conversationKey);
		}

		return conversationKeySet;
	};

	siteChat.getUserIdSet = function() {

		var userIdSet = [];
		for(var userId in siteChat.userMap) {
			userIdSet.push(parseInt(userId));
		}
		return userIdSet;
	};

	siteChat.submitChatMessage = function(messageContent, chatWindowId, conversationId, recipientUserId) {
		var offset = 0, messageLength = messageContent.length;
		while(offset < messageLength) {
			var siteChatPacket = {
				command:"SendMessage",
				message:messageContent.substr(offset, siteChat.MAX_MESSAGE_LENGTH),
				siteChatConversationId:conversationId == null ? null : parseInt(conversationId),
				recipientUserId:recipientUserId == null ? null : parseInt(recipientUserId)
			};
			this.sendPacket(siteChatPacket);
			offset += siteChat.MAX_MESSAGE_LENGTH;
		}
	};

	siteChat.handleSocketOpen = function() {

		console.log("[" + new Date() + "] Socket Opened.");
		siteChat.socket.connected = true;
		if(siteChat.attemptReconnectIntervalId != null) {
			window.clearInterval(siteChat.attemptReconnectIntervalId);
			siteChat.attemptReconnectIntervalId = null;
		}
		$("#utilitywindow .exclamation").addClass("hidden");

		var conversationKeySet = siteChat.getConversationKeySet();
		var coversationIdToAuthCodeMap = {};
		var self = this;
		conversationKeySet.forEach(function(conversationKey) {

			var authCode = siteChat.chatWindows[ conversationKey ].authCode;
			var conversationId = siteChat.chatWindows[ conversationKey ].siteChatConversationId;
			if(authCode)
				coversationIdToAuthCodeMap[conversationId] = authCode;
		});

		var siteChatPacket = {
			command:"LogIn",
			userId:siteChat.userId,
			sessionId:siteChat.sessionId,
			conversationKeySet:conversationKeySet,
			coversationIdToAuthCodeMap:coversationIdToAuthCodeMap,
			conversationKeyToMostRecentMessageIdMap:{}
		};

		for(var conversationKey in siteChat.chatWindows) {

			var chatWindow = siteChat.chatWindows[ conversationKey ];
			if(chatWindow.messages && chatWindow.messages.length > 0) {
				siteChatPacket.conversationKeyToMostRecentMessageIdMap[ conversationKey ] = chatWindow.messages[ chatWindow.messages.length - 1 ].id;
			}
		}

		siteChat.attemptingLogin = true;
		siteChat.sendPacket(siteChatPacket);
	};

	siteChat.handleSocketClose = function() {

		console.log("[" + new Date() + "] Web Socket Closed.");
		siteChat.socket.connected = false;
		if(!siteChat.unloading && siteChat.tryReconnect) {

			if(siteChat.attemptReconnectIntervalId != null)
				window.clearInterval(siteChat.attemptReconnectIntervalId);

			if( !siteChat.attemptingLogin )
				siteChat.attemptReconnectIntervalId = setInterval(siteChat.attemptReconnect, 20000);
			$("#utilitywindow .exclamation").removeClass("hidden");
		}
		siteChat.attemptingLogin = false;
	};

	siteChat.handleRoomListJoinRoom = function(e) {

		e.preventDefault();
		e.stopPropagation();

		siteChat.sendConnectMessage($(e.target).siblings(".roomName").text());
	};

	siteChat.createChatWindow = function(conversationId, recipientUserId, createdByUserId, title, userIdSet, expanded, messages, save, blinking, width, height, authCode) {

		var chatWindowIdPrefix = (conversationId != null ? "C" : "P");
		var chatWindowUniqueIdentifier = (conversationId != null ? conversationId : recipientUserId);

		$("#chatPanel").append(this.chatWindowTemplate({
			idPrefix: chatWindowIdPrefix,
			uniqueIdentifier: chatWindowUniqueIdentifier,
			title: title,
			conversationId: conversationId,
			recipientUserId: recipientUserId,
			key: chatWindowIdPrefix + chatWindowUniqueIdentifier
		}));

		var $chatWindow = $("#chat" + chatWindowIdPrefix + chatWindowUniqueIdentifier);
		var $inputBuffer = $chatWindow.find(".inputBuffer");
		var $outputBuffer = $chatWindow.find(".outputBuffer");
		var $title = $chatWindow.find(".title");

		//Window defaults to an expanded state so autogrow can see the proper CSS values.
		$inputBuffer.autoGrow();
		//We now collapse it.
		$inputBuffer.removeClass("expanded").addClass("collapsed");

		setTimeout(function(){$title.addClass("backgroundColorTransition");}, 50);

		var chatWindow = new ChatWindow(
			conversationId,
			recipientUserId,
			createdByUserId,
			[],
			title,
			[],
			(width ? width : $chatWindow.width()),
			(height ? height : $outputBuffer.height()),
			authCode,
			expanded != undefined ? expanded : false,
			blinking != undefined ? blinking : false
		);

		$chatWindow.windowObject = chatWindow;
		$chatWindow.width(chatWindow.width);
		$outputBuffer.height(chatWindow.height);

		if(chatWindow.expanded)
			$chatWindow.addClass("expanded").removeClass("collapsed");
		else
			$chatWindow.addClass("collapsed").removeClass("expanded");

		if(siteChat.userId != createdByUserId)
			$title.find(".options").addClass("hidden");

		siteChat.chatWindows[chatWindowIdPrefix + chatWindowUniqueIdentifier] = chatWindow;
		
		if(messages && messages.length > 0) {
			siteChat.addSiteChatConversationMessages(messages, save, false);
		}
		
		if(blinking)
			chatWindow.startBlinking();

		if(save) {
			$inputBuffer.focus();
			chatWindow.save();
		}
		else {
			$outputBuffer.scrollTop($outputBuffer[0].scrollHeight);

			$outputBuffer.find("img").on("load", function(e) {
				$outputBuffer.scrollTop($outputBuffer[0].scrollHeight);
			});
		}
	};

	siteChat.getMessageMapKeyUserId = function(siteChatConversationMessage) {
		return (siteChatConversationMessage.recipientUserId == siteChat.userId ? siteChatConversationMessage.userId : siteChatConversationMessage.recipientUserId);
	};

	siteChat.getMessageMapKey = function(siteChatConversationMessage) {
		return siteChatConversationMessage.recipientUserId != null ? ("P" + siteChat.getMessageMapKeyUserId(siteChatConversationMessage)) : ("C" + siteChatConversationMessage.siteChatConversationId);
	};

	siteChat.getWindowMapKeyFromDomObject = function($window) {
		var conversationId = $window.data("conversation-id");
		var recipientUserId = $window.data("recipient-user-id");
		return conversationId != null ? ("C" + conversationId) : ("P" + recipientUserId);
	};
	
	siteChat.getUserColorStyle = function(siteChatUser) {
		return (siteChatUser.userColor != null && siteChatUser.userColor != "" ? ('color: #' + siteChatUser.userColor + ';"') : "");
	};
	
	siteChat.renderMessage = function(siteChatConversationMessage, siteChatUser) {
		var messageDate = new Date(siteChatConversationMessage.createdDatetime);
		var avatarUrl = siteChatUser.avatarUrl != '' ?  ('http://forum.mafiascum.net/download/file.php?avatar=' + siteChatUser.avatarUrl) : defaultAvatar;
		var messageDateString = zeroFill(messageDate.getHours(), 2) + ":" + zeroFill(messageDate.getMinutes(), 2);
		
		return	'<div class="message">'
			+	'	<a href="http://forum.mafiascum.net/memberlist.php?mode=viewprofile&u=' + siteChatUser.id + '"><div class="avatar-container"><img src="' + avatarUrl + '" class="profile"></img></div></a>'
			+	'	<div class="messageUserName"><a style="' + siteChat.getUserColorStyle(siteChatUser) + '" href="http://forum.mafiascum.net/memberlist.php?mode=viewprofile&u=' + siteChatUser.id + '">' + siteChatUser.name + '</a></div> <span class="messageTimestamp">(' + messageDateString + ')</span>'
			+	'	<div class="messagecontent">' + siteChat.parseBBCode(siteChatConversationMessage.message) + '</div>'
			+	'</div>'
	};

	siteChat.addSiteChatConversationMessage = function(siteChatConversationMessage, save, isNew, prepend) {
		siteChat.addSiteChatConversationMessages([siteChatConversationMessage], save, isNew, prepend);
	};

	siteChat.addSiteChatConversationMessages = function(siteChatConversationMessages, save, isNew, prepend) {
		var messageKeyToDataMap = {};
		for(var messageIndex in siteChatConversationMessages) {
			var siteChatConversationMessage = siteChatConversationMessages[messageIndex];
			
			var messageKey = siteChat.getMessageMapKey(siteChatConversationMessage);
			var siteChatUser = siteChat.userMap[ siteChatConversationMessage.userId ];
						
			if(!siteChat.chatWindows[messageKey] && siteChatConversationMessage.recipientUserId != null) {

				//If this is a private conversation & the window has not yet been created,
				//we should have enough information to make it ourselves.

				var windowSiteChatUser = siteChat.userMap[ siteChat.userId == siteChatConversationMessage.userId ? siteChatConversationMessage.recipientUserId : siteChatConversationMessage.userId ];
				siteChat.createChatWindow(null, windowSiteChatUser.id, null, windowSiteChatUser.name, [windowSiteChatUser.id], true, [], true, null, null, null);
			}
			
			var $outputBuffer = $("#chat" + messageKey + " .outputBuffer");
			
			if(!messageKeyToDataMap.hasOwnProperty(messageKey)) {
				messageKeyToDataMap[messageKey] = {
					numberOfMessagesAdded: 0,
					messagesHtmlToAdd: [],
					outputBuffer: $outputBuffer,
					messages: $outputBuffer.children(".messages"),
					messageObjects: []
				};
			}
			
			messageKeyToDataMap[messageKey]["messagesHtmlToAdd"].push(siteChat.renderMessage(siteChatConversationMessage, siteChatUser));
			messageKeyToDataMap[messageKey]["messageObjects"].push(siteChatConversationMessage);
		}
		
		//With all HTML generated and everything grouped by conversation, let's go through and add the messages.
		for(var messageKey in messageKeyToDataMap) {
			var $messages = messageKeyToDataMap[messageKey]["messages"];
			var $outputBuffer = messageKeyToDataMap[messageKey]["outputBuffer"];
			var isScrolledToBottom = $outputBuffer.get(0).scrollTop == ($outputBuffer.get(0).scrollHeight - $outputBuffer.get(0).offsetHeight);
			
			var $messageDomElements = $(messageKeyToDataMap[messageKey]["messagesHtmlToAdd"].join(""));
			
			(prepend ? $messages.prepend : $messages.append).bind($messages)($messageDomElements);
			
			$messageDomElements.find("a").each(siteChat.adjustElementColor);
			
			var chatWindow = siteChat.chatWindows[ messageKey ];
			
			for(var messageIndex in messageKeyToDataMap[messageKey]["messageObjects"]) {
				
				chatWindow.messages.splice(prepend ? 0 : chatWindow.messages.length, 0, messageKeyToDataMap[messageKey]["messageObjects"][messageIndex]);
			}
			var messagesLength = chatWindow.messages.length;
			if(messagesLength > siteChat.MAX_MESSAGES_PER_WINDOW) {
				chatWindow.messages.splice(0, messagesLength - siteChat.MAX_MESSAGES_PER_WINDOW);
			}
			
			if(chatWindow.expanded == false && isNew && siteChatConversationMessage.userId != siteChat.userId)
				chatWindow.startBlinking();
				
			if(isScrolledToBottom)
				$outputBuffer.get(0).scrollTop = $outputBuffer.get(0).scrollHeight;
			
			if(save)
				chatWindow.save();
		}
	};

	siteChat.addUser = function(siteChatUser, save, doNotAddToOnlineList) {
		if(!siteChat.userMap.hasOwnProperty(siteChatUser.id)) {
			siteChat.userMap[ siteChatUser.id ] = siteChatUser;

			if(!doNotAddToOnlineList)
				siteChat.addUserToOnlineList(siteChatUser, false);
			if(save)
				siteChat.saveUser(siteChatUser, true);
			}
	};

	siteChat.addUserToOnlineList = function(siteChatUser, onlyAddToHTML) {
		var indexToInsert = _.sortedIndex(siteChat.onlineUserIdSet, siteChatUser.id, function(userId) { return siteChat.userMap[ userId ].name.toLowerCase() });
		if(indexToInsert < siteChat.onlineUserIdSet.length && siteChat.onlineUserIdSet[ indexToInsert ] == siteChatUser.id) {
			return;
		}

		var active = siteChatUser.lastActivityDatetime ? ((new Date().getTime() - siteChatUser.lastActivityDatetime) / 1000) < (60) * (5) : false;
		var html
			= '<li class="username" id="username' + siteChatUser.id + '"><span class="onlineindicator ' + (active ? "active" : "idle") + '"></span>'
			+ '<span style="' + siteChat.getUserColorStyle(siteChatUser) + '">' + siteChatUser.name + '</span>'
			+ '</li>';
		var $userDomElement = $(html);
		$userDomElement.find("*").each(siteChat.adjustElementColor);
		
		if(indexToInsert >= siteChat.onlineUserIdSet.length) {
			$("#onlinelist").append($userDomElement);
			if(!onlyAddToHTML)
				siteChat.onlineUserIdSet.push(siteChatUser.id);
		}
		else {
			$("#username" + siteChat.onlineUserIdSet[indexToInsert]).before($userDomElement);
			if(!onlyAddToHTML)
				siteChat.onlineUserIdSet.splice(indexToInsert, 0, siteChatUser.id);
		}

		$("#username" + siteChatUser.id).data("username", siteChatUser.name).data("user-id", siteChatUser.id);

		if(!onlyAddToHTML)
			siteChat.setLocalStorage("onlineUserIdSet", JSON.stringify(siteChat.onlineUserIdSet));
		siteChat.onlineUsers += 1;
		$('#onlinelisttitle .usercount').html('(' + siteChat.onlineUsers + ')');
	};

	siteChat.saveUser = function(siteChatUser, saveUserIdSet) {
		if(saveUserIdSet)
			siteChat.setLocalStorage("userIdSet", JSON.stringify(siteChat.getUserIdSet()));
		siteChat.setLocalStorage("user" + siteChatUser.id, JSON.stringify(siteChatUser));
	};

	siteChat.processPendingMessages = function() {
		for(var index = 0;index < siteChat.pendingMessages.length;++index) {
			var siteChatConversationMessage = siteChat.pendingMessages[ index ];
			var siteChatUser = siteChat.userMap[ siteChatConversationMessage.userId ];

			if(!siteChatUser) {
				console.log("Still could not process pending message. User ID: " + siteChatConversationMessage.userId);
				break;
			}
			siteChat.addSiteChatConversationMessage(siteChatConversationMessage, true, true);
			siteChat.pendingMessages.splice(index, 1);
			--index;
		}
	};

	siteChat.isInChatRoom = function(roomName) {
		return _.find(siteChat.chatWindows, function(chatWindow) {return chatWindow.title.toLowerCase() == roomName.toLowerCase();});
	};

	siteChat.handleSocketMessage = function(message) {
		var data = message.data;
		var siteChatPacket = JSON.parse(data);

		if(!siteChat.commandHandlers.hasOwnProperty(siteChatPacket.command)) {

			console.log("Error : Unknown command received: `" + siteChatPacket.command + "`");
		}
		else {
			var commandHandler = siteChat.commandHandlers[siteChatPacket.command];

			commandHandler(siteChat, siteChatPacket);
		}
	};

	siteChat.generateRooms = function (save) {
		if(!siteChat.rooms)
			return;

		_.forEach(siteChat.rooms, function(room) {

			if(room === undefined || room === null)
				return;

			$("#roomstab").append(siteChat.roomListRoomTemplate({
				roomId: room.id,
				expandIcon: room.expanded ? '-' : '+',
				roomName: room.name,
				numberOfUsers: room.userIdSet.length,
				users: room.userIdSet.map(function(userId) {
					var siteChatUser = siteChat.userMap[userId];
					var active = siteChatUser.lastActivityDatetime ? ((new Date().getTime() - siteChatUser.lastActivityDatetime) / 1000) < (60) * (5) : false;
					return {
						roomNameCleaned: room.name.replace(/[^A-Za-z0-9]/g, ''),
						userId: siteChatUser.id,
						activeClass: active ? "active" : "idle",
						userName: siteChatUser.name
					}
				})
			}));
		});

		var $roomTitles = $(".roomtitle");

		$roomTitles.on('click', siteChat.roomlistexpand);
		$roomTitles.find(".joinroom").on("click", this.handleRoomListJoinRoom);

		if (save) {
			siteChat.setLocalStorage("rooms", JSON.stringify(siteChat.rooms));
			siteChat.setLocalStorage("userIdSet", JSON.stringify(siteChat.getUserIdSet()));
		}
	};

	siteChat.sendPacket = function(packetObject) {
		if(siteChat.socket.connected)
			siteChat.socket.send(JSON.stringify(packetObject));
	};

		siteChat.createChatPanel = function() {
		$("body").append("<div class='chatPanel' id='chatPanel'></div>");
	};

	siteChat.createUtilityWindow = function() {
		$("#chatPanel").prepend(siteChat.utilityWindowTemplate({
			windowStateClass: sessionStorage[siteChat.namespace + "utilityExpanded"] == "true" ? "expanded" : "collapsed",
		}));
		var index = siteChat.tabs.push({}) -1;
		siteChat.tabs[index].id = 0;
		index =	siteChat.tabs.push({}) -1;
		siteChat.tabs[index].id = 1;
		index = siteChat.tabs.push({}) -1;
		siteChat.tabs[index].id = 2;

		$("#utilitywindow .title").addClass("backgroundColorTransition");
	};

	siteChat.disableChat = function() {
		xmlhttp=new XMLHttpRequest();
		xmlhttp.open("GET","remove_ms_chat.php", false);
		xmlhttp.send();
		response = xmlhttp.responseText;
		if (response == 'confirm'){
			siteChat.close();
			alert ('Chat disabled. It can be enable through the user control panel');
		}
	};

	siteChat.setActiveTab = function(id) {
		$('#tab' + id).addClass('active');
		$($('#tab' + id).children('a').attr('href')).css('display','block');
		for (var i= 0; i < siteChat.tabs.length; i++){
			if(id != siteChat.tabs[i].id){
				$($('#tab' + siteChat.tabs[i].id).children('a').attr('href')).css('display','none');
				$('#tab' + siteChat.tabs[i].id).removeClass('active');
			}
		}
	};

	siteChat.populateUtilityWindow = function () {
		siteChat.generateRooms(false);
		if (siteChat.onlineGroup.expanded == false) {
			$('#onlinelist').css('display','none');
			$('#onlinelisttitle .expand-icon').html('+');
		}
		siteChat.setActiveTab(siteChat.selectedTab == null ? 0 : siteChat.selectedTab);

		$("#utilitywindow .inputBuffer").bind("keypress", siteChat.handleWindowInputSubmission);
		$("#onlinelisttitle").bind('click', this.onlinelistexpand);

		$('#tab0, #tab1, #tab2').bind('click', function() {
			siteChat.setActiveTab(Number($(this).data("tab-id")));
			siteChat.setLocalStorage('selectedTab', Number($(this).data("tab-id")));
			return false;
		});
	};

	siteChat.onlinelistexpand = function() {
		if (siteChat.onlineGroup.expanded == false) {
			$('#onlinelist').css('display', 'block');
			$('#onlinelisttitle .expand-icon').html('-');
			siteChat.onlineGroup.expanded = true;
			localStorage[siteChat.namespace +'onlineGroup'] = JSON.stringify(siteChat.onlineGroup);
		}
		else {
			$('#onlinelist').css('display', 'none');
			$('#onlinelisttitle .expand-icon').html('+');
			siteChat.onlineGroup.expanded = false;
			localStorage[siteChat.namespace +'onlineGroup'] = JSON.stringify(siteChat.onlineGroup);
		}
	};

	siteChat.roomlistexpand = function() {
		if ($(this).next().css('display') == 'none') {
			$(this).next().css('display', 'block');
			$('.expand-icon',this).html('-');
			var id = $(this).parent().attr('id').match(/\d+/);
			siteChat.rooms[id].expanded = true;
			localStorage[siteChat.namespace +'rooms'] = JSON.stringify(siteChat.rooms);
		}
		else {
			$(this).next().css('display', 'none');
			$('.expand-icon',this).html('+');
			var id = $(this).parent().attr('id').match(/\d+/);
			siteChat.rooms[id].expanded = false;
			localStorage[siteChat.namespace +'rooms'] = JSON.stringify(siteChat.rooms);
		}
	};

	siteChat.heartbeat = function() {
		var siteChatPacket = {};
		siteChatPacket.command = "Heartbeat";
		siteChatPacket.isAlive = "true";

		if(siteChat.socket.connected)
			siteChat.sendPacket(siteChatPacket);
	};

	siteChat.generateCommandHandlers = function(siteChat, siteChatPacket) {
		
		var commandHandlers = {};
		commandHandlers["LogIn"] = function(siteChat, siteChatPacket) {

			siteChat.attemptingLogin = false;
			if(siteChatPacket.missedSiteChatConversationMessages && siteChatPacket.missedSiteChatConversationMessages.length > 0) {
				var missedMessagesLength = siteChatPacket.missedSiteChatConversationMessages.length;
				for(var messageIndex = 0;messageIndex < missedMessagesLength;++messageIndex) {
					var message = siteChatPacket.missedSiteChatConversationMessages[ messageIndex ];
					if(!siteChat.userMap[message.userId])
						console.log("Missed Message. User ID: " + message.userId + ", In Map: " + siteChat.userMap[message.userId]);
					else
						siteChat.addSiteChatConversationMessage(message, true, true);
				}
			}

			if(siteChat.firstConnectionThisPageLoad && siteChat.autoJoinLobby && !siteChat.isInChatRoom("Lobby")) {
				siteChat.sendConnectMessage("Lobby");
			}
			siteChat.firstConnectionThisPageLoad = false;
		};

		commandHandlers["Connect"] = function(siteChat, siteChatPacket) {
			var chatWindow = siteChat.chatWindows["C" + siteChatPacket.siteChatConversationId];
			if(chatWindow == undefined) {//Create chat window
				var siteChatUserIdSet = [];
				for(var siteChatUserIndex = 0;siteChatUserIndex < siteChatPacket.users.length;++siteChatUserIndex) {
					var siteChatUser = siteChatPacket.users[ siteChatUserIndex ];
					siteChatUserIdSet.push(siteChatUser.id);

					siteChat.addUser(siteChatUser, true);
				}
				//Setting recipientUserId to null because I do not believe we will be "connecting" to private conversations.
				siteChat.createChatWindow(siteChatPacket.siteChatConversationId, null, siteChatPacket.createdByUserId, siteChatPacket.titleText, siteChatUserIdSet, true, [], true, false, null, null, siteChatPacket.authCode);
			}
			else if(siteChatPacket.authCode != null) {
				chatWindow.authCode = siteChatPacket.authCode;
				chatWindow.save();
			}

			$.fancybox.close();
		};

		commandHandlers["NewMessage"] = function(siteChat, siteChatPacket) {
			var message = siteChatPacket.siteChatConversationMessage;
			var uniqueIdentifier = message.recipientUserId != null ? (siteChat.getMessageMapKeyUserId(message)) : message.siteChatConversationId;
			var prefix = message.recipientUserId != null ? "P" : "C";
			var key = prefix + uniqueIdentifier;

			if(prefix == "P" || siteChat.chatWindows[ key ] != undefined) {
				var siteChatUser = siteChat.userMap[ message.userId ];

				if(!siteChatUser || siteChat.pendingMessages.length > 0) {
					//If for some reason we have no data on a user(or if there are other pending messages),
					//queue the message to process later and kick off a user lookup request.
					console.log("Adding pending message. Site Chat User: " + siteChatUser + ", Previous Pending Messages: " + siteChat.pendingMessages.length);
					siteChat.pendingMessages.push(message);

					if(!siteChatUser) {
						var lookupUserPacket = {};
						lookupUserPacket.command = "LookupUser";
						lookupUserPacket.userId = message.userId;

						siteChat.sendPacket(lookupUserPacket);
					}
				}
				else
					siteChat.addSiteChatConversationMessage(message, true, true);
			}
		};

		commandHandlers["UserJoin"] = function(siteChat, siteChatPacket) {

			if(!siteChat.userMap.hasOwnProperty(siteChatPacket.siteChatUser.id))
				siteChat.addUser(siteChatPacket.siteChatUser, true);

			if(siteChat.chatWindows.hasOwnProperty("C" + siteChatPacket.siteChatConversationId))
				siteChat.chatWindows[ "C" + siteChatPacket.siteChatConversationId ].userIdSet.push( siteChatPacket.siteChatUser.id );
		};

		commandHandlers["LookupUser"] = function(siteChat, siteChatPacket) {

			if(!siteChatPacket.siteChatUser) {
				console.log("Lookup for user #" + siteChatPacket.userId + " returned no result. Removing corresponding pending messages.");
				for(var index = 0;index < siteChat.pendingMessages.length;++index) {
					if(siteChat.pendingMessages[index].userId = siteChatPacket.userId) {
						siteChat.pendingMessages.splice(index, 1);
						--index;
					}
				}
			}
			else {
				console.log("Adding User From Lookup. User ID: " + siteChatPacket.siteChatUser.id + ", Name: " + siteChatPacket.siteChatUser.name);
				siteChat.addUser(siteChatPacket.siteChatUser, true);
				siteChat.processPendingMessages();
			}
		};

		commandHandlers["LeaveConversation"] = function(siteChat, siteChatPacket) {

			var chatWindow = siteChat.chatWindows[ "C" + siteChatPacket.siteChatConversationId ];
			if(chatWindow)
				chatWindow.userIdSet.splice($.inArray(siteChatPacket.userId, chatWindow.userIdSet), 1);
		};

		commandHandlers["UserList"] = function(siteChat, siteChatPacket) {
			var oldRooms = siteChat.rooms;
			siteChat.onlineUsers = 0;
			siteChat.onlineUserIdSet = [];
			siteChat.rooms = {};

			$("#onlinelist").html("");
			$("#roomstab").html("");

			//Import users.
			siteChatPacket.siteChatUsers.forEach(function(siteChatUser) {
				if(siteChat.userMap[siteChatUser.id] == null)
					siteChat.addUser(siteChatUser, true, true);
				siteChat.addUserToOnlineList(siteChatUser, false);
				siteChat.saveUser(siteChatUser, false);
			});

			//Import rooms.
			siteChatPacket.siteChatConversations.forEach(function(conversationFromPacket) {
				var room = (oldRooms[conversationFromPacket.id] != null ? oldRooms[conversationFromPacket.id] : {});
				room.expanded = room.expanded == null ? false : room.expanded;
				room.userIdSet = conversationFromPacket.userIdSet;
				room.name = conversationFromPacket.name;
				room.id = conversationFromPacket.id;
				room.createdByUserId = conversationFromPacket.createdByUserId;
				siteChat.rooms[room.id] = room;
			});

			siteChat.generateRooms(true);
		};

		commandHandlers["PasswordRequired"] = function(siteChat, siteChatPacket) {

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
		};

		commandHandlers["IncorrectPassword"] = function(siteChat, siteChatPacket) {

			$("#siteChatPasswordLightbox").find("ul").html("<li>The password you entered is incorrect.</li>");
		};

		commandHandlers["SetPassword"] = function(siteChat, siteChatPacket) {

			if(siteChatPacket.errorMessage)
				$("#siteChatSetPasswordLightbox").find("ul").html(siteChat.liTemplate({content: siteChatPacket.errorMessage}));
			else
				$.fancybox.close();
		};

		commandHandlers["LoadMessages"] = function(siteChat, siteChatPacket) {

			if(siteChatPacket.errorMessage) {
				alert("Error: " + siteChatPacket.errorMessage);
			}
			else {
				siteChatPacket.messages.forEach(function(message) {
					if(!siteChat.userMap.hasOwnProperty(message.userId))
						siteChat.addUser(siteChatPacket.userMap[message.userId], true, true);
					siteChat.addSiteChatConversationMessage(message, true, false, true);
				});
			}
		};

		return commandHandlers;
	};
	
	siteChat.setup = function(sessionId, userId, autoJoinLobby, siteChatUrl, siteChatProtocol) {
		
		siteChat.sessionId = sessionId;
		siteChat.userId = userId;
		siteChat.autoJoinLobby = autoJoinLobby && !sessionStorage[siteChat.namespace + "lobbyForcefullyClosed"];
		siteChat.siteChatUrl = siteChatUrl;
		siteChat.siteChatProtocol = siteChatProtocol;
		siteChat.adjustElementColor = typeof window.adjustColor === "function" ? window.adjustColor : function(){};

		if(!supportsHtml5Storage() || (typeof(WebSocket) != "function" && typeof(WebSocket) != "object"))
			return;

		this.commandHandlers = this.generateCommandHandlers();

		//Before doing anything, ensure that the user that will attempt to connect is the same one for which we have localstorage data.
		if(localStorage[siteChat.namespace + "userId"] == null || parseInt(localStorage[siteChat.namespace + "userId"]) != siteChat.userId) {
			siteChat.clearLocalStorage();
			localStorage[siteChat.namespace + "userId"] = siteChat.userId;
		}

		$(window).bind("beforeunload", function() {

			siteChat.unloading = true;
			if(siteChat.socket.connected)
				siteChat.socket.close();
		});


		$(document).on("submit", "#joinConversationForm", function(event) {
			event.preventDefault();
			var $input = $(this).children("input");
			siteChat.sendConnectMessage($input.val());
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
			var conversation = siteChat.chatWindows[ siteChat.getWindowMapKeyFromDomObject($window) ];

			var $lightbox = $("#siteChatSetPasswordLightbox");
			var $password = $lightbox.find("input[name='Password']");

			$lightbox.find(".conversationName").text(conversation.title);
			$password.val("");
			$lightbox.find("input[name='ConversationID']").val(conversation.siteChatConversationId);

			$window.find(".menu").removeClass("expanded");

			$.fancybox.open("#siteChatSetPasswordLightbox");

			$password.focus();
		});

		$(document).on("click", "#chatPanel .chatWindow .title", siteChat.handleWindowTitleClick);
		$(document).on("click", "#chatPanel .chatWindow .title .close", siteChat.handleWindowCloseButtonClick);
		$(document).on("keypress", "#chatPanel .chatWindow .inputBuffer", siteChat.handleWindowInputSubmission);
		$(document).on("click", "#utilitywindow .username", siteChat.handleUserListUsernameClick);
		$(document).on("mousewheel", "#onlinelistcontainer, #chatPanel .outputBuffer, #roomstab", function(e) {

			var wheelDistance = function(evt){
				if (!evt) evt = event;
				var w=evt.wheelDelta, d=evt.detail;
				if (d) {
					if (w) return w/d/40*d>0?1:-1;	// Opera
					else return -d/3;				// Firefox;
				}
				else return w/120;					// IE/Safari/Chrome
			};

			var wheelDirection = function(evt){
				if (!evt) evt = event;
				return (evt.detail<0) ? 1 : (evt.wheelDelta>0) ? 1 : -1;
			};

			this.scrollTop -= wheelDistance(e.originalEvent) * 15;

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

			if(siteChat.dragWindow)
				return;
			e.stopPropagation();
		});

		$(document).on("mouseenter", "#chatPanel > .chatWindow.conversation > .chatWindowOuter > .chatWindowInner", function(e) {

			if(!siteChat.dragWindow)
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

			siteChat.dragWindow = {
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
				siteChat.dragWindow.edge = "topleft";
			else if(top < 3)
				siteChat.dragWindow.edge = "top";
			else if(left < 3)
				siteChat.dragWindow.edge = "left";
		});

		$(document).on("mousedown", "#chatPanel > .chatWindow.conversation > .chatWindowOuter > .chatWindowInner", function(e) {

			e.stopPropagation();
		});

		$(document).on("mouseup", "body", function(e) {

			if(siteChat.dragWindow) {
				var $window = siteChat.dragWindow.window;
				var $outputBuffer = siteChat.dragWindow.outputBuffer;
				var chatWindow = siteChat.chatWindows[$window.data("key")];

				chatWindow.height = $outputBuffer.height();
				chatWindow.width = $window.width();
				chatWindow.save();

				siteChat.dragWindow = null;
			}
		});

		$(document).on("transitionend", "#chatPanel .title.blink", function(e) {

			if(e.originalEvent.propertyName == "background-color")
				$(e.target).toggleClass("blinkEnd");
		});

		$(document).on("mousemove", "body", function(e) {

			if(siteChat.dragWindow) {
				var $window = siteChat.dragWindow.window;
				var $outputBuffer = siteChat.dragWindow.outputBuffer;
				if(siteChat.dragWindow.edge == "left" || siteChat.dragWindow.edge == "topleft")
					$window.css("width", siteChat.dragWindow.startWindowWidth + (siteChat.dragWindow.startPageX - e.pageX) );
				if(siteChat.dragWindow.edge == "top" || siteChat.dragWindow.edge == "topleft")
					$outputBuffer.css("height", siteChat.dragWindow.startWindowHeight + (siteChat.dragWindow.startPageY - e.pageY) );
			}
		});

		$(document).on("click", "#chatPanel a.chatroomlink", function(e) {

			e.preventDefault();
			var roomName = $(this).data("room");
			if(!siteChat.isInChatRoom(roomName))
				siteChat.sendConnectMessage(roomName);
		});

		$(document).on("submit", "#siteChatPasswordForm", function(e) {

			e.preventDefault();
			var $form = $(this);

			var password = $form.find("input[name='Password']").val();
			var conversationName = $form.find("input[name='ConversationName']").val();

			siteChat.sendConnectMessage(conversationName, password);
		});

		$(document).on("submit", "#siteChatSetPasswordForm", function(e) {
			e.preventDefault();
			var $form = $(this);
			var password = $form.find("input[name='Password']").val();
			var conversationId = parseInt($form.find("input[name='ConversationID']").val());

			var packet = {
				command:"SetPassword",
				conversationId:conversationId,
				password:password
			};

			siteChat.sendPacket(packet);
		});

		$(document).on("click", "#chatPanel .loadMore", function(e) {
			e.preventDefault();
			e.stopPropagation();

			var $chatWindow = $(e.target).closest(".chatWindow");
			var chatWindow = siteChat.chatWindows[ siteChat.getWindowMapKeyFromDomObject($chatWindow) ];
			var oldestMessage = chatWindow.messages[0];
			siteChat.sendPacket({
				command: "LoadMessages",
				conversationKey: chatWindow.getWindowMapKey(),
				oldestMessageId: oldestMessage ? oldestMessage.id : null
			});
		});

		siteChat.setupWebSocket();
		siteChat.createChatPanel();
		siteChat.createUtilityWindow();
		siteChat.loadFromLocalStorage();
		siteChat.populateUtilityWindow();
		setInterval(siteChat.heartbeat, 150000);
		$("#disable_button").on("click", siteChat.disableChat);
		siteChat.createPasswordLightbox();
	};

	siteChat.createPasswordLightbox = function() {

		if($("#siteChatPasswordLightbox").length == 0) {
			$("body").append("<div style='display:none;'><div id='siteChatPasswordLightbox' class='siteChatLightbox'><ul></ul>The room `<span class='conversationName'></span>` requires a password.<br/><br/><form id='siteChatPasswordForm'>Password: <input type='password' name='Password' /> <input type='hidden' name='ConversationName' /> <button type='submit' class='button1'>Join Room</button></form></div</div>");
		}

		if($("#siteChatSetPasswordLightbox").length == 0) {
			$("body").append("<div style='display:none;'><div id='siteChatSetPasswordLightbox' class='siteChatLightbox'><ul></ul>Set password for the room `<span class='conversationName'></span>`.<br/><br/><form id='siteChatSetPasswordForm'>New Password: <input type='password' name='Password' /> <input type='hidden' name='ConversationID' /> <button type='submit' class='button1'>Set Password</button></form></div</div>");
		}
	};

	siteChat.sendConnectMessage = function(conversationName, password) {
		siteChat.sendPacket({command: "Connect", siteChatConversationName: conversationName, password: password});
	};

	siteChat.close = function() {
		$("#chatPanel").remove();
		siteChat.socket.close();
		siteChat.tryReconnect = false;
	};

	siteChat.setupWebSocket = function() {
		console.log("[" + new Date() + "] CONNECTING");
		siteChat.socket = new WebSocket(siteChat.siteChatUrl, siteChat.siteChatProtocol);
		siteChat.socket.connected = false;
		siteChat.socket.onopen = siteChat.handleSocketOpen;
		siteChat.socket.onclose = siteChat.handleSocketClose;
		siteChat.socket.onmessage = siteChat.handleSocketMessage;
		siteChat.attemptingLogin = false;
	};

	return siteChat;
})();


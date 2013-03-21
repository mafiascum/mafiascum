var client = null;
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

function ChatWindow()
{
	this.id = undefined;
	this.title = undefined;
	this.expanded = false;
	this.userIdSet = [];

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

	this.loadFromLocalStorage = function()
	{
		console.log("BEGIN LOAD FROM LOCAL STORAGE...");

		var userIdSet, converstionIdSet;
		//Load users.
		if(localStorage["userIdSet"])
		{
			userIdSet = JSON.parse(localStorage["userIdSet"]);
			for(var index = 0;index < userIdSet.length;++index)
			{
				var userId = userIdSet[ index ];

				console.log("LOADING USER #" + userId);

				if(localStorage["user" + userId])
				{
					try {
						var siteChatUser = JSON.parse(localStorage["user" + userId]);
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
		if(localStorage["conversationIdSet"])
		{
			conversationIdSet = JSON.parse(localStorage["conversationIdSet"]);
			for(var index = 0;index < conversationIdSet.length;++index)
			{
				var siteChatConversationId = conversationIdSet[ index ];

				console.log("LOADING CONVERSATION #" + siteChatConversationId);

				var siteChatConversation = JSON.parse(localStorage["conversation" + siteChatConversationId]);
				client.createChatWindow(siteChatConversationId, siteChatConversation.title, siteChatConversation.userIdSet, siteChatConversation.expanded, false);
				
				console.log("Loaded Chat Window Marked Expanded: " + siteChatConversation.expanded);
			}
		}
	}

	this.handleWindowTitleClick = function(event)
	{
		event.preventDefault();
		event.stopPropagation();
		var $window = $(this).closest(".chatWindow");
		var siteChatConversation = client.chatWindows[ parseInt($window.attr("id").replace("chat", "")) ];

		if($window.hasClass("expanded"))
		{
			$window.removeClass("expanded");
			$window.addClass("collapsed");
			if(siteChatConversation)
				siteChatConversation.expanded = false;
		}
		else
		{
			$window.removeClass("collapsed");
			$window.addClass("expanded");
			if(siteChatConversation)
				siteChatConversation.expanded = true;
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

		if(localStorage["conversation" + conversationId])
			delete localStorage["conversation" + conversationId];

		localStorage["conversationIdSet"] = JSON.stringify(client.getConversationIdSet());
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
		console.log("Connection Opened.");
		
					var siteChatPacket = new Object();
		siteChatPacket.command = "LogIn";
		siteChatPacket.userId = client.userId;
		siteChatPacket.sessionId = client.sessionId;
		siteChatPacket.conversationIdSet = client.getConversationIdSet();
	
		client.sendSiteChatPacket(siteChatPacket);
	
		var siteChatPacket = new Object();
		siteChatPacket.command = "Connect";
		siteChatPacket.siteChatConversationName = "ScumChat";
	
		client.sendSiteChatPacket(siteChatPacket);
	}
	
	this.handleSocketClose = function()
	{
		console.log("Connection Closed.");
	}

	this.createChatWindow = function(conversationId, title, userIdSet, expanded, save)
	{
	
		console.log("Creating Chat Window.");
	
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

		if(save)
		{
			client.saveChatWindow(chatWindow);
		}
	}

	this.saveChatWindow = function(chatWindow)
	{
		console.log("SAVING CONVERSATION #" + chatWindow.siteChatConversationId);

		localStorage["conversationIdSet"] = JSON.stringify(client.getConversationIdSet());
		localStorage["conversation" + chatWindow.siteChatConversationId] = JSON.stringify(chatWindow);
	}

	this.addUser = function(siteChatUser, save)
	{
		console.log("ADDING USER #" + siteChatUser.id + ": " + siteChatUser.name + ", Save: " + save);
		client.userMap[ siteChatUser.id ] = siteChatUser;

		if(save)
		{
			client.saveUser(siteChatUser);
		}
	}

	this.saveUser = function(siteChatUser)
	{
		console.log("SAVING USER #" + siteChatUser.id);

		var userIdSet = [];
		for(var userId in client.userMap)
		{
			userIdSet.push(parseInt(userId));
		}

		localStorage["userIdSet"] = JSON.stringify(userIdSet);
		localStorage["user" + siteChatUser.id] = JSON.stringify(siteChatUser);
	}

	this.createMessage = function(siteChatPacket)
	{
		var siteChatUser = client.userMap[ siteChatPacket.userId ];
		$("#chat" + siteChatPacket.siteChatConversationId + " .outputBuffer").append
		(
				'<div class="message">'
			+	'	<img src="' + siteChatUser.avatarUrl + '" class="profile"></img>'
			+	'	<div class="content">' + siteChatPacket.message + '</div>'
			+	'</div>'
		);

		var outputBuffer = $("#chat" + siteChatPacket.siteChatConversationId + " .outputBuffer").get(0);
		outputBuffer.scrollTop = outputBuffer.scrollHeight;
	}

	this.processPendingMessages = function()
	{
		for(var index = 0;index < client.pendingMessages.length;++index)
		{
			var siteChatPacket = client.pendingMessages[ index ];
			var siteChatUser = client.userMap[ siteChatPacket.userId ];

			if(!siteChatUser)
			{
				console.log("Still could not process pending message. User ID: " + siteChatPacket.userId);
				break;
			}

			client.createMessage(siteChatPacket);
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
		}
		else if(siteChatPacket.command == "Connect")
		{
			console.log("Connect Message Received.");

			if(client.chatWindows[siteChatPacket.siteChatConversationId] == undefined)
			{//Create the chat window.

				var siteChatUserIdSet = [];
				for(var siteChatUserIndex = 0;siteChatUserIndex < siteChatPacket.users.length;++siteChatUserIndex)
				{
					var siteChatUser = siteChatPacket.users[ siteChatUserIndex ];
					siteChatUserIdSet.push(siteChatUser.id);

					client.addUser(siteChatUser, true);
				}

				client.createChatWindow(siteChatPacket.siteChatConversationId, siteChatPacket.titleText, siteChatUserIdSet, true, true);
			}
		}
		else if(siteChatPacket.command == "NewMessage")
		{
			console.log("New Message. User ID: " + siteChatPacket.userId + ", Conversation ID: " + siteChatPacket.siteChatConversationId + ", Message: " + siteChatPacket.message);

			if(client.chatWindows[ siteChatPacket.siteChatConversationId ] != undefined)
			{
				var siteChatUser = client.userMap[ siteChatPacket.userId ];

				if(!siteChatUser || client.pendingMessages.length > 0)
				{
					console.log("Adding pending message. Site Chat User: " + siteChatUser + ", Previous Pending Messages: " + client.pendingMessages.length);
					client.pendingMessages.push(siteChatPacket);

					var lookupUserPacket = new Object();
					lookupUserPacket.command = "LookupUser";
					lookupUserPacket.userId = siteChatPacket.userId;

					client.sendSiteChatPacket(lookupUserPacket);
				}
				else
					client.createMessage(siteChatPacket);
			}
		}
		else if(siteChatPacket.command == "UserJoin")
		{
			if(client.userMap[ siteChatPacket.siteChatUser.id ] == undefined)
				client.addUser(siteChatPacket.siteChatUser, true);
			
			if(client.chatWindows[ siteChatPacket.siteChatConversationId ] != undefined)
				client.chatWindows[ siteChatPacket.siteChatConversationId ].userIdSet.push( siteChatPacket.siteChatUser.id );

			console.log("User `" + siteChatPacket.siteChatUser.name + "` has joined chat #" + siteChatPacket.siteChatConversationId + ".");
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

				console.log("Processing pending messages...");
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

	this.setup = function(sessionId, userId)
	{
		client.sessionId = sessionId;
		client.userId = userId;
		if(!supportsHtml5Storage())
		{
			return;
		}

		client.socket = new WebSocket("ws://localhost:4241", "site-chat");
		client.socket.onopen = client.handleSocketOpen;
		client.socket.onclose = client.handleSocketClose;
		client.socket.onmessage = client.handleSocketMessage;

		client.createChatPanel();
		client.loadFromLocalStorage();
	}
}
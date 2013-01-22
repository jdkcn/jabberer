package com.jdkcn.jabber.robot;

import java.io.Serializable;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

public class RetryTask implements Serializable{

	private static final long serialVersionUID = 1263284775133155591L;

	private Chat chat;
	
	private Message message;

	public Chat getChat() {
		return chat;
	}

	public void setChat(Chat chat) {
		this.chat = chat;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}
	
}

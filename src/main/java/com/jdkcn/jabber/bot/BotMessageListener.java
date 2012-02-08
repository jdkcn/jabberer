/**
 * Project:jabberer
 * Copyright 2004-2010 Homolo Co., Ltd. All rights reserved.
 */
package com.jdkcn.jabber.bot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

/**
 * @author <a href="mailto:rory.cn@gmail.com">Rory</a>
 * @since Mar 17, 2010 2:40:32 PM
 * @version $Id$
 */
public class BotMessageListener implements MessageListener {

	private List<RosterEntry> rosterEntries = new ArrayList<RosterEntry>();
	
	private Roster roster;
	
	private XMPPConnection connection;
	
	private boolean sendOfflineMessage = true;
	
	private static final List<String> commandList;
	
	static {
		commandList = new ArrayList<String>();
		commandList.add("/help");
		commandList.add("/h");
		commandList.add("/?");
		commandList.add("/list");
		commandList.add("/l");
		commandList.add("/online");
		commandList.add("/o");
	}
	
	public void setSendOfflineMessage(boolean sendOfflineMessage) {
    	this.sendOfflineMessage = sendOfflineMessage;
    }

	public BotMessageListener(XMPPConnection connection, Roster roster,Collection<RosterEntry> rosterEntries, Boolean sendOfflineMessage) {
		this.connection = connection;
		this.roster = roster;
		this.rosterEntries.addAll(rosterEntries);
		if (sendOfflineMessage != null) {
			this.sendOfflineMessage = sendOfflineMessage; 
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.jivesoftware.smack.MessageListener#processMessage(org.jivesoftware.smack.Chat, org.jivesoftware.smack.packet.Message)
	 */
	@Override
	public void processMessage(Chat chat, Message message) {
		String body = StringUtils.trim(message.getBody());
		String from = message.getFrom();
		System.out.println(from);
		String name = org.jivesoftware.smack.util.StringUtils.parseName(from);
		String server = org.jivesoftware.smack.util.StringUtils.parseServer(from);
		String sender = name + "@" + server;
		if (message.getType() == Message.Type.chat && StringUtils.isNotBlank(body)) {
			if (commandList.contains(body.toLowerCase())) {
				processCommand(body, sender);
			} else {
				for (RosterEntry entry : rosterEntries) {
					if (entry.getUser().equalsIgnoreCase(sender)) {
						continue;
					}
					Presence presence = roster.getPresence(entry.getUser());
					if (!sendOfflineMessage && !presence.isAvailable()) {
						continue;
					}
					System.out.println("sending to :" + entry.getUser() + "[" + entry.getName() + "]");
					Message msg = new Message(entry.getUser(), Message.Type.chat);
					msg.setBody("<" + findPosterName(sender, name) + "> " + body);
					try {
						connection.getChatManager().createChat(entry.getUser(), new MessageListener() {
							public void processMessage(Chat chat, Message msg) {
								// do something..?
							}
						}).sendMessage(msg);
					} catch (XMPPException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * @param sender
	 * @param name
	 * @return
	 */
	private String findPosterName(String sender, String name) {
		for (RosterEntry entry : rosterEntries) {
			if (entry.getUser().equalsIgnoreCase(sender)) {
				if (StringUtils.isNotBlank(entry.getName())) {
					return entry.getName();
				}
			}
		}
		return name;
	}

	private void processCommand(String command, String sender) {
		if ("/l".equalsIgnoreCase(command) || "/list".equalsIgnoreCase(command)) {
			Message message = new Message(sender, Message.Type.chat);
			message.setBody("\nUsers：\n" + getRosterEntryNames(" \n"));
			try {
				connection.getChatManager().createChat(sender, new MessageListener() {
					public void processMessage(Chat chat, Message msg) {
						// do something..?
					}
				}).sendMessage(message);
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		} else if ("/?".equalsIgnoreCase(command) || "/h".equalsIgnoreCase(command) || "/help".equalsIgnoreCase(command)) {
			Message message = new Message(sender, Message.Type.chat);
			StringBuffer sb = new StringBuffer();
			sb.append("\n Jabberer Help：\n");
			sb.append("\t /help /h /? for help message \n");
			sb.append("\t /list /l to list all friends. \n");
			sb.append("\t /online /o to list online friends. \n");
			message.setBody(sb.toString());
			try {
				connection.getChatManager().createChat(sender, new MessageListener() {
					public void processMessage(Chat chat, Message msg) {
						// do something..?
					}
				}).sendMessage(message);
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		} else if ("/online".equalsIgnoreCase(command) || "/o".equalsIgnoreCase(command)) {
			Message message = new Message(sender, Message.Type.chat);
			message.setBody("\n Online Users:\n" + getRosterEntryNames(true, "\n"));
			try {
				connection.getChatManager().createChat(sender, new MessageListener() {
					public void processMessage(Chat chat, Message msg) {
						// do something..?
					}
				}).sendMessage(message);
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String getRosterEntryNames(String separator) {
		return getRosterEntryNames(false, separator);
	}
	
	private String getRosterEntryNames(boolean onlineOnly, String separator) {
		StringBuffer sb = new StringBuffer();
		for(RosterEntry entry : rosterEntries) {
			if (onlineOnly) {
				Presence presence = roster.getPresence(entry.getUser());
				if (!presence.isAvailable()) {
					continue;
				}
			}
			if (StringUtils.isNotBlank(entry.getName())) {
				sb.append(entry.getName());
			}
			sb.append(" [").append(entry.getUser()).append("]").append(separator);
		}
		return "\t" + sb.toString();
	}
	

}

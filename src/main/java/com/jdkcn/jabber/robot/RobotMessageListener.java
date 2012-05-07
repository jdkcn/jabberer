/**
 * Project:jabberer
 * Copyright 2004-2010 Homolo Co., Ltd. All rights reserved.
 */
package com.jdkcn.jabber.robot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
public class RobotMessageListener implements MessageListener {

	private List<RosterEntry> rosterEntries = new ArrayList<RosterEntry>();
	
	private Roster roster;
	
	private XMPPConnection connection;
	
	private Robot robot;
	
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
		commandList.add("/add");
		commandList.add("/name");
		commandList.add("/remove");
	}
	
	public void setSendOfflineMessage(boolean sendOfflineMessage) {
    	this.sendOfflineMessage = sendOfflineMessage;
    }

	public RobotMessageListener(XMPPConnection connection, Roster roster,Collection<RosterEntry> rosterEntries, Boolean sendOfflineMessage, Robot robot) {
		this.connection = connection;
		this.roster = roster;
		this.robot = robot;
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
			boolean isCommand = false;
			for (String command : commandList) {
				if (body.toLowerCase().startsWith(command)) {
					isCommand = true;
					break;
				}
			}
			if (isCommand) {
				processCommand(chat, body, sender);
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
//						final MessageListener messageListener = new RobotMessageListener(connection, roster, rosterEntries, sendOfflineMessage, robot);
						connection.getChatManager().createChat(entry.getUser(), this).sendMessage(msg);
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

	private void processCommand(Chat chat, String command, String sender) {
		if ("/l".equalsIgnoreCase(command) || "/list".equalsIgnoreCase(command)) {
			Message message = new Message(sender, Message.Type.chat);
			message.setBody("\nUsers：\n" + getRosterEntryNames(" \n"));
			try {
				chat.sendMessage(message);
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
			sb.append("\t /add <account> <nickname> [groupname]... to add a user as friends. \n");
			sb.append("\t /name <account> <nickname> set a nickname to a user. \n");
			message.setBody(sb.toString());
			try {
				chat.sendMessage(message);
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		} else if ("/online".equalsIgnoreCase(command) || "/o".equalsIgnoreCase(command)) {
			Message message = new Message(sender, Message.Type.chat);
			message.setBody("\n Online Users:\n" + getRosterEntryNames(true, "\n"));
			try {
				chat.sendMessage(message);
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		} else if (command.startsWith("/add")) {
			if (robot.getAdministratorNames().indexOf(sender) == -1) {
				sendNoPermissionMessage(chat, sender);
			} else {
				String[] args = StringUtils.split(command.substring(4), " ");
				if (args == null || args.length == 0) {
					Message message = new Message(sender, Message.Type.chat);
					message.setBody("\n args wrong please use:\n \t /add <account> <nickname> [groupname]...");
					try {
						chat.sendMessage(message);
					} catch (XMPPException e) {
						e.printStackTrace();
					}
				} else {
					try {
						String nickname = null;
						if (args.length > 1) {
							nickname = args[1].trim();
						}
						String[] groups = null;
						if (args.length > 2) {
							groups = Arrays.copyOfRange(args, 2, args.length);
						}
						roster.createEntry(args[0].trim(), nickname, groups);
					} catch (XMPPException e) {
						e.printStackTrace();
					}
				}
			}
		} else if (command.startsWith("/name")) {
			if (robot.getAdministratorNames().indexOf(sender) == -1) {
				sendNoPermissionMessage(chat, sender);
			} else {
				String[] args = StringUtils.split(command.substring(5), " ");
				if (args == null || args.length < 2) {
					Message message = new Message(sender, Message.Type.chat);
					message.setBody("\n args wrong please use:\n \t /name <account> <nickname>");
					try {
						chat.sendMessage(message);
					} catch (XMPPException e) {
						e.printStackTrace();
					}
				} else {
					RosterEntry rosterEntry = roster.getEntry(args[0].trim());
					if (rosterEntry != null) {
						rosterEntry.setName(args[1]);
					}
				}
			}
		} else if (command.startsWith("/remove")) {
			if (robot.getAdministratorNames().indexOf(sender) == -1) {
				sendNoPermissionMessage(chat, sender);
			} else {
				String[] args = StringUtils.split(command.substring(7), " ");
				if (args == null || args.length < 1) {
					Message message = new Message(sender, Message.Type.chat);
					message.setBody("\n args wrong please use:\n \t /name <account> <nickname>");
					try {
						chat.sendMessage(message);
					} catch (XMPPException e) {
						e.printStackTrace();
					}
				} else {
					try {
						RosterEntry rosterEntry = roster.getEntry(args[0]);
						if (rosterEntry != null) {
							roster.removeEntry(rosterEntry);
						}
					} catch (XMPPException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * @param chat
	 * @param sender
	 */
	private void sendNoPermissionMessage(Chat chat, String sender) {
		Message message = new Message(sender, Message.Type.chat);
		message.setBody("\n no permission to add more entry.");
		try {
			chat.sendMessage(message);
		} catch (XMPPException e) {
			e.printStackTrace();
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
